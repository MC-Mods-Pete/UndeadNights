package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

public class StatusCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("status")
                        .executes(StatusCommand::status)));
    }

    private static int status(CommandContext<ServerCommandSource> context) {
        String message = "DayCounter: " + UndeadNights.serverState.getDaysCounter() + " (max " + MainConfig.getDaysBetweenHordeNights() + ")\n" +
                "Grace period: " + UndeadNights.serverState.getGracePeriod() + " (of " + MainConfig.getGracePeriodBeforeFirstHordeNight() + " days remaining)\n" +
                "UndeadNights enabled: " + MainConfig.getUndeadNightsEnabled() + "\n" +
                "Is it HordeNight: " + UndeadNights.serverState.getHordeNight() + "\n" +
                "SpawnCounter: " + UndeadNights.globalSpawnCounter + " of max " + MainConfig.getHordeMobsSpawnCap() + "\n" +
                "Block breaking: " + (HordeMobsCommand.hordeZombiesCanBreakBlocks ? ("enabled, tier: " + HordeMobsCommand.hordeZombiesBlockBreakingTier) : "disabled") + "\n" +
                "Default Horde: " + UndeadSpawner.hordeToSpawn + ((UndeadSpawner.hordeToSpawn == 0) ? " (a horde will be chosen randomly)": "") + "\n";
                if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.sendMessage(Text.literal(message));
                    }

        if (HordeConfig.getReadingConfigFailed()) {
                        if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
                                serverPlayer.sendMessage(Text.literal("Reading horde config failed!\nPlease check: https://github.com/MC-Mods-Pete/UndeadNights/wiki").formatted(Formatting.YELLOW));
                            }
        }

        message = message + " (spawnedHordeMobs: " + UndeadNights.serverState.spawnedHordeMobs.size() + ", hordeMobsToRemove: " + UndeadNights.serverState.hordeMobsToRemove.size() + ")";

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info(message);
        }
        return Command.SINGLE_SUCCESS;
    }
}
