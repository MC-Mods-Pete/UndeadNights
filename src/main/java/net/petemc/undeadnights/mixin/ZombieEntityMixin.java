package net.petemc.undeadnights.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombieEntity;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public class ZombieEntityMixin  implements BlockBreakingZombie
{
    private static final TrackedData<Byte> DATA_FLAGS_ID = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BYTE);

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieEntity;setTarget(Lnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
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

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    public void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(DATA_FLAGS_ID, (byte)0);
    }

    @Inject(method = "burnsInDaylight", at = @At("TAIL"), cancellable = true)
    public void burnsInDaylight(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isVanillaZombiesBurnInTheSun());
    }

    @Unique
    public boolean isBreakingBlock() {
        return (((Entity) (Object) this).getDataTracker().get(DATA_FLAGS_ID) & 1) != 0;
    }

    @Unique
    public void setBreakingBlock(boolean pBreaking) {
        byte b0 = ((Entity) (Object) this).getDataTracker().get(DATA_FLAGS_ID);
        if (pBreaking) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        ((Entity) (Object) this).getDataTracker().set(DATA_FLAGS_ID, b0);
    }
}
