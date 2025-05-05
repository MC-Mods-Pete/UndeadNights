package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

import java.util.Objects;

public class StatusCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean var) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("status")
                        .executes(StatusCommand::status)));
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        String message = "DayCounter: " + UndeadNights.serverState.getDaysCounter() + " (max " + MainConfig.getDaysBetweenHordeNights() + ")\n" +
                "HordeNight: " + UndeadNights.serverState.getHordeNight() + "\n" +
                "SpawnCounter: " + UndeadNights.globalSpawnCounter + " of max " + MainConfig.getHordeMobsSpawnCap() + "\n" +
                "Block breaking: " + (HordeMobsCommand.hordeZombiesCanBreakBlocks ? ("enabled, tier: " + HordeMobsCommand.hordeZombiesBlockBreakingTier) : "disabled") + "\n" +
                "Default Horde: " + UndeadSpawner.hordeToSpawn + ((UndeadSpawner.hordeToSpawn == 0) ? " (a horde will be chosen randomly)": "") + "\n";
        Objects.requireNonNull(context.getSource().getEntity())
                .sendSystemMessage(Text.of(message), context.getSource().getEntity().getUuid());

        if (HordeConfig.getReadingConfigFailed()) {
            Objects.requireNonNull(context.getSource().getEntity())
                    .sendSystemMessage(Text.of("Reading horde config failed!\nPlease check: https://github.com/MC-Mods-Pete/UndeadNights/wiki").copy().formatted(Formatting.YELLOW), context.getSource().getEntity().getUuid());


        }

        message = message + " (spawnedHordeMobs: " + UndeadNights.serverState.spawnedHordeMobs.size() + ", hordeMobsToRemove: " + UndeadNights.serverState.hordeMobsToRemove.size() + ")";

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return Command.SINGLE_SUCCESS;
    }
}
