package net.petemc.undeadnights.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.petemc.undeadnights.Config;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

public class ModEvents {
    @EventBusSubscriber(modid = UndeadNights.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if (event.getEntity() instanceof HordeZombieEntity || event.getEntity() instanceof DemolitionZombieEntity
                        || event.getEntity() instanceof EliteZombieEntity) {

                    UndeadNights.globalSpawnCounter++;
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("LOAD GlobalSpawnCount: : {}", UndeadNights.globalSpawnCounter);
                    }
                    //event.getEntity().kill();
                }
            }
        }

        @SubscribeEvent
        public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if (event.getEntity() instanceof HordeZombieEntity || event.getEntity() instanceof DemolitionZombieEntity
                        || event.getEntity() instanceof EliteZombieEntity) {

                    UndeadNights.globalSpawnCounter--;
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {}", UndeadNights.globalSpawnCounter);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerTrySleep(CanPlayerSleepEvent event) {
            if (UndeadNights.serverState.getHordeNight() && Config.getHordeNightsDisableSleeping()) {
                event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
            }
        }
    }
}


