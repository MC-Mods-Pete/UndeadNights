package net.petemc.undeadnights.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.petemc.undeadnights.attachment.ModAttachmentTypes;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieEntityMixin implements BlockBreakingZombie
{
    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/Zombie;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER), cancellable = true)
    public void hurtServer_disableReinforcements(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
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

    // isSunSensitive() is dead code in MC 26.1 - sun burning logic was replaced by
    // EntityTypeTags.BURN_IN_DAYLIGHT + Mob.burnUndead(). Burn control is now in MobEntityMixin.

    @Unique
    public boolean isBreakingBlock() {
        return ((Entity) (Object) this).getData(ModAttachmentTypes.BLOCK_BREAKING) != 0;
    }

    @Unique
    public void setBreakingBlock(boolean pBreaking) {
        ((Entity) (Object) this).setData(ModAttachmentTypes.BLOCK_BREAKING, pBreaking ? 1 : 0);
    }
}
