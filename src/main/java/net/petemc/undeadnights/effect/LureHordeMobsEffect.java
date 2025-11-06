package net.petemc.undeadnights.effect;

import net.minecraft.util.RandomSource;
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
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class LureHordeMobsEffect extends MobEffect {
    public LureHordeMobsEffect(MobEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
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
                RandomSource randomSource = pPlayer.getRandom();
                if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isLureHordeEffectSpawnsHorde()) {
                    if (!UndeadNights.serverState.entitiesWithReceivedHorde.contains(pPlayer.getUUID())) {
                        if (randomSource.nextDouble() < UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getChanceForLureEffectToSpawnHorde()) {
                            UndeadNights.serverState.entitiesWithPendingHorde.add(pPlayer.getUUID());
                        } else {
                            UndeadNights.serverState.entitiesWithReceivedHorde.add(pPlayer.getUUID());
                        }
                    }
                }

                final Vec3 entityPosition = pPlayer.position();
                final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(15d);

                double randomValue = Math.random();
                if (randomValue < (chance / 20)) {
                    List<Entity> sortedEntityList = world.getEntitiesOfClass(Entity.class, entitySearchArea, entityUUIDCheck ->
                            UndeadNights.serverState.spawnedHordeMobs.contains(entityUUIDCheck.getUUID()))
                            .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.distanceToSqr(entityPosition))).toList();

                    for (Entity hordeMobIterator : sortedEntityList) {
                        if (hordeMobIterator instanceof Mob mob) {
                            mob.setTarget(pPlayer);
                        }
                    }
                }
            }
        }
        super.applyEffectTick(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
}