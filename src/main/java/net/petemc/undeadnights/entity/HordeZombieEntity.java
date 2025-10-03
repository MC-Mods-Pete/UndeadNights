package net.petemc.undeadnights.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeKeys;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;

public class HordeZombieEntity extends ZombieEntity {
    private static final TrackedData<Byte> DATA_FLAGS_ID = DataTracker.registerData(HordeZombieEntity.class, TrackedDataHandlerRegistry.BYTE);

    public HordeZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        this.setLeftHanded(random.nextFloat() < 0.05F);
        float f = difficulty.getClampedLocalDifficulty();
        this.setCanPickUpLoot(random.nextFloat() < 0.55F * f);
        if (entityData == null) {
            entityData = new ZombieData(false, false);
        }

        if (entityData instanceof ZombieData) {
            this.setCanBreakDoors(true);
            this.initEquipment(random, difficulty);
            this.updateEnchantments(world, random, difficulty);
        }

        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localDate = LocalDate.now();
            int i = localDate.getDayOfMonth();
            int j = localDate.getMonth().getValue();
            if (j == 10 && i == 31 && random.nextFloat() < 0.25F) {
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0F);
            }
        }
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).addPersistentModifier(new EntityAttributeModifier(Identifier.of(UndeadNights.MOD_ID, "horde_zombie_health_bonus"), MainConfig.getMaxHealthHordeZombies() - 20.0F, EntityAttributeModifier.Operation.ADD_VALUE));

        this.applyAttributeModifiers(f);
        this.setHealth(this.getMaxHealth());
        this.setBaby(false);
        return entityData;
    }

    public static DefaultAttributeContainer.Builder createHordeZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.FOLLOW_RANGE, 128.0)    // default 35.0
                .add(EntityAttributes.MOVEMENT_SPEED, 0.30)   // default 0.23000000417232513
                .add(EntityAttributes.ATTACK_DAMAGE, 5.0)     // default 3.0
                .add(EntityAttributes.ARMOR, 4.0)             // default 2.0
                .add(EntityAttributes.SPAWN_REINFORCEMENTS, 0.0);
    }

    @Override
    protected void initCustomGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new BreakBlockGoal(this));
        this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.add(4, new ChasePlayerGoal(this));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[]{HordeZombieEntity.class, EliteZombieEntity.class, DemolitionZombieEntity.class}).setGroupRevenge(HordeZombieEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        initCustomEquipment(random, localDifficulty);
        if (random.nextFloat() < (this.getEntityWorld().getDifficulty() == Difficulty.HARD ? 0.07F : 0.03F)) {
            int i = random.nextInt(3);
            if (i == 0) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            if (i == 1) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            }
        }
    }

    protected void initCustomEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.2F * localDifficulty.getClampedLocalDifficulty()) {
            int i = random.nextInt(2);
            float f = this.getEntityWorld().getDifficulty() == Difficulty.HARD ? 0.2F : 0.45F;
            if (random.nextFloat() < 0.095F) {
                i++;
            }

            if (random.nextFloat() < 0.095F) {
                i++;
            }

            if (random.nextFloat() < 0.095F) {
                i++;
            }

            boolean bl = true;

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack itemStack = this.getEquippedStack(equipmentSlot);
                    if (!bl && random.nextFloat() < f) {
                        break;
                    }

                    bl = false;
                    if (itemStack.isEmpty()) {
                        Item item = getEquipmentForSlot(equipmentSlot, i);
                        if (item != null) {
                            this.equipStack(equipmentSlot, new ItemStack(item));
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean burnsInDaylight() {
        return MainConfig.getHordeZombiesBurnInDaylight();
    }

    @Override
    public boolean canBreakDoors()
    {
        return false;
    }

    @Override
    public void setCanBreakDoors(boolean val) {
    }

    @Override
    protected float getBaseWaterMovementSpeedMultiplier() {
        return MainConfig.getHordeZombiesHaveIncreasedWaterMovementSpeed() ? 0.94F : 0.8F;
    }

    public boolean isBreakingBlock() {
        return (this.dataTracker.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setBreakingBlock(boolean isBreaking) {
        byte b0 = this.dataTracker.get(DATA_FLAGS_ID);
        if (isBreaking) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.dataTracker.set(DATA_FLAGS_ID, b0);
    }

    @Override
    protected void initAttributes() {
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS)).setBaseValue(0.0F);
    }

    public static void init() {
        SpawnRestriction.register(ModEntities.HORDE_ZOMBIE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (entityType, world, reason, pos, random) ->
                        MainConfig.getDemolitionZombiesSpawnNaturally()
                                && UndeadNights.serverState.getIsNaturalSpawningOk()
                                && !(world.getBiome(pos).matchesKey(BiomeKeys.MUSHROOM_FIELDS))
                                && world.getDifficulty() != Difficulty.PEACEFUL
                                && HostileEntity.isSpawnDark(world, pos, random)
                                && HostileEntity.canMobSpawn(entityType, world, reason, pos, random));

        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                SpawnGroup.MONSTER, ModEntities.HORDE_ZOMBIE, 20, 2, 3);
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        super.pushAwayFrom(entity);
    }

    static class ChasePlayerGoal extends Goal {
        private final HordeZombieEntity hordeZombie;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(HordeZombieEntity hordeZombie) {
            this.hordeZombie = hordeZombie;
            this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
        }

        @Override
        public boolean canStart() {
            this.target = this.hordeZombie.getTarget();
            return this.target instanceof PlayerEntity;
        }

        @Override
        public void start() {
            this.hordeZombie.getNavigation().stop();
        }

        @Override
        public void tick() {
            assert this.target != null;
            this.hordeZombie.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }
}
