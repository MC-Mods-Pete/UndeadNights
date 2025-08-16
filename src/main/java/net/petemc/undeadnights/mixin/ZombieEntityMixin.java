package net.petemc.undeadnights.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieEntityMixin implements BlockBreakingZombie
{
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BYTE);

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER), cancellable = true)
    public void hurtServer_disableReinforcements(ServerLevel pLevel, DamageSource pDamageSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
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

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    public void defineSyncedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_FLAGS_ID, (byte)0);
    }

    @Inject(method = "isSunSensitive", at = @At("TAIL"), cancellable = true)
    public void isSunSensitive(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(MainConfig.getVanillaZombiesBurnInDaylight());
    }

    @Unique
    public boolean isBreakingBlock() {
        return (((Entity) (Object) this).getEntityData().get(DATA_FLAGS_ID) & 1) != 0;
    }

    @Unique
    public void setBreakingBlock(boolean pBreaking) {
        byte b0 = ((Entity) (Object) this).getEntityData().get(DATA_FLAGS_ID);
        if (pBreaking) {
            b0 = (byte)(b0 | 1);
        } else {
            b0 = (byte)(b0 & -2);
        }

        ((Entity) (Object) this).getEntityData().set(DATA_FLAGS_ID, b0);
    }
}
