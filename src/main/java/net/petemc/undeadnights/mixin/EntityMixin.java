package net.petemc.undeadnights.mixin;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin
{
    @Inject(method = "push*", at = @At("TAIL"))
    public void push(Entity entity, CallbackInfo ci) {
        if (((Entity) (Object) this) instanceof Zombie thisZombie) {
            if ((entity instanceof Zombie) || (entity instanceof HordeZombieEntity) ||
                (entity instanceof EliteZombieEntity) || (entity instanceof DemolitionZombieEntity)) {
                double randomMove = Math.random();
                if (randomMove > 0.16) {
                    randomMove = 0.16;
                }
                double deltaX = entity.getDeltaMovement().x() + (randomMove / 20) * Mth.nextInt(RandomSource.create(), -1, 1);
                double deltaZ = entity.getDeltaMovement().z() + (randomMove / 20) * Mth.nextInt(RandomSource.create(), -1, 1);
                if (entity instanceof LivingEntity livingEntity && livingEntity.isBaby()) {
                    entity.setDeltaMovement(new Vec3(deltaX, (randomMove * 2), deltaZ));
                    entity.fallDistance = 0;
                } else {
                    entity.setDeltaMovement(new Vec3(deltaX, randomMove, deltaZ));
                    entity.fallDistance = 0;
                    if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                        _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
                }
            }
        }
    }
}
