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
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
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
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.spawner.SpecialSpawner;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;

import java.util.List;
import java.util.Objects;

public class UndeadSpawner implements SpecialSpawner {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeToSpawn = 1;
    public static long prevNormalizedTimeOfDay = 0;

    private double x = 0;
    private double z = 0;
    private double d = 0;

    private boolean checkSpawnLocation(ServerWorld world, double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        BlockState blockState = world.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotblockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().isIn(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }
        return doesNotblockMovement && notLeaves && notWater;//(bl && !bl2 && !bl3);
    }

    private BlockPos getBlockPosWithDistance(BlockPos pos, World world, int distanceMin, int distanceMax) {
        final Random random = world.random;
        double _d = 0;
        double _x = 0;
        double _z = 0;
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


    private void spawnHordeMob(ServerWorld world, Random randomSource, BlockPos pos, PlayerEntity player, HordeConfig.MobSpawnData mobSpawnData) {
        EntityType<?> mobType = Registries.ENTITY_TYPE.get(Identifier.of(mobSpawnData.mobId()));
        if (!mobSpawnData.mobId().contains(mobType.getUntranslatedName())) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Reading entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            //mobType = Registries.ENTITY_TYPE.get(Identifier.of("undeadnights:horde_zombie"));
        }
        //MobEntity mob = (MobEntity) mobType.create(world);
        int deltaX = randomSource.nextInt(8);
        int deltaZ = randomSource.nextInt(8);
        //assert mob != null;
        BlockPos.Mutable mutable = new BlockPos.Mutable(pos.getX(), pos.getY() - 1, pos.getZ());
        BlockState blockState = world.getBlockState(mutable);
        int y = 0;
        if (blockState.getFluidState().isIn(FluidTags.WATER)) {
            y = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }
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
                nbtCompound = StringNbtReader.readCompound(mobSpawnData.nbt());
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
        Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, world, SpawnReason.COMMAND, entityx -> {
            entityx.refreshPositionAndAngles(pos.getX() + deltaX, finalY, pos.getZ() + deltaZ, entityx.getYaw(), entityx.getPitch());
            return entityx;
        });

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
                mob.initialize(world, localDifficulty, SpawnReason.NATURAL, null);
                mob.setTarget(player);

                if ((!mobSpawnData.mobId().equals("undeadnights:horde_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:elite_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:demolition_zombie"))) {
                    Objects.requireNonNull(mob.getAttributeInstance(EntityAttributes.FOLLOW_RANGE)).setBaseValue(128.0f);
                    mob.targetSelector.add(1, new ActiveTargetGoal<>(mob, PlayerEntity.class, false, false));
                }
            }
            assert entity != null;
            UndeadNights.serverState.spawnedHordeMobs.put(entity.getUuid(), entity.getUuidAsString());
            world.spawnNewEntityAndPassengers(entity);
        } catch (Exception e) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Reading entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            HordeZombieEntity hZombie = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, world);
            hZombie.setPos(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
            if (MainConfig.getPersistentMobs()) {
                hZombie.setPersistent();
            }
            hZombie.initialize(world, localDifficulty, SpawnReason.NATURAL, null);
            hZombie.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.put(hZombie.getUuid(), hZombie.getUuidAsString());
            world.spawnEntity(hZombie);
        }
    }

    public int spawnHorde(ServerWorld world, ServerPlayerEntity player, Random randomSource) {
        int randomValue = 0;
        BlockPos pos = player.getBlockPos();
        boolean foundHordeSpawnLocation = false;
        int currentHordeCounter = UndeadNights.globalSpawnCounter;

        for (int i= 0; i < 20; i++){
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
            pos = new BlockPos(pos.getX(), world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()), pos.getZ());
            foundHordeSpawnLocation = checkSpawnLocation(world, pos.getX(), pos.getY() - 1, pos.getZ());
            if (!foundHordeSpawnLocation) {
                d = 0;
                x = 0;
                z = 0;
            } else {
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("It took {} tries to find a valid Horde spawn location for player: {}", i + 1, player.getName().getString());
                }
                break;
            }
        }

        if (!foundHordeSpawnLocation) {
            UndeadNights.LOGGER.info("Could not find a valid Horde spawn location for player: {}", player.getName().getString());
            return -1;
        }

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("Spawning Horde for player: {}", player.getName().getString());
        }

        boolean spawnCapReached = false;
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
                            spawnHordeMob(world, randomSource, pos, player, (randomValue > (100 - mobSpawnData.chance())) ? mobSpawnData : HordeConfig.getDefaultHordeMob());
                            waveMobCounter++;
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
                    spawnHordeMob(world, randomSource, pos, player, HordeConfig.getDefaultHordeMob());
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
            List<HordeConfig.HordesData> hordes = HordeConfig.getHordes();
            int hordeIdToSpawn = hordeToSpawn - 1;
            if (hordeToSpawn == 0) {
                hordeIdToSpawn = randomSource.nextBetween(0, hordes.size()-1);
            }
            for (var mobSpawnData : hordes.get(hordeIdToSpawn).hordeMobs()) {
                //
                int mobCount = 0;
                if (mobSpawnData.countMin() >= mobSpawnData.countMax()) {
                    mobCount = mobSpawnData.countMin();
                } else {
                    mobCount = randomSource.nextBetween(mobSpawnData.countMin(),mobSpawnData.countMax());
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Spawning {} {}", mobCount, mobSpawnData.mobId());
                    }
                }
                for (int i = 0; i < mobCount; i++) {
                    boolean spawnMob = true;
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
                        spawnHordeMob(world, randomSource, pos, player, mobSpawnData);
                    }
                    if (UndeadNights.globalSpawnCounter >= MainConfig.getHordeMobsSpawnCap()) {
                        spawnCapReached = true;
                        break;
                    }
                }
                if (UndeadNights.globalSpawnCounter >= MainConfig.getHordeMobsSpawnCap()) {
                    spawnCapReached = true;
                    break;
                }
            }
        }

        if (currentHordeCounter != UndeadNights.globalSpawnCounter) {
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM, SoundCategory.HOSTILE, 4.0F, 1);
            player.sendMessage(Text.translatable("message.undeadnights.horde_spawned").formatted(Formatting.RED));
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("A Horde has spanned!");
            }
        }

        d = 0;
        if (spawnCapReached) {
            UndeadNights.LOGGER.info("Spawncap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
            return -1;
        }
        return 0;
    }


    @Override
    public void spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning is enabled
        if (!spawnMonsters || !MainConfig.getUndeadNightsEnabled()) {
            return;
        }

        // Are we in the Overworld?
        if (!(world.getRegistryKey() == World.OVERWORLD)) {
            return;
        }

        // Check if the SaveState is already initialized
        if (UndeadNights.serverState == null) {
            return;
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = world.getTimeOfDay() - ((world.getTimeOfDay() / 24000L) * 24000);
        if (prevNormalizedTimeOfDay == normalizedTimeOfDay) {
            return;
        }
        boolean nightIsStarting = ((prevNormalizedTimeOfDay < 12000L) && (normalizedTimeOfDay >= 12000L));
        prevNormalizedTimeOfDay = normalizedTimeOfDay;

        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final Random randomSource = world.random;
        int randomValue = 0;

        if (SpawnHordeCommand.spawnHorde) {
            SpawnHordeCommand.spawnHorde = false;
            if (SpawnHordeCommand.entities != null) {
                for (var player : SpawnHordeCommand.entities.stream().toList()) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        if (spawnHorde(world, serverPlayer, randomSource) == -1) {
                            break;
                        }
                    }
                }
                SpawnHordeCommand.entities = null;
            } else {
                for (ServerPlayerEntity player : world.getPlayers(LivingEntity::isAlive)) {
                    if (spawnHorde(world, player, randomSource) == -1) {
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
                    return;
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
                    return;
                }
            }

            // if a new night just started count down the days
            if ((nightIsStarting) && (UndeadNights.serverState.getDaysCounter() >= 1)) {
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (MainConfig.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendMessage(Text.translatable("message.undeadnights.nights_remaining", String.valueOf(UndeadNights.serverState.getDaysCounter())));
                        } else {
                            player.sendMessage(Text.translatable("message.undeadnights.last_nights"));
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {} DaysCounter: {}", normalizedTimeOfDay, world.getTimeOfDay(), UndeadNights.serverState.getDaysCounter());
                }
            }

            // spawn stray zombies for non horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (MainConfig.getSpawnStrayHordeZombies() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                    if (UndeadNights.serverState.getTickCounter() > 0) {
                        UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                        return;
                    } else {
                        UndeadNights.serverState.setTickCounter(5 * 20);
                    }
                    if (!(randomSource.nextFloat() < 0.03F)) {
                        return;
                    }
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        BlockPos pos = getBlockPosWithDistance(player.getBlockPos(), world, MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                        if (!checkSpawnLocation(world, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return;
                        } else {
                            spawnHordeMob(world,randomSource,pos,player,new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, "none", ""));
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return;
            }

            // this is the first tick of a new night
            if (nightIsStarting) {
                randomValue = randomSource.nextBetween(1, 100);
                if (!(randomValue > (100 - MainConfig.getChanceForHordeNight()))) {
                    return;
                } else {
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        player.sendMessage(Text.translatable("message.undeadnights.horde_night").formatted(Formatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && normalizedTimeOfDay >= 12542) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (spawnHorde(world, player, randomSource) == -1) {
                        break;
                    }
                }

                UndeadNights.serverState.setTickCounter(MainConfig.getCooldownBetweenWaves() * 20);
                UndeadNights.serverState.setSpawnZombies(false);
                UndeadNights.serverState.setRespawnZombies(true);
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Spawned Waves for every player: DaysCounter: {} GlobalSpawnCounter: {} Spawn: {}, respawn: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter, UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
                }
                d = 0;
                x = 0;
                z = 0;
            }
        } else {
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    player.sendMessage(Text.translatable("message.undeadnights.horde_night_over"));
                }
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", world.getTimeOfDay(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }
            }
            UndeadNights.serverState.setHordeNight(false);
            UndeadNights.serverState.setSpawnZombies(true);
            UndeadNights.serverState.setRespawnZombies(false);
        }
        return;
    }
}
