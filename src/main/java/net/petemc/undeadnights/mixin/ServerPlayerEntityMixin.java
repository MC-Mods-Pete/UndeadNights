package net.petemc.undeadnights.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.petemc.undeadnights.UndeadNights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
    @Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;ofBottomCenter(Lnet/minecraft/util/math/Vec3i;)Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.AFTER), cancellable = true)
    public void trySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir)
    {
        if (UndeadNights.hordeNight) {
            cir.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.NOT_SAFE));
        }
    }
}
