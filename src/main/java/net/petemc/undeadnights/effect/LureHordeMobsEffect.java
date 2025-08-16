package net.petemc.undeadnights.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Comparator;
import java.util.List;

public class LureHordeMobsEffect extends StatusEffect {
    public LureHordeMobsEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld pWorld, LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLivingEntity.getWorld().isClient()) {
            if (pLivingEntity instanceof PlayerEntity pPlayer) {
                WorldAccess world = pPlayer.getWorld();
                Random randomSource = pPlayer.getRandom();
                if (MainConfig.getLureHordeEffectSpawnsHorde()) {
                    if (!UndeadNights.serverState.entitiesWithReceivedHorde.containsKey(pPlayer.getUuid())) {
                        if (randomSource.nextDouble() < MainConfig.getChanceForLureEffectToSpawnHorde()) {
                            UndeadNights.serverState.entitiesWithPendingHorde.put(pPlayer.getUuid(), pPlayer.getUuid().toString());
                        } else {
                            UndeadNights.serverState.entitiesWithReceivedHorde.put(pPlayer.getUuid(), pPlayer.getUuid().toString());
                        }
                    }
                }

                final Vec3d entityPosition = pPlayer.getPos();
                final Box entitySearchArea = new Box(entityPosition, entityPosition).expand(15d);

                if (randomSource.nextDouble() < 0.03D) {
                    List<Entity> sortedEntityList = world.getEntitiesByClass(Entity.class, entitySearchArea, entityUUIDCheck ->
                                    UndeadNights.serverState.spawnedHordeMobs.containsKey(entityUUIDCheck.getUuid()))
                            .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.squaredDistanceTo(entityPosition))).toList();

                    for (Entity hordeMobIterator : sortedEntityList) {
                        if (hordeMobIterator instanceof MobEntity mob) {
                            mob.setTarget(pPlayer);
                        }
                    }
                }
            }
        }
        return super.applyUpdateEffect(pWorld, pLivingEntity, pAmplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int pDuration, int pAmplifier) {
        return true;
    }
}