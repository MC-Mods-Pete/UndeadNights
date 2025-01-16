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
import net.petemc.undeadnights.config.Config;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;

public class UndeadSpawner implements SpecialSpawner {
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
        if (!spawnMonsters || !Config.getUndeadNightsEnabled()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!world.getDimension().hasSkyLight()) {
            return 0;
        }

        // Initialize everything
        if (UndeadNights.serverState == null) {
            UndeadNights.serverState = StateSaverAndLoader.getServerState(world.getServer());
            // check if the DaysCounter in the config was changed
            if (UndeadNights.serverState.getLastMaxDaysCounter() != Config.getDaysBetweenHordeNights()) {
                UndeadNights.serverState.setDaysCounter(Config.getDaysBetweenHordeNights());
                UndeadNights.serverState.setLastMaxDaysCounter(Config.getDaysBetweenHordeNights());
            }

            if (Config.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
                UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
            }
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = world.getTimeOfDay() - ((world.getTimeOfDay() / 24000L) * 24000);
        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final Random random = world.random;
        int randomValue = 0;

        // is it night...?
        if (itIsNight) {
            // if it's already a horde night, check if we should respawn new waves
            if (UndeadNights.serverState.getRespawnZombies() && UndeadNights.serverState.getHordeNight() && Config.getSpawnAdditionalWaves()) {
                if (UndeadNights.serverState.getTickCounter() > 0) {
                    UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                    return 0;
                } else {
                    UndeadNights.serverState.setTickCounter(Config.getCooldownBetweenWaves() * 20);
                }
                randomValue = random.nextBetween(1, 100);
                if (randomValue > (100 - Config.getChanceForAdditionalWaves())) {
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("New Wave, randomValue was: {}", randomValue);
                    }
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setRespawnZombies(false);
                } else {
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("RandomValue: {}", randomValue);
                    }
                    return 0;
                }
            }

            // if a new night just started count down the days
            boolean nightIsStarting = (((world.getTimeOfDay() % 12000L) == 0) && ((world.getTimeOfDay() % 24000L) != 0));
            if ((nightIsStarting) && (UndeadNights.serverState.getDaysCounter() >= 1)) {
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (Config.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendMessage(Text.literal(UndeadNights.serverState.getDaysCounter() + " nights remaining until the next Night of the Undead!"));
                        } else {
                            player.sendMessage(Text.literal("This is the last night before the next Night of the Undead!"));
                        }
                    }
                }
                if (Config.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {} DaysCounter: {}", normalizedTimeOfDay, world.getTimeOfDay(), UndeadNights.serverState.getDaysCounter());
                }
            }

            // spawn stray zombies for non horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (Config.getSpawnStrayHordeZombies() && (UndeadNights.globalSpawnCounter < Config.getHordeZombiesSpawnCap())) {
                    if (UndeadNights.serverState.getTickCounter() > 0) {
                        UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                        return 0;
                    } else {
                        UndeadNights.serverState.setTickCounter(5 * 20);
                    }
                    if (!(random.nextFloat() < 0.03F)) {
                        return 0;
                    }
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        BlockPos pos = getBlockPosWithDistance(player.getBlockPos(), world, Config.getDistanceMin(), Config.getDistanceMax());
                        if (!checkSpawnLocation(world, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            HordeZombieEntity e = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, world);
                            e.setPosition(pos.getX(), pos.getY(), pos.getZ());
                            if (Config.getPersistentZombies()) {
                                e.setPersistent();
                            }
                            EntityData entityData = null;
                            LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                            entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                            e.setTarget(player);
                            world.spawnEntity(e);
                            if (Config.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return 0;
            }

            // this is the first tick of a new night
            if (nightIsStarting) {
                randomValue = random.nextBetween(1, 100);
                if (!(randomValue > (100 - Config.getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        player.sendMessage(Text.literal("The sun is starting to set and you feel uneasy about the coming night...").formatted(Formatting.RED));
                    }
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && normalizedTimeOfDay >= 12542) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    BlockPos pos = player.getBlockPos();
                    boolean foundHordeSpawnLocation = false;

                    for (int i= 0; i < 20; i++){
                        // for the given min/max distance, calculate the x and z coordinates deltas
                        if (d == 0) {
                            d = random.nextBetween(Config.getDistanceMin(), Config.getDistanceMax());
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
                            if (Config.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("It took {} tries to find a valid Horde spawn location for player: {}", i + 1, player.getName().getString());
                            }
                            break;
                        }
                    }

                    if (!foundHordeSpawnLocation) {
                        UndeadNights.LOGGER.info("Could not find a valid Horde spawn location for player: {}", player.getName().getString());
                        break;
                    }

                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Spawning Horde for player: {}", player.getName().getString());
                    }

                    for (int i = 0; i < Config.getZombieHordeWaveSize(); i++) {
                        if (UndeadNights.globalSpawnCounter < Config.getHordeZombiesSpawnCap()) {
                            if (i == 0) {
                                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM, SoundCategory.HOSTILE, 4.0F, 1);
                                player.sendMessage(Text.literal("A horde has spawned!").formatted(Formatting.RED));
                                if (Config.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("A Horde has spanned!");
                                }
                            }

                            // spawn demolition zombies
                            randomValue = MathHelper.nextInt(Random.create(), 1, 100);
                            if (Config.getSpawnDemolitionZombies() && (randomValue > (100 - Config.getChanceForDemolitionZombieToSpawn()))) {
                                DemolitionZombieEntity e = new DemolitionZombieEntity(ModEntities.DEMOLITION_ZOMBIE, world);
                                int deltaX = random.nextInt(8);
                                int deltaZ = random.nextInt(8);
                                e.setPosition(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                                if (Config.getPersistentZombies()) {
                                    e.setPersistent();
                                }
                                e.setTarget(player);
                                EntityData entityData = null;
                                LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                                entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                                world.spawnEntity(e);
                                i++;
                                if (i >= Config.getZombieHordeWaveSize()) {
                                    break;
                                }
                            }
                            // spawn elite horde zombies
                            randomValue = MathHelper.nextInt(Random.create(), 1, 100);
                            if (Config.getSpawnEliteZombies() && (randomValue > (100 - Config.getChanceForEliteZombieToSpawn()))) {
                                EliteZombieEntity e = new EliteZombieEntity(ModEntities.ELITE_ZOMBIE, world);
                                int deltaX = random.nextInt(8);
                                int deltaZ = random.nextInt(8);
                                e.setPosition(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                                if (Config.getPersistentZombies()) {
                                    e.setPersistent();
                                }
                                EntityData entityData = null;
                                LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                                entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                                e.setTarget(player);
                                world.spawnEntity(e);
                                i++;
                                if (i >= Config.getZombieHordeWaveSize()) {
                                    break;
                                }
                            }

                            // spawn normal horde zombies
                            HordeZombieEntity e = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, world);
                            int deltaX = random.nextInt(8);
                            int deltaZ = random.nextInt(8);
                            e.setPosition(pos.getX() + deltaX, world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                            if (Config.getPersistentZombies()) {
                                e.setPersistent();
                            }
                            EntityData entityData = null;
                            LocalDifficulty localDifficulty = world.getLocalDifficulty(player.getBlockPos());
                            entityData = e.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                            e.setTarget(player);
                            world.spawnEntity(e);

                        } else {
                            UndeadNights.LOGGER.info("Spawncap reached, {} Horde Zombies are already loaded into this world.", Config.getHordeZombiesSpawnCap());
                            break;
                        }
                    }
                    if (!(UndeadNights.globalSpawnCounter < Config.getHordeZombiesSpawnCap())) {
                        break;
                    }
                }
                UndeadNights.serverState.setTickCounter(Config.getCooldownBetweenWaves() * 20);
                UndeadNights.serverState.setSpawnZombies(false);
                UndeadNights.serverState.setRespawnZombies(true);
                UndeadNights.serverState.setDaysCounter(Config.getDaysBetweenHordeNights());
                d = 0;
                x = 0;
                z = 0;
            }
        } else {
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    player.sendMessage(Text.literal("You feel at ease, this Night of the Undead is over..."));
                }
                UndeadNights.serverState.setDaysCounter(Config.getDaysBetweenHordeNights());
                if (Config.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", world.getTimeOfDay(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }
            }
            UndeadNights.serverState.setHordeNight(false);
            UndeadNights.serverState.setSpawnZombies(true);
            UndeadNights.serverState.setRespawnZombies(false);
        }
        return 0;
    }
}
