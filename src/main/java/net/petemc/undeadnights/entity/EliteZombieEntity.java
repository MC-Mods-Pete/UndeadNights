package net.petemc.undeadnights.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
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
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(new AttributeModifier(Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_health_bonus"), MainConfig.getMaxHealthEliteZombies() - 20.0F, AttributeModifier.Operation.ADD_VALUE));

        this.handleAttributes(f);
        this.setHealth(this.getMaxHealth());
        this.setBaby(false);
        return spawnGroupData;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                //.add(Attributes.MAX_HEALTH, 40.0D)          // default 20.F
                .add(Attributes.FOLLOW_RANGE, 128.0D)       // default 35.0D
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
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
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
    protected float getWaterSlowDown() {
        return MainConfig.getHordeZombiesHaveIncreasedWaterMovementSpeed() ? 0.94F : 0.8F;
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

    /*
    public static void init() {
        SpawnPlacements.register(ModEntities.ELITE_ZOMBIE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, serverLevel, reason, pos, random) ->
                        MainConfig.getEliteZombiesSpawnNaturally()
                                && UndeadNights.serverState.getIsNaturalSpawningOk()
                                && !(serverLevel.getBiome(pos).is(Biomes.MUSHROOM_FIELDS))
                                && serverLevel.getDifficulty() != Difficulty.PEACEFUL
                                && Monster.isDarkEnoughToSpawn(serverLevel, pos, random)
                                && Mob.checkMobSpawnRules(entityType, serverLevel, reason, pos, random));
    }
     */

    public static boolean checkEliteZombieSpawnRules(EntityType<EliteZombieEntity> eliteZombieEntityType, ServerLevelAccessor serverLevel, EntitySpawnReason entitySpawnReason, BlockPos pos, RandomSource random) {
        return MainConfig.getEliteZombiesSpawnNaturally()
                && UndeadNights.serverState.getIsNaturalSpawningOk()
                && !(serverLevel.getBiome(pos).is(Biomes.MUSHROOM_FIELDS))
                && serverLevel.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(serverLevel, pos, random)
                && Mob.checkMobSpawnRules(eliteZombieEntityType, serverLevel, entitySpawnReason, pos, random);
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);
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
