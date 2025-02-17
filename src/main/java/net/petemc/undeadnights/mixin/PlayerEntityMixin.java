package net.petemc.undeadnights.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.petemc.undeadnights.Config;
import net.petemc.undeadnights.UndeadNights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.BedSleepingProblem.class)
public class PlayerEntityMixin
{
    @Inject(method = "getMessage", at = @At(value = "RETURN"), cancellable = true)
    public void getMessage (CallbackInfoReturnable<Component> cir)
    {
        if (UndeadNights.serverState.getHordeNight() && Config.getHordeNightsDisableSleeping()) {
            Component tx = Component.nullToEmpty("You may not rest now; this is a Night of the Undead");
            cir.setReturnValue(tx);
        }
    }
}
