package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.HordeSpawner;

public class SetDefaultHordeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(Commands.literal("undeadnights")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("default_horde")
                .then(Commands.argument("hordeId", IntegerArgumentType.integer(0))
                .executes((command) -> {
                    return defaultHorde(command.getSource(), IntegerArgumentType.getInteger(command, "hordeId"));
                }))));
    }

    private static int defaultHorde(CommandSourceStack source, int defaultHordeId) throws CommandSyntaxException {
        String message = null;
        if (HordeConfig.getConfigVariant() == 2) {
            if ((defaultHordeId > HordeConfig.getHordes().size()) || (defaultHordeId < 0)) {
                HordeSpawner.hordeIdFromHordesConfig = 1;
                message = "Not a valid hordeId, value set to 1";
            } else {
                HordeSpawner.hordeIdFromHordesConfig = defaultHordeId;
                message = "Set default horde to " + defaultHordeId + "\nPlease note: after a server restart this will be reverted to the value in the config file.";
            }
        } else {
            HordeSpawner.hordeIdFromHordesConfig = 1;
            message = "Please note: Horde config variant 1 does not support multiple horde configs, value is always 1";
        }

        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(message));
        }

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return 0;
    }
}
