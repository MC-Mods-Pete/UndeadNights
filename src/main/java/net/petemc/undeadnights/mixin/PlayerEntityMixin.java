package net.petemc.undeadnights.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.petemc.undeadnights.UndeadNights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.SleepFailureReason.class)
public class PlayerEntityMixin
{
    @Inject(method = "getMessage", at = @At(value = "RETURN"), cancellable = true)
    public void getMessage (CallbackInfoReturnable<Text> cir)
    {
        if (UndeadNights.hordeNight) {
            Text tx = Text.of("You may not rest now; this is a night of the Undead");
            cir.setReturnValue(tx);
        }
    }
}
