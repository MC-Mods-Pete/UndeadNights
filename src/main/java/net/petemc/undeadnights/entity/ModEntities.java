package net.petemc.undeadnights.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;

public class ModEntities {
    public static final EntityType<HordeZombieEntity> HORDE_ZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(UndeadNights.MOD_ID, "horde_zombie"),
            EntityType.Builder.create(HordeZombieEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 1.95f).build(Identifier.of(UndeadNights.MOD_ID, "horde_zombie").toString()));
    public static final EntityType<DemolitionZombieEntity> DEMOLITION_ZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(UndeadNights.MOD_ID, "demolition_zombie"),
            EntityType.Builder.create(DemolitionZombieEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 1.95f).build(Identifier.of(UndeadNights.MOD_ID, "demolition_zombie").toString()));
}
