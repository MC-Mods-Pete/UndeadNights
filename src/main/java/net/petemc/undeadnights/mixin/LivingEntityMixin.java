package net.petemc.undeadnights.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.util.ModTags;
import net.petemc.undeadnights.util.RandomExtention;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "getWaterSlowDown", at = @At("HEAD"), cancellable = true)
    protected void getWaterSlowDown(CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this) instanceof Zombie) {
            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesAreFasterOnWater()) {
                cir.setReturnValue(0.94f);
            }
        }
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;", shift = At.Shift.AFTER))
    public void die_lureEffect(DamageSource pDamageSource, CallbackInfo ci) {
        if ((pDamageSource != null) && (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isEnableLureHordeEffect())) {
            if (pDamageSource.getEntity() instanceof ServerPlayer player) {
                boolean isZombie = (((Entity)(Object) this) instanceof Zombie);
                boolean flag = (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isNonHordeZombiesCanCauseLureHordeEffect() && isZombie);
                if ((UndeadNights.serverState.spawnedHordeMobs.containsKey(((Entity)(Object) this).getUUID())) || flag) {
                    RandomExtention randomSource = new RandomExtention();//player.level().random;
                    double rand = randomSource.nextDouble();
                    if (rand <= UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getChanceForLureHordeEffect()) {
                        if (!player.hasEffect(ModEffects.LURE_HORDE)) {
                            if (player instanceof UndeadNightsExtendedPlayer hordeLurePlayer) {
                                hordeLurePlayer.undeadnights_setHordeLureEffect(false);
                            }
                            player.addEffect(new MobEffectInstance(ModEffects.LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 20, 0));
                        }
                    }
                }
            }
        }
    }


    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;

        if (entity.getType().builtInRegistryHolder().is(ModTags.EntityTypes.HORDE_MOBS)) {
            if (entity instanceof BlockBreakingZombie blockBreakingZombie) {
                if (blockBreakingZombie.isBreakingBlock()) {
                    return;
                }
            }

            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeMobsCanClimbEachOther()) {
                final Vec3 entityPosition = entity.position();
                final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(0.45 / 2d);
                List<Entity> sortedEntityList = entity.level().getEntitiesOfClass(Entity.class, entitySearchArea, entityTagCheck ->
                                entityTagCheck.getType().builtInRegistryHolder().is(ModTags.EntityTypes.HORDE_MOBS))
                        .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.distanceToSqr(entityPosition))).toList();

                for (Entity hordeMobIterator : sortedEntityList) {
                    if (entity.getX() != hordeMobIterator.getX()) {
                        double randomValue = Math.random();
                        if (randomValue > 0.16D) {
                            randomValue = 0.16D;
                        }
                        Vec3 entityVec3 = new Vec3(
                                (entity.getDeltaMovement().x + (randomValue / 20.0D) * Mth.nextInt(entity.getRandom(), -1, 1)),
                                randomValue,
                                (entity.getDeltaMovement().z + (randomValue / 20.0D) * Mth.nextInt(entity.getRandom(), -1, 1)));

                        entity.setDeltaMovement(entityVec3);
                        if (entity instanceof LivingEntity livingEntity) {
                            if (livingEntity.isBaby()) {
                                entity.setDeltaMovement(entityVec3.add(0, randomValue, 0));
                            }
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
                        }
                        entity.fallDistance = 0;
                    }
                }
            }
        }
    }
}
