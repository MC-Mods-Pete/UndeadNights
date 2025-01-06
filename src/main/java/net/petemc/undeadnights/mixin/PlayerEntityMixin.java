package net.petemc.undeadnights.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.UndeadNightsConfig;
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
        if (UndeadNights.hordeNight && UndeadNightsConfig.INSTANCE.hordeNightsDisableSleeping) {
            Text tx = Text.of("You may not rest now; this is a night of the Undead");
            cir.setReturnValue(tx);
        }
    }
}
