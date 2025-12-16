package net.petemc.undeadnights.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.difficulty.DifficultyLevel;

import java.util.List;
import java.util.Objects;

public class DifficultyLevelCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("undeadnights")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("difficulty")
                .then(CommandManager.literal("query")
                    .executes(DifficultyLevelCommand::queryDifficulty))
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("levelIndex", IntegerArgumentType.integer(1))
                        .executes((command ->
                            setDifficulty(command.getSource(), IntegerArgumentType.getInteger(command, "levelIndex"))))))
                .then(CommandManager.literal("auto_progression")
                    .then(CommandManager.literal("enable")
                        .executes((command ->
                            setAutoProgression(command.getSource(), true))))
                    .then(CommandManager.literal("disable")
                        .executes((command ->
                            setAutoProgression(command.getSource(), false))))
                )));
    }

    private static int queryDifficulty(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        DifficultyLevel currentDifficultyLevel = UndeadNights.difficultyConfig.getCurrentDifficultyLevel();
        Objects.requireNonNull(source.getEntity())
            .sendMessage(Text.literal("Current difficulty level: " + currentDifficultyLevel.getDifficultyName() + "\n"
            + "Automatic difficulty progression: " + (UndeadNights.automaticDifficultyProgressionActive ? "enabled" : "disabled") + "\n"));
        return 1;
    }

    private static int setDifficulty(ServerCommandSource source, int levelIndex) throws CommandSyntaxException {
        List<DifficultyLevel> difficultyLevels = UndeadNights.difficultyConfig.getDifficultyLevels();
        if (levelIndex <= 0 || levelIndex > difficultyLevels.size()) {
            Objects.requireNonNull(source.getEntity())
                .sendMessage(Text.literal("Invalid difficulty level index!"));
            return 0;
        }
        levelIndex--; // Adjust for 0-based index
        UndeadNights.difficultyConfig.setCurrentDifficultyLevel(difficultyLevels.get(levelIndex));
        UndeadNights.serverState.setCurrentDifficultyLevelIndex(levelIndex);
        UndeadNights.serverState.setPossibleHordesIndex(-1);
        HordeMobsCommand.hordeZombiesCanBreakBlocks = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isBlockBreaking();
        HordeMobsCommand.hordeZombiesBlockBreakingTier = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getBlockBreakingTier();
        Objects.requireNonNull(source.getEntity())
            .sendMessage(Text.literal("Difficulty level set to: " + difficultyLevels.get(levelIndex).getDifficultyName()));
        return 1;
    }

    private static int setAutoProgression(ServerCommandSource source, boolean value) {
        UndeadNights.automaticDifficultyProgressionActive = value;
        Objects.requireNonNull(source.getEntity())
            .sendMessage(Text.literal("Automatic difficulty progression has been " + (value ? "enabled" : "disabled") + "."));
        return 1;
    }
}
