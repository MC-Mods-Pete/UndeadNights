package net.petemc.undeadnights.world.spawner;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.spawner.Spawner;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.sound.UndeadNightsSounds;

import java.util.List;
import java.util.Objects;

public class UndeadSpawner implements Spawner {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeToSpawn = 1;
    public static long prevNormalizedTimeOfDay = 0;

    private double x = 0;
    private double z = 0;
    private double d = 0;

    public static boolean isDarkEnoughToSpawn(ServerWorldAccess level, BlockPos pos) {
        return level.getLightLevel(LightType.BLOCK, pos) <= MainConfig.getMaxBlockLightLevelForMonsterSpawns();
    }

    private boolean checkSpawnLocation(ServerWorld world, double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        BlockState blockState = world.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotBlockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        boolean darkEnough = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().isIn(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }
        if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
            darkEnough = isDarkEnoughToSpawn(world, mutable);
        }

        return doesNotBlockMovement && notLeaves && notWater && darkEnough;
    }

    private BlockPos getBlockPosWithDistance(BlockPos pos, World world, int distanceMin, int distanceMax) {
        final Random random = world.random;
        double _d;
        double _x;
        double _z;
        _d = random.nextBetween(distanceMin, distanceMax);
        _x = random.nextBetween(0, (int) _d);
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

        return new BlockPos(pos.getX() + (int) _x, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + (int) _x, pos.getZ() + (int) _z), pos.getZ() + (int) _z);
    }


    private int spawnHordeMob(ServerWorld world, Random randomSource, BlockPos pos, PlayerEntity player, HordeConfig.MobSpawnData mobSpawnData) {
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(new Identifier(mobSpawnData.mobId()));
        if (!mobSpawnData.mobId().contains(entityType.getUntranslatedName())) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            //entityType = Registries.ENTITY_TYPE.get(new Identifier("undeadnights:horde_zombie"));
        }
        int deltaX = randomSource.nextInt(5);
        int deltaZ = randomSource.nextInt(5);
        if (!randomSource.nextBoolean()) {
            deltaX = deltaX * -1;
        }
        if (!randomSource.nextBoolean()) {
            deltaZ = deltaZ * -1;
        }
        //BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX() + deltaX, pos.getY() - 1, pos.getZ() + deltaZ);
        //BlockState blockState = level.getBlockState(mutable);
        int y;
        /*
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }
        */
        if (MainConfig.getHordeWavesCanSpawnOnTrees()) {
            y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ);
        } else {
            y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }
        if (mobSpawnData.mobId().equals("minecraft:ghast")) {
            y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ) + 20;
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

        int finalY = y;
        int finalDeltaX = deltaX;
        int finalDeltaZ = deltaZ;
        Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, world, entityx -> {
            entityx.refreshPositionAndAngles(pos.getX() + finalDeltaX, finalY, pos.getZ() + finalDeltaZ, entityx.getYaw(), entityx.getPitch());
            return entityx;
        });

        if (entity instanceof Monster) {
            if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
                if (!isDarkEnoughToSpawn(world, new BlockPos(pos.getX() + deltaX, finalY, pos.getZ() + deltaZ))) {
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
        LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
        try {
            if (entity instanceof MobEntity mob) {
                mob.initialize(world, localDifficulty, SpawnReason.NATURAL, null, null);
                mob.setTarget(player);

                if (mobSpawnData.mobId().equals("minecraft:zombie")) {
                    mob.goalSelector.add(1, new SwimGoal(mob));
                    mob.goalSelector.add(1, new BreakBlockGoal((ZombieEntity) mob));
                }

                if ((!mobSpawnData.mobId().equals("undeadnights:horde_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:elite_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:demolition_zombie"))) {
                    Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE)).setBaseValue(128.0f);
                    mob.targetSelector.add(1, new ActiveTargetGoal<>(mob, PlayerEntity.class, false, false));
                }
            }
            assert entity != null;
            UndeadNights.serverState.spawnedHordeMobs.add(entity.getUuid());
            world.spawnNewEntityAndPassengers(entity);
        } catch (Exception e) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            HordeZombieEntity hZombie = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, world);
            hZombie.setPos(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
            if (MainConfig.getPersistentMobs()) {
                hZombie.setPersistent();
            }
            hZombie.initialize(world, localDifficulty, SpawnReason.NATURAL, null, null);
            hZombie.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.add(hZombie.getUuid());
            world.spawnEntity(hZombie);
        }
        return 0;
    }

    public int spawnHorde(ServerWorld world, ServerPlayerEntity player, Random randomSource) {
        int randomValue;
        int spawnCounter = 0;
        BlockPos pos;
        boolean foundHordeSpawnLocation = false;
        int currentHordeCounter = UndeadNights.globalSpawnCounter;

        for (int i= 0; i < 20; i++) {
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

            pos = player.getBlockPos().add((int) x, 0, (int) z);

            /*
            // TODO cave horde spawning
            BlockPos posPlayerLevel = player.blockPosition().offset((int) x, 0, (int) z);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(posPlayerLevel.getX(), posPlayerLevel.getY(), posPlayerLevel.getZ());

            if (level.getBlockState(posPlayerLevel).getBlock().toString().contains("air")) {
                foundHordeSpawnLocation = true;
                while (blockpos$mutableblockpos.getY() > level.getMinBuildHeight() && !level.getBlockState(blockpos$mutableblockpos).blocksMotion()) {
                    blockpos$mutableblockpos.move(Direction.DOWN);
                }
            }

            UndeadNights.LOGGER.info("Block at position {} is {} was changed to {}", pos, level.getBlockState(posPlayerLevel).getBlock(), level.getBlockState(blockpos$mutableblockpos).getBlock());
*/
            pos = new BlockPos(pos.getX(), world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()), pos.getZ());
            foundHordeSpawnLocation = checkSpawnLocation(world, pos.getX(), pos.getY() - 1, pos.getZ());
            if (!foundHordeSpawnLocation) {
                d = 0;
                x = 0;
                z = 0;
                continue;
            }

            //pos = blockpos$mutableblockpos;

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
                while (waveMobCounter < (HordeConfig.getMaxWaveSize())) {
                    if (!HordeConfig.getHordeMobs().isEmpty()) {
                        for (var mobSpawnData : HordeConfig.getHordeMobs()) {
                            if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                                randomValue = randomSource.nextBetween(1, 100);
                                if (spawnHordeMob(world, randomSource, pos, player, (randomValue > (100 - mobSpawnData.chance())) ? mobSpawnData : HordeConfig.getDefaultHordeMob()) == 0) {
                                    spawnCounter++;
                                    waveMobCounter++;
                                }
                                if (waveMobCounter >= HordeConfig.getMaxWaveSize()) {
                                    d = 0;
                                    break;
                                }
                            } else {
                                // spawn cap reached, don't spawn anymore mobs in this wave
                                waveMobCounter = HordeConfig.getMaxWaveSize();
                                spawnCapReached = true;
                                d = 0;
                                break;
                            }
                        }
                    } else {
                        if (spawnHordeMob(world, randomSource, pos, player, HordeConfig.getDefaultHordeMob()) == 0) {
                            spawnCounter++;
                            waveMobCounter++;
                        }
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
                List<HordeConfig.HordesData> hordes = HordeConfig.getHordes();
                int hordeIdToSpawn = hordeToSpawn - 1;
                if (hordeToSpawn == 0) {
                    hordeIdToSpawn = randomSource.nextBetween(0, hordes.size() - 1);
                }
                for (var mobSpawnData : hordes.get(hordeIdToSpawn).hordeMobs()) {
                    int mobCount;
                    if (mobSpawnData.countMin() >= mobSpawnData.countMax()) {
                        mobCount = mobSpawnData.countMin();
                    } else {
                        mobCount = randomSource.nextBetween(mobSpawnData.countMin(),mobSpawnData.countMax());
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Spawning {} {}", mobCount, mobSpawnData.mobId());
                        }
                    }
                    for (int j = 0; j < mobCount; j++) {
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
                            if (spawnHordeMob(world, randomSource, pos, player, mobSpawnData) == 0) {
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

            if (currentHordeCounter != UndeadNights.globalSpawnCounter) {
                if (spawnCounter != 0) {
                    if (MainConfig.getHordeSpawnedMessageAndSound()) {
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM, SoundCategory.HOSTILE, 4.0F, 1);
                        player.sendMessage(Text.translatable("message.undeadnights.horde_spawned").formatted(Formatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("A Horde has spanned!");
                    }
                    return 0;
                } else {
                    d = 0;
                    x = 0;
                    z = 0;
                    continue;
                }
            }

            d = 0;
            if (spawnCapReached) {
                UndeadNights.LOGGER.info("Spawn cap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
                return -1;
            }
        } // <---
        if (spawnCounter == 0) {
            UndeadNights.LOGGER.info("Failed to spawn a horde.");
            return -1;
        }

        return 0;
    }


    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning are enabled
        if (!spawnMonsters || !MainConfig.getUndeadNightsEnabled()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!(world.getRegistryKey() == World.OVERWORLD)) {
            return 0;
        }

        // Check if the SaveState is already initialized
        if (UndeadNights.serverState == null) {
            return 0;
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = world.getTimeOfDay() - ((world.getTimeOfDay() / 24000L) * 24000);
        if (UndeadNights.serverState.getPrevNormalizedTimeOfDay() == normalizedTimeOfDay) {
            return 0;
        }
        UndeadNights.serverState.setNightIsStarting((UndeadNights.serverState.getPrevNormalizedTimeOfDay() <  12000L) && (normalizedTimeOfDay >= 12000L));
        UndeadNights.serverState.setPrevNormalizedTimeOfDay(normalizedTimeOfDay);

        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final Random randomSource = world.random;
        int randomValue = 0;


        if (!UndeadNights.serverState.entitiesWithPendingHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithPendingHorde.stream().toList()) {
                if (!UndeadNights.serverState.entitiesWithReceivedHorde.contains(playerUUID)) {
                    Entity entity = world.getEntity(playerUUID);
                    if (entity instanceof ServerPlayerEntity serverPlayer) {
                        if (spawnHorde(world, serverPlayer, randomSource) == -1) {
                            serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_spawn_horde_failed"));
                        }
                        UndeadNights.serverState.entitiesWithReceivedHorde.add(playerUUID);
                        UndeadNights.serverState.entitiesWithPendingHorde.remove(playerUUID);
                    }
                }
            }
        }

        if (!UndeadNights.serverState.entitiesWithReceivedHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithReceivedHorde.stream().toList()) {
                Entity entity = world.getEntity(playerUUID);
                if (entity instanceof ServerPlayerEntity serverPlayer) {
                    if (!serverPlayer.hasStatusEffect(ModEffects.LURE_HORDE)) {
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(playerUUID);
                    }
                }
            }
        }

        if (SpawnHordeCommand.spawnHorde) {
            SpawnHordeCommand.spawnHorde = false;
            if (SpawnHordeCommand.entities != null) {
                for (var player : SpawnHordeCommand.entities.stream().toList()) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        if (spawnHorde(world, serverPlayer, randomSource) == -1) {
                            serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_spawn_horde_failed"));
                            break;
                        }
                    }
                }
                SpawnHordeCommand.entities = null;
            } else {
                for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                    if (spawnHorde(world, player, randomSource) == -1) {
                        player.sendMessage(Text.translatable("message.undeadnights.command_spawn_horde_failed"));
                        break;
                    }
                }
            }
        }

        // is it night...?
        if (itIsNight) {
            // if it's already a horde night, check if we should respawn new waves
            if (UndeadNights.serverState.getRespawnZombies() && UndeadNights.serverState.getHordeNight() && MainConfig.getSpawnAdditionalWaves()) {
                if (UndeadNights.serverState.getTickCounter() > 0) {
                    UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                    return 0;
                } else {
                    UndeadNights.serverState.setTickCounter(MainConfig.getCooldownBetweenWaves() * 20);
                }
                randomValue = randomSource.nextBetween(1, 100);
                if (randomValue > (100 - MainConfig.getChanceForAdditionalWaves())) {
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
                        for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                            player.sendMessage(Text.translatable("message.undeadnights.days_of_grace_remaining", String.valueOf(UndeadNights.serverState.getGracePeriod())));
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
                    for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendMessage(Text.translatable("message.undeadnights.nights_remaining", String.valueOf(UndeadNights.serverState.getDaysCounter())));
                        } else {
                            player.sendMessage(Text.translatable("message.undeadnights.last_nights"));
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {}, DaysCounter: {}, GameTime: {}, GameTimeDays: {}", normalizedTimeOfDay, world.getTimeOfDay(), UndeadNights.serverState.getDaysCounter(), world.getTime(), (world.getTime() / 24000));
                }
            }

            // spawn a random horde and/or stray zombies for non-horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (UndeadNights.serverState.getTryToSpawnRandomHorde()) {
                    if (MainConfig.getEnableRandomHordes() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                        randomValue = randomSource.nextBetween(1, 100);
                        if ((randomValue > (100 - MainConfig.getChanceForRandomHordes()))) {
                            for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                                if (spawnHorde(world, player, randomSource) == -1) {
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
                    for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                        BlockPos pos = getBlockPosWithDistance(player.getBlockPos(), world, MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                        if (!checkSpawnLocation(world, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            spawnHordeMob(world, randomSource, pos, player, new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, "none", ""));
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
                randomValue = randomSource.nextBetween(1, 100);
                if (!(randomValue > (100 - MainConfig.getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setIsNaturalSpawningOk(true);
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setFirstWaveHasSpawned(false);
                    for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                        player.sendMessage(Text.translatable("message.undeadnights.horde_night").formatted(Formatting.RED));
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
                for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                    if (spawnHorde(world, player, randomSource) == -1) {
                        break;
                    } else {
                        UndeadNights.serverState.setFirstWaveHasSpawned(true);
                    }
                    UndeadNights.serverState.setHordesCounter(UndeadNights.serverState.getHordesCounter() - 1);
                }

                if (!UndeadNights.serverState.getFirstWaveHasSpawned()) {
                    UndeadNights.serverState.setTickCounter(10 * 20);
                } else {
                    UndeadNights.serverState.setTickCounter(MainConfig.getCooldownBetweenWaves() * 20);
                    UndeadNights.serverState.setSpawnZombies(false);
                    UndeadNights.serverState.setRespawnZombies(true);
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Spawned waves for every player: DaysCounter: {} GlobalSpawnCounter: {} Spawn: {}, respawn: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter, UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
                    }
                }
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                d = 0;
                x = 0;
                z = 0;
            }
        } else {
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                    player.sendMessage(Text.translatable("message.undeadnights.horde_night_over"));
                }
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                if (MainConfig.getMaxHordesPerHordeNight() != 0) {
                    UndeadNights.serverState.setHordesCounter(MainConfig.getMaxHordesPerHordeNight() + 1);
                } else {
                    UndeadNights.serverState.setHordesCounter(0);
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", world.getTimeOfDay(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
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
