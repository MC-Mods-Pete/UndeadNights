package net.petemc.undeadnights.entity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.petemc.undeadnights.config.UndeadNightsConfig;
import net.petemc.undeadnights.entity.ai.goal.DemolitionZombieIgniteGoal;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.EnumSet;

public class DemolitionZombieEntity extends ZombieEntity {

    public DemolitionZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createHordeZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)       // default 20.0
                .add(EntityAttributes.FOLLOW_RANGE, 128.0)    // default 35.0
                .add(EntityAttributes.MOVEMENT_SPEED, 0.30f)  // default 0.23000000417232513
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0)     // default 3.0
                .add(EntityAttributes.ARMOR, 3.0)             // default 2.0
                .add(EntityAttributes.SPAWN_REINFORCEMENTS);
    }

    @Override
    protected void initCustomGoals() {
        this.goalSelector.add(1, new DemolitionZombieIgniteGoal(this));
        this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.add(3, new DemolitionZombieEntity.ChasePlayerGoal(this));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        this.setLeftHanded(random.nextFloat() < 0.05F);
        float f = difficulty.getClampedLocalDifficulty();
        this.setCanPickUpLoot(random.nextFloat() < 0.55F * f);
        if (entityData == null) {
            entityData = new ZombieEntity.ZombieData(false, false);
        }

        if (entityData instanceof ZombieData) {
            this.setCanBreakDoors(this.canBreakDoors() && random.nextFloat() < f * 0.1F);
            this.initEquipment(random, difficulty);
            this.updateEnchantments(world, random, difficulty);
        }

        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localDate = LocalDate.now();
            int i = localDate.get(ChronoField.DAY_OF_MONTH);
            int j = localDate.get(ChronoField.MONTH_OF_YEAR);
            if (j == 10 && i == 31 && random.nextFloat() < 0.25F) {
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0F;
            }
        }
        return entityData;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isOnFire()) {
            //World world = this.getEntityWorld();
            BlockPos pos = this.getBlockPos();
            this.remove(RemovalReason.KILLED);
            world.createExplosion(this, pos.getX(), pos.getY(), pos.getZ(), 5, true, World.ExplosionSourceType.TNT);
            return true;
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        dropInventory(world);
    }

    public void dropInventory(ServerWorld world) {
        super.dropInventory(world);
            if (!this.getMainHandStack().isEmpty()) {
                this.dropStack(world, this.getMainHandStack());
                this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (UndeadNightsConfig.INSTANCE.demolitionZombieTntStackSize == 0) {
            this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.TNT));
        }
        if ((UndeadNightsConfig.INSTANCE.demolitionZombieTntStackSize > 0) && (UndeadNightsConfig.INSTANCE.demolitionZombieTntStackSize <= 64)){
            this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.TNT, UndeadNightsConfig.INSTANCE.demolitionZombieTntStackSize));
        }
        initCustomEquipment(random, localDifficulty);
    }

    protected void initCustomEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.15F * localDifficulty.getClampedLocalDifficulty()) {
            int i = random.nextInt(2);
            float f = this.getWorld().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
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
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    public boolean canBreakDoors()
    {
        return true;
    }

    static class ChasePlayerGoal extends Goal {
        private final DemolitionZombieEntity demolitionZombie;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(DemolitionZombieEntity demolitionZombie) {
            this.demolitionZombie = demolitionZombie;
            this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
        }

        @Override
        public boolean canStart() {
            this.target = this.demolitionZombie.getTarget();
            return this.target instanceof PlayerEntity;
        }

        @Override
        public void start() {
            this.demolitionZombie.getNavigation().stop();
        }

        @Override
        public void tick() {
            assert this.target != null;
            this.demolitionZombie.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }
}
