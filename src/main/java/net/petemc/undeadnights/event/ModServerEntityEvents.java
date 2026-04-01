package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;

public class ModServerEntityEvents {

    private static Entity pEntity;
    private static ServerLevel pServerLevel;

    public ModServerEntityEvents() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            pEntity = entity;
            pServerLevel = serverLevel;
            executeLoadEntity();
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, serverLevel) -> {
            pEntity = entity;
            pServerLevel = serverLevel;
            executeUnloadEntity();
        });
    }

    public static void executeLoadEntity() {
        if(!pServerLevel.isClientSide()) {
            if (UndeadNights.serverState != null) {
                if (UndeadNights.serverState.spawnedHordeMobs.containsKey(pEntity.getUUID())) {
                    if (UndeadNights.serverState.hordeMobsToRemove.containsKey(pEntity.getUUID())) {
                        UndeadNights.serverState.hordeMobsToRemove.remove(pEntity.getUUID());
                        pEntity.remove(Entity.RemovalReason.DISCARDED);
                        UndeadNights.LOGGER.info("LOAD canceled, Entity marked for removal: {}", pEntity.getUUID());
                    } else {
                        UndeadNights.globalSpawnCounter++;
                        if (pEntity instanceof Zombie zombie) {
                            if (!(zombie instanceof HordeZombieEntity) && !(zombie instanceof DemolitionZombieEntity) && !(zombie instanceof EliteZombieEntity)) {
                                if (MainConfig.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("Vanilla zombie detected, adding float and block breaking goals.");
                                }
                                zombie.goalSelector.addGoal(1, new FloatGoal(zombie));
                                zombie.goalSelector.addGoal(1, new BreakBlockGoal(zombie));
                            }
                            if (zombie instanceof EliteZombieEntity) {
                                UndeadNights.serverState.setFirstEliteZombieHasSpawned(true);
                            }
                            if (zombie instanceof DemolitionZombieEntity) {
                                UndeadNights.serverState.setFirstDemolitionZombieHasSpawned(true);
                            }
                        }
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("LOAD GlobalSpawnCount: : {} {} {}", UndeadNights.globalSpawnCounter, pEntity.getName().getString(), pEntity.getUUID());
                    }
                    //event.getEntity().kill();
                }
            }
        }
    }

    public static void executeUnloadEntity() {
        if (UndeadNights.serverState != null) {
            if (UndeadNights.serverState.spawnedHordeMobs.containsKey(pEntity.getUUID())) {
                if (pEntity.getRemovalReason() != null) {
                    if ((pEntity.getRemovalReason() == Entity.RemovalReason.KILLED) || (pEntity.getRemovalReason() == Entity.RemovalReason.DISCARDED)) {
                        UndeadNights.globalSpawnCounter--;
                        UndeadNights.serverState.spawnedHordeMobs.remove(pEntity.getUUID());
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, pEntity.getRemovalReason().name(), pEntity.getUUID());
                        }
                    }
                }
            }
        }
    }

    public static void registerEvents() { new ModServerEntityEvents(); }
}
