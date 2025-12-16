package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Collection;
import java.util.Objects;

public class SpawnHordeCommand {
    public static boolean spawnHordeByCommand = false;

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
        ServerCommandSource source = context.getSource();
        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
            Objects.requireNonNull(source.getEntity()).sendMessage(Text.translatable("message.undeadnights.command_spawn_horde_all"));
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Command to spawn hordes for all players issued.");
                if (MainConfig.getHordeWavesCanSpawnInCaves()) {
                    Objects.requireNonNull(source.getEntity()).sendMessage(Text.literal("Note: Cave spawning is ENABLED for hordes. Finding a spawn location may take longer."));
                }
            }
            spawnHordeByCommand = true;
            for (Entity entity : source.getWorld().getPlayers()) {
                if (entity instanceof PlayerEntity) {
                    UndeadNights.serverState.entitiesWithPendingHorde.add(entity.getUuid());
                    UndeadNights.serverState.entitiesWithReceivedHorde.remove(entity.getUuid());
                }
            }
        } else {
            Objects.requireNonNull(source.getEntity()).sendMessage(Text.translatable("message.undeadnights.spawn_cap_reached"));
        }
        return 0;
    }

    private static int spawnHorde(ServerCommandSource source, Collection<? extends Entity> pTargets) {
        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
            spawnHordeByCommand = true;
            if (!pTargets.isEmpty()) {
                for (Entity entity : pTargets) {
                    if (entity instanceof PlayerEntity) {
                        UndeadNights.serverState.entitiesWithPendingHorde.add(entity.getUuid());
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(entity.getUuid());
                    }
                }
            }
            Objects.requireNonNull(source.getEntity()).sendMessage(Text.translatable("message.undeadnights.command_spawn_horde"));
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Command to spawn hordes for certain players issued.");
            }
        } else {
            Objects.requireNonNull(source.getEntity()).sendMessage(Text.translatable("message.undeadnights.spawn_cap_reached"));
        }
        return 0;
    }
}
