package net.petemc.undeadnights.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.undeadnights.UndeadNights;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UndeadNights.MOD_ID);

    public static final RegistryObject<EntityType<HordeZombieEntity>> HORDE_ZOMBIE =
            ENTITY_TYPES.register("horde_zombie", () -> EntityType.Builder.of(HordeZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build("horde_zombie"));
    public static final RegistryObject<EntityType<DemolitionZombieEntity>> DEMOLITION_ZOMBIE =
            ENTITY_TYPES.register("demolition_zombie", () -> EntityType.Builder.of(DemolitionZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build("demolition_zombie"));
    public static final RegistryObject<EntityType<EliteZombieEntity>> ELITE_ZOMBIE =
            ENTITY_TYPES.register("elite_zombie", () -> EntityType.Builder.of(EliteZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build("elite_zombie"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
