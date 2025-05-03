package net.petemc.undeadnights.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.command.SetDefaultHordeCommand;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.command.StatusCommand;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void denyReinforcementsForHordeZombies(ZombieEvent.SummonAidEvent event) {
            if ((event.getEntity() instanceof DemolitionZombieEntity) ||
                    (event.getEntity() instanceof HordeZombieEntity) ||
                    (event.getEntity() instanceof EliteZombieEntity)) {
                //if (MainConfig.getPrintDebugMessages()) {
                //    UndeadNights.LOGGER.info("Reinforcement denied for {}", event.getEntity().getName().getString());
                //}
                event.setResult(Event.Result.DENY);
            }
        }

        @SubscribeEvent
        public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if (UndeadNights.serverState != null) {
                    if (UndeadNights.serverState.spawnedHordeMobs.containsKey(event.getEntity().getUUID())) {
                        if (UndeadNights.serverState.hordeMobsToRemove.containsKey(event.getEntity().getUUID())) {
                            UndeadNights.serverState.hordeMobsToRemove.remove(event.getEntity().getUUID());
                            event.setCanceled(true);
                            UndeadNights.LOGGER.info("LOAD canceled, Entity marked for removal: {}", event.getEntity().getUUID());
                        } else {
                            UndeadNights.globalSpawnCounter++;
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("LOAD GlobalSpawnCount  : {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getUUID());
                            }
                        }
                        //event.getEntity().kill();
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if (UndeadNights.serverState != null) {
                    if (UndeadNights.serverState.spawnedHordeMobs.containsKey(event.getEntity().getUUID())) {
                        if (event.getEntity().getRemovalReason() != null) {
                            if ((event.getEntity().getRemovalReason() == Entity.RemovalReason.KILLED) || (event.getEntity().getRemovalReason() == Entity.RemovalReason.DISCARDED)) {
                                UndeadNights.globalSpawnCounter--;
                                UndeadNights.serverState.spawnedHordeMobs.remove(event.getEntity().getUUID());
                                if (event.getEntity().getRemovalReason() != null) {
                                    UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getRemovalReason().name(), event.getEntity().getUUID());
                                }
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onCommandsRegister(RegisterCommandsEvent event) {
            new SpawnHordeCommand(event.getDispatcher());
            new HordeMobsCommand(event.getDispatcher());
            new StatusCommand(event.getDispatcher());
            new SetDefaultHordeCommand(event.getDispatcher());

            ConfigCommand.register(event.getDispatcher());
        }
    }
}


