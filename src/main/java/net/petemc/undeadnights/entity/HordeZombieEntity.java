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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeKeys;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;

public class HordeZombieEntity extends ZombieEntity {
    private static final TrackedData<Byte> DATA_FLAGS_ID = DataTracker.registerData(HordeZombieEntity.class, TrackedDataHandlerRegistry.BYTE);

    public HordeZombieEntity(EntityType<? extends ZombieEntity> entityType, World level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess level, LocalDifficulty difficulty, SpawnReason mobSpawnType, @Nullable EntityData spawnGroupData, @Nullable NbtCompound compoundTag) {
        spawnGroupData = super.initialize(level, difficulty, mobSpawnType, spawnGroupData, compoundTag);
        this.setLeftHanded(random.nextFloat() < 0.05F);
        float f = difficulty.getClampedLocalDifficulty();
        this.setCanPickUpLoot(random.nextFloat() < 0.55F * f);
        if (spawnGroupData == null) {
            spawnGroupData = new ZombieData(false, false);
        }

        if (spawnGroupData instanceof ZombieData) {
            this.setCanBreakDoors(true);
            this.initEquipment(random, difficulty);
            this.updateEnchantments(random, difficulty);
        }

        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localdate = LocalDate.now();
            int i = localdate.getDayOfMonth();
            int j = localdate.getMonth().getValue();
            if (j == 10 && i == 31 && random.nextFloat() < 0.25F) {
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0F;
            }
        }
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).addPersistentModifier(new EntityAttributeModifier("Horde zombie health bonus", MainConfig.getMaxHealthHordeZombies() - 20.0F, EntityAttributeModifier.Operation.ADDITION));

        int playerCount = 1;
        if (!this.getWorld().isClient()) {
            playerCount = this.getWorld().getPlayers().size();
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

        if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isUpdateHordeMobAttributes()) {
            healthScaleFactor = healthScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHealthAttributeScaleFactor() - 1.0;
            speedScaleFactor = speedScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getSpeedAttributeScaleFactor() - 1.0;
            damageScaleFactor = damageScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getDamageAttributeScaleFactor() - 1.0;
            armorScaleFactor = armorScaleFactor + UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getArmorAttributeScaleFactor() - 1.0;
        }

        boolean flag = (healthScaleFactor > 0.0) || (speedScaleFactor > 0.0) || (damageScaleFactor > 0.0) || (armorScaleFactor > 0.0);

        if (flag) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH))
                    .addPersistentModifier(new EntityAttributeModifier("Horde zombie difficulty health bonus", healthScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED))
                    .addPersistentModifier(new EntityAttributeModifier("Horde zombie difficulty speed bonus", speedScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE))
                    .addPersistentModifier(new EntityAttributeModifier("Horde zombie difficulty attack damage bonus", damageScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));

            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR))
                    .addPersistentModifier(new EntityAttributeModifier("Horde zombie difficulty armor bonus", armorScaleFactor, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }

        this.applyAttributeModifiers(f);
        this.setHealth(this.getMaxHealth());
        this.setBaby(false);
        return spawnGroupData;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange())    // default 35.0
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)   // default 0.23000000417232513
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)     // default 3.0
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)             // default 2.0
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0);
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
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, LivingEntity.class, true,
                (entity) -> (HordeConfig.getTargetEntities().contains(entity.getType().toString()))));
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    protected boolean burnsInDaylight() {
        return UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesBurnInTheSun();
    }

    @Override
    public boolean canBreakDoors() {
        return false;
    }

    @Override
    public void setCanBreakDoors(boolean val) {
    }

    @Override
    protected float getBaseMovementSpeedMultiplier() {
        return UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesAreFasterOnWater() ? 0.94F : 0.8F;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_FLAGS_ID, (byte)0);
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
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        initCustomEquipment(random, localDifficulty);
        if (random.nextFloat() < (this.getWorld().getDifficulty() == Difficulty.HARD ? 0.07F : 0.03F)) {
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
            float f = this.getWorld().getDifficulty() == Difficulty.HARD ? 0.2F : 0.45F;
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
                if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
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
    protected void initAttributes() {
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS)).setBaseValue(0.0F);
    }

    public static void initSpawnCondition() {
        SpawnRestriction.register(ModEntities.HORDE_ZOMBIE, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
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
