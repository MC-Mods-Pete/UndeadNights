package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

public class ModEntitySleepEvents {

    private static Player pPlayer;
    private static BlockPos pBlockPos;

    public ModEntitySleepEvents() {
        EntitySleepEvents.ALLOW_SLEEPING.register((player, blockPos) -> {
            pPlayer = player;
            pBlockPos = blockPos;
            return executeTryAllowSleeping();
        });
    }

    public static Player.BedSleepingProblem executeTryAllowSleeping() {
        if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()
                && !pPlayer.level().isClientSide() && !pPlayer.isCreative()) {
            return Player.BedSleepingProblem.NOT_SAFE;
        }
        return null;
    }

    public static void registerEvents() { new ModEntitySleepEvents(); }
}
