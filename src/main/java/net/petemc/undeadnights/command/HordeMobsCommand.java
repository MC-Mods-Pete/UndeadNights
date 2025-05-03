package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

import java.util.Objects;

public class HordeMobsCommand {
    public static boolean hordeZombiesCanBreakBlocks = false;
    public static int hordeZombiesBlockBreakingTier = 1;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("remove_all")
                .executes(HordeMobsCommand::removeMobs))));
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("print_config")
                .executes(HordeMobsCommand::printConfig))));
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("block_breaking")
                .then(CommandManager.literal("enable")
                .executes((command) -> {
                    return blockBreaking(command.getSource(), true, 0);
                })))));
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("block_breaking")
                .then(CommandManager.literal("disable")
                .executes((command) -> {
                    return blockBreaking(command.getSource(), false, 0);
                })))));
        dispatcher.register(CommandManager.literal("undeadnights")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("block_breaking")
                .then(CommandManager.literal("set_tier")
                .then(CommandManager.argument("tierValue", IntegerArgumentType.integer(1))
                .executes((command) -> {
                    return blockBreaking(command.getSource(), false, IntegerArgumentType.getInteger(command, "tierValue"));
                }))))));

    }

    private static int removeMobs(CommandContext<ServerCommandSource> context) {
        int count = 0;
        if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_remove_horde_mobs"));
        }
        for (var hordeMobUUID : UndeadNights.serverState.spawnedHordeMobs.stream().toList()) {
            count++;
            Entity entity = context.getSource().getWorld().getEntity(hordeMobUUID);
            if (entity != null) {
                entity.discard();
            } else {
                UndeadNights.serverState.hordeMobsToRemove.add(hordeMobUUID);
            }
        }
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("{} horde mobs removed or marked for removal", count);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int printConfig(CommandContext<ServerCommandSource> context) {
        if (HordeConfig.getReadingConfigFailed()) {
            if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("Reading the horde mob config failed!\nSpawning 15 default horde zombies instead.\nPlease check: https://github.com/MC-Mods-Pete/UndeadNights/wiki").formatted(Formatting.YELLOW));
            }
            return 0;
        }
        StringBuilder message = new StringBuilder("Variant: " + HordeConfig.getConfigVariant() + "\n");
        if (HordeConfig.getConfigVariant() == 1) {
            message.append("defaultHordeMob: ").append(HordeConfig.getDefaultHordeMob().mobId()).append("\n");
            if (!HordeConfig.getHordeMobs().isEmpty()) {
                for (var hordeMob : HordeConfig.getHordeMobs()) {
                    message.append(hordeMob.mobId()).append(" chance: ").append(hordeMob.chance()).append("\n");
                }
            }
        } else {
            if (!HordeConfig.getHordes().isEmpty()) {
                for (var horde : HordeConfig.getHordes()) {
                    message.append("hordeId: ").append(horde.hordeId()).append("\n");
                    message.append("hordeName: ").append(horde.hordeName()).append("\n");
                    if (!horde.hordeMobs().isEmpty()) {
                        for (var hordeMob : horde.hordeMobs()) {
                            message.append(hordeMob.mobId());
                            if (hordeMob.countMin() >= hordeMob.countMax()) {
                                message.append(" count: ").append(hordeMob.countMin()).append("\n");
                            } else {
                                message.append(" count: ").append(hordeMob.countMin()).append("-").append(hordeMob.countMax()).append("\n");
                            }
                        }
                    }
                }
            }
        }
        if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal(message.toString()));
            }
        if (UndeadSpawner.invalidHordeMobEntry) {
            if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("""
                        A horde mob entry in the horde mob config could not be read!
                        A default horde zombie will be spawned instead.
                        Please check: https://github.com/MC-Mods-Pete/UndeadNights/wiki""").formatted(Formatting.YELLOW));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int blockBreaking(ServerCommandSource source, boolean value, int tier) {
        if (tier == 0) {
            hordeZombiesCanBreakBlocks = value;
            if (source.getEntity() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("Block breaking for Horde and Elite Zombies " + (value ? "enabled" : "disabled")));
            }
        } else {
            hordeZombiesBlockBreakingTier = tier;
            if (source.getEntity() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.literal("Setting block breaking tier " + tier));
            }
        }
        return 0;
    }
}
