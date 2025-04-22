package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

import java.util.Objects;

public class SetDefaultHordeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .then(CommandManager.literal("default_horde")
                .then(CommandManager.argument("hordeId", IntegerArgumentType.integer(0))
                .executes((command) -> {
                    return defaultHorde(command, IntegerArgumentType.getInteger(command, "hordeId"));
                }))));
    }

    private static int defaultHorde(CommandContext<ServerCommandSource> context, int defaultHordeId) {
        String message = null;
        if (HordeConfig.getConfigVariant() == 2) {
            if ((defaultHordeId > HordeConfig.getHordes().size()) || (defaultHordeId < 0)) {
                UndeadSpawner.hordeToSpawn = 1;
                message = "Not a valid hordeId, value set to 1";
            } else {
                UndeadSpawner.hordeToSpawn = defaultHordeId;
                message = "Set default horde to " + defaultHordeId + "\nPlease note: after a server restart this will be reverted to the value in the config file.";
            }
        } else {
            UndeadSpawner.hordeToSpawn = 1;
            message = "Please note: Horde config variant 1 does not support multiple horde configs, value is always 1";
        }

        Objects.requireNonNull(context.getSource().getEntity())
                .sendMessage(Text.literal(message));

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return 0;
    }
}
