package net.petemc.undeadnights.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.world.spawner.HordeSpawner;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class SpawnProcess {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeIdFromHordesConfig = 1;

    private static double x = 0;
    private static double z = 0;
    private static double d = 0;

    // spawn a single horde mob at the given location (Asynchronous wrapper)
    public static CompletableFuture<HordeSpawner.SpawnHordeResult> asynchronousHordeSpawner(ServerWorld level, ServerPlayerEntity player, RandomExtention randomSource) {
        return CompletableFuture.supplyAsync(() -> spawnHordeImplementation(
                level,
                player,
                randomSource
        ), ForkJoinPool.commonPool());
    }

    public static HordeSpawner.SpawnHordeResult synchronousHordeSpawner(ServerWorld level, ServerPlayerEntity player, RandomExtention randomSource) {
        return spawnHordeImplementation(
                level,
                player,
                randomSource
        );
    }

    // spawn a horde for the given player at a suitable location
    public static HordeSpawner.SpawnHordeResult spawnHordeImplementation(ServerWorld level, ServerPlayerEntity player, RandomExtention randomSource) {
        int randomValue;
        int spawnCounter = 0;
        BlockPos possibleSpawnLocation;
        boolean foundHordeSpawnLocation = false;
        int currentHordeCounter = UndeadNights.globalSpawnCounter;
        int maxChecks = 30;

        for (int i= 0; i < maxChecks; i++) {
            // for the given min/max distance, calculate the x and z coordinates deltas
            if (d == 0) {
                d = randomSource.nextBetween(MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                x = randomSource.nextBetween(0, (int) d);
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
            possibleSpawnLocation = player.getBlockPos().add((int) x, 0, (int) z);

            // cave spawning check
            if (MainConfig.getHordeWavesCanSpawnInCaves() && playerInCave) {
                //possibleSpawnLocation = Helpers.findEndPositionForPathAStar(level, player.blockPosition(), MainConfig.getCaveSpawnDistance(), false, 0.6f, 1.8f, 20000, 1, 4, 10);
                possibleSpawnLocation = SpawnLocationFinder.findEndPositionUsingMinecraftPathfinding(level, player, MainConfig.getCaveSpawnDistance(), MainConfig.getHordeWavesCanSpawnInWater());
                if (possibleSpawnLocation == null) {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Cave horde spawn location calculation for player {} failed, trying again.", player.getName().getString());
                    }
                    d = 0;
                    continue;
                }
                if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
                    if (!SpawnLocationFinder.isDarkEnoughToSpawn(level, possibleSpawnLocation)) {
                        d = 0;
                        continue;
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    player.sendMessage(Text.literal("[DEBUG] Cave horde spawn location calculated.").formatted(Formatting.DARK_AQUA));
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Cave horde spawn location calculation for player {} is done, result: {}", player.getName().getString(), possibleSpawnLocation);
                }
                foundHordeSpawnLocation = true;
            } else {
                // use the heightmap to find the surface level at the possible spawn location
                possibleSpawnLocation = new BlockPos(possibleSpawnLocation.getX(), level.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, possibleSpawnLocation.getX(), possibleSpawnLocation.getZ()) - 1, possibleSpawnLocation.getZ());

                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Surface horde spawning check for player {} at position {}", player.getName().getString(), possibleSpawnLocation);
                }
                foundHordeSpawnLocation = SpawnLocationFinder.checkSpawnLocation(level, possibleSpawnLocation.getX(), possibleSpawnLocation.getY() - 1, possibleSpawnLocation.getZ());
            }

            if (!foundHordeSpawnLocation) {
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Horde spawn location check for player {} at position {} failed, trying again.", player.getName().getString(), possibleSpawnLocation);
                }
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
                                randomValue = randomSource.nextBetween(1, 100);
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
                        int index = randomSource.nextBetween(0, (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getListOfPossibleHordes().size() - 1));
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
                        mobCount = randomSource.nextBetween(mobSpawnData.countMin(), mobSpawnData.countMax());
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
                            randomValue = randomSource.nextBetween(1, 100);
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
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM, SoundCategory.HOSTILE, 4.0F, 1);
                        player.sendMessage(Text.translatable("message.undeadnights.horde_spawned").formatted(Formatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Horde was spawned for player {} (removing from horde lists)", player.getName().getString());
                    }
                    //UndeadNights.serverState.entitiesWithPendingHorde.remove(player.getUUID());
                    //UndeadNights.serverState.entitiesWithReceivedHorde.add(player.getUUID());
                    return HordeSpawner.SpawnHordeResult.DONE;
                } else {
                    continue;
                }
            }

            if (spawnCapReached) {
                UndeadNights.LOGGER.info("Spawn cap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
                return HordeSpawner.SpawnHordeResult.FAILED;
            }
        } // <---
        if (spawnCounter == 0) {
            UndeadNights.LOGGER.info("Failed to spawn a horde.");
            return HordeSpawner.SpawnHordeResult.FAILED;
        }

        return HordeSpawner.SpawnHordeResult.DONE;
    }

    // spawn a single horde mob at the given location
    public static int spawnHordeMob(ServerWorld level, RandomExtention randomSource, BlockPos pos, PlayerEntity player, HordeConfig.MobSpawnData mobSpawnData) {
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(new Identifier(mobSpawnData.mobId()));
        if (!mobSpawnData.mobId().contains(entityType.getUntranslatedName())) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            //entityType = Registries.ENTITY_TYPE.get(new Identifier("undeadnights:horde_zombie"));
        }

        NbtCompound nbtCompound = new NbtCompound();
        if (!Objects.equals(mobSpawnData.nbt(), "")) {
            try {
                nbtCompound = StringNbtReader.parse(mobSpawnData.nbt());
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
            pos = SpawnLocationFinder.findSpawnablePosition(level, pos, 8, 5);
        } else {
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Player {} is not in cave, attempting to spawn horde mob on surface.", player.getName().getString());
            }
            pos = SpawnLocationFinder.findNearbySurfaceSpawnPosition(level, pos, randomSource, playerInCave);
        }

        Double trackingRange = mobSpawnData.trackingRange() != 0.0 ? mobSpawnData.trackingRange() : UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange();
        BlockPos finalPos = pos;
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("Spawning horde mob {} at position {}, {}, {} with TrackingRange: {}", mobSpawnData.mobId(), finalPos.getX(), finalPos.getY(), finalPos.getZ(), trackingRange);
        }
        Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, level, entityX -> {
            entityX.refreshPositionAndAngles(finalPos.getX(),finalPos.getY(),finalPos.getZ(), entityX.getYaw(), entityX.getPitch());
            return entityX;
        });

        if (entity instanceof Monster) {
            if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
                if (!SpawnLocationFinder.isDarkEnoughToSpawn(level, new BlockPos(pos.getX(), pos.getY(), pos.getZ()))) {
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
        LocalDifficulty localDifficulty = level.getLocalDifficulty(player.getBlockPos());
        try {
            if (entity instanceof MobEntity mob) {
                mob.initialize(level, localDifficulty, SpawnReason.NATURAL, null, null);
                mob.setTarget(player);

                // make vanilla zombies in hordes float on water and give ability to break blocks
                if (mobSpawnData.mobId().equals("minecraft:zombie")) {
                    mob.goalSelector.add(1, new SwimGoal(mob));
                    mob.goalSelector.add(1, new BreakBlockGoal((ZombieEntity) mob));
                }

                Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE)).setBaseValue(trackingRange);

                if ((!mobSpawnData.mobId().equals("undeadnights:horde_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:elite_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:demolition_zombie"))) {
                    mob.targetSelector.add(1, new ActiveTargetGoal<>(mob, PlayerEntity.class, false, false));
                    //Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE)).setBaseValue(128.0f);

                    int playerCount = 1;
                    if (!mob.getWorld().isClient()) {
                        playerCount = mob.getWorld().getPlayers().size();
                    }

                    double healthScaleFactor = 0.0;
                    double damageScaleFactor = 0.0;
                    double speedScaleFactor = 0.0;
                    double armorScaleFactor = 0.0;

                    if (UndeadNights.difficultyConfig.getDynamicScaling().isDynamicScalingEnabled()) {
                        if (playerCount > 1) {
                            healthScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getHealthScalePerPlayer() * (playerCount - 1);
                            speedScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getSpeedScalePerPlayer() * (playerCount - 1);
                            damageScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getDamageScalePerPlayer() * (playerCount - 1);
                            armorScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getArmorScalePerPlayer() * (playerCount - 1);
                        }

                        healthScaleFactor = healthScaleFactor + UndeadNights.serverState.getCurrentHealthScale();
                        speedScaleFactor = speedScaleFactor + UndeadNights.serverState.getCurrentSpeedScale();
                        damageScaleFactor = damageScaleFactor + UndeadNights.serverState.getCurrentDayScaleCounter();
                        armorScaleFactor = armorScaleFactor + UndeadNights.serverState.getCurrentArmorScale();

                        if (healthScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale()) {
                            healthScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale();
                        }
                        if (speedScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale()) {
                            speedScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale();
                        }
                        if (damageScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale()) {
                            damageScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale();
                        }
                        if (armorScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale()) {
                            armorScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale();
                        }
                    }

                    if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isUpdateAttributesOfThirdPartyMobs() ||
                            mobSpawnData.mobId().equals("minecraft:zombie")) {
                        healthScaleFactor = healthScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHealthAttributeScaleFactor() - 1.0;
                        speedScaleFactor = speedScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getSpeedAttributeScaleFactor() - 1.0;
                        damageScaleFactor = damageScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getDamageAttributeScaleFactor() - 1.0;
                        armorScaleFactor = armorScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getArmorAttributeScaleFactor() - 1.0;
                    }

                    boolean flag = (healthScaleFactor > 0.0) || (speedScaleFactor > 0.0) || (damageScaleFactor > 0.0) || (armorScaleFactor > 0.0);

                    if (flag) {
                        Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH))
                                .addPersistentModifier(new EntityAttributeModifier("Monster difficulty health bonus", healthScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

                        Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED))
                                .addPersistentModifier(new EntityAttributeModifier("Monster difficulty speed bonus", speedScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

                        Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE))
                                .addPersistentModifier(new EntityAttributeModifier("Monster difficulty attack damage bonus", damageScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

                        Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_ARMOR))
                                .addPersistentModifier(new EntityAttributeModifier("Monster difficulty armor bonus", armorScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

                        mob.setHealth(mob.getMaxHealth());
                    }
                }
            }
            assert entity != null;
            UndeadNights.serverState.spawnedHordeMobs.add(entity.getUuid());
            if (entity instanceof LivingEntity livingEntity) {
                if (MainConfig.getHordeWavesCanSpawnInCaves()) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 2 * 20, 1));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 2 * 20, 1));
                }
                if (MainConfig.getDebugMakeHordeMobsGlow()) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200 * 20, 1));
                }
            }
            level.spawnNewEntityAndPassengers(entity);
        } catch (Exception e) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            HordeZombieEntity hZombie = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, level);
            hZombie.setPos(pos.getX(), level.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ());
            if (MainConfig.getPersistentMobs()) {
                hZombie.setPersistent();
            }
            hZombie.initialize(level, localDifficulty, SpawnReason.NATURAL, null, null);
            hZombie.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.add(hZombie.getUuid());
            level.spawnEntity(hZombie);
        }
        return 0;
    }


}
