package net.petemc.undeadnights.world.spawner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class UndeadSpawner implements CustomSpawner {
    private double x = 0;
    private double z = 0;
    private double d = 0;

    private boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        BlockState blockState = level.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotblockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().is(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }
        return doesNotblockMovement && notLeaves && notWater;
    }

    private BlockPos getBlockPosWithDistance(BlockPos pos, Level level, int distanceMin, int distanceMax) {
        final Random random = level.random;
        double _d = 0;
        double _x = 0;
        double _z = 0;
        _d = RandomUtils.nextInt(distanceMin, distanceMax);
        _x = RandomUtils.nextInt(0, (int) _d);
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

    
    private void spawnHordeMob(ServerLevel level, Random randomSource, BlockPos pos, Player player, HordeConfig.MobSpawnData mobSpawnData) {
        EntityType<?> mobType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mobSpawnData.mobId()));
        assert mobType != null;
        Mob mob = (Mob) mobType.create(level);
        int deltaX = randomSource.nextInt(8);
        int deltaZ = randomSource.nextInt(8);
        assert mob != null;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        BlockState blockState = level.getBlockState(mutable);
        int y = 0;
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }
        if ((MainConfig.getHordeWavesCanSpawnOnTrees())) {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ);
        } else {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }
        mob.setPos(pos.getX() + deltaX, y, pos.getZ() + deltaZ);
        if (MainConfig.getPersistentMobs()) {
            mob.setPersistenceRequired();
        }
        if (mobSpawnData.mobId().equals("undeadnights:demolition_zombie")) {
            String str = mobSpawnData.extra();
            String[] strA = str.split(":");
            if (strA[0].equals("tnt")) {
                try {
                    int numberTnt = Integer.parseInt(strA[1]);
                    if (numberTnt > 64) {
                        numberTnt = 64;
                    }
                    DemolitionZombieEntity demolitionZombie = (DemolitionZombieEntity) mob;
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
            mob.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, new Zombie.ZombieGroupData(false, false), null);
            mob.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.add(mob.getUUID());
            level.addFreshEntity(mob);
        } catch (Exception e) {
            UndeadNights.LOGGER.warn("Reading entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            HordeZombieEntity hZombie = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
            hZombie.setPos(pos.getX() + deltaX, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
            if (MainConfig.getPersistentMobs()) {
                hZombie.setPersistenceRequired();
            }
            hZombie.finalizeSpawn(level, localDifficulty, MobSpawnType.NATURAL, null, null);
            hZombie.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.add(hZombie.getUUID());
            level.addFreshEntity(hZombie);
        }
    }


    @Override
    public int tick(@NotNull ServerLevel level, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning is enabled
        if (!spawnMonsters || !MainConfig.getUndeadNightsEnabled()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!(level.dimension() == Level.OVERWORLD)) {
            return 0;
        }

        // Initialize everything
        if (UndeadNights.serverState == null) {
            UndeadNights.serverState = StateSaverAndLoader.getServerState(level.getServer());
            // check if the DaysCounter in the config was changed
            if (UndeadNights.serverState.getLastMaxDaysCounter() != MainConfig.getDaysBetweenHordeNights()) {
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                UndeadNights.serverState.setLastMaxDaysCounter(MainConfig.getDaysBetweenHordeNights());
            }

            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
                UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
            }
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = level.getDayTime() - ((level.getDayTime() / 24000L) * 24000);
        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final Random randomSource = level.random;
        int randomValue = 0;

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

                randomValue = randomSource.nextInt(1, 100);
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

            // if a new night just started count down the days
            boolean nightIsStarting = (((level.getDayTime() % 12000L) == 0) && ((level.getDayTime() % 24000L) != 0));
            if ((nightIsStarting) && (UndeadNights.serverState.getDaysCounter() >= 1)) {
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (MainConfig.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendMessage(Component.nullToEmpty(String.valueOf(UndeadNights.serverState.getDaysCounter()) + " nights remaining until the next Night of the Undead!"), player.getUUID());
                        } else {
                            player.sendMessage(Component.nullToEmpty("This is the last night before the next Night of the Undead!"), player.getUUID());
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {} DaysCounter: {}", normalizedTimeOfDay, level.getDayTime(), UndeadNights.serverState.getDaysCounter());
                }
            }

            // spawn stray zombies for non horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (MainConfig.getSpawnStrayHordeZombies() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
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
                            spawnHordeMob(level, randomSource, pos, player, new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, "none"));
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return 0;
            }

            // this is the first tick of a new night
            if (nightIsStarting) {
                randomValue = randomSource.nextInt(1, 100);
                if (!(randomValue > (100 - MainConfig.getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendMessage(Component.nullToEmpty("The sun is starting to set and you feel uneasy about the coming night...").copy().withStyle(ChatFormatting.RED), player.getUUID());
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && normalizedTimeOfDay >= 12542) {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    BlockPos pos = player.blockPosition();
                    boolean foundHordeSpawnLocation = false;
                    int currentHordeCounter = UndeadNights.globalSpawnCounter;

                    for (int i= 0; i < 20; i++){
                        // for the given min/max distance, calculate the x and z coordinates deltas
                        if (d == 0) {
                            d = randomSource.nextInt(MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                            x = randomSource.nextInt(0, (int) d);
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
                        pos = new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ());
                        foundHordeSpawnLocation = checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ());
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
                        break;
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
                            for (var mobSpawnData : HordeConfig.getHordeMobs()) {
                                if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                                    randomValue = randomSource.nextInt(1, 100);
                                    spawnHordeMob(level, randomSource, pos, player, (randomValue > (100 - mobSpawnData.chance())) ? mobSpawnData : HordeConfig.getDefaultHordeMob());
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
                        }
                    }

                    /*
                     * Horde config variant 2
                     */
                    if (HordeConfig.getConfigVariant() == 2) {
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Horde config variant 2 detected.");
                        }
                        for (var mobSpawnData : HordeConfig.getHordeMobs()) {
                            int mobCount = 0;
                            if (mobSpawnData.countMin() >= mobSpawnData.countMax()) {
                                mobCount = mobSpawnData.countMin();
                            } else {
                                mobCount = randomSource.nextInt(mobSpawnData.countMin(),mobSpawnData.countMax());
                            }
                            for (int i = 0; i < mobCount; i++) {
                                spawnHordeMob(level, randomSource, pos, player, mobSpawnData);
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
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM.get(), SoundSource.HOSTILE, 4.0F, 1);
                        player.sendMessage(Component.nullToEmpty("A horde has spawned!").copy().withStyle(ChatFormatting.RED), player.getUUID());
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("A Horde has spanned!");
                        }
                    }

                    d = 0;
                    if (spawnCapReached) {
                        UndeadNights.LOGGER.info("Spawncap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
                        break;
                    }
                } // for loop player

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
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    player.sendMessage(Component.nullToEmpty("You feel at ease, this Night of the Undead is over..."), player.getUUID());
                }
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                if (MainConfig.getPrintDebugMessages()) {
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
