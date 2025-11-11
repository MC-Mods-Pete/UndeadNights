package net.petemc.undeadnights.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class HordesSpawning {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeIdFromHordesConfig = 1;

    public HashMap<UUID, CompletableFuture<BlockPos>> completableFutureBlockPositionsPerPlayer = new HashMap<>();
    public HashMap<UUID, Integer> hordeSpawnLocationAttemptsPerPlayer = new HashMap<>();

    private static double x = 0;
    private static double z = 0;
    private static double d = 0;

    // check if the block light level at the given position is dark enough for monster spawns
    public static boolean isDarkEnoughToSpawn(ServerLevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= MainConfig.getMaxBlockLightLevelForMonsterSpawns();
    }

    // check if the given location is suitable for spawning horde mobs
    private static boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        BlockState blockState = level.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotBlockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        boolean darkEnough = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().is(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }
        if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
            darkEnough = isDarkEnoughToSpawn(level, mutable);
        }

        return doesNotBlockMovement && notLeaves && notWater && darkEnough;
    }

    // spawn a single horde mob at the given location (Asynchronous wrapper)
    public static CompletableFuture<UndeadSpawner.SpawnHordeResult> spawnHordeAsync(ServerLevel level, ServerPlayer player, RandomSource randomSource) {
        return CompletableFuture.supplyAsync(() -> spawnHorde(
                level,
                player,
                randomSource
        ), ForkJoinPool.commonPool());
    }

    // spawn a single horde mob at the given location
    public static UndeadSpawner.SpawnHordeResult spawnHorde(ServerLevel level, ServerPlayer player, RandomSource randomSource) {
        int randomValue;
        int spawnCounter = 0;
        BlockPos possibleSpawnLocation;
        boolean foundHordeSpawnLocation = false;
        int currentHordeCounter = UndeadNights.globalSpawnCounter;
        int maxChecks = 20;

        for (int i= 0; i < maxChecks; i++) {
            // for the given min/max distance, calculate the x and z coordinates deltas
            if (d == 0) {
                d = randomSource.nextIntBetweenInclusive(MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                x = randomSource.nextIntBetweenInclusive(0, (int) d);
                if (x == 0) {
                    z = d;
                } else {
                    z = Math.sqrt((d * d) - (x * x));
                    if (randomSource.nextBoolean()) {
                        x = x * -1;
                    }
                }
                if (randomSource.nextBoolean()) {
                    z = z * -1;
                }
            }

            boolean playerInCave = false;
            if (player instanceof UndeadNightsExtendedPlayer undeadNightsExtendedPlayer) {
                playerInCave = undeadNightsExtendedPlayer.undeadnights_isInCave();
            }

            // use the above calculated x and z to find a possible spawn location
            possibleSpawnLocation = player.blockPosition().offset((int) x, 0, (int) z);

            // cave spawning check
            if (MainConfig.getHordeWavesCanSpawnInCaves() && playerInCave) {
                //possibleSpawnLocation = Helpers.findEndPositionForPathAStar(level, player.blockPosition(), MainConfig.getCaveSpawnDistance(), false, 0.6f, 1.8f, 20000, 1, 4, 10);
                possibleSpawnLocation = Helpers.findEndPositionUsingMinecraftPathfinding(level, player, player.blockPosition(), MainConfig.getCaveSpawnDistance());
                if (possibleSpawnLocation == null) {
                    UndeadNights.LOGGER.info("Cave horde spawn location calculation for player {} failed, trying again.", player.getName().getString());
                    d = 0;
                    continue;
                }
                player.sendSystemMessage(Component.literal("Cave horde spawn location calculated.").withStyle(ChatFormatting.DARK_AQUA));
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Cave horde spawn location calculation for player {} is done, result: {}", player.getName().getString(), possibleSpawnLocation);
                }
                foundHordeSpawnLocation = true;
            } else {
                // use the heightmap to find the surface level at the possible spawn location
                possibleSpawnLocation = new BlockPos(possibleSpawnLocation.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, possibleSpawnLocation.getX(), possibleSpawnLocation.getZ()) - 1, possibleSpawnLocation.getZ());

                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Surface horde spawning check for player {} at position {}", player.getName().getString(), possibleSpawnLocation);
                }
                foundHordeSpawnLocation = checkSpawnLocation(level, possibleSpawnLocation.getX(), possibleSpawnLocation.getY() - 1, possibleSpawnLocation.getZ());
            }

            //if (foundHordeSpawnLocation) {
            //    foundHordeSpawnLocation = Pathfinding.canPathfind(level, possibleSpawnLocation, player.blockPosition(),0.8f, 1.6f, 20000, 4, 1, 10);
            //}

            if (foundHordeSpawnLocation) {
                UndeadNights.LOGGER.info("There is a direct path from player {} to possible spawn location {}", player.getName().getString(), possibleSpawnLocation);
            } else {
                UndeadNights.LOGGER.info("There is NO direct path from player {} to possible spawn location {}", player.getName().getString(), possibleSpawnLocation);
                d = 0;
                continue;
            }

            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("A possible Horde spawn location for player {} was found.", player.getName().getString());
            }

            boolean spawnCapReached = false;
            spawnCounter = 0;
            /*
             * Horde config variant 1
             */
            if (HordeConfig.getConfigVariant() == 1) {
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Horde config variant 1 detected.");
                }
                int waveMobCounter = 0;

                int scaledHordeSize = (int) Math.round(HordeConfig.getMaxWaveSize() * UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getHordeSizeScaleFactor());
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Scaled horde size: {}", scaledHordeSize);
                }
                while (waveMobCounter < (scaledHordeSize)) {
                    if (!HordeConfig.getHordeMobs().isEmpty()) {
                        for (var mobSpawnData : HordeConfig.getHordeMobs()) {
                            if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                                if (spawnHordeMob(level, randomSource, possibleSpawnLocation, player, (randomValue > (100 - mobSpawnData.chance())) ? mobSpawnData : HordeConfig.getDefaultHordeMob()) == 0) {
                                    spawnCounter++;
                                }
                                waveMobCounter++;
                                if (waveMobCounter >= scaledHordeSize) {
                                    d = 0;
                                    break;
                                }
                            } else {
                                // spawn cap reached, don't spawn anymore mobs in this wave
                                waveMobCounter = scaledHordeSize;
                                spawnCapReached = true;
                                d = 0;
                                break;
                            }
                        }
                    } else {
                        if (spawnHordeMob(level, randomSource, possibleSpawnLocation, player, HordeConfig.getDefaultHordeMob()) == 0) {
                            spawnCounter++;
                        }
                        waveMobCounter++;
                    }
                }
            }

            /*
             * Horde config variant 2
             */
            if (HordeConfig.getConfigVariant() == 2) {
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Horde config variant 2 detected.");
                }
                if (hordeIdFromHordesConfig == 0) {
                    UndeadNights.LOGGER.warn("Horde to spawn was set to 0, setting it to 1.");
                    hordeIdFromHordesConfig = 1;
                }
                int hordeIdToSpawn = hordeIdFromHordesConfig - 1;
                if (!UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().isEmpty()) {
                    if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getHordeSelectionMode().equalsIgnoreCase("sequential")) {
                        UndeadNights.serverState.setPossibleHordesIndex(UndeadNights.serverState.getPossibleHordesIndex() + 1);
                        if (UndeadNights.serverState.getPossibleHordesIndex() >= UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().size()) {
                            UndeadNights.serverState.setPossibleHordesIndex(0);
                        }
                        hordeIdToSpawn = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().get(UndeadNights.serverState.getPossibleHordesIndex()) - 1;
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Horde selection mode: sequential, listOfPossibleHordes({}) = {} will be spawned", UndeadNights.serverState.getPossibleHordesIndex(), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().get(UndeadNights.serverState.getPossibleHordesIndex()));
                        }
                    } else if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getHordeSelectionMode().equalsIgnoreCase("random")) {
                        int index = randomSource.nextIntBetweenInclusive(0, (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().size() - 1));
                        hordeIdToSpawn = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().get(index) - 1;
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Horde selection mode: random, listOfPossibleHordes({}) = {} will be spawned", index, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().get(index));
                        }
                    }
                }

                if (hordeIdToSpawn >= HordeConfig.getHordes().size()) {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.error("Horde index {} from config is out of range, using default horde instead.", hordeIdToSpawn);
                    }
                    hordeIdToSpawn = hordeIdFromHordesConfig - 1;
                }

                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Spawning Horde \"{}\"", HordeConfig.getHordes().get(hordeIdToSpawn).hordeName());
                }
                List<HordeConfig.HordesData> hordes = HordeConfig.getHordes();
                for (var mobSpawnData : hordes.get(hordeIdToSpawn).hordeMobs()) {
                    int mobCount;
                    if (mobSpawnData.countMin() >= mobSpawnData.countMax()) {
                        mobCount = mobSpawnData.countMin();
                    } else {
                        mobCount = randomSource.nextInt(mobSpawnData.countMin(), mobSpawnData.countMax());
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Spawning {} {}", mobCount, mobSpawnData.mobId());
                        }
                    }
                    int scaledMobCount = (int) Math.round(mobCount * UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getHordeSizeScaleFactor());
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Scaled mob count: {}", scaledMobCount);
                    }
                    for (int j = 0; j < scaledMobCount; j++) {
                        boolean spawnMob = true;
                        if (UndeadNights.globalSpawnCounter >= MainConfig.getHordeMobsSpawnCap()) {
                            spawnCapReached = true;
                            break;
                        }
                        if (mobSpawnData.chance() != 100) {
                            randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("Chance value for horde config (variant 2) found, chance value: {}, randomValue: {}", mobSpawnData.chance(), randomValue);
                            }
                            if (!(randomValue > (100 - mobSpawnData.chance()))) {
                                spawnMob = false;
                            }
                        }
                        if (spawnMob) {
                            if (spawnHordeMob(level, randomSource, possibleSpawnLocation, player, mobSpawnData) == 0) {
                                spawnCounter++;
                            }
                        }
                    }
                    if (UndeadNights.globalSpawnCounter >= MainConfig.getHordeMobsSpawnCap()) {
                        spawnCapReached = true;
                        break;
                    }
                }
            }

            d = 0;
            if (currentHordeCounter != UndeadNights.globalSpawnCounter) {
                if (spawnCounter != 0) {
                    if (MainConfig.getHordeSpawnedMessageAndSound()) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM.get(), SoundSource.HOSTILE, 4.0F, 1);
                        player.sendSystemMessage(Component.translatable("message.undeadnights.horde_spawned").withStyle(ChatFormatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Horde was spawned for player {} (removing from horde lists)", player.getName().getString());
                    }
                    UndeadNights.serverState.entitiesWithPendingHorde.remove(player.getUUID());
                    UndeadNights.serverState.entitiesWithReceivedHorde.add(player.getUUID());
                    return UndeadSpawner.SpawnHordeResult.DONE;
                } else {
                    continue;
                }
            }

            if (spawnCapReached) {
                UndeadNights.LOGGER.info("Spawn cap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
                return UndeadSpawner.SpawnHordeResult.FAILED;
            }
        } // <---
        if (spawnCounter == 0) {
            UndeadNights.LOGGER.info("Failed to spawn a horde.");
            return UndeadSpawner.SpawnHordeResult.FAILED;
        }

        return UndeadSpawner.SpawnHordeResult.DONE;
    }

    private BlockPos getBlockPosWithDistance(BlockPos pos, Level level, int distanceMin, int distanceMax) {
        final RandomSource random = level.random;
        double _d;
        double _x;
        double _z;
        _d = random.nextIntBetweenInclusive(distanceMin, distanceMax);
        _x = random.nextIntBetweenInclusive(0, (int) _d);
        if (_x == 0) {
            _z = _d;
        } else {
            _z = Math.sqrt((_d * _d) - (_x * _x));
            if (random.nextBoolean()) {
                _x = _x * -1;
            }
        }
        if (random.nextBoolean()) {
            _z = _z * -1;
        }

        return new BlockPos(pos.getX() + (int) _x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + (int) _x, pos.getZ() + (int) _z), pos.getZ() + (int) _z);
    }

    private static BlockPos findNearbySurfaceSpawnPosition(ServerLevel level, BlockPos pos, RandomSource randomSource, boolean playerInCave) {
        int deltaX = randomSource.nextInt(5);
        int deltaZ = randomSource.nextInt(5);
        if (!randomSource.nextBoolean()) {
            deltaX = deltaX * -1;
        }
        if (!randomSource.nextBoolean()) {
            deltaZ = deltaZ * -1;
        }
        int y;

        if (MainConfig.getHordeWavesCanSpawnOnTrees()) {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ);
        } else {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }

        //if (MainConfig.getHordeWavesCanSpawnInCaves() && playerInCave) {
        //    y = pos.getY();
        //}

        return new BlockPos(pos.getX() + deltaX, y, pos.getZ() + deltaZ);
    }


    private static int spawnHordeMob(ServerLevel level, RandomSource randomSource, BlockPos pos, Player player, HordeConfig.MobSpawnData mobSpawnData) {
        EntityType<?> mobType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobSpawnData.mobId()));
        if (mobType == null) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            //mobType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("undeadnights:horde_zombie"));
        }

        CompoundTag nbtCompound = new CompoundTag();
        if (!Objects.equals(mobSpawnData.nbt(), "")) {
            try {
                nbtCompound = TagParser.parseTag(mobSpawnData.nbt());
            } catch (CommandSyntaxException e) {
                UndeadNights.LOGGER.error("Parsing NBT-tags for {} failed!", mobSpawnData.mobId());
            }
        }

        if (!invalidHordeMobEntry) {
            nbtCompound.putString("id", mobSpawnData.mobId());
        } else {
            nbtCompound.putString("id", "undeadnights:horde_zombie");
        }

        boolean playerInCave = false;
        if (player instanceof UndeadNightsExtendedPlayer undeadNightsExtendedPlayer) {
            playerInCave = undeadNightsExtendedPlayer.undeadnights_isInCave();
        }

        if (MainConfig.getHordeWavesCanSpawnInCaves() && playerInCave) {
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Player {} is in cave, attempting to spawn horde mob in cave.", player.getName().getString());
            }
            pos = Helpers.findSpawnablePosition(level, pos, 8, 5);
        } else {
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Player {} is not in cave, attempting to spawn horde mob on surface.", player.getName().getString());
            }
            pos = findNearbySurfaceSpawnPosition(level, pos, randomSource, playerInCave);
        }

        BlockPos finalPos = pos;
        UndeadNights.LOGGER.info("Spawning horde mob {} at position {}, {}, {}", mobSpawnData.mobId(), finalPos.getX(), finalPos.getY(), finalPos.getZ());
        Entity entity = EntityType.loadEntityRecursive(nbtCompound, level, entityX -> {
            entityX.moveTo(finalPos.getX(),finalPos.getY(),finalPos.getZ(), entityX.getYRot(), entityX.getXRot());
            return entityX;
        });

        if (entity instanceof Monster) {
            if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
                if (!isDarkEnoughToSpawn(level, new BlockPos(pos.getX(), pos.getY(), pos.getZ()))) {
                    UndeadNights.LOGGER.info("Horde mob {} can't spawn here, it's not dark enough!", mobSpawnData.mobId());
                    return -1;
                }
            }
        }

        if (entity instanceof DemolitionZombieEntity demolitionZombie) {
            String str = mobSpawnData.extra();
            String[] strA = str.split(":");
            if (strA[0].equals("tnt")) {
                try {
                    int numberTnt = Integer.parseInt(strA[1]);
                    if (numberTnt > 64) {
                        numberTnt = 64;
                    }
                    demolitionZombie.setNumberTnt(numberTnt);
                } catch (Exception e) {
                    UndeadNights.LOGGER.warn("extraSpawnInfo for {} has non valid value, using default TNT stack size!", mobSpawnData.mobId());
                }
            } else {
                UndeadNights.LOGGER.warn("extraSpawnInfo for {} could be read, using default TNT stack size!", mobSpawnData.mobId());
            }
        }
        DifficultyInstance localDifficulty = level.getCurrentDifficultyAt(player.blockPosition());
        try {
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
                mob.setTarget(player);

                // make vanilla zombies in hordes float on water and give ability to break blocks
                if (mobSpawnData.mobId().equals("minecraft:zombie")) {
                    mob.goalSelector.addGoal(1, new FloatGoal(mob));
                    mob.goalSelector.addGoal(1, new BreakBlockGoal((Zombie) mob));
                }

                if ((!mobSpawnData.mobId().equals("undeadnights:horde_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:elite_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:demolition_zombie"))) {
                    mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, Player.class, false, false));
                    Objects.requireNonNull(mob.getAttribute(Attributes.FOLLOW_RANGE)).setBaseValue(128.0f);

                    int playerCount = 1;
                    if (!mob.level().isClientSide) {
                        playerCount = mob.level().players().size();
                    }

                    double healthScaleFactor = 0.0;
                    double damageScaleFactor = 0.0;
                    double speedScaleFactor = 0.0;
                    double armorScaleFactor = 0.0;

                    if (UndeadNights.difficultyConfig.getDynamicScaling().isDynamicScalingEnabled() && (playerCount > 1)) {
                        healthScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getHealthScalePerPlayer() * (playerCount - 1);
                        damageScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getDamageScalePerPlayer() * (playerCount - 1);
                        speedScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getSpeedScalePerPlayer() * (playerCount - 1);
                        armorScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getArmorScalePerPlayer() * (playerCount - 1);

                        healthScaleFactor = healthScaleFactor + UndeadNights.serverState.getCurrentHealthScale();
                        damageScaleFactor = damageScaleFactor + UndeadNights.serverState.getCurrentDayScaleCounter();
                        speedScaleFactor = speedScaleFactor + UndeadNights.serverState.getCurrentSpeedScale();
                        armorScaleFactor = armorScaleFactor + UndeadNights.serverState.getCurrentArmorScale();

                        if (healthScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale()) {
                            healthScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale();
                        }
                        if (damageScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale()) {
                            damageScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale();
                        }
                        if (speedScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale()) {
                            speedScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale();
                        }
                        if (armorScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale()) {
                            armorScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale();
                        }
                    }

                    if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isUpdateAttributesOfThirdPartyMobs() ||
                            (mobSpawnData.mobId().equals("minecraft:zombie"))) {
                        Objects.requireNonNull(mob.getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(new AttributeModifier("Monster difficulty health bonus", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHealthAttributeScaleFactor() - 1.0 + healthScaleFactor, AttributeModifier.Operation.MULTIPLY_BASE));
                        Objects.requireNonNull(mob.getAttribute(Attributes.MOVEMENT_SPEED)).addPermanentModifier(new AttributeModifier("Monster difficulty speed bonus", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getSpeedAttributeScaleFactor() - 1.0 + speedScaleFactor, AttributeModifier.Operation.MULTIPLY_BASE));
                        Objects.requireNonNull(mob.getAttribute(Attributes.ATTACK_DAMAGE)).addPermanentModifier(new AttributeModifier("Monster difficulty attack damage bonus", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getDamageAttributeScaleFactor() - 1.0 + damageScaleFactor, AttributeModifier.Operation.MULTIPLY_BASE));
                        Objects.requireNonNull(mob.getAttribute(Attributes.ARMOR)).addPermanentModifier(new AttributeModifier("Monster difficulty armor bonus", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getArmorAttributeScaleFactor() - 1.0 + armorScaleFactor, AttributeModifier.Operation.MULTIPLY_BASE));
                        mob.setHealth(mob.getMaxHealth());
                    }
                }
            }
            assert entity != null;
            UndeadNights.serverState.spawnedHordeMobs.add(entity.getUUID());
            if (MainConfig.getHordeWavesCanSpawnInCaves()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,15*20, 1));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,2*20, 1));
                    if (MainConfig.getDebugMakeHordeMobsGlow()) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200 * 20, 1));
                    }
                }
            }
            level.tryAddFreshEntityWithPassengers(entity);
        } catch (Exception e) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            HordeZombieEntity hZombie = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
            hZombie.setPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ());
            if (MainConfig.getPersistentMobs()) {
                hZombie.setPersistenceRequired();
            }
            hZombie.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
            hZombie.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.add(hZombie.getUUID());
            level.addFreshEntity(hZombie);
        }
        return 0;
    }


}
