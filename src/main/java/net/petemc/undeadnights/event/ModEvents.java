package net.petemc.undeadnights.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.command.StatusCommand;
import net.petemc.undeadnights.config.MainConfig;

public class ModEvents {
    @EventBusSubscriber(modid = UndeadNights.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if (UndeadNights.serverState != null) {
                    if (UndeadNights.serverState.spawnedHordeMobs.containsKey(event.getEntity().getUUID())) {
                        UndeadNights.globalSpawnCounter++;
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("LOAD GlobalSpawnCount: : {} {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getName().getString(), event.getEntity().getUUID());
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
                                if (MainConfig.getPrintDebugMessages()) {
                                    if (event.getEntity().getRemovalReason() != null) {
                                        UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getRemovalReason().name(), event.getEntity().getUUID());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerTrySleep(CanPlayerSleepEvent event) {
            if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()) {
                event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
            }
        }

        @SubscribeEvent
        public static void onCommandsRegister(RegisterCommandsEvent event) {
            new SpawnHordeCommand(event.getDispatcher());
            new HordeMobsCommand(event.getDispatcher());
            new StatusCommand(event.getDispatcher());

            ConfigCommand.register(event.getDispatcher());
        }
    }
}


