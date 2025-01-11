package net.petemc.undeadnights.world.spawner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.spawner.SpecialSpawner;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.UndeadNightsConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;

public class UndeadSpawner implements SpecialSpawner {
    private StateSaverAndLoader serverState = null;

    public static int hordeSpawnCounter = 0;
    private int daysCounter = 0;

    private static int randomValue;

    private static boolean spawnZombies = false;
    private static boolean respawnZombies = false;

    private int tickCounter = 60;
    private double x = 0;
    private double z = 0;
    private double d = 0;

    private boolean checkSpawnLocation(ServerWorld world, double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        BlockState blockState = world.getBlockState(mutable);
        boolean bl = blockState.blocksMovement();
        boolean bl2 = blockState.getFluidState().isIn(FluidTags.WATER);
        return (bl && !bl2);
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

    @Override
    public int spawn(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning is enabled
        if (!spawnMonsters || !UndeadNightsConfig.INSTANCE.undeadNightsEnabled) {
            return 0;
        }

        // Are we in the Overworld?
        if (!world.getDimension().hasSkyLight()) {
            return 0;
        }

        // Initialize everything
        if (serverState == null) {
            serverState = StateSaverAndLoader.getServerState(world.getServer());
            daysCounter = serverState.daysCounter;
            if (daysCounter > UndeadNightsConfig.INSTANCE.daysBetweenHordeNights) {
                daysCounter = UndeadNightsConfig.INSTANCE.daysBetweenHordeNights;
                serverState.daysCounter = daysCounter;
            }
            UndeadNights.hordeNight = serverState.hordeNight;
            spawnZombies = serverState.spawnZombies;
            respawnZombies = serverState.respawnZombies;
            hordeSpawnCounter = serverState.hordeSpawnCounter;
            if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                UndeadNights.LOGGER.info("INIT CurrentDaysCounter: {} CurrentHordeSpawnCounter: {} HordeNight: {}", daysCounter, hordeSpawnCounter, UndeadNights.hordeNight);
                UndeadNights.LOGGER.info("INIT spawnZombies: {} respawnZombies: {}", spawnZombies, respawnZombies);
            }
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = world.getTimeOfDay() - ((world.getTimeOfDay() / 24000L) * 24000);
        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        // is it night...?
        if (itIsNight) {
            // if it's already a horde night, check if we should respawn new waves
            if (respawnZombies && UndeadNights.hordeNight && UndeadNightsConfig.INSTANCE.spawnAdditionalWaves) {
                if (tickCounter > 0) {
                    tickCounter--;
                    serverState.tickCounter = tickCounter;
                    serverState.markDirty();
                    return 0;
                } else {
                    tickCounter = UndeadNightsConfig.INSTANCE.cooldownBetweenWaves * 20;
                    serverState.tickCounter = tickCounter;
                    serverState.markDirty();
                }
                randomValue = MathHelper.nextInt(Random.create(), 1, 100);
                if (randomValue > (100 - UndeadNightsConfig.INSTANCE.chanceForAdditionalWaves)) {
                    if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                        UndeadNights.LOGGER.info("New Wave, randomValue was: {}", randomValue);
                    }
                    spawnZombies = true;
                    respawnZombies = false;
                    hordeSpawnCounter = 0;
                    serverState.hordeSpawnCounter = 0;
                    serverState.spawnZombies = true;
                    serverState.respawnZombies = false;
                    serverState.markDirty();
                } else {
                    if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                        UndeadNights.LOGGER.info("RandomValue: {}", randomValue);
                    }
                    return 0;
                }
            }

            // if a new night just started count down the days
            boolean nightIsStarting = (((world.getTimeOfDay() % 12000L) == 0) && ((world.getTimeOfDay() % 24000L) != 0));
            if ((nightIsStarting) && (daysCounter >= 1)) {
                daysCounter = daysCounter - 1;
                serverState.daysCounter = daysCounter;
                serverState.markDirty();
                if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {} DaysCounter: {}", normalizedTimeOfDay, world.getTimeOfDay(), daysCounter);
                }
            }

            // spawn stray zombies for non horde nights
            if (daysCounter > 0 && !UndeadNights.hordeNight) {
                if (UndeadNightsConfig.INSTANCE.spawnStrayHordeZombies && (UndeadNights.globalSpawnCounter < UndeadNightsConfig.INSTANCE.hordeZombiesSpawnCap)) {
                    if (tickCounter > 0) {
                        tickCounter--;
                        serverState.tickCounter = tickCounter;
                        serverState.markDirty();
                        return 0;
                    } else {
                        tickCounter = 5 * 20;
                        serverState.tickCounter = tickCounter;
                        serverState.markDirty();
                    }
                    final Random random = world.random;
                    if (!(random.nextFloat() < 0.03F)) {
                        return 0;
                    }
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        BlockPos pos = getBlockPosWithDistance(player.getBlockPos(), world, UndeadNightsConfig.INSTANCE.distanceMin, UndeadNightsConfig.INSTANCE.distanceMax);
                        if (!checkSpawnLocation(world, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            HordeZombieEntity e = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, world);
                            e.setPosition(pos.getX(), pos.getY(), pos.getZ());
                            if (UndeadNightsConfig.INSTANCE.persistentZombies) {
                                e.setPersistent();
                            }
                            EntityData entityData = null;
                            LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                            entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                            e.setTarget(player);
                            world.spawnEntity(e);
                            if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return 0;
            }

            // this is the first tick of a new night
            if (nightIsStarting) {
                randomValue = MathHelper.nextInt(Random.create(), 1, 100);
                if (!(randomValue > (100 - UndeadNightsConfig.INSTANCE.chanceForHordeNight))) {
                    return 0;
                } else {
                    UndeadNights.hordeNight = true;
                    serverState.hordeNight = true;
                    spawnZombies = true;
                    serverState.spawnZombies = true;
                    serverState.markDirty();
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        player.sendMessage(Text.literal("The sun is starting to set and you feel uneasy about the coming night...").formatted(Formatting.RED));
                    }
                    if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.hordeNight);
                    }
                }
            }

            // spawn the waves
            final Random random = world.random;
            if (spawnZombies && UndeadNights.hordeNight && normalizedTimeOfDay >= 12542) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    BlockPos pos = player.getBlockPos();
                    boolean foundHordeSpawnLocation = false;

                    for (int i= 0; i < 20; i++){
                        // for the given min/max distance, calculate the x and z coordinates deltas
                        if (d == 0) {
                            d = random.nextBetween(UndeadNightsConfig.INSTANCE.distanceMin, UndeadNightsConfig.INSTANCE.distanceMax);
                            x = random.nextBetween(0, (int) d);
                            if (x == 0) {
                                z = d;
                            } else {
                                z = Math.sqrt((d * d) - (x * x));
                                if (random.nextBoolean()) {
                                    x = x * -1;
                                }
                            }
                            if (random.nextBoolean()) {
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
                            if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                                UndeadNights.LOGGER.info("It took {} tries to find a valid Horde spawn location for player: {}", i + 1, player.getName().getString());
                            }
                            break;
                        }
                    }

                    if (!foundHordeSpawnLocation) {
                        UndeadNights.LOGGER.info("Could not find a valid Horde spawn location for player: {}", player.getName().getString());
                        break;
                    }

                    if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                        UndeadNights.LOGGER.info("Spawning Horde for player: {}", player.getName().getString());
                    }

                    for (int i = 0; i < UndeadNightsConfig.INSTANCE.zombieHordeWaveSize; i++) {
                        if (UndeadNights.globalSpawnCounter < UndeadNightsConfig.INSTANCE.hordeZombiesSpawnCap) {
                            if (i == 0) {
                                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM, SoundCategory.HOSTILE, 4.0F, 1);
                                player.sendMessage(Text.literal("A horde has spawned!").formatted(Formatting.RED));
                                if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                                    UndeadNights.LOGGER.info("A Horde has spanned!");
                                }
                            }

                            // spawn demolition zombies
                            randomValue = MathHelper.nextInt(Random.create(), 1, 100);
                            if (UndeadNightsConfig.INSTANCE.spawnDemolitionZombies && (randomValue > (100 - UndeadNightsConfig.INSTANCE.chanceForDemolitionZombieToSpawn))) {
                                DemolitionZombieEntity e = new DemolitionZombieEntity(ModEntities.DEMOLITION_ZOMBIE, world);
                                int deltaX = random.nextInt(8);
                                int deltaZ = random.nextInt(8);
                                e.setPosition(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                                if (UndeadNightsConfig.INSTANCE.persistentZombies) {
                                    e.setPersistent();
                                }
                                e.setTarget(player);
                                EntityData entityData = null;
                                LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                                entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                                world.spawnEntity(e);
                                i++;
                                if (i >= UndeadNightsConfig.INSTANCE.zombieHordeWaveSize) {
                                    break;
                                }
                            }
                            // spawn elite horde zombies
                            randomValue = MathHelper.nextInt(Random.create(), 1, 100);
                            if (UndeadNightsConfig.INSTANCE.spawnEliteZombies && (randomValue > (100 - UndeadNightsConfig.INSTANCE.chanceForEliteZombieToSpawn))) {
                                EliteZombieEntity e = new EliteZombieEntity(ModEntities.ELITE_ZOMBIE, world);
                                int deltaX = random.nextInt(8);
                                int deltaZ = random.nextInt(8);
                                e.setPosition(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                                if (UndeadNightsConfig.INSTANCE.persistentZombies) {
                                    e.setPersistent();
                                }
                                EntityData entityData = null;
                                LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                                entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                                e.setTarget(player);
                                world.spawnEntity(e);
                                i++;
                                if (i >= UndeadNightsConfig.INSTANCE.zombieHordeWaveSize) {
                                    break;
                                }
                            }

                            // spawn normal horde zombies
                            HordeZombieEntity e = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, world);
                            int deltaX = random.nextInt(8);
                            int deltaZ = random.nextInt(8);
                            e.setPosition(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                            if (UndeadNightsConfig.INSTANCE.persistentZombies) {
                                e.setPersistent();
                            }
                            EntityData entityData = null;
                            LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                            entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                            e.setTarget(player);
                            world.spawnEntity(e);

                        } else {
                            UndeadNights.LOGGER.info("{} Horde Zombie spawned, spawncap reached.", UndeadNightsConfig.INSTANCE.hordeZombiesSpawnCap);
                            break;
                        }
                    }
                    if (!(UndeadNights.globalSpawnCounter < UndeadNightsConfig.INSTANCE.hordeZombiesSpawnCap)) {
                        break;
                    }
                }
                tickCounter = UndeadNightsConfig.INSTANCE.cooldownBetweenWaves * 20;
                spawnZombies = false;
                respawnZombies = true;
                serverState.spawnZombies = false;
                serverState.respawnZombies = true;
                daysCounter = UndeadNightsConfig.INSTANCE.daysBetweenHordeNights;
                serverState.daysCounter = daysCounter;
                serverState.markDirty();
                d = 0;
                x = 0;
                z = 0;
            }
        } else {
            if (UndeadNights.hordeNight) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    player.sendMessage(Text.literal("You feel at ease, this Night of the Undead is over..."));
                }
                if (UndeadNightsConfig.INSTANCE.printDebugMessages) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", world.getTimeOfDay(), daysCounter, UndeadNights.globalSpawnCounter);
                }
            }
            UndeadNights.hordeNight = false;
            serverState.hordeNight = false;
            serverState.markDirty();
            spawnZombies = true;
            respawnZombies = false;
            serverState.spawnZombies = true;
            serverState.respawnZombies = false;
            hordeSpawnCounter = 0;
            serverState.hordeSpawnCounter = hordeSpawnCounter;
            serverState.markDirty();
        }

        return 0;
    }
}
