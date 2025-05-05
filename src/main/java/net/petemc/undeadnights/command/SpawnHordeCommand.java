package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.Collection;
import java.util.Objects;

public class SpawnHordeCommand {
    public static boolean spawnHorde = false;
    public static Collection<? extends Entity> entities = null;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean var) {
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
        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
            spawnHorde = true;
            Objects.requireNonNull(context.getSource().getEntity()).sendSystemMessage(Text.of("Trying to spawn hordes for all available players"), context.getSource().getEntity().getUuid());
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Command to spawn hordes for all players issued.");
            }
        } else {
            Objects.requireNonNull(context.getSource().getEntity()).sendSystemMessage(Text.of("Spawning horde mobs not possible, spawncap reached."), context.getSource().getEntity().getUuid());
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int spawnHorde(ServerCommandSource source, Collection<? extends Entity> targets) {
        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
            spawnHorde = true;
            entities = targets;
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Text.of("Trying to spawn hordes for given players"), source.getEntity().getUuid());
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Command to spawn hordes for certain players issued.");
            }
        } else {
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Text.of("Spawning horde mobs not possible, spawncap reached."), source.getEntity().getUuid());
        }
        return Command.SINGLE_SUCCESS;
    }
}
