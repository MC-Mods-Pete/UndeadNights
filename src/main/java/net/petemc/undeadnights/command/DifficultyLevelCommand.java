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
                        .executes(ctx -> setAutoProgression(ctx.getSource(), false))))));
    }

    private int queryDifficulty(CommandSourceStack source) throws CommandSyntaxException {
        DifficultyLevel currentDifficultyLevel = UndeadNights.difficultyConfig.getCurrentDifficultyLevel();
        Objects.requireNonNull(source.getEntity())
            .sendSystemMessage(Component.literal("Current difficulty level: " + currentDifficultyLevel.getDifficultyName() + "\n"
            + "Automatic difficulty progression: " + (UndeadNights.automaticDifficultyProgressionActive ? "enabled" : "disabled") + "\n"));
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
}
