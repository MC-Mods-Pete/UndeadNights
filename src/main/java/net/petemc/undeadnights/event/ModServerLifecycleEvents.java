package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import net.petemc.undeadnights.world.spawner.HordeSpawner;

public class ModServerLifecycleEvents {

    private static MinecraftServer pServer;

    public ModServerLifecycleEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            pServer = server;
            executeServerStarted();
        });
    }

    public static void executeServerStarted() {
        UndeadNights.serverState = StateSaverAndLoader.getServerState(pServer);

        // TODO remove, only for testing
        //UndeadNights.serverState.setFirstDifficultyLevelPrinted(false);
        //UndeadNights.serverState.setCurrentDifficultyLevelIndex(0);

        if (UndeadNights.serverState.getCurrentDifficultyLevelIndex() >= UndeadNights.difficultyConfig.getDifficultyLevels().size()) {
            UndeadNights.LOGGER.warn("Current difficulty level index in server state is out of bounds, setting to max index");
            UndeadNights.serverState.setCurrentDifficultyLevelIndex(UndeadNights.difficultyConfig.getDifficultyLevels().size() - 1);
        }
        UndeadNights.difficultyConfig.setCurrentDifficultyLevel(UndeadNights.difficultyConfig.getDifficultyLevels().get(UndeadNights.serverState.getCurrentDifficultyLevelIndex()));
        UndeadNights.automaticDifficultyProgressionActive = MainConfig.getEnableAutomaticDifficultyProgression();

        // check if the max DayScaleCounter was changed in the config
        if (UndeadNights.serverState.getLastMaxDayScaleCounter() != UndeadNights.difficultyConfig.getDynamicScaling().getDaysBetweenScaleIncreases().intValue()) {
            UndeadNights.LOGGER.info("Day scale counter max value changed in config, resetting current day scale counter to 0");
            UndeadNights.serverState.setCurrentDayScaleCounter(0);
            UndeadNights.serverState.setLastMaxDayScaleCounter(UndeadNights.difficultyConfig.getDynamicScaling().getDaysBetweenScaleIncreases().intValue());
        }

        // check if the DaysCounter in the config was changed
        if (UndeadNights.serverState.getLastMaxDaysCounter() != UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights()) {
            UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
            UndeadNights.serverState.setLastMaxDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
        }

        // check if the Grace Period in the config was changed
        if (UndeadNights.serverState.getLastMaxGracePeriod() != MainConfig.getGracePeriodBeforeFirstHordeNight()) {
            UndeadNights.serverState.setGracePeriod(MainConfig.getGracePeriodBeforeFirstHordeNight());
            UndeadNights.serverState.setLastMaxGracePeriod(MainConfig.getGracePeriodBeforeFirstHordeNight());
        }

        // check if the maximum number of hordes per night in the config was changed
        if (UndeadNights.serverState.getLastMaxHordesCounter() != UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight()) {
            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() != 0) {
                UndeadNights.serverState.setHordesCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() + 1);
            } else {
                UndeadNights.serverState.setHordesCounter(0);
            }
            UndeadNights.serverState.setLastMaxHordesCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight());
        }

        if (!MainConfig.getNoNaturalSpawningBeforeFirstHordeNight()) {
            UndeadNights.serverState.setIsNaturalSpawningOk(true);
        }

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
            UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
            UndeadNights.LOGGER.info("INIT Difficulty level: {}", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName());
        }
        HordeSpawner.hordeIdFromHordesConfig = HordeConfig.getDefaultHorde();
        //UndeadSpawner.prevNormalizedTimeOfDay = event.getServer().overworld().getDayTime() - 1;
        HordeMobsCommand.hordeZombiesCanBreakBlocks = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isBlockBreaking();
        HordeMobsCommand.hordeZombiesBlockBreakingTier = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getBlockBreakingTier();
    }

    public static void registerEvents() { new ModServerLifecycleEvents(); }
}
