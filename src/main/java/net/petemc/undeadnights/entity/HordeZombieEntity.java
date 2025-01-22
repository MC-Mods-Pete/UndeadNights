package net.petemc.undeadnights.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.petemc.undeadnights.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;


public class HordeZombieEntity extends Zombie {
    public HordeZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomsource = level.getRandom();
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        float f = difficulty.getSpecialMultiplier();
        this.setCanPickUpLoot(randomsource.nextFloat() < 0.55F * f);
        if (spawnGroupData == null) {
            spawnGroupData = new Zombie.ZombieGroupData(false, false);
        }

        if (spawnGroupData instanceof Zombie.ZombieGroupData) {
            this.setCanBreakDoors(true);
            this.populateDefaultEquipmentSlots(randomsource, difficulty);
            this.populateDefaultEquipmentEnchantments(level, randomsource, difficulty);
        }

        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localdate = LocalDate.now();
            int i = localdate.getDayOfMonth();
            int j = localdate.getMonth().getValue();
            if (j == 10 && i == 31 && randomsource.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomsource.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        this.handleAttributes(f);
        this.setBaby(false);
        return spawnGroupData;
    }

        public static AttributeSupplier.@NotNull Builder createAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 40.0F)          // default 20.F
                    .add(Attributes.FOLLOW_RANGE, 128.0D)       // default 35.0D
                    .add(Attributes.MOVEMENT_SPEED, (double) 0.30F)    // default 0.23F
                    .add(Attributes.ATTACK_DAMAGE, 5.0D)        // default 3.0
                    .add(Attributes.ARMOR, 4.0D)                // default 2.0
                    .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0f);
        }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        //this.goalSelector.addGoal(3, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new HordeZombieEntity.ChasePlayerGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(HordeZombieEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, WanderingTrader.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected boolean isSunSensitive() {
        return Config.getZombiesBurnInDaylight();
    }

    @Override
    public boolean canBreakDoors()
    {
        return true;
    }

    @Override
    public void randomizeReinforcementsChance() {
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue((double)0.0F);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource pRandom, @NotNull DifficultyInstance pDifficulty) {
        initCustomEquipment(pRandom, pDifficulty);
        if (pRandom.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.07F : 0.03F)) {
            int i = random.nextInt(3);
            if (i == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            if (i == 1) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            }
        }
    }

    protected void initCustomEquipment(RandomSource random, DifficultyInstance localDifficulty) {
        if (random.nextFloat() < 0.2F * localDifficulty.getSpecialMultiplier()) {
            int i = random.nextInt(2);
            float f = this.level().getDifficulty() == Difficulty.HARD ? 0.2F : 0.45F;
            if (random.nextFloat() < 0.095F) {
                i++;
            }

            if (random.nextFloat() < 0.095F) {
                i++;
            }

            if (random.nextFloat() < 0.095F) {
                i++;
            }

            boolean flag = true;

            for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack itemstack = this.getItemBySlot(equipmentslot);
                    if (!flag && random.nextFloat() < f) {
                        break;
                    }

                    flag = false;
                    if (itemstack.isEmpty()) {
                        Item item = getEquipmentForSlot(equipmentslot, i);
                        if (item != null) {
                            this.setItemSlot(equipmentslot, new ItemStack(item));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
        if ((this.getDeltaMovement().x != 0.0f) || (this.getDeltaMovement().z != 0.0f)) {
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
                if (!this.isVehicle() && this.isPushable()) {
                    this.push(0, y, 0);
                }

                if (!entity.isVehicle() && entity.isPushable()) {
                    entity.push(0, y, 0);
                }
            }
        }
    }

    static class ChasePlayerGoal extends Goal {
        private final HordeZombieEntity hordeZombie;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(HordeZombieEntity hordeZombie) {
            this.hordeZombie = hordeZombie;
            //todo this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canUse() {
            this.target = this.hordeZombie.getTarget();
            return this.target instanceof Player;
        }

        @Override
        public void start() {
            this.hordeZombie.getNavigation().stop();
        }

        @Override
        public void tick() {
            assert this.target != null;
            this.hordeZombie.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }
}
