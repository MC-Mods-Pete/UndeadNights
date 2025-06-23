package net.petemc.undeadnights.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.petemc.undeadnights.UndeadNights;

public class RegisterEntityAttributes {
    @EventBusSubscriber(modid = UndeadNights.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.HORDE_ZOMBIE.get(), HordeZombieEntity.createAttributes().build());
            event.put(ModEntities.DEMOLITION_ZOMBIE.get(), DemolitionZombieEntity.createAttributes().build());
            event.put(ModEntities.ELITE_ZOMBIE.get(), EliteZombieEntity.createAttributes().build());
        }
    }

}
