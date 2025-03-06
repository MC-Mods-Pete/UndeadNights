package net.petemc.undeadnights.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin
{
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieEntity;setTarget(Lnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER), cancellable = true)
    public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this instanceof HordeZombieEntity) {
            cir.setReturnValue(true);
        }
        if ((Object) this instanceof DemolitionZombieEntity) {
            cir.setReturnValue(true);
        }
        if ((Object) this instanceof EliteZombieEntity) {
            cir.setReturnValue(true);
        }
    }
}
