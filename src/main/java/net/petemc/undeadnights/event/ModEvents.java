package net.petemc.undeadnights.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
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
        public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
            if(!event.getLevel().isClientSide()) {
                if (event.getEntity() instanceof HordeZombieEntity || event.getEntity() instanceof DemolitionZombieEntity
                        || event.getEntity() instanceof EliteZombieEntity) {

                    UndeadNights.globalSpawnCounter++;
                    if (Config.printDebugMessages) {
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
                    if (Config.printDebugMessages) {
                        UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {}", UndeadNights.globalSpawnCounter);
                    }
                }
            }
        }
    }
}


