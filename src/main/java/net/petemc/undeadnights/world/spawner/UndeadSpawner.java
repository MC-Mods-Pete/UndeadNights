package net.petemc.undeadnights.world.spawner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.petemc.undeadnights.Config;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import org.jetbrains.annotations.NotNull;

public class UndeadSpawner implements CustomSpawner {
    private double x = 0;
    private double z = 0;
    private double d = 0;

    private boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        BlockState blockState = level.getBlockState(mutable);
        boolean flag1 = blockState.blocksMotion();
        boolean flag2 = blockState.getFluidState().is(FluidTags.WATER);
        return (flag1 && !flag2);
    }

    private BlockPos getBlockPosWithDistance(BlockPos pos, Level level, int distanceMin, int distanceMax) {
        final RandomSource random = level.random;
        double _d = 0;
        double _x = 0;
        double _z = 0;
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

        return new BlockPos(pos.getX() + (int) _x, level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + (int) _x, pos.getZ() + (int) _z), pos.getZ() + (int) _z);
    }

    @Override
    public int tick(@NotNull ServerLevel level, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning is enabled
        if (!spawnMonsters || !Config.getUndeadNightsEnabled()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!level.dimensionType().hasSkyLight()) {
            return 0;
        }
        
        // Initialize everything
        if (UndeadNights.serverState == null) {
            UndeadNights.serverState = StateSaverAndLoader.getServerState(level.getServer());
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
        long normalizedTimeOfDay = level.getDayTime() - ((level.getDayTime() / 24000L) * 24000);
        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final RandomSource randomSource = level.random;
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

                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
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
            boolean nightIsStarting = (((level.getDayTime() % 12000L) == 0) && ((level.getDayTime() % 24000L) != 0));
            if ((nightIsStarting) && (UndeadNights.serverState.getDaysCounter() >= 1)) {
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (Config.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendSystemMessage(Component.literal(UndeadNights.serverState.getDaysCounter() + " nights remaining until the next Night of the Undead!"));
                        } else {
                            player.sendSystemMessage(Component.literal("This is the last night before the next Night of the Undead!"));
                        }
                    }
                }
                if (Config.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {} DaysCounter: {}", normalizedTimeOfDay, level.getDayTime(), UndeadNights.serverState.getDaysCounter());
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
                    if (!(randomSource.nextFloat() < 0.03F)) {
                        return 0;
                    }
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        BlockPos pos = getBlockPosWithDistance(player.blockPosition(), level, Config.getDistanceMin(), Config.getDistanceMax());
                        if (!checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            HordeZombieEntity e = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
                            e.setPos(pos.getX(), pos.getY(), pos.getZ());
                            if (Config.getPersistentZombies()) {
                                e.setPersistenceRequired();
                            }
                            DifficultyInstance localDifficulty = level.getCurrentDifficultyAt(player.blockPosition());
                            e.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
                            e.setTarget(player);
                            level.addFreshEntity(e);
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
                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (!(randomValue > (100 - Config.getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendSystemMessage(Component.literal("The sun is starting to set and you feel uneasy about the coming night...").withStyle(ChatFormatting.RED));
                    }
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && normalizedTimeOfDay >= 12542) {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    BlockPos pos = player.blockPosition();
                    boolean foundHordeSpawnLocation = false;

                    for (int i= 0; i < 20; i++){
                        // for the given min/max distance, calculate the x and z coordinates deltas
                        if (d == 0) {
                            d = randomSource.nextIntBetweenInclusive(Config.getDistanceMin(), Config.getDistanceMax());
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

                        pos = player.blockPosition().offset((int) x, 0, (int) z);
                        pos = new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()), pos.getZ());
                        foundHordeSpawnLocation = checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ());
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
                                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM.get(), SoundSource.HOSTILE, 4.0F, 1);
                                player.sendSystemMessage(Component.literal("A horde has spawned!").withStyle(ChatFormatting.RED));
                                if (Config.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("A Horde has spanned!");
                                }
                            }

                            // spawn demolition zombies
                            randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                            if (Config.getSpawnDemolitionZombies() && (randomValue > (100 - Config.getChanceForDemolitionZombieToSpawn()))) {
                                DemolitionZombieEntity e = new DemolitionZombieEntity(ModEntities.DEMOLITION_ZOMBIE.get(), level);
                                int deltaX = randomSource.nextInt(8);
                                int deltaZ = randomSource.nextInt(8);
                                e.setPos(pos.getX() + deltaX, level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                                if (Config.getPersistentZombies()) {
                                    e.setPersistenceRequired();
                                }
                                DifficultyInstance localDifficulty = level.getCurrentDifficultyAt(player.blockPosition());
                                e.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
                                e.setTarget(player);
                                level.addFreshEntity(e);
                                i++;
                                if (i >= Config.getZombieHordeWaveSize()) {
                                    break;
                                }
                            }
                            // spawn elite horde zombies
                            randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                            if (Config.getSpawnEliteZombies() && (randomValue > (100 - Config.getChanceForEliteZombieToSpawn()))) {
                                EliteZombieEntity e = new EliteZombieEntity(ModEntities.ELITE_ZOMBIE.get(), level);
                                int deltaX = randomSource.nextInt(8);
                                int deltaZ = randomSource.nextInt(8);
                                e.setPos(pos.getX() + deltaX, level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                                if (Config.getPersistentZombies()) {
                                    e.setPersistenceRequired();
                                }
                                DifficultyInstance localDifficulty = level.getCurrentDifficultyAt(player.blockPosition());
                                e.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
                                e.setTarget(player);
                                level.addFreshEntity(e);
                                i++;
                                if (i >= Config.getZombieHordeWaveSize()) {
                                    break;
                                }
                            }

                            // spawn normal horde zombies
                            HordeZombieEntity e = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
                            int deltaX = randomSource.nextInt(8);
                            int deltaZ = randomSource.nextInt(8);
                            e.setPos(pos.getX() + deltaX, level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
                            if (Config.getPersistentZombies()) {
                                e.setPersistenceRequired();
                            }
                            DifficultyInstance localDifficulty = level.getCurrentDifficultyAt(player.blockPosition());
                            e.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
                            e.setTarget(player);
                            level.addFreshEntity(e);

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
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    player.sendSystemMessage(Component.literal("You feel at ease, this Night of the Undead is over..."));
                }
                UndeadNights.serverState.setDaysCounter(Config.getDaysBetweenHordeNights());
                if (Config.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", level.getDayTime(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }
            }
            UndeadNights.serverState.setHordeNight(false);
            UndeadNights.serverState.setSpawnZombies(true);
            UndeadNights.serverState.setRespawnZombies(false);
        }

        return 0;
    }
}
