package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Objects;

public class StatusCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .then(CommandManager.literal("status")
                        .executes(StatusCommand::status)));
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        String message = "DayCounter: " + UndeadNights.serverState.getDaysCounter() + " (max " + MainConfig.getDaysBetweenHordeNights() + ")\n" +
                "HordeNight: " + UndeadNights.serverState.getHordeNight() + "\n" +
                "SpawnCounter: " + UndeadNights.globalSpawnCounter + " of max " + MainConfig.getHordeMobsSpawnCap();
        Objects.requireNonNull(context.getSource().getEntity())
                .sendMessage(Text.literal(message));

        message = message + " (spawnedHordeMobs: " + UndeadNights.serverState.spawnedHordeMobs.size() + ", hordeMobsToRemove: " + UndeadNights.serverState.hordeMobsToRemove.size() + ")";

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return Command.SINGLE_SUCCESS;
    }
}