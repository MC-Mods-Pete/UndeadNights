package net.petemc.undeadnights.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class StrongLureHordeMobsEffect extends StatusEffect {
    private static final HashMap<UUID, CompletableFuture<Boolean>> cachedNearbyHordeMobsPerPlayer = new HashMap<>();

    // Asynchronous method to find nearby horde mobs and set player as target
    public static CompletableFuture<Boolean> asynchronousSetPlayerAsTargetForNearbyHordeMobs(ServerWorld level, ServerPlayerEntity player) {
        return CompletableFuture.supplyAsync(() -> setPlayerAsTargetForNearbyHordeMobs(
                level,
                player
        ), ForkJoinPool.commonPool());
    }

    // Method to find nearby horde mobs and set player as target
    private static Boolean setPlayerAsTargetForNearbyHordeMobs(ServerWorld level, ServerPlayerEntity pPlayer) {
        final Vec3d entityPosition = pPlayer.getEntityPos();
        final Box entitySearchArea = new Box(entityPosition, entityPosition).expand(15d);

        List<Entity> sortedEntityList = level.getEntitiesByClass(Entity.class, entitySearchArea, entityUUIDCheck ->
                        UndeadNights.serverState.spawnedHordeMobs.containsKey(entityUUIDCheck.getUuid()))
                .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.squaredDistanceTo(entityPosition))).toList();

        for (Entity hordeMobIterator : sortedEntityList) {
            if (hordeMobIterator instanceof MobEntity mob) {
                mob.setTarget(pPlayer);
            }
        }

        return true;
    }

    public StrongLureHordeMobsEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld pWorld, @NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.getEntityWorld().isClient()) {
            boolean removeEffect = false;
            if (pLivingEntity instanceof PlayerEntity player) {
                if (player.hasStatusEffect(ModEffects.LURE_HORDE)) {
                    player.removeStatusEffect(ModEffects.LURE_HORDE);
                    removeEffect = true;
                }
            }

            double chance = 0.08D;
            if (pLivingEntity instanceof UndeadNightsExtendedPlayer hordeLurePlayer) {
                if (removeEffect) {
                    hordeLurePlayer.undeadnights_setHordeLureEffect(false);
                }
                if (!hordeLurePlayer.undeadnights_hasHordeLureEffect()) {
                    chance = 20.0D;
                }
                hordeLurePlayer.undeadnights_setHordeLureEffect(true);
            }
            if (pLivingEntity instanceof PlayerEntity pPlayer) {
                WorldAccess world = pPlayer.getEntityWorld();
                if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isStrongLureHordeEffectSpawnsHorde()) {
                    if (!UndeadNights.serverState.entitiesWithReceivedHorde.containsKey(pPlayer.getUuid())) {
                        UndeadNights.serverState.entitiesWithPendingHorde.put(pPlayer.getUuid(), pPlayer.getUuid().toString());
                    }
                }

                double randomValue = Math.random();
                if (randomValue < (chance / 10)) {
                    if (!cachedNearbyHordeMobsPerPlayer.containsKey(pPlayer.getUuid())) {
                        CompletableFuture<Boolean> future = asynchronousSetPlayerAsTargetForNearbyHordeMobs((ServerWorld) world, (ServerPlayerEntity) pPlayer);
                        cachedNearbyHordeMobsPerPlayer.put(pPlayer.getUuid(), future);
                    } else {
                        CompletableFuture<Boolean> existingFuture = cachedNearbyHordeMobsPerPlayer.get(pPlayer.getUuid());
                        if (existingFuture.isDone()) {
                            cachedNearbyHordeMobsPerPlayer.remove(pPlayer.getUuid());
                        }
                    }
                }

                /*
                final Vec3 entityPosition = pPlayer.position();
                final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(15d);

                double randomValue = Math.random();
                if (randomValue < (chance / 20)) {
                    List<Entity> sortedEntityList = world.getEntitiesOfClass(Entity.class, entitySearchArea, entityUUIDCheck ->
                            UndeadNights.serverState.spawnedHordeMobs.contains(entityUUIDCheck.getUuid()))
                            .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.distanceToSqr(entityPosition))).toList();

                    for (Entity hordeMobIterator : sortedEntityList) {
                        if (hordeMobIterator instanceof Mob mob) {
                            mob.setTarget(pPlayer);
                        }
                    }
                }
                */
            }
        }
        return super.applyUpdateEffect(pWorld, pLivingEntity, pAmplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) { return true; }
}