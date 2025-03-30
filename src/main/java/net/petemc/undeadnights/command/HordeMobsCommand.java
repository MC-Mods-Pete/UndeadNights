package net.petemc.undeadnights.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("remove_all")
                .executes(HordeMobsCommand::removeMobs))));
        dispatcher.register(CommandManager.literal("undeadnights")
                .then(CommandManager.literal("horde_mobs")
                .then(CommandManager.literal("print_config")
                .executes(HordeMobsCommand::printConfig))));
    }

    private static int removeMobs(CommandContext<ServerCommandSource> context) {
        int count = 0;
        if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_remove_horde_mobs"));
        }
        for (var hordeMobUUID : UndeadNights.serverState.spawnedHordeMobs.keySet().stream().toList()) {
            count++;
            Entity entity = context.getSource().getWorld().getEntity(hordeMobUUID);
            if (entity != null) {
                entity.discard();
            } else {
                UndeadNights.serverState.hordeMobsToRemove.put(hordeMobUUID,hordeMobUUID.toString());
            }
        }
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("{} horde mobs removed or marked for removal", count);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int printConfig(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getEntity() instanceof ServerPlayerEntity serverPlayer) {
            if (HordeConfig.getReadingConfigFailed()) {
                serverPlayer.sendMessage(Text.literal("Reading the horde mob config failed!\nSpawning 15 default horde zombies instead.").formatted(Formatting.YELLOW));
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
                if (!HordeConfig.getHordeMobs().isEmpty()) {
                    for (var hordeMob : HordeConfig.getHordeMobs()) {
                        message.append(hordeMob.mobId());
                        if (hordeMob.countMin() >= hordeMob.countMax()) {
                            message.append(" count: ").append(hordeMob.countMin()).append("\n");
                        } else {
                            message.append(" count: ").append(hordeMob.countMin()).append("-").append(hordeMob.countMax()).append("\n");
                        }
                    }
                }
            }
            serverPlayer.sendMessage(Text.literal(message.toString()));
            if (UndeadSpawner.invalidHordeMobEntry) {
                serverPlayer.sendMessage(Text.literal("A horde mob entry in the horde mob config could not be read!\nA default horde zombie will be spawned instead.").formatted(Formatting.YELLOW));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
