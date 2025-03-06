package net.petemc.undeadnights.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Zombie;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieEntityMixin
{
    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER), cancellable = true)
    public void hurtServer(ServerLevel pLevel, DamageSource pDamageSource, float pAmount, CallbackInfoReturnable<Boolean> cir)
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
