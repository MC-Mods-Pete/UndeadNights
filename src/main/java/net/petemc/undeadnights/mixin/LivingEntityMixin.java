package net.petemc.undeadnights.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "getWaterSlowDown", at = @At("HEAD"), cancellable = true)
    protected void getWaterSlowDown(CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this) instanceof Zombie) {
            if (MainConfig.getHordeZombiesHaveIncreasedWaterMovementSpeed()) {
                cir.setReturnValue(0.94f);
            }
        }
    }
}
