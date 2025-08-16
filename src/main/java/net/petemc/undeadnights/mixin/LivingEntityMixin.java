package net.petemc.undeadnights.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.util.ModTags;
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
    @Inject(method = "getBaseMovementSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    protected void getBaseMovementSpeedMultiplier(CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this) instanceof ZombieEntity) {
            if (MainConfig.getHordeZombiesHaveIncreasedWaterMovementSpeed()) {
                cir.setReturnValue(0.94f);
            }
        }
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;", shift = At.Shift.AFTER))
    public void onDeath_lureEffect(DamageSource pDamageSource, CallbackInfo ci) {
        if ((pDamageSource != null) && (MainConfig.getEnableLureHordeEffect())) {
            if (pDamageSource.getAttacker() instanceof ServerPlayerEntity player) {
                boolean isZombie = (((Entity)(Object) this) instanceof ZombieEntity);
                boolean flag = (MainConfig.getNonHordeZombiesCanCauseLureHordeEffect() && isZombie);
                if ((UndeadNights.serverState.spawnedHordeMobs.contains(((Entity)(Object) this).getUuid())) || flag) {
                    Random randomSource = player.getWorld().random;
                    double rand = randomSource.nextDouble();
                    if (rand < MainConfig.getChanceForLureHordeEffect()) {
                        if (!player.hasStatusEffect(ModEffects.LURE_HORDE)) {
                            player.addStatusEffect(new StatusEffectInstance(ModEffects.LURE_HORDE, MainConfig.getDurationForLureHordeEffect() * 20, 0));
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    public void tick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        WorldAccess world = entity.getWorld();
        if (entity == null) {
            ci.cancel();
        }


        if (entity.getType().isIn(ModTags.EntityTypes.HORDE_MOBS)) {
            if (entity instanceof HordeZombieEntity hordeZombie) {
                if (hordeZombie.isBreakingBlock()) {
                    return;
                }
            }
            if (entity instanceof EliteZombieEntity eliteZombieEntity) {
                if (eliteZombieEntity.isBreakingBlock()) {
                    return;
                }
            }

            if (entity instanceof BlockBreakingZombie blockBreakingZombie) {
                if (blockBreakingZombie.isBreakingBlock()) {
                    return;
                }
            }

            if (MainConfig.getHordeZombiesCanPushEachOtherUp()) {
                final Vec3d entityPosition = entity.getPos();
                final Box entitySearchArea = new Box(entityPosition, entityPosition).expand(0.45 / 2d);
                List<Entity> sortedEntityList = world.getEntitiesByClass(Entity.class, entitySearchArea, entityTagCheck ->
                                entityTagCheck.getType().isIn(ModTags.EntityTypes.HORDE_MOBS))
                        .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.squaredDistanceTo(entityPosition))).toList();

                for (Entity hordeMobIterator : sortedEntityList) {
                    if (!(entity.getX() == hordeMobIterator.getX())) {
                        double randomValue = Math.random();
                        if (randomValue > 0.16D) {
                            randomValue = 0.16D;
                        }
                        Vec3d entityVec3 = new Vec3d(
                                (entity.getVelocity().getX() + (randomValue / 20.0D) * MathHelper.nextInt(Random.create(), -1, 1)),
                                randomValue,
                                (entity.getVelocity().getZ() + (randomValue / 20.0D) * MathHelper.nextInt(Random.create(), -1, 1)));

                        entity.setVelocity(entityVec3);
                        if (entity instanceof LivingEntity livingEntity) {
                            if (livingEntity.isBaby()) {
                                entity.setVelocity(entityVec3.add(0, randomValue, 0));
                            }
                            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5, 0, false, false));
                        }
                        entity.fallDistance = 0;
                    }
                }
            }
        }
    }
}
