package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Collection;

public class SpawnHordeCommand {
    public static boolean spawnHorde = false;
    public static Collection<? extends Entity> entities = null;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn_horde")
                .executes(SpawnHordeCommand::spawnHorde)));
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn_horde")
                .then(CommandManager.argument("targets", EntityArgumentType.entities())
                .executes((command) -> {
                return spawnHorde(command.getSource(), EntityArgumentType.getEntities(command, "targets"));
        }))));
    }

    private static int spawnHorde(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
            if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                spawnHorde = true;
                serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_spawn_horde_all"));
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Command to spawn hordes for all players issued.");
                }
            } else {
                serverPlayer.sendMessage(Text.translatable("message.undeadnights.spawn_cap_reached"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnHorde(ServerCommandSource source, Collection<? extends Entity> targets) {
        if (source.getEntity() instanceof ServerPlayerEntity serverPlayer) {
            if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                spawnHorde = true;
                entities = targets;
                serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_spawn_horde"));
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Command to spawn hordes for certain players issued.");
                }
            } else {
                serverPlayer.sendMessage(Text.translatable("message.undeadnights.spawn_cap_reached"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
