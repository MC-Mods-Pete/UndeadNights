package net.petemc.undeadnights.world.spawner;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.level.CustomSpawner;
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
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.Helpers;
import net.petemc.undeadnights.util.HordesSpawning;
import net.petemc.undeadnights.util.Pathfinding;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UndeadSpawner implements CustomSpawner {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeIdFromHordesConfig = 1;

    public static boolean useAsyncHordeSpawning = true;

    public HashMap<UUID, CompletableFuture<BlockPos>> completableFutureBlockPositionsPerPlayer = new HashMap<>();
    public HashMap<UUID, CompletableFuture<SpawnHordeResult>> hordeSpawningPerPlayer = new HashMap<>();

    private double x = 0;
    private double z = 0;
    private double d = 0;

    public static boolean isDarkEnoughToSpawn(ServerLevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= MainConfig.getMaxBlockLightLevelForMonsterSpawns();
    }

    private boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
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

    private BlockPos findNearbySurfaceSpawnPosition(ServerLevel level, BlockPos pos, RandomSource randomSource, boolean playerInCave) {
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

    
    private int spawnHordeMob(ServerLevel level, RandomSource randomSource, BlockPos pos, Player player, HordeConfig.MobSpawnData mobSpawnData) {
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

    public static enum SpawnHordeResult {
        DONE,
        NOT_DONE_YET,
        FAILED;
    }

    public SpawnHordeResult spawnHorde(ServerLevel level, ServerPlayer player, RandomSource randomSource) {
        if (useAsyncHordeSpawning) {
            if (!hordeSpawningPerPlayer.containsKey(player.getUUID())) {
                hordeSpawningPerPlayer.put(player.getUUID(),
                        HordesSpawning.spawnHordeAsync(level, player, randomSource));
                player.sendSystemMessage(Component.literal("Finding horde spawn location (async)...").withStyle(ChatFormatting.DARK_AQUA));
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Async horde spawning location calculation for player {} at {}", player.getName().getString(), player.blockPosition());
                }
            }
            CompletableFuture<SpawnHordeResult> existingFutureHorde = hordeSpawningPerPlayer.get(player.getUUID());
            if (existingFutureHorde != null && existingFutureHorde.isDone()) {
                SpawnHordeResult result = SpawnHordeResult.FAILED;
                try {
                    result = existingFutureHorde.get();
                } catch (Exception e) {
                    UndeadNights.LOGGER.error("Spawning horde for player {} failed!", player.getName().getString());
                }
                hordeSpawningPerPlayer.remove(player.getUUID());
                return SpawnHordeResult.DONE;
            } else {
                // still not done, skip this spawn attempt
                return SpawnHordeResult.NOT_DONE_YET;
            }
        }

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
                if (!completableFutureBlockPositionsPerPlayer.containsKey(player.getUUID())) {
                    player.sendSystemMessage(Component.literal("Calculating cave horde spawn location...").withStyle(ChatFormatting.DARK_AQUA));
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Starting cave horde spawn location calculation for player {} at {}", player.getName().getString(), player.blockPosition());
                    }
                    completableFutureBlockPositionsPerPlayer.put(player.getUUID(),
                            Pathfinding.findEndPositionAStarAsync(level, player.blockPosition(), MainConfig.getCaveSpawnDistance()));
                }
                CompletableFuture<BlockPos> existingFuture = completableFutureBlockPositionsPerPlayer.get(player.getUUID());
                if (existingFuture != null && existingFuture.isDone()) {
                    try {
                        possibleSpawnLocation = existingFuture.get();
                    } catch (Exception e) {
                        //possibleSpawnLocation = player.blockPosition().offset((int) x, 0, (int) z);
                    }
                    if (possibleSpawnLocation == null) {
                        UndeadNights.LOGGER.info("Cave horde spawn location calculation for player {} failed, trying again.", player.getName().getString());
                        completableFutureBlockPositionsPerPlayer.remove(player.getUUID());
                        d = 0;
                        continue;
                    }
                    player.sendSystemMessage(Component.literal("Cave horde spawn location calculated.").withStyle(ChatFormatting.DARK_AQUA));
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Cave horde spawn location calculation for player {} is done, result: {}", player.getName().getString(), possibleSpawnLocation);
                    }
                    foundHordeSpawnLocation = true;
                    completableFutureBlockPositionsPerPlayer.remove(player.getUUID());
                } else {
                    // still not done, skip this spawn attempt
                    d = 0;
                    return SpawnHordeResult.FAILED;
                    //continue;
                }
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
                    return SpawnHordeResult.DONE;
                } else {
                    continue;
                }
            }

            if (spawnCapReached) {
                UndeadNights.LOGGER.info("Spawn cap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
                return SpawnHordeResult.FAILED;
            }
        } // <---
        if (spawnCounter == 0) {
            UndeadNights.LOGGER.info("Failed to spawn a horde.");
            return SpawnHordeResult.FAILED;
        }

        return SpawnHordeResult.DONE;
    }


    @Override
    public int tick(@NotNull ServerLevel level, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning are enabled
        if (!MainConfig.getUndeadNightsEnabled()) {
            return 0;
        }

        if (!spawnMonsters && !MainConfig.getIgnoreDoMobSpawningGamerule()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!(level.dimension() == Level.OVERWORLD)) {
            return 0;
        }

        // Check if the SaveState is already initialized
        if (UndeadNights.serverState == null) {
            return 0;
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = level.getDayTime() - ((level.getDayTime() / 24000L) * 24000);
        if (UndeadNights.serverState.getPrevNormalizedTimeOfDay() == normalizedTimeOfDay) {
            return 0;
        }
        UndeadNights.serverState.setNightIsStarting((UndeadNights.serverState.getPrevNormalizedTimeOfDay() <  12000L) && (normalizedTimeOfDay >= 12000L));
        UndeadNights.serverState.setPrevNormalizedTimeOfDay(normalizedTimeOfDay);

        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final RandomSource randomSource = level.random;
        int randomValue = 0;


        if (!UndeadNights.serverState.entitiesWithPendingHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithPendingHorde.stream().toList()) {
                if (!UndeadNights.serverState.entitiesWithReceivedHorde.contains(playerUUID)) {
                    Entity entity = level.getEntity(playerUUID);
                    if (entity instanceof ServerPlayer serverPlayer) {
                        if (spawnHorde(level, serverPlayer, randomSource) == SpawnHordeResult.FAILED) {
                            //serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.command_spawn_horde_failed"));
                        }
                        //UndeadNights.serverState.entitiesWithReceivedHorde.add(playerUUID);
                        //UndeadNights.serverState.entitiesWithPendingHorde.remove(playerUUID);
                    }
                }
            }
        }

        if (!UndeadNights.serverState.entitiesWithReceivedHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithReceivedHorde.stream().toList()) {
                Entity entity = level.getEntity(playerUUID);
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (!serverPlayer.hasEffect(ModEffects.LURE_HORDE.get())) {
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(playerUUID);
                    }
                }
            }
        }

        // is it night...?
        if (itIsNight) {
            UndeadNights.serverState.setPerformDifficultySwitchCheck(true);
            // if it's already a horde night, check if we should respawn new waves
            if (UndeadNights.serverState.getRespawnZombies() && UndeadNights.serverState.getHordeNight() &&
                    UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().isSpawnAdditionalWaves()) {
                if (UndeadNights.serverState.getTickCounter() > 0) {
                    UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                    return 0;
                } else {
                    UndeadNights.serverState.setTickCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getCooldownBetweenWaves() * 20);
                }

                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getChanceForAdditionalWave())) {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("New Wave, randomValue was: {}", randomValue);
                    }
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setRespawnZombies(false);
                } else {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("RandomValue: {}", randomValue);
                    }
                    return 0;
                }
            }

            // if a new night just started, count down the days
            if ((UndeadNights.serverState.getNightIsStarting() && (UndeadNights.serverState.getDaysCounter() >= 1))) {
                if (UndeadNights.serverState.getGracePeriod() > 0) {
                    UndeadNights.serverState.setGracePeriod(UndeadNights.serverState.getGracePeriod() - 1);
                    if (MainConfig.getSendHordeNightsCountdownMessage()) {
                        for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.days_of_grace_remaining", String.valueOf(UndeadNights.serverState.getGracePeriod())));
                        }
                    }
                    if (UndeadNights.serverState.getGracePeriod() == 0) {
                        UndeadNights.serverState.setDaysCounter(1);
                    } else {
                        return 0;
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (MainConfig.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.nights_remaining", String.valueOf(UndeadNights.serverState.getDaysCounter())));
                        } else {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.last_nights"));
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {}, DaysCounter: {}, GameTime: {}, GameTimeDays: {}", normalizedTimeOfDay, level.getDayTime(), UndeadNights.serverState.getDaysCounter(), level.getGameTime(), (level.getGameTime() / 24000));
                }
            }

            // spawn a random horde and/or stray zombies for non-horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (UndeadNights.serverState.getTryToSpawnRandomHorde()) {
                    if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().isEnableRandomHordes() &&
                            (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                        randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                        if ((randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getChanceForRandomHorde()))) {
                            for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                                if (spawnHorde(level, player, randomSource) == SpawnHordeResult.FAILED) {
                                    break;
                                }
                                if (MainConfig.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("A random horde has spawned!");
                                }
                            }
                        }
                    }
                    UndeadNights.serverState.setTryToSpawnRandomHorde(false);
                }

                if (MainConfig.getHordeZombiesSpawnNaturally() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                    if (UndeadNights.serverState.getTickCounter() > 0) {
                        UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                        return 0;
                    } else {
                        UndeadNights.serverState.setTickCounter(5 * 20);
                    }
                    if (!(randomSource.nextFloat() < 0.03F)) {
                        return 0;
                    }
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        BlockPos pos = getBlockPosWithDistance(player.blockPosition(), level, MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                        if (!checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            spawnHordeMob(level, randomSource, pos, player, new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, "none", ""));
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return 0;
            }

            // this is the first tick of a new night
            if (UndeadNights.serverState.getNightIsStarting()) {
                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (!(randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setIsNaturalSpawningOk(true);
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setFirstWaveHasSpawned(false);
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendSystemMessage(Component.translatable("message.undeadnights.horde_night").withStyle(ChatFormatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && normalizedTimeOfDay >= 12542) {
                if (UndeadNights.serverState.getHordesCounter() != 0) {
                    if ((UndeadNights.serverState.getHordesCounter() - 1) == 0) {
                        return 0;
                    }
                }
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    UndeadNights.serverState.entitiesWithPendingHorde.add(player.getUUID());
                    UndeadNights.serverState.entitiesWithPendingWave.add(player.getUUID());
                    UndeadNights.serverState.setFirstWaveHasSpawned(true);
                }
                UndeadNights.serverState.setHordesCounter(UndeadNights.serverState.getHordesCounter() - 1);

                if (!UndeadNights.serverState.getFirstWaveHasSpawned()) {
                    UndeadNights.serverState.setTickCounter(10 * 20);
                } else {
                    UndeadNights.serverState.setTickCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getCooldownBetweenWaves() * 20);
                    UndeadNights.serverState.setSpawnZombies(false);
                    UndeadNights.serverState.setRespawnZombies(true);
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Spawned waves for every player: DaysCounter: {} GlobalSpawnCounter: {} Spawn: {}, respawn: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter, UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
                d = 0;
                x = 0;
                z = 0;
            }
        } else {
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    player.sendSystemMessage(Component.translatable("message.undeadnights.horde_night_over"));
                    if (UndeadNights.serverState.entitiesWithPendingWave.contains(player.getUUID())) {
                        UndeadNights.serverState.entitiesWithPendingWave.remove(player.getUUID());
                        UndeadNights.serverState.entitiesWithPendingHorde.remove(player.getUUID());
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
                if (MainConfig.getMaxHordesPerHordeNight() != 0) {
                    UndeadNights.serverState.setHordesCounter(MainConfig.getMaxHordesPerHordeNight() + 1);
                } else {
                    UndeadNights.serverState.setHordesCounter(0);
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", level.getDayTime(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }
            }

            if ((normalizedTimeOfDay >= 11500) && !level.getPlayers(LivingEntity::isAlive).isEmpty() && UndeadNights.automaticDifficultyProgressionActive) {
                boolean flag = false;
                if (UndeadNights.serverState.isPerformDifficultySwitchCheck()) {
                    flag = UndeadNights.difficultyConfig.checkForDifficultyLevelSwitch((int) (level.getDayTime() / 24000L)+1, level.random);
                    UndeadNights.serverState.setPerformDifficultySwitchCheck(false);
                }
                if ((flag || !UndeadNights.serverState.isFirstDifficultyLevelPrinted()) &&
                        (UndeadNights.difficultyConfig.getDifficultyLevels().size() > 1)) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendSystemMessage(Component.literal(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName()).withStyle(ChatFormatting.YELLOW));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Difficulty level set to {}", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName());
                    }
                    UndeadNights.serverState.setPossibleHordesIndex(-1);
                    UndeadNights.serverState.setFirstDifficultyLevelPrinted(true);
                }
            }

            UndeadNights.serverState.setHordeNight(false);
            UndeadNights.serverState.setSpawnZombies(true);
            UndeadNights.serverState.setRespawnZombies(false);
            UndeadNights.serverState.setTryToSpawnRandomHorde(true);
        }
        return 0;
    }
}
