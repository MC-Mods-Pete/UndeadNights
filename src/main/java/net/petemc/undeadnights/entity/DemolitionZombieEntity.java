package net.petemc.undeadnights.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
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
import net.petemc.undeadnights.entity.ai.goal.TntIgniteAndThrowGoal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;

public class DemolitionZombieEntity extends Zombie  {
    private int numberTnt = 3;

    public DemolitionZombieEntity(EntityType<? extends Zombie> entityType, Level world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, mobSpawnType, spawnGroupData);
        float f = difficulty.getSpecialMultiplier();
        this.setCanPickUpLoot(random.nextFloat() < 0.55F * f);
        if (spawnGroupData == null) {
            spawnGroupData = new ZombieGroupData(false, false);
        }

        if (spawnGroupData instanceof ZombieGroupData) {
            this.setCanBreakDoors(true);
            this.populateDefaultEquipmentSlots(random, difficulty);
            this.populateDefaultEquipmentEnchantments(level, random, difficulty);
        }

        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localdate = LocalDate.now();
            int i = localdate.getDayOfMonth();
            int j = localdate.getMonth().getValue();
            if (j == 10 && i == 31 && random.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH))
                .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_health_boost"), MainConfig.getMaxHealthDemolitionZombies() - 20.0F, AttributeModifier.Operation.ADD_VALUE));

        if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isUpdateHordeMobAttributes()) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_health_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHealthAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_speed_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getSpeedAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_attack_damage_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getDamageAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ARMOR))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_armor_bonus"), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getArmorAttributeScaleFactor() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        int playerCount = 1;
        if (!this.level().isClientSide) {
            playerCount = this.level().players().size();
        }

        double healthScaleFactor = 0.0;
        double speedScaleFactor = 0.0;
        double damageScaleFactor = 0.0;
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
            damageScaleFactor = damageScaleFactor + UndeadNights.serverState.getCurrentDamageScale();
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
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_difficulty_health_bonus"), healthScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_difficulty_speed_bonus"), speedScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_difficulty_attack_damage_bonus"), damageScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

            Objects.requireNonNull(this.getAttribute(Attributes.ARMOR))
                    .addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_difficulty_armor_bonus"), armorScaleFactor, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        this.handleAttributes(f);
        this.setHealth(this.getMaxHealth());
        this.setBaby(false);
        return spawnGroupData;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange())       // default 35.0D
                .add(Attributes.MOVEMENT_SPEED, 0.30D)      // default 0.23F
                .add(Attributes.ATTACK_DAMAGE, 5.0D)        // default 3.0
                .add(Attributes.ARMOR, 4.0D)                // default 2.0
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TntIgniteAndThrowGoal(this));
        this.goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new ChasePlayerGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[]{HordeZombieEntity.class, EliteZombieEntity.class, DemolitionZombieEntity.class}).setAlertOthers(HordeZombieEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                (entity) -> (HordeConfig.getTargetEntities().contains(entity.getType().toString()))));
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel pLevel, @NotNull DamageSource pDamageSource, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(pLevel, pDamageSource, pRecentlyHit);
        dropInventory();
    }

    public void dropInventory() {
        if (!this.getMainHandItem().isEmpty()) {
            this.spawnAtLocation(this.getMainHandItem());
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource pRandom, @NotNull DifficultyInstance pDifficulty) {
        if (numberTnt == 0) {
            this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TNT));
        }
        if ((numberTnt > 0) && (numberTnt <= 64)) {
            this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TNT, numberTnt));
        }
        initCustomEquipment(pRandom, pDifficulty);
    }

    protected void initCustomEquipment(@NotNull RandomSource pRandom, @NotNull DifficultyInstance pDifficulty) {
        if (pRandom.nextFloat() < 0.15F * pDifficulty.getSpecialMultiplier()) {
            int i = pRandom.nextInt(2);
            float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
            if (pRandom.nextFloat() < 0.095F) {
                ++i;
            }

            if (pRandom.nextFloat() < 0.095F) {
                ++i;
            }

            if (pRandom.nextFloat() < 0.095F) {
                ++i;
            }

            boolean flag = true;

            for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack itemstack = this.getItemBySlot(equipmentslot);
                    if (!flag && pRandom.nextFloat() < f) {
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
        return true;
    }

    @Override
    protected float getWaterSlowDown() {
        return UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesAreFasterOnWater() ? 0.94F : 0.8F;
    }

    @Override
    public void randomizeReinforcementsChance() {
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue((double)0.0F);
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (this.isOnFire()) {
            Level level = this.level();
            BlockPos pos = this.blockPosition();
            this.remove(RemovalReason.KILLED);
            level.explode(this, pos.getX(), pos.getY(), pos.getZ(), 5, true, Level.ExplosionInteraction.TNT);
            return true;
        }
        return super.hurt(pSource, pAmount);
    }

    public void setNumberTnt(int value) {
        this.numberTnt = value;
    }

    public int getNumberTnt() {
        return this.numberTnt;
    }

    public static boolean checkDemolitionZombieSpawnRules(EntityType<DemolitionZombieEntity> demolitionZombieEntityType, ServerLevelAccessor serverLevel, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return MainConfig.getDemolitionZombiesSpawnNaturally()
                && UndeadNights.serverState.getIsNaturalSpawningOk()
                && !(serverLevel.getBiome(pos).is(Biomes.MUSHROOM_FIELDS))
                && !(serverLevel.getBiome(pos).is(Biomes.DEEP_DARK))
                && serverLevel.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(serverLevel, pos, random)
                && Mob.checkMobSpawnRules(demolitionZombieEntityType, serverLevel, spawnType, pos, random);
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
    }

    static class ChasePlayerGoal extends Goal {
        private final DemolitionZombieEntity demolitionZombie;
        @Nullable
        private LivingEntity target;

        public ChasePlayerGoal(DemolitionZombieEntity demolitionZombie) {
            this.demolitionZombie = demolitionZombie;
            //Todo this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
        }

        @Override
        public boolean canUse() {
            this.target = this.demolitionZombie.getTarget();
            return this.target instanceof Player;
        }

        @Override
        public void start() {
            this.demolitionZombie.getNavigation().stop();
        }

        @Override
        public void tick() {
            assert this.target != null;
            this.demolitionZombie.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }
}
