package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Collection;
import java.util.Objects;

public class SpawnHordeCommand {
    public static boolean spawnHordeByCommand = false;

    public SpawnHordeCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn_horde")
                .executes((command) -> {
                    return spawnHorde(command.getSource());
                })));
        dispatcher.register(Commands.literal("undeadnights")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn_horde")
                .then(Commands.argument("targets", EntityArgument.entities()).executes((command) -> {
                    return spawnHorde(command.getSource(), EntityArgument.getEntities(command, "targets"));
                }))));
    }

    private int spawnHorde(CommandSourceStack source) throws CommandSyntaxException {
        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.translatable("message.undeadnights.command_spawn_horde_all"));
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Command to spawn hordes for all players issued.");
                if (MainConfig.getHordeWavesCanSpawnInCaves()) {
                    Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.literal("Note: Cave spawning is ENABLED for hordes. Finding a spawn location may take longer."));
                }
            }
            spawnHordeByCommand = true;
            for (Entity entity : source.getLevel().players()) {
                if (entity instanceof Player) {
                    UndeadNights.serverState.entitiesWithPendingHorde.add(entity.getUUID());
                    UndeadNights.serverState.entitiesWithReceivedHorde.remove(entity.getUUID());
                }
            }
        } else {
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.translatable("message.undeadnights.spawn_cap_reached"));
        }
        return 0;
    }

    private int spawnHorde(CommandSourceStack source, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
            spawnHordeByCommand = true;
            if (!pTargets.isEmpty()) {
                for (Entity entity : pTargets) {
                    if (entity instanceof Player) {
                        UndeadNights.serverState.entitiesWithPendingHorde.add(entity.getUUID());
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(entity.getUUID());
                    }
                }
            }
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.translatable("message.undeadnights.command_spawn_horde"));
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Command to spawn hordes for certain players issued.");
            }
        } else {
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.translatable("message.undeadnights.spawn_cap_reached"));
        }
        return 0;
    }
}
