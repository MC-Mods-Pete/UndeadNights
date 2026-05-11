package net.petemc.undeadnights.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.BedSleepingProblem.class)
public class PlayerEntityBedSleepingMessageMixin
{
    @Inject(method = "message", at = @At(value = "RETURN"), cancellable = true, remap = false)
    public void getMessage (CallbackInfoReturnable<Component> cir)
    {
        if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()) {
            Component tx = Component.literal("You may not rest now; this is a Night of the Undead");
            cir.setReturnValue(tx);
        }
    }
}
