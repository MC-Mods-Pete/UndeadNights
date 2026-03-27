package net.petemc.undeadnights.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.petemc.undeadnights.UndeadNights;
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

    @Inject(method = "isSunSensitive", at = @At("TAIL"), cancellable = true)
    public void isSunSensitive(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isVanillaZombiesBurnInTheSun());
    }

    @Unique
    public boolean isBreakingBlock() {
        return ((Zombie) (Object) this).getData(ModAttachmentTypes.BLOCK_BREAKING) == 1;
    }

    @Unique
    public void setBreakingBlock(boolean pBreaking) {
        int blockBreaking = pBreaking ? 1 : 0;
        ((Zombie) (Object) this).setData(ModAttachmentTypes.BLOCK_BREAKING, blockBreaking);
    }
}

