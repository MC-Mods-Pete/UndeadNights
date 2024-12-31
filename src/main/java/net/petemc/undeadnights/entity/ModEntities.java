package net.petemc.undeadnights.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;

public class ModEntities {
    private static final Identifier idHordeZombie = Identifier.of(UndeadNights.MOD_ID, "horde_zombie");
    private static final RegistryKey<EntityType<?>> keyHordeZombie = RegistryKey.of(RegistryKeys.ENTITY_TYPE, idHordeZombie);
    public static final EntityType<HordeZombieEntity> HORDE_ZOMBIE = Registry.register(Registries.ENTITY_TYPE, keyHordeZombie,
            EntityType.Builder.create(HordeZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).build(keyHordeZombie));

    private static final Identifier idDemolitionZombie = Identifier.of(UndeadNights.MOD_ID, "demolition_zombie");
    private static final RegistryKey<EntityType<?>> keyDemolitionZombie = RegistryKey.of(RegistryKeys.ENTITY_TYPE, idDemolitionZombie);
    public static final EntityType<DemolitionZombieEntity> DEMOLITION_ZOMBIE = Registry.register(Registries.ENTITY_TYPE, keyDemolitionZombie,
            EntityType.Builder.create(DemolitionZombieEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).build(keyHordeZombie));
}
