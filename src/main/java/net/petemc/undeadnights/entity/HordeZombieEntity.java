package net.petemc.undeadnights.entity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.petemc.undeadnights.config.UndeadNightsConfig;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.EnumSet;


public class HordeZombieEntity extends ZombieEntity {
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
            entityData = new ZombieEntity.ZombieData(false, false);
        }

        if (entityData instanceof ZombieData) {
            this.setCanBreakDoors(this.shouldBreakDoors() && random.nextFloat() < f * 0.1F);
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

    public static DefaultAttributeContainer.Builder createHordeZombieAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)       // default 20.0
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 128.0)    // default 35.0
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30f)  // default 0.23000000417232513
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)     // default 3.0
                .add(EntityAttributes.GENERIC_ARMOR, 3.0)             // default 2.0
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
    }

    @Override
    protected void initCustomGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.add(3, new HordeZombieEntity.ChasePlayerGoal(this));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        //this.goalSelector.add(14, new ZombiePounceAtTargetGo(instance, config.pounceVelocity));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
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
        super.initEquipment(random, localDifficulty);
    }

    @Override
    protected boolean burnsInDaylight() {
        return UndeadNightsConfig.INSTANCE.zombiesBurnInDaylight;
    }

    @Override
    public boolean canBreakDoors()
    {
        return true;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        super.pushAwayFrom(entity);
        double y = 0.18F;
        if (y < 0.0) {
            y = -y;
        }
        double f = y;
        if (f >= 0.01F) {
            f = Math.sqrt(f);
            y /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }

            y *= g;
            y *= 0.05F;
            if (!this.hasPassengers() && this.isPushable()) {
                this.addVelocity(0, y, 0);
            }

            if (!entity.hasPassengers() && entity.isPushable()) {
                entity.addVelocity(0, y, 0);
            }
        }
    }

    static class ChasePlayerGoal extends Goal {
        private final HordeZombieEntity hordeZombie;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(HordeZombieEntity hordeZombie) {
            this.hordeZombie = hordeZombie;
            this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
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
