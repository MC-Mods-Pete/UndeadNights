package net.petemc.undeadnights.world.spawner;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.spawner.Spawner;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.util.RandomExtention;
import net.petemc.undeadnights.util.SpawnLocationFinder;
import net.petemc.undeadnights.util.SpawnProcess;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HordeSpawner implements Spawner {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeIdFromHordesConfig = 1;

    public HashMap<UUID, HordeSpawnTask> hordeSpawningPerPlayer = new HashMap<>();

    public enum SpawnHordeResult {
        DONE,
        NOT_DONE_YET,
        FAILED;
    }

    public static class HordeSpawnTask {
        public CompletableFuture<SpawnHordeResult> futureResult;
        public int tries = 0;

        public HordeSpawnTask(CompletableFuture<SpawnHordeResult> futureResult, int tries) {
            this.futureResult = futureResult;
            this.tries = tries;
        }
    }

    public SpawnHordeResult spawnHorde(ServerWorld level, ServerPlayerEntity player, RandomExtention randomSource) {
        if (MainConfig.getEnableAsynchronousHordeSpawning()) {
            if (!hordeSpawningPerPlayer.containsKey(player.getUuid())) {
                hordeSpawningPerPlayer.put(player.getUuid(),
                        new HordeSpawnTask(SpawnProcess.asynchronousHordeSpawner(level, player, randomSource),10));
                if (MainConfig.getPrintDebugMessages()) {
                    player.sendMessage(Text.literal("[DEBUG] Finding horde spawn location (async)...").formatted(Formatting.DARK_AQUA));
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Async horde spawning location calculation for player {} at {}", player.getName().getString(), player.getBlockPos());
                }
            }
            HordeSpawnTask existingHordeSpawnTask = hordeSpawningPerPlayer.get(player.getUuid());
            CompletableFuture<SpawnHordeResult> existingFutureSpawnHorde = existingHordeSpawnTask.futureResult;
            if (existingFutureSpawnHorde != null && existingFutureSpawnHorde.isDone()) {
                SpawnHordeResult result = SpawnHordeResult.FAILED;
                try {
                    result = existingFutureSpawnHorde.get();
                } catch (Exception e) {
                    UndeadNights.LOGGER.warn("Spawning horde for player {} failed!", player.getName().getString());
                }
                if (result == SpawnHordeResult.DONE) {
                    hordeSpawningPerPlayer.remove(player.getUuid());
                    return result;
                }
                if (existingHordeSpawnTask.tries > 0) {
                    // still not done, skip this spawn attempt
                    existingHordeSpawnTask.tries--;
                    return SpawnHordeResult.NOT_DONE_YET;
                } else {
                    // exceeded max tries, consider this a failed attempt
                    hordeSpawningPerPlayer.remove(player.getUuid());
                    return SpawnHordeResult.FAILED;
                }
            }
        } else {
            return SpawnProcess.synchronousHordeSpawner(level, player, randomSource);
        }
        return SpawnHordeResult.NOT_DONE_YET;
    }


    @Override
    public int spawn(ServerWorld level, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights is enabled
        if (!MainConfig.getUndeadNightsEnabled()) {
            return 0;
        }

        if (!spawnMonsters && !MainConfig.getIgnoreDoMobSpawningGamerule()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!(level.getRegistryKey() == World.OVERWORLD)) {
            return 0;
        }

        // Check if the SaveState is already initialized
        if (UndeadNights.serverState == null) {
            return 0;
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = level.getTimeOfDay() - ((level.getTimeOfDay() / 24000L) * 24000);
        if (UndeadNights.serverState.getPrevNormalizedTimeOfDay() == normalizedTimeOfDay) {
            return 0;
        }
        UndeadNights.serverState.setNightIsStarting((UndeadNights.serverState.getPrevNormalizedTimeOfDay() <  12000L) && (normalizedTimeOfDay >= 12000L));
        UndeadNights.serverState.setPrevNormalizedTimeOfDay(normalizedTimeOfDay);

        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;
        boolean allDayLong = (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getAllDayLongHordeNights() && UndeadNights.serverState.getHordeNight() && (normalizedTimeOfDay >= 22500 || normalizedTimeOfDay < 11000));

        final RandomExtention randomSource = new RandomExtention();
        int randomValue = 0;

        // process pending horde spawns per player
        if (!UndeadNights.serverState.entitiesWithPendingHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithPendingHorde.stream().toList()) {
                if (!UndeadNights.serverState.entitiesWithReceivedHorde.contains(playerUUID)) {
                    Entity entity = level.getEntity(playerUUID);
                    if (entity instanceof ServerPlayerEntity serverPlayer) {
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Processing pending horde spawn for player {}.", serverPlayer.getName().getString());
                        }
                        SpawnHordeResult result = spawnHorde(level, serverPlayer, randomSource);
                        if ((result == SpawnHordeResult.FAILED) || (result == SpawnHordeResult.DONE)) {
                            if (result == SpawnHordeResult.DONE) {
                                if (MainConfig.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("Spawning horde for player {} succeeded.", serverPlayer.getName().getString());
                                }
                            }
                            UndeadNights.serverState.entitiesWithPendingHorde.remove(playerUUID);
                            UndeadNights.serverState.entitiesWithReceivedHorde.add(playerUUID);
                            if (result == SpawnHordeResult.FAILED) {
                                UndeadNights.LOGGER.info("Spawning horde for player {} failed.", serverPlayer.getName().getString());
                                if (SpawnHordeCommand.spawnHordeByCommand) {
                                    serverPlayer.sendMessage(Text.translatable("message.undeadnights.command_spawn_horde_failed"));
                                    SpawnHordeCommand.spawnHordeByCommand = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        // clean up players that lost the Lure Horde effect
        if (!UndeadNights.serverState.entitiesWithReceivedHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithReceivedHorde.stream().toList()) {
                Entity entity = level.getEntity(playerUUID);
                if (entity instanceof ServerPlayerEntity serverPlayer) {
                    if (!serverPlayer.hasStatusEffect(ModEffects.LURE_HORDE) && !serverPlayer.hasStatusEffect(ModEffects.STRONG_LURE_HORDE)) {
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(playerUUID);
                    }
                }
            }
        }

        // is it night...?
        if (itIsNight || allDayLong) {
            UndeadNights.serverState.setPerformDifficultySwitchCheck(true);
            // if it's already a horde night, check if we should respawn new waves
            if (UndeadNights.serverState.getRespawnZombies() && UndeadNights.serverState.getHordeNight() &&
                    UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().isSpawnAdditionalWaves()) {
                if (UndeadNights.serverState.getTickCounter() > 0) {
                    UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                    return 0;
                } else {
                    UndeadNights.serverState.setTickCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getCooldownBetweenWaves() * 20);
                }

                randomValue = randomSource.nextBetween(1, 100);
                if (randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getChanceForAdditionalWave())) {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("New Wave, randomValue was: {}", randomValue);
                    }
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setRespawnZombies(false);
                } else {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("RandomValue: {}", randomValue);
                    }
                    return 0;
                }
            }

            // if a new night just started, count down the days
            if ((UndeadNights.serverState.getNightIsStarting() && (UndeadNights.serverState.getDaysCounter() >= 1))) {
                if (UndeadNights.serverState.getGracePeriod() > 0) {
                    UndeadNights.serverState.setGracePeriod(UndeadNights.serverState.getGracePeriod() - 1);
                    if (MainConfig.getSendHordeNightsCountdownMessage()) {
                        for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                            player.sendMessage(Text.translatable("message.undeadnights.days_of_grace_remaining", String.valueOf(UndeadNights.serverState.getGracePeriod())));
                        }
                    }
                    if (UndeadNights.serverState.getGracePeriod() == 0) {
                        UndeadNights.serverState.setDaysCounter(1);
                    } else {
                        return 0;
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (MainConfig.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendMessage(Text.translatable("message.undeadnights.nights_remaining", String.valueOf(UndeadNights.serverState.getDaysCounter())));
                        } else {
                            player.sendMessage(Text.translatable("message.undeadnights.last_nights"));
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {}, DaysCounter: {}, GameTime: {}, GameTimeDays: {}", normalizedTimeOfDay, level.getTimeOfDay(), UndeadNights.serverState.getDaysCounter(), level.getTimeOfDay(), (level.getTimeOfDay() / 24000));
                }
            }

            // spawn a random horde and/or stray zombies for non-horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (UndeadNights.serverState.getTryToSpawnRandomHorde()) {
                    if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().isEnableRandomHordes() &&
                            (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                        randomValue = randomSource.nextBetween(1, 100);
                        if ((randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getChanceForRandomHorde()))) {
                            for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                                if (spawnHorde(level, player, randomSource) == SpawnHordeResult.FAILED) {
                                    break;
                                }
                                if (MainConfig.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("A random horde has spawned!");
                                }
                            }
                        }
                    }
                    UndeadNights.serverState.setTryToSpawnRandomHorde(false);
                }

                // spawn stray horde zombies
                if (MainConfig.getHordeZombiesSpawnNaturally() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                    if (UndeadNights.serverState.getTickCounter() > 0) {
                        UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                        return 0;
                    } else {
                        UndeadNights.serverState.setTickCounter(5 * 20);
                    }
                    if (!(randomSource.nextFloat() < 0.03F)) {
                        return 0;
                    }
                    for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                        BlockPos pos = SpawnLocationFinder.getBlockPosWithDistance(player.getBlockPos(), level, MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                        if (!SpawnLocationFinder.checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            SpawnProcess.spawnHordeMob(level, randomSource, pos, player, new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange(), "none", ""));
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return 0;
            }

            // this is the first tick of a new night
            if (UndeadNights.serverState.getNightIsStarting()) {
                randomValue = randomSource.nextBetween(1, 100);
                if (!(randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setIsNaturalSpawningOk(true);
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setFirstWaveHasSpawned(false);
                    for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendMessage(Text.translatable("message.undeadnights.horde_night").formatted(Formatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && (normalizedTimeOfDay >= 12542 || allDayLong)) {
                if (UndeadNights.serverState.getHordesCounter() != 0) {
                    if ((UndeadNights.serverState.getHordesCounter() - 1) == 0) {
                        return 0;
                    }
                }
                int playerWithHordes = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getNumberOfPlayersToGetHordePerHordeEvent();
                boolean allPlayersGetHordes = (playerWithHordes == 0);
                List<ServerPlayerEntity> players = level.getPlayers(LivingEntity::isAlive);
                Collections.shuffle(players);
                for (ServerPlayerEntity player : players) {
                    if (!allPlayersGetHordes) {
                        if (playerWithHordes == 0) {
                            break;
                        }
                        playerWithHordes--;
                    }
                    UndeadNights.serverState.entitiesWithPendingHorde.add(player.getUuid());
                    UndeadNights.serverState.entitiesWithPendingWave.add(player.getUuid());
                    UndeadNights.serverState.entitiesWithReceivedHorde.remove(player.getUuid());
                    UndeadNights.serverState.setFirstWaveHasSpawned(true);
                }
                UndeadNights.serverState.setHordesCounter(UndeadNights.serverState.getHordesCounter() - 1);

                if (!UndeadNights.serverState.getFirstWaveHasSpawned()) {
                    UndeadNights.serverState.setTickCounter(10 * 20);
                } else {
                    UndeadNights.serverState.setTickCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getCooldownBetweenWaves() * 20);
                    UndeadNights.serverState.setSpawnZombies(false);
                    UndeadNights.serverState.setRespawnZombies(true);
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Spawned waves for every player: DaysCounter: {} GlobalSpawnCounter: {} Spawn: {}, respawn: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter, UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
            }
        } else {
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                    if (!UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getAllDayLongHordeNights()) {
                        player.sendMessage(Text.translatable("message.undeadnights.horde_night_over"));
                    } else {
                        player.sendMessage(Text.translatable("message.undeadnights.horde_time_over"));
                    }
                    if (UndeadNights.serverState.entitiesWithPendingWave.contains(player.getUuid())) {
                        UndeadNights.serverState.entitiesWithPendingWave.remove(player.getUuid());
                        UndeadNights.serverState.entitiesWithPendingHorde.remove(player.getUuid());
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
                if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() != 0) {
                    UndeadNights.serverState.setHordesCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() + 1);
                } else {
                    UndeadNights.serverState.setHordesCounter(0);
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", level.getTimeOfDay(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }
            }

            if ((normalizedTimeOfDay >= 11500) && !level.getPlayers(LivingEntity::isAlive).isEmpty() && UndeadNights.automaticDifficultyProgressionActive) {
                boolean flag = false;
                if (UndeadNights.serverState.isPerformDifficultySwitchCheck()) {
                    flag = UndeadNights.difficultyConfig.checkForDifficultyLevelSwitch((int) (level.getTimeOfDay() / 24000L)+1, randomSource);
                    UndeadNights.serverState.setPerformDifficultySwitchCheck(false);
                }
                if ((flag || !UndeadNights.serverState.isFirstDifficultyLevelPrinted()) &&
                        (UndeadNights.difficultyConfig.getDifficultyLevels().size() > 1)) {
                    for (ServerPlayerEntity player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendMessage(Text.literal(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName()).formatted(Formatting.YELLOW));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Difficulty level set to {}", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName());
                    }
                    UndeadNights.serverState.setPossibleHordesIndex(-1);
                    UndeadNights.serverState.setFirstDifficultyLevelPrinted(true);
                }
            }

            UndeadNights.serverState.setHordeNight(false);
            UndeadNights.serverState.setSpawnZombies(true);
            UndeadNights.serverState.setRespawnZombies(false);
            UndeadNights.serverState.setTryToSpawnRandomHorde(true);
        }
        return 0;
    }
}
