package net.petemc.undeadnights.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.config.MainConfig;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public class LureHordeMobsEffect extends MobEffect {
    private static final int LURE_SEARCH_INTERVAL = 100; // 5 seconds at 20 ticks/sec
    private static final double LURE_SEARCH_AREA = 15D;
    private static final int MAX_TARGETED_MOBS_PER_SEARCH = 15;
    private static final double INITIAL_LURE_CHANCE = 1.00D;
    private static final double DEFAULT_LURE_CHANCE = 0.12D;

    public LureHordeMobsEffect(MobEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean applyEffectTick(@NonNull ServerLevel pWorld, @NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (!UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isEnableLureHordeEffect()) {
            return false;
        }

        if (!pLivingEntity.level().isClientSide()) {
            if (!(pLivingEntity instanceof ServerPlayer serverPlayer)) {
                return false;
            }

            boolean debugMessages = MainConfig.getPrintDebugMessages();

            boolean spawnHorde = false;
            double chance = DEFAULT_LURE_CHANCE;
            if (serverPlayer instanceof UndeadNightsExtendedPlayer hordeLurePlayer) {
                if (!hordeLurePlayer.undeadnights_hasHordeLureEffect()) {
                    spawnHorde = true;
                    chance = INITIAL_LURE_CHANCE;
                }
                hordeLurePlayer.undeadnights_setHordeLureEffect(true);
            }

            ServerLevel serverLevel = serverPlayer.level();
            RandomSource randomSource = serverPlayer.getRandom();
            UUID uuid = serverPlayer.getUUID();

            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isLureHordeEffectSpawnsHorde()) {
                if (!UndeadNights.serverState.entitiesWithReceivedHorde.containsKey(uuid)
                        && spawnHorde) {

                    double rolledChance = randomSource.nextDouble();
                    if (rolledChance < UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getChanceForLureEffectToSpawnHorde()) {
                        UndeadNights.serverState.entitiesWithPendingHorde.put(uuid, uuid.toString());
                    } else {
                        UndeadNights.serverState.entitiesWithReceivedHorde.put(uuid, uuid.toString());
                    }
                    if (debugMessages) {
                        UndeadNights.LOGGER.info("Player " + uuid + " rolled a chance of " + rolledChance + " to spawn a horde.");
                    }
                }
            }

            // Entity search - runs every LURE_SEARCH_INTERVAL ticks per serverPlayer using server tick counter
            boolean shouldSearch = (serverPlayer.tickCount + serverPlayer.getId()) % LURE_SEARCH_INTERVAL == 0;
            if (shouldSearch || (chance == INITIAL_LURE_CHANCE)) {
                if (debugMessages) {
                    UndeadNights.LOGGER.info("Searching for nearby horde mobs to lure for player " + serverPlayer.getUUID());
                }
                if (serverPlayer.getRandom().nextDouble() < chance) {
                    setPlayerAsTargetForNearbyHordeMobs(serverLevel, serverPlayer);
                    if (debugMessages) {
                        UndeadNights.LOGGER.info("Player " + serverPlayer.getUUID() + " has successfully lured nearby horde mobs.");
                    }
                }
            }
        }

        return super.applyEffectTick(pWorld, pLivingEntity, pAmplifier);
    }

    private void setPlayerAsTargetForNearbyHordeMobs(@NonNull ServerLevel level, @NonNull ServerPlayer serverPlayer) {
        if (UndeadNights.serverState.spawnedHordeMobs.isEmpty()) {
            return;
        }

        Vec3 pos = serverPlayer.position();
        AABB searchArea = new AABB(pos, pos).inflate(LURE_SEARCH_AREA);
        List<Zombie> hordeMobsToLure = level.getEntitiesOfClass(Zombie.class, searchArea, zombie ->
                UndeadNights.serverState.spawnedHordeMobs.containsKey(zombie.getUUID()));

        int targetedCount = 0;
        for (Zombie mob : hordeMobsToLure) {
            mob.setTarget(serverPlayer);
            targetedCount++;
            if (targetedCount >= MAX_TARGETED_MOBS_PER_SEARCH) {
                break;
            }
        }
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("Player " + serverPlayer.getUUID() + " has lured " + targetedCount + " nearby horde mobs.");
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
}