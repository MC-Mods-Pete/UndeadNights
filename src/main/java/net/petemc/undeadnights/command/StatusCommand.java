package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

public class StatusCommand {
    public StatusCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                .executes((command) -> {
                    return status(command.getSource());
                })));
    }

    private int status(CommandSourceStack source) throws CommandSyntaxException {
        String message = "DayCounter: " + UndeadNights.serverState.getDaysCounter() + " (max " + MainConfig.getDaysBetweenHordeNights() + ")\n" +
                         "UndeadNights enabled: " + MainConfig.getUndeadNightsEnabled() + "\n" +
                         "Is it HordeNight: " + UndeadNights.serverState.getHordeNight() + "\n" +
                         "SpawnCounter: " + UndeadNights.globalSpawnCounter + " of max " + MainConfig.getHordeMobsSpawnCap() + "\n" +
                         "Block breaking: " + (HordeMobsCommand.hordeZombiesCanBreakBlocks ? ("enabled, tier: " + HordeMobsCommand.hordeZombiesBlockBreakingTier) : "disabled") + "\n" +
                         "Default Horde: " + UndeadSpawner.hordeToSpawn + ((UndeadSpawner.hordeToSpawn == 0) ? " (a horde will be chosen randomly)": "") + "\n";
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(message));
        }

        if (HordeConfig.getReadingConfigFailed()) {
            if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("Reading horde config failed!\nPlease check: https://github.com/MC-Mods-Pete/UndeadNights/wiki").withStyle(ChatFormatting.YELLOW));
            }
        }

        message = message + " (spawnedHordeMobs: " + UndeadNights.serverState.spawnedHordeMobs.size() + ", hordeMobsToRemove: " + UndeadNights.serverState.hordeMobsToRemove.size() + ")";

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return 0;
    }
}
