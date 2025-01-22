package net.petemc.undeadnights.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.undeadnights.UndeadNights;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, UndeadNights.MOD_ID);

    public static final Supplier<EntityType<HordeZombieEntity>> HORDE_ZOMBIE =
            ENTITY_TYPES.register("horde_zombie", () -> EntityType.Builder.of(HordeZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "horde_zombie"))));
    public static final Supplier<EntityType<DemolitionZombieEntity>> DEMOLITION_ZOMBIE =
            ENTITY_TYPES.register("demolition_zombie", () -> EntityType.Builder.of(DemolitionZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID,"demolition_zombie"))));
    public static final Supplier<EntityType<EliteZombieEntity>> ELITE_ZOMBIE =
            ENTITY_TYPES.register("elite_zombie", () -> EntityType.Builder.of(EliteZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID,"elite_zombie"))));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
