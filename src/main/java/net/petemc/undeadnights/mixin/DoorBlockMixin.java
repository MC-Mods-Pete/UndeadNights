package net.petemc.undeadnights.mixin;
/*
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public class DoorBlockMixin
{
    @Inject(method = "isWoodenDoor(Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DoorBlock;type()Lnet/minecraft/world/level/block/state/properties/BlockSetType;", shift = At.Shift.AFTER), cancellable = true)
    private static void isWoodenDoor(BlockState pState, CallbackInfoReturnable<Boolean> cir)
    {
        if (MainConfig.getHordeZombiesCanBreakAllDoors()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}

 */
