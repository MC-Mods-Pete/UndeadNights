package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

public class ModEntitySleepEvents {

    private static PlayerEntity pPlayer;
    private static BlockPos pBlockPos;

    public ModEntitySleepEvents() {
        EntitySleepEvents.ALLOW_SLEEPING.register((player, blockPos) -> {
            pPlayer = player;
            pBlockPos = blockPos;
            return executeTryAllowSleeping();
        });
    }

    public static PlayerEntity.SleepFailureReason executeTryAllowSleeping() {
        if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()
                && !pPlayer.getEntityWorld().isClient() && !pPlayer.isCreative()) {
            return PlayerEntity.SleepFailureReason.NOT_SAFE;
        }
        return null;
    }

    public static void registerEvents() { new ModEntitySleepEvents(); }
}
