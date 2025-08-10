package net.petemc.undeadnights.entity;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.undeadnights.UndeadNights;

public class RegisterEntityAttributes {
    @Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.HORDE_ZOMBIE.get(), HordeZombieEntity.createAttributes().build());
            event.put(ModEntities.DEMOLITION_ZOMBIE.get(), DemolitionZombieEntity.createAttributes().build());
            event.put(ModEntities.ELITE_ZOMBIE.get(), EliteZombieEntity.createAttributes().build());
        }
    }

}


