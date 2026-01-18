// File: src/main/java/net/petemc/undeadnights/command/DifficultyLevelCommand.java
package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.difficulty.DifficultyLevel;
import net.petemc.undeadnights.config.difficulty.DifficultySettingDynamicScaling;

import java.util.List;
import java.util.Objects;

public class DifficultyLevelCommand {

    public DifficultyLevelCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("undeadnights")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("difficulty")
                .then(Commands.literal("query")
                    .executes(ctx -> queryDifficulty(ctx.getSource())))
                .then(Commands.literal("set")
                    .then(Commands.argument("levelIndex", IntegerArgumentType.integer(1))
                        .executes(ctx -> setDifficulty(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "levelIndex")))))
                .then(Commands.literal("auto_progression")
                    .then(Commands.literal("enable")
                        .executes(ctx -> setAutoProgression(ctx.getSource(), true)))
                    .then(Commands.literal("disable")
                        .executes(ctx -> setAutoProgression(ctx.getSource(), false))))
                .then(Commands.literal("dynamic_scaling")
                    .then(Commands.literal("enable")
                        .executes(ctx -> setDynamicScaling(ctx.getSource(), true)))
                    .then(Commands.literal("disable")
                        .executes(ctx -> setDynamicScaling(ctx.getSource(), false)))
                    .then(Commands.literal("reset")
                        .executes(ctx -> resetDynamicScalingValues(ctx.getSource())))
                )
            ));
    }

    private int queryDifficulty(CommandSourceStack source) throws CommandSyntaxException {
        DifficultyLevel currentDifficultyLevel = UndeadNights.difficultyConfig.getCurrentDifficultyLevel();
        Objects.requireNonNull(source.getEntity())
            .sendSystemMessage(Component.literal("Current difficulty level: " + currentDifficultyLevel.getDifficultyName() + " (max: " + UndeadNights.difficultyConfig.getDifficultyLevels().size() +")\n"
            + "Automatic difficulty progression: " + (UndeadNights.automaticDifficultyProgressionActive ? "enabled" : "disabled") + "\n"
            + "Dynamic scaling: " + (UndeadNights.difficultyConfig.getDynamicScaling().isDynamicScalingEnabled() ? "enabled" : "disabled") + "\n"
            + "Dynamic scaling day counter: " + UndeadNights.serverState.getCurrentDayScaleCounter() + " / " + String.format("%.0f", UndeadNights.difficultyConfig.getDynamicScaling().getDaysBetweenScaleIncreases()) + "\n"
            + "Current health scaling: +" + String.format("%.1f", UndeadNights.serverState.getCurrentHealthScale()*100) + "%\n"
            + "Current speed scaling: +" + String.format("%.1f", UndeadNights.serverState.getCurrentSpeedScale()*100) + "%\n"
            + "Current damage scaling: +" + String.format("%.1f", UndeadNights.serverState.getCurrentDamageScale()*100) + "%\n"
            + "Current armor scaling: +" + String.format("%.1f", UndeadNights.serverState.getCurrentArmorScale()*100) + "%\n"
            ));
        return 1;
    }

    private int setDifficulty(CommandSourceStack source, int levelIndex) throws CommandSyntaxException {
        List<DifficultyLevel> difficultyLevels = UndeadNights.difficultyConfig.getDifficultyLevels();
        if (levelIndex <= 0 || levelIndex > difficultyLevels.size()) {
            Objects.requireNonNull(source.getEntity())
                .sendSystemMessage(Component.literal("Invalid difficulty level index!"));
            return 0;
        }
        levelIndex--; // Adjust for 0-based index
        UndeadNights.difficultyConfig.setCurrentDifficultyLevel(difficultyLevels.get(levelIndex));
        UndeadNights.serverState.setCurrentDifficultyLevelIndex(levelIndex);
        UndeadNights.serverState.setPossibleHordesIndex(-1);
        HordeMobsCommand.hordeZombiesCanBreakBlocks = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isBlockBreaking();
        HordeMobsCommand.hordeZombiesBlockBreakingTier = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getBlockBreakingTier();
        Objects.requireNonNull(source.getEntity())
            .sendSystemMessage(Component.literal("Difficulty level set to: " + difficultyLevels.get(levelIndex).getDifficultyName()));
        return 1;
    }

    private int setAutoProgression(CommandSourceStack source, boolean value) {
        UndeadNights.automaticDifficultyProgressionActive = value;
        Objects.requireNonNull(source.getEntity())
            .sendSystemMessage(Component.literal("Automatic difficulty progression has been " + (value ? "enabled" : "disabled") + "."));
        return 1;
    }

    private int setDynamicScaling(CommandSourceStack source, boolean value) {
        DifficultySettingDynamicScaling dynamicScaling = UndeadNights.difficultyConfig.getDynamicScaling();
        dynamicScaling.setDynamicScalingEnabled(value);
        Objects.requireNonNull(source.getEntity())
                .sendSystemMessage(Component.literal("Dynamic difficulty scaling has been " + (value ? "enabled" : "disabled") + "."));
        return 1;
    }

    private int resetDynamicScalingValues(CommandSourceStack source) {
        UndeadNights.serverState.setCurrentHealthScale(0.0f);
        UndeadNights.serverState.setCurrentSpeedScale(0.0f);
        UndeadNights.serverState.setCurrentDamageScale(0.0f);
        UndeadNights.serverState.setCurrentArmorScale(0.0f);
        UndeadNights.serverState.setCurrentDayScaleCounter(0);
        Objects.requireNonNull(source.getEntity())
                .sendSystemMessage(Component.literal("Dynamic difficulty scaling values and day counter have been reset."));
        return 1;
    }
}
