package net.petemc.undeadnights.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.Config;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

public class ServerEntityLoadEvent {

    private static Entity pEntity;
    private static ServerWorld pWorld;

    public ServerEntityLoadEvent() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            execute();
        });
    }

    public static void execute() {
       if (pEntity instanceof HordeZombieEntity || pEntity instanceof DemolitionZombieEntity
           || pEntity instanceof EliteZombieEntity) {

           UndeadNights.globalSpawnCounter++;
           if (Config.getPrintDebugMessages()) {
               UndeadNights.LOGGER.info("LOAD GlobalSpawnCount  : {}", UndeadNights.globalSpawnCounter);
           }
           //pEntity.remove(Entity.RemovalReason.KILLED);
       }
    }

    public static void registerEvent() { new ServerEntityLoadEvent(); }
}
