package net.petemc.undeadnights.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.petemc.undeadnights.UndeadNights;

public class ModServerTickEvents {

    public ModServerTickEvents() {
        ServerTickEvents.END_LEVEL_TICK.register((ServerLevel serverLevel) -> {
            if (!serverLevel.isClientSide()) {
                boolean spawnMonsters = serverLevel.getGameRules().get(GameRules.SPAWN_MONSTERS);
                UndeadNights.hordeSpawner.tick(serverLevel, spawnMonsters);
            }
        });
    }

    public static void registerEvents() {
        new ModServerTickEvents();
    }
}

