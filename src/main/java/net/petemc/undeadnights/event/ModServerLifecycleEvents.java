package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

public class ModServerLifecycleEvents {

    private static MinecraftServer pServer;

    public ModServerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            pServer = server;
            executeServerStarted();
        });
    }

    public static void executeServerStarted() {
        if (UndeadNights.serverState == null) {
            UndeadNights.serverState = pServer.getOverworld().getPersistentStateManager().getOrCreate(StateSaverAndLoader.createStateType());
            // check if the DaysCounter in the config was changed
            if (UndeadNights.serverState.getLastMaxDaysCounter() != MainConfig.getDaysBetweenHordeNights()) {
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                UndeadNights.serverState.setLastMaxDaysCounter(MainConfig.getDaysBetweenHordeNights());
            }

            // check if the Grace Period in the config was changed
            if (UndeadNights.serverState.getLastMaxGracePeriod() != MainConfig.getGracePeriodBeforeFirstHordeNight()) {
                UndeadNights.serverState.setGracePeriod(MainConfig.getGracePeriodBeforeFirstHordeNight());
                UndeadNights.serverState.setLastMaxGracePeriod(MainConfig.getGracePeriodBeforeFirstHordeNight());
            }

            // check if the maximum number of hordes per night in the config was changed
            if (UndeadNights.serverState.getLastMaxHordesCounter() != MainConfig.getMaxHordesPerHordeNight()) {
            if (MainConfig.getMaxHordesPerHordeNight() != 0) {
                    UndeadNights.serverState.setHordesCounter(MainConfig.getMaxHordesPerHordeNight() + 1);
                } else {
                    UndeadNights.serverState.setHordesCounter(0);
                }
                UndeadNights.serverState.setLastMaxHordesCounter(MainConfig.getMaxHordesPerHordeNight());
            }

            if (!MainConfig.getNoNaturalSpawningBeforeFirstHordeNight()) {
                UndeadNights.serverState.setIsNaturalSpawningOk(true);
            }

            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
                UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
            }
            UndeadSpawner.hordeToSpawn = HordeConfig.getDefaultHorde();
            //UndeadSpawner.prevNormalizedTimeOfDay = pServer.getOverworld().getTimeOfDay() - 1;
            HordeMobsCommand.hordeZombiesCanBreakBlocks = MainConfig.getHordeZombiesCanBreakBlocks();
            HordeMobsCommand.hordeZombiesBlockBreakingTier = MainConfig.getZombiesBlockBreakTier();
        }
    }

    public static void registerEvents() { new ModServerLifecycleEvents(); }
}
