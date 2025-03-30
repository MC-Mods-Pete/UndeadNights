package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.util.StateSaverAndLoader;

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

            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
                UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
            }
        }
    }

    public static void registerEvents() { new ModServerLifecycleEvents(); }
}
