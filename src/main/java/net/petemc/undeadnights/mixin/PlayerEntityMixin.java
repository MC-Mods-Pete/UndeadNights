package net.petemc.undeadnights.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.util.Helpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin implements UndeadNightsExtendedPlayer {
    @Unique
    private boolean hasHordeLureEffect = false;
    @Unique
    private boolean isInCave = false;
    @Unique
    private boolean isInCaveStageOne = false;
    @Unique
    private boolean previousIsInCaveStageOne = false;
    @Unique
    private boolean isInCaveStageTwo = false;
    @Unique
    private boolean previousIsInCaveStageTwo = false;
    @Unique
    private int coolDown = 5 * 20;
    @Unique
    private int delay = 3;

    @Override
    public void undeadnights_setHordeLureEffect(boolean hordeLureValue) {
        hasHordeLureEffect = hordeLureValue;
    }

    @Override
    public boolean undeadnights_hasHordeLureEffect() {
        return hasHordeLureEffect;
    }

    @Override
    public void undeadnights_setIsInCave(boolean isInCaveValue) {
        isInCave = isInCaveValue;
    }

    @Override
    public boolean undeadnights_isInCave() {
        return isInCave;
    }

    @Override
    public boolean undeadnights_isInCaveDelayed() {
        return isInCaveStageOne;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;updatePlayerPose()V", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.level().isClientSide()) {
            if (coolDown > 0) {
                coolDown--;
            } else {
                coolDown = 5 * 20;

                this.isInCaveStageOne = Helpers.caveCheckStageOne(player.level(), player.blockPosition());

                //UndeadNights.LOGGER.info("----------------------------------------> Player isInCaveStageOne: " + isInCaveStageOne + " " + player.getName());
                if (isInCaveStageOne) {
                    if (delay > 0) {
                        delay--;
                    } else {
                        delay = 3;
                        isInCaveStageTwo = Helpers.caveCheckStageTwo(player.level(), player.blockPosition());
                        //UndeadNights.LOGGER.info("----------------------------------------> Player isInCaveStageTwo: " + isInCaveStageTwo + " " + player.getName());
                        if (isInCaveStageTwo) {
                            undeadnights_setIsInCave(true);
                        }
                    }
                } else {
                    isInCaveStageTwo = false;
                    previousIsInCaveStageTwo = false;
                    undeadnights_setIsInCave(false);
                    delay = 3;
                    //UndeadNights.LOGGER.info("----------------------------------------> Player isInCave2: FALSE " + player.getName());
                }

                if (previousIsInCaveStageOne != isInCaveStageOne) {
                    previousIsInCaveStageOne = isInCaveStageOne;
                    if (isInCaveStageOne) {
                        //player.sendSystemMessage(Component.literal("Cave detected!"));
                    } else {
                        //player.sendSystemMessage(Component.literal("Not in cave."));
                    }
                }

                if (isInCaveStageTwo != previousIsInCaveStageTwo) {
                    //UndeadNights.LOGGER.info("----------------------------------------> Player entered deep cave: " + player.getName());
                    previousIsInCaveStageTwo = isInCaveStageTwo;
                    //player.sendSystemMessage(Component.literal("Deep cave detected!"));
                }
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void injectToReadNbt(CompoundTag nbt, CallbackInfo ci) {
        this.isInCave = nbt.getBoolean("undeadnights_is_player_in_cave");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void injectToWriteNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("undeadnights_is_player_in_cave", this.isInCave);
    }
}
