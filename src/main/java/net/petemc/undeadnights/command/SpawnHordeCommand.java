package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Collection;

public class SpawnHordeCommand {
    public static boolean spawnHorde = false;
    public static Collection<? extends Entity> entities = null;

    public SpawnHordeCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spawn_horde")
                .executes((command) -> {
                    return spawnHorde(command.getSource());
                })));
        dispatcher.register(Commands.literal("undeadnights")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spawn_horde")
                .then(Commands.argument("targets", EntityArgument.entities()).executes((command) -> {
                    return spawnHorde(command.getSource(), EntityArgument.getEntities(command, "targets"));
                }))));
    }

    private int spawnHorde(CommandSourceStack source) throws CommandSyntaxException {
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                spawnHorde = true;
                serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.command_spawn_horde_all"));
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Command to spawn hordes for all players issued.");
                }
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.spawn_cap_reached"));
            }
        }
        return 0;
    }

    private int spawnHorde(CommandSourceStack source, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                spawnHorde = true;
                entities = pTargets;
                serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.command_spawn_horde"));
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Command to spawn hordes for certain players issued.");
                }
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.spawn_cap_reached"));
            }
        }
        return 0;
    }
}