package net.petemc.undeadnights.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class LureHordeMobsEffect extends MobEffect {
    private static final HashMap<UUID, CompletableFuture<Boolean>> cachedNearbyHordeMobsPerPlayer = new HashMap<>();

    public static CompletableFuture<Boolean> asynchronousSetPlayerAsTargetForNearbyHordeMobs(ServerLevel level, ServerPlayer player) {
        return CompletableFuture.supplyAsync(() -> setPlayerAsTargetForNearbyHordeMobs(level, player), ForkJoinPool.commonPool());
    }

    private static Boolean setPlayerAsTargetForNearbyHordeMobs(ServerLevel level, ServerPlayer pPlayer) {
        final Vec3 entityPosition = pPlayer.position();
        final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(15d);

        List<Entity> sortedEntityList = level.getEntitiesOfClass(Entity.class, entitySearchArea, entityUUIDCheck ->
                        UndeadNights.serverState.spawnedHordeMobs.containsKey(entityUUIDCheck.getUUID()))
                .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.distanceToSqr(entityPosition))).toList();

        for (Entity hordeMobIterator : sortedEntityList) {
            if (hordeMobIterator instanceof Mob mob) {
                mob.setTarget(pPlayer);
            }
        }

        return true;
    }

    public LureHordeMobsEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel pWorld, LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.level().isClientSide()) {
            double chance = 0.08D;
            if (pLivingEntity instanceof UndeadNightsExtendedPlayer hordeLurePlayer) {
                if (!hordeLurePlayer.undeadnights_hasHordeLureEffect()) {
                    chance = 20.0D;
                }
                hordeLurePlayer.undeadnights_setHordeLureEffect(true);
            }
            if (pLivingEntity instanceof Player pPlayer) {
                LevelAccessor world = pPlayer.level();
                if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isLureHordeEffectSpawnsHorde()) {
                    if (!UndeadNights.serverState.entitiesWithReceivedHorde.containsKey(pPlayer.getUUID())) {
                        if (pPlayer.getRandom().nextDouble() < UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getChanceForLureEffectToSpawnHorde()) {
                            UndeadNights.serverState.entitiesWithPendingHorde.put(pPlayer.getUUID(), pPlayer.getUUID().toString());
                        } else {
                            UndeadNights.serverState.entitiesWithReceivedHorde.put(pPlayer.getUUID(), pPlayer.getUUID().toString());
                        }
                    }
                }

                double randomValue = Math.random();
                if (randomValue < (chance / 10)) {
                    if (!cachedNearbyHordeMobsPerPlayer.containsKey(pPlayer.getUUID())) {
                        CompletableFuture<Boolean> future = asynchronousSetPlayerAsTargetForNearbyHordeMobs((ServerLevel) world, (ServerPlayer) pPlayer);
                        cachedNearbyHordeMobsPerPlayer.put(pPlayer.getUUID(), future);
                    } else {
                        CompletableFuture<Boolean> existingFuture = cachedNearbyHordeMobsPerPlayer.get(pPlayer.getUUID());
                        if (existingFuture.isDone()) {
                            cachedNearbyHordeMobsPerPlayer.remove(pPlayer.getUUID());
                        }
                    }
                }
            }
        }
        return super.applyEffectTick(pWorld, pLivingEntity, pAmplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
}