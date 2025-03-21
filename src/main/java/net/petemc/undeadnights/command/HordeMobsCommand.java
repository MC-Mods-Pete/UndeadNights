package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;

import java.util.Objects;

public class HordeMobsCommand {
    public HordeMobsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
                .then(Commands.literal("horde_mobs")
                .then(Commands.literal("remove_all")
                .executes((command) -> {
                    return removeMobs(command.getSource());
        }))));
        dispatcher.register(Commands.literal("undeadnights")
                .then(Commands.literal("horde_mobs")
                .then(Commands.literal("print_config")
                .executes((command) -> {
                    return printConfig(command.getSource());
        }))));
    }

    private int removeMobs(CommandSourceStack source) throws CommandSyntaxException {
        int count = 0;
         if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.command_remove_horde_mobs"));
         }
        for (var hordeMobUUID : UndeadNights.serverState.spawnedHordeMobs.stream().toList()) {
            count++;
            Entity entity = source.getLevel().getEntities().get(hordeMobUUID);
            if (entity != null) {
                entity.discard();
            } else {
                UndeadNights.serverState.hordeMobsToRemove.add(hordeMobUUID);
            }
        }
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("{} horde mobs removed or marked for removal", count);
        }
        return 0;
    }

    private int printConfig(CommandSourceStack source) throws CommandSyntaxException {
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            if (HordeConfig.getReadingConfigFailed()) {
                serverPlayer.sendSystemMessage(Component.literal("Reading the horde mob config failed!\nSpawning 15 default horde zombies instead.").withStyle(ChatFormatting.YELLOW));
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
            serverPlayer.sendSystemMessage(Component.literal(message.toString()));
            if (UndeadSpawner.invalidHordeMobEntry) {
                serverPlayer.sendSystemMessage(Component.literal("A horde mob entry in the horde mob config could not be read!\nA default horde zombie will be spawned instead.").withStyle(ChatFormatting.YELLOW));
            }
        }
        return 0;
    }
}