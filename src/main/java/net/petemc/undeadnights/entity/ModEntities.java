package net.petemc.undeadnights.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.petemc.undeadnights.UndeadNights;

public class ModEntities {
    private static final Identifier idHordeZombie = Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "horde_zombie");
    private static final ResourceKey<EntityType<?>> keyHordeZombie = ResourceKey.create(Registries.ENTITY_TYPE, idHordeZombie);
    public static final EntityType<HordeZombieEntity> HORDE_ZOMBIE = Registry.register(BuiltInRegistries.ENTITY_TYPE, keyHordeZombie,
            FabricEntityType.Builder.createMob(HordeZombieEntity::new, MobCategory.MONSTER,
                    mob -> mob.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            Monster::checkMonsterSpawnRules))
                    .sized(0.6f, 1.95f)
                    .build(keyHordeZombie));

    private static final Identifier idDemolitionZombie = Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie");
    private static final ResourceKey<EntityType<?>> keyDemolitionZombie = ResourceKey.create(Registries.ENTITY_TYPE, idDemolitionZombie);
    public static final EntityType<DemolitionZombieEntity> DEMOLITION_ZOMBIE = Registry.register(BuiltInRegistries.ENTITY_TYPE, keyDemolitionZombie,
            FabricEntityType.Builder.createMob(DemolitionZombieEntity::new, MobCategory.MONSTER,
                            mob -> mob.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                    Monster::checkMonsterSpawnRules))
                    .sized(0.6f, 1.95f)
                    .build(keyDemolitionZombie));

    private static final Identifier idEliteZombie = Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie");
    private static final ResourceKey<EntityType<?>> keyEliteZombie = ResourceKey.create(Registries.ENTITY_TYPE, idEliteZombie);
    public static final EntityType<EliteZombieEntity> ELITE_ZOMBIE = Registry.register(BuiltInRegistries.ENTITY_TYPE, keyEliteZombie,
            FabricEntityType.Builder.createMob(EliteZombieEntity::new, MobCategory.MONSTER,
                            mob -> mob.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                    Monster::checkMonsterSpawnRules))
                    .sized(0.6f, 1.95f)
                    .build(keyEliteZombie));

    private static final Identifier idTntProjectile = Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "tnt_projectile");
    private static final ResourceKey<EntityType<?>> keyTntProjectile = ResourceKey.create(Registries.ENTITY_TYPE, idTntProjectile);
    public static final EntityType<DemolitionZombieProjectileEntity> TNT_PROJECTILE = Registry.register(BuiltInRegistries.ENTITY_TYPE, keyTntProjectile,
            EntityType.Builder.<DemolitionZombieProjectileEntity>of(DemolitionZombieProjectileEntity::new, MobCategory.MISC)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .sized(0.5f, 0.5f)
                    .noSummon()
                    .build(keyTntProjectile));

    public static void initModEntities() {
        HordeZombieEntity.initSpawnCondition();
        EliteZombieEntity.initSpawnConditions();
        DemolitionZombieEntity.initSpawnConditions();
    }
}
