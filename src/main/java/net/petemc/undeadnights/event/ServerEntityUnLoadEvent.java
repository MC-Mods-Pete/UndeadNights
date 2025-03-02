package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.petemc.undeadnights.UndeadNights;

public class ServerEntityUnLoadEvent {

    private static Entity pEntity;
    private static ServerWorld pWorld;

    public ServerEntityUnLoadEvent() {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            execute();
        });
    }

    public static void execute() {
        if (UndeadNights.serverState != null) {
            if (UndeadNights.serverState.spawnedHordeMobs.contains(pEntity.getUuid())) {
                if (pEntity.getRemovalReason() != null) {
                    if ((pEntity.getRemovalReason() == Entity.RemovalReason.KILLED) || (pEntity.getRemovalReason() == Entity.RemovalReason.DISCARDED)) {
                        UndeadNights.globalSpawnCounter--;
                        UndeadNights.serverState.spawnedHordeMobs.remove(pEntity.getUuid());
                        if (pEntity.getRemovalReason() != null) {
                            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, pEntity.getRemovalReason().name(), pEntity.getUuid());
                        } else {
                            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {}", UndeadNights.globalSpawnCounter, pEntity.getUuid());
                        }
                    }
                }
            }
        }
    }

    public static void registerEvent() { new ServerEntityUnLoadEvent(); }
}
