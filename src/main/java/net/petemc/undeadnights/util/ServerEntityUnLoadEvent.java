package net.petemc.undeadnights.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.Config;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

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
        if (pEntity instanceof HordeZombieEntity || pEntity instanceof DemolitionZombieEntity
                || pEntity instanceof EliteZombieEntity) {

           UndeadNights.globalSpawnCounter--;
           if (Config.getPrintDebugMessages()) {
               if (pEntity.getRemovalReason() != null) {
                   UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {}", UndeadNights.globalSpawnCounter, pEntity.getRemovalReason().name());
               } else {
                   UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {}", UndeadNights.globalSpawnCounter);
               }
           }
       }
    }

    public static void registerEvent() { new ServerEntityUnLoadEvent(); }
}
