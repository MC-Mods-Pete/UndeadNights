package net.petemc.undeadnights.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

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
       if (pEntity instanceof HordeZombieEntity || pEntity instanceof DemolitionZombieEntity) {
           UndeadNights.globalSpawnCounter--;
           //if (UndeadSpawner.hordeSpawnCounter > 0) {
           //    UndeadSpawner.hordeSpawnCounter--;
           //}
           UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {}", UndeadNights.globalSpawnCounter);
       }
    }

    public static void registerEvent() { new ServerEntityUnLoadEvent(); }
}
