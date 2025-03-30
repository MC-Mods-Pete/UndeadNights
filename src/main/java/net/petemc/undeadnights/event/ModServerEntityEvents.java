package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

public class ModServerEntityEvents {

    private static Entity pEntity;
    private static ServerWorld pWorld;

    public ModServerEntityEvents() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            executeLoadEntity();
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            executeUnloadEntity();
        });
    }

    public static void executeLoadEntity() {
        if (UndeadNights.serverState != null) {
            if (UndeadNights.serverState.spawnedHordeMobs.containsKey(pEntity.getUuid())) {
                if (UndeadNights.serverState.hordeMobsToRemove.containsKey(pEntity.getUuid())) {
                    UndeadNights.serverState.hordeMobsToRemove.remove(pEntity.getUuid());
                    pEntity.remove(Entity.RemovalReason.DISCARDED);
                    UndeadNights.LOGGER.info("LOAD canceled, Entity marked for removal: {}", pEntity.getUuid());
                } else {
                    UndeadNights.globalSpawnCounter++;
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("LOAD GlobalSpawnCount  : {} {}", UndeadNights.globalSpawnCounter, pEntity.getUuid());
                    }
                }
            }
        }
    }

    public static void executeUnloadEntity() {
        if (UndeadNights.serverState != null) {
            if (UndeadNights.serverState.spawnedHordeMobs.containsKey(pEntity.getUuid())) {
                if (pEntity.getRemovalReason() != null) {
                    if ((pEntity.getRemovalReason() == Entity.RemovalReason.KILLED) || (pEntity.getRemovalReason() == Entity.RemovalReason.DISCARDED)) {
                        UndeadNights.globalSpawnCounter--;
                        UndeadNights.serverState.spawnedHordeMobs.remove(pEntity.getUuid());
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, pEntity.getRemovalReason().name(), pEntity.getUuid());
                        }
                    }
                }
            }
        }
    }

    public static void registerEvents() { new ModServerEntityEvents(); }
}
