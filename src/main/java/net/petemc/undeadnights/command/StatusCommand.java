package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

public class StatusCommand {
    public StatusCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
                .then(Commands.literal("status")
                .executes((command) -> {
                    return status(command.getSource());
                })));
    }

    private int status(CommandSourceStack source) throws CommandSyntaxException {
        String message = "DayCounter: " + UndeadNights.serverState.getDaysCounter() + " (max " + MainConfig.getDaysBetweenHordeNights() + ")\n" +
                         "HordeNight: " + UndeadNights.serverState.getHordeNight() + "\n" +
                         "SpawnCounter: " + UndeadNights.globalSpawnCounter + " of max " + MainConfig.getHordeMobsSpawnCap();
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(message));
        }

        message = message + " (spawnedHordeMobs: " + UndeadNights.serverState.spawnedHordeMobs.size() + ", hordeMobsToRemove: " + UndeadNights.serverState.hordeMobsToRemove.size() + ")";

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return 0;
    }
}