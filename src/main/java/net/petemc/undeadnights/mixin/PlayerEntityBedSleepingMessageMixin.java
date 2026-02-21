package net.petemc.undeadnights.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.SleepFailureReason.class)
public class PlayerEntityBedSleepingMessageMixin
{
    @Inject(method = "message", at = @At(value = "RETURN"), cancellable = true)
    public void getMessage (CallbackInfoReturnable<Text> cir)
    {
        if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()) {
            Text tx = Text.literal("You may not rest now; this is a Night of the Undead");
            cir.setReturnValue(tx);
        }
    }
}
