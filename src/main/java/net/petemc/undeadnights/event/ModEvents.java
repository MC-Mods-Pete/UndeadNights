package net.petemc.undeadnights.event;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.undeadnights.Config;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
            if(!event.getWorld().isClientSide()) {
                if (event.getEntity() instanceof HordeZombieEntity || event.getEntity() instanceof DemolitionZombieEntity
                        || event.getEntity() instanceof EliteZombieEntity) {

                    UndeadNights.globalSpawnCounter++;
                    if (Config.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("LOAD GlobalSpawnCount  : {}", UndeadNights.globalSpawnCounter);
                    }
                    //event.getEntity().kill();
                }
            }
        }

        @SubscribeEvent
        public static void onEntityLeaveWorld(EntityLeaveWorldEvent event) {
            if(!event.getWorld().isClientSide()) {
                if (event.getEntity() instanceof HordeZombieEntity || event.getEntity() instanceof DemolitionZombieEntity
                        || event.getEntity() instanceof EliteZombieEntity) {

                    UndeadNights.globalSpawnCounter--;
                    if (Config.getPrintDebugMessages()) {
                        if (event.getEntity().getRemovalReason() != null) {
                            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getRemovalReason().name());
                        } else {
                            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {}", UndeadNights.globalSpawnCounter);
                        }
                    }
                }
            }
        }
    }
}


