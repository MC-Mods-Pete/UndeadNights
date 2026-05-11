package net.petemc.undeadnights.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;

public class EliteZombieEntity extends Zombie {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(EliteZombieEntity.class, EntityDataSerializers.BYTE);

    public EliteZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomsource = level.getRandom();
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        float f = difficulty.getSpecialMultiplier();
        this.setCanPickUpLoot(randomsource.nextFloat() < 0.55F * f);
        if (spawnGroupData == null) {
            spawnGroupData = new ZombieGroupData(false, false);
        }

        if (spawnGroupData instanceof ZombieGroupData) {
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
                this.setDropChance(EquipmentSlot.HEAD, 0.0F);
            }
        }
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH))
                .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_health_boost"), MainConfig.getMaxHealthEliteZombies() - 20.0F, AttributeModifier.Operation.ADD_VALUE));

        if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isUpdateHordeMobAttributes()) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_health_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHealthAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_speed_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getSpeedAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_attack_damage_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getDamageAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ARMOR))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_armor_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getArmorAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        int playerCount = 1;
        if (!this.level().isClientSide()) {
            playerCount = this.level().players().size();
        }

        double healthScaleFactor = 0.0;
        double damageScaleFactor = 0.0;
        double speedScaleFactor = 0.0;
        double armorScaleFactor = 0.0;

        if (UndeadNights.difficultyConfig.getDynamicScaling().isDynamicScalingEnabled()) {
            if (playerCount > 1) {
                healthScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getHealthScalePerPlayer() * (playerCount - 1);
                speedScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getSpeedScalePerPlayer() * (playerCount - 1);
                damageScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getDamageScalePerPlayer() * (playerCount - 1);
                armorScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getArmorScalePerPlayer() * (playerCount - 1);
            }

            healthScaleFactor = healthScaleFactor + UndeadNights.serverState.getCurrentHealthScale();
            speedScaleFactor = speedScaleFactor + UndeadNights.serverState.getCurrentSpeedScale();
            damageScaleFactor = damageScaleFactor + UndeadNights.serverState.getCurrentDayScaleCounter();
            armorScaleFactor = armorScaleFactor + UndeadNights.serverState.getCurrentArmorScale();

            if (healthScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale()) {
                healthScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale();
            }
            if (speedScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale()) {
                speedScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale();
            }
            if (damageScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale()) {
                damageScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale();
            }
            if (armorScaleFactor > UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale()) {
                armorScaleFactor = UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale();
            }
        }

        boolean flag = (healthScaleFactor > 0.0) || (speedScaleFactor > 0.0) || (damageScaleFactor > 0.0) || (armorScaleFactor > 0.0);

        if (flag) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_difficulty_health_bonus"), healthScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_difficulty_speed_bonus"), speedScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_difficulty_attack_damage_bonus"), damageScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ARMOR))
                    .addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_difficulty_armor_bonus"), armorScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }


        this.handleAttributes(f, spawnReason);
        this.setHealth(this.getMaxHealth());
        this.setBaby(false);
        return spawnGroupData;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange())       // default 35.0D
                .add(Attributes.MOVEMENT_SPEED, 0.32D)      // default 0.23F
                .add(Attributes.ATTACK_DAMAGE, 6.0D)        // default 3.0
                .add(Attributes.ARMOR, 5.0D)                // default 2.0
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakBlockGoal(this));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new ChasePlayerGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[]{HordeZombieEntity.class, EliteZombieEntity.class, DemolitionZombieEntity.class}).setAlertOthers(HordeZombieEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                new EliteZombieCustomTargetSelector(this)));
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource pRandom, @NotNull DifficultyInstance pDifficulty) {
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        initCustomEquipment(pRandom, pDifficulty);
    }

    protected void initCustomEquipment(RandomSource random, DifficultyInstance pDifficulty) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (itemStack.isEmpty()) {
                    Item item = getEquipmentForSlot(equipmentSlot, 4);
                    if (item != null) {
                        this.setItemSlot(equipmentSlot, new ItemStack(item));
                    }
                }
            }
        }
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected boolean isSunSensitive() {
        return UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesBurnInTheSun();
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
    protected float getWaterSlowDown() {
        return UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesAreFasterOnWater() ? 0.94F : 0.8F;
    }

    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
    }

    public boolean isBreakingBlock() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setBreakingBlock(boolean pClimbing) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pClimbing) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    @Override
    public void randomizeReinforcementsChance() {
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue((double)0.0F);
    }

    public static boolean checkEliteZombieSpawnRules(EntityType<EliteZombieEntity> eliteZombieEntityType, ServerLevelAccessor serverLevel, EntitySpawnReason entitySpawnReason, BlockPos pos, RandomSource random) {
        return MainConfig.getEliteZombiesSpawnNaturally()
                && UndeadNights.serverState.getIsNaturalSpawningOk()
                && !(serverLevel.getBiome(pos).is(Biomes.MUSHROOM_FIELDS))
                && serverLevel.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(serverLevel, pos, random)
                && Mob.checkMobSpawnRules(eliteZombieEntityType, serverLevel, entitySpawnReason, pos, random);
    }

    @Override
    public void push(@NotNull Entity entity) {
        super.push(entity);
    }

    static class EliteZombieCustomTargetSelector implements TargetingConditions.Selector {
        private final EliteZombieEntity eliteZombie;

        public EliteZombieCustomTargetSelector(EliteZombieEntity eliteZombie) {
            this.eliteZombie = eliteZombie; }
        public boolean test(LivingEntity entity, @NotNull ServerLevel level) {
            return HordeConfig.getTargetEntities().contains(entity.getType().toString());
        }
    }

    static class ChasePlayerGoal extends Goal {
        private final EliteZombieEntity hordeZombie;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(EliteZombieEntity hordeZombie) {
            this.hordeZombie = hordeZombie;
            //todo this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
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
