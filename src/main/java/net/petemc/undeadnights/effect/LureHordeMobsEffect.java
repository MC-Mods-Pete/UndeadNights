package net.petemc.undeadnights.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class LureHordeMobsEffect extends MobEffect {
    public LureHordeMobsEffect(MobEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull ServerLevel pLevel, LivingEntity pLivingEntity, int pAmplifier) {
        if (!pLevel.isClientSide()) {
            if (pLivingEntity instanceof Player pPlayer) {
                RandomSource randomSource = pPlayer.getRandom();
                if (MainConfig.getLureHordeEffectSpawnsHorde()) {
                    if (!UndeadNights.serverState.entitiesWithReceivedHorde.containsKey(pPlayer.getUUID())) {
                        if (randomSource.nextDouble() < MainConfig.getChanceForLureEffectToSpawnHorde()) {
                            UndeadNights.serverState.entitiesWithPendingHorde.put(pPlayer.getUUID(), pPlayer.getUUID().toString());
                        } else {
                            UndeadNights.serverState.entitiesWithReceivedHorde.put(pPlayer.getUUID(), pPlayer.getUUID().toString());
                        }
                    }
                }

                final Vec3 entityPosition = pPlayer.position();
                final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(15d);

                double randomValue = Math.random();
                if (randomValue < 0.03D) {
                    List<Entity> sortedEntityList = pLevel.getEntitiesOfClass(Entity.class, entitySearchArea, entityUUIDCheck ->
                            UndeadNights.serverState.spawnedHordeMobs.containsKey(entityUUIDCheck.getUUID()))
                            .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.distanceToSqr(entityPosition))).toList();

                    for (Entity hordeMobIterator : sortedEntityList) {
                        if (hordeMobIterator instanceof Mob mob) {
                            mob.setTarget(pPlayer);
                        }
                    }
                }
            }
        }
        return super.applyEffectTick(pLevel, pLivingEntity, pAmplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
}