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
            .dimensions(0.6f, 1.95f)
            .build(Identifier.of(UndeadNights.MOD_ID, "horde_zombie").toString()));
    public static final EntityType<DemolitionZombieEntity> DEMOLITION_ZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(UndeadNights.MOD_ID, "demolition_zombie"),
            EntityType.Builder.create(DemolitionZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(Identifier.of(UndeadNights.MOD_ID, "demolition_zombie").toString()));
    public static final EntityType<EliteZombieEntity> ELITE_ZOMBIE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(UndeadNights.MOD_ID, "elite_zombie"),
            EntityType.Builder.create(EliteZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(Identifier.of(UndeadNights.MOD_ID, "elite_zombie").toString()));
    public static final EntityType<DemolitionZombieProjectileEntity> TNT_PROJECTILE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(UndeadNights.MOD_ID, "tnt_projectile"),
            EntityType.Builder.<DemolitionZombieProjectileEntity>create(DemolitionZombieProjectileEntity::new, SpawnGroup.MISC)
            .maxTrackingRange(64)
            .trackingTickInterval(1)
            .dimensions(0.5f, 0.5f)
            .disableSummon()
            .build(Identifier.of(UndeadNights.MOD_ID, "tnt_projectile").toString()));

    public static void initModEntities() {
        HordeZombieEntity.initSpawnCondition();
        EliteZombieEntity.initSpawnConditions();
        DemolitionZombieEntity.initSpawnConditions();
    }
}
