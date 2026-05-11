package net.petemc.undeadnights.world.spawner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.util.RandomExtention;
import net.petemc.undeadnights.util.SpawnLocationFinder;
import net.petemc.undeadnights.util.SpawnProcess;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HordeSpawner implements CustomSpawner {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeIdFromHordesConfig = 1;

    public HashMap<UUID, HordeSpawnTask> hordeSpawningPerPlayer = new HashMap<>();

    public static int bossHordeTime;

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

    public SpawnHordeResult spawnHorde(ServerLevel level, ServerPlayer player, RandomExtention randomSource) {
        if (MainConfig.getEnableAsynchronousHordeSpawning()) {
            if (!hordeSpawningPerPlayer.containsKey(player.getUUID())) {
                hordeSpawningPerPlayer.put(player.getUUID(),
                        new HordeSpawnTask(SpawnProcess.asynchronousHordeSpawner(level, player, randomSource), 10));
                if (MainConfig.getPrintDebugMessages()) {
                    player.sendSystemMessage(Component.literal("[DEBUG] Finding horde spawn location (async)...").withStyle(ChatFormatting.DARK_AQUA));
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Async horde spawning location calculation for player {} at {}", player.getName().getString(), player.blockPosition());
                }
            }
            HordeSpawnTask existingHordeSpawnTask = hordeSpawningPerPlayer.get(player.getUUID());
            CompletableFuture<SpawnHordeResult> existingFutureSpawnHorde = existingHordeSpawnTask.futureResult;
            if (existingFutureSpawnHorde != null && existingFutureSpawnHorde.isDone()) {
                SpawnHordeResult result = SpawnHordeResult.FAILED;
                try {
                    result = existingFutureSpawnHorde.get();
                } catch (Exception e) {
                    UndeadNights.LOGGER.warn("Spawning horde for player {} failed!", player.getName().getString());
                }
                if (result == SpawnHordeResult.DONE) {
                    hordeSpawningPerPlayer.remove(player.getUUID());
                    return result;
                }
                if (existingHordeSpawnTask.tries > 0) {
                    // still not done, skip this spawn attempt
                    existingHordeSpawnTask.tries--;
                    return SpawnHordeResult.NOT_DONE_YET;
                } else {
                    // exceeded max tries, consider this a failed attempt
                    hordeSpawningPerPlayer.remove(player.getUUID());
                    return SpawnHordeResult.FAILED;
                }
            }
        } else {
            return SpawnProcess.synchronousHordeSpawner(level, player, randomSource);
        }
        return SpawnHordeResult.NOT_DONE_YET;
    }


    @Override
    public void tick(@NotNull ServerLevel level, boolean spawnEnemies) {
        // check if Horde Nights is enabled
        if (level.isClientSide()) {
            return;
        }

        // is the mod enabled?
        if (!MainConfig.getUndeadNightsEnabled()) {
            return;
        }

        // is mob spawning enabled?
        if (!spawnEnemies && !MainConfig.getIgnoreDoMobSpawningGamerule()) {
            return;
        }

        // Are we in the Overworld?
        if (!(level.dimension() == Level.OVERWORLD)) {
            return;
        }

        // Check if the SaveState is already initialized
        if (UndeadNights.serverState == null) {
            return;
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = level.getOverworldClockTime() - ((level.getOverworldClockTime() / 24000L) * 24000);
        if (UndeadNights.serverState.getPrevNormalizedTimeOfDay() == normalizedTimeOfDay) {
            return;
        }
        UndeadNights.serverState.setNightIsStarting((UndeadNights.serverState.getPrevNormalizedTimeOfDay() <  12000L) && (normalizedTimeOfDay >= 12000L));
        UndeadNights.serverState.setPrevNormalizedTimeOfDay(normalizedTimeOfDay);

        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;
        boolean allDayLong = (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getAllDayLongHordeNights() && UndeadNights.serverState.getHordeNight() && (normalizedTimeOfDay >= 22500 || normalizedTimeOfDay < 11000));

        // create a random source
        final RandomExtention randomSource = new RandomExtention();
        int randomValue = 0;

        boolean withinBossHordeTime = false;
        if (UndeadNights.serverState.getHordeNight()) {
            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getAllDayLongHordeNights()) {
                withinBossHordeTime = (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getBossHordeEnabled() &&
                        (normalizedTimeOfDay >= 9800 && normalizedTimeOfDay < 11000));
            } else {
                withinBossHordeTime = (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getBossHordeEnabled() &&
                        (normalizedTimeOfDay >= 21300 && normalizedTimeOfDay < 22500));
            }
            if ((withinBossHordeTime) && !UndeadNights.serverState.isSpawnBossHorde()) {
                if (bossHordeTime <= normalizedTimeOfDay) {
                    UndeadNights.serverState.setSpawnBossHorde(true);
                    List<ServerPlayer> players = level.getPlayers(LivingEntity::isAlive);
                    if (!players.isEmpty()) {
                        Collections.shuffle(players);
                        randomValue = randomSource.nextInt(players.size());
                        ServerPlayer player = players.get(randomValue);

                        UndeadNights.serverState.entitiesWithPendingHorde.put(player.getUUID(), player.getUUID().toString());
                        UndeadNights.serverState.entitiesWithPendingWave.put(player.getUUID(), player.getUUID().toString());
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(player.getUUID());
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Boss Horde Wave is spawning, NormalizedTimeOfDay: {}, BossHordeTime: {}", normalizedTimeOfDay, bossHordeTime);
                        }
                    }
                }
            }
        }

        // process pending horde spawns per player
        if (!UndeadNights.serverState.entitiesWithPendingHorde.isEmpty()) {
            for (var playerUUID : UndeadNights.serverState.entitiesWithPendingHorde.values().stream().toList()) {
                if (!UndeadNights.serverState.entitiesWithReceivedHorde.containsKey(UUID.fromString(playerUUID))) {
                    Entity entity = level.getEntity(UUID.fromString(playerUUID));
                    if (entity instanceof ServerPlayer serverPlayer) {
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
                            UndeadNights.serverState.entitiesWithPendingHorde.remove(serverPlayer.getUUID());
                            UndeadNights.serverState.entitiesWithReceivedHorde.put(UUID.fromString(playerUUID), playerUUID);
                            if (result == SpawnHordeResult.FAILED) {
                                UndeadNights.LOGGER.info("Spawning horde for player {} failed.", serverPlayer.getName().getString());
                                if (SpawnHordeCommand.spawnHordeByCommand) {
                                    serverPlayer.sendSystemMessage(Component.translatable("message.undeadnights.command_spawn_horde_failed"));
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
            for (var playerUUID : UndeadNights.serverState.entitiesWithReceivedHorde.values().stream().toList()) {
                Entity entity = level.getEntity(UUID.fromString(playerUUID));
                if (entity instanceof ServerPlayer serverPlayer) {
                    if (!serverPlayer.hasEffect(ModEffects.LURE_HORDE.getHolder().get()) &&
                            !serverPlayer.hasEffect(ModEffects.STRONG_LURE_HORDE.getHolder().get())) {
                        UndeadNights.serverState.entitiesWithReceivedHorde.remove(serverPlayer.getUUID());
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
                if ((UndeadNights.serverState.getTickCounter() > 0) && !UndeadNights.serverState.isSpawnBossHorde()) {
                    UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                    return;
                } else {
                    UndeadNights.serverState.setTickCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getCooldownBetweenWaves() * 20);
                }

                if (!UndeadNights.serverState.isSpawnBossHorde() && !withinBossHordeTime) {
                    randomValue = randomSource.nextIntBetweenInclusive(1, 100);
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
                        return;
                    }
                }
            }

            // if a new night just started, count down the days
            if ((UndeadNights.serverState.getNightIsStarting() && (UndeadNights.serverState.getDaysCounter() >= 1))) {
                if (UndeadNights.serverState.getGracePeriod() > 0) {
                    UndeadNights.serverState.setGracePeriod(UndeadNights.serverState.getGracePeriod() - 1);
                    if (MainConfig.getSendHordeNightsCountdownMessage()) {
                        for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.days_of_grace_remaining", String.valueOf(UndeadNights.serverState.getGracePeriod())));
                        }
                    }
                    if (UndeadNights.serverState.getGracePeriod() == 0) {
                        UndeadNights.serverState.setDaysCounter(1);
                    } else {
                        return;
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (MainConfig.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.nights_remaining", String.valueOf(UndeadNights.serverState.getDaysCounter())));
                        } else {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.last_nights"));
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {}, DaysCounter: {}, GameTime: {}, GameTimeDays: {}", normalizedTimeOfDay, level.getOverworldClockTime(), UndeadNights.serverState.getDaysCounter(), level.getGameTime(), (level.getGameTime() / 24000));
                }
            }

            // spawn a random horde and/or stray zombies for non-horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight() && !UndeadNights.serverState.isSpawnBossHorde()) {
                if (UndeadNights.serverState.getTryToSpawnRandomHorde()) {
                    if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().isEnableRandomHordes() &&
                            (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                        randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                        if ((randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordes().getChanceForRandomHorde()))) {
                            for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
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
                if ((MainConfig.getHordeZombiesSpawnNaturally() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) && !UndeadNights.serverState.isSpawnBossHorde()) {
                    if (UndeadNights.serverState.getTickCounter() > 0) {
                        UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                        return;
                    } else {
                        UndeadNights.serverState.setTickCounter(5 * 20);
                    }
                    if (!(randomSource.nextFloat() < 0.03F)) {
                        return;
                    }
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        BlockPos pos = SpawnLocationFinder.getBlockPosWithDistance(player.blockPosition(), level, MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                        if (!SpawnLocationFinder.checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return;
                        } else {
                            SpawnProcess.spawnHordeMob(level, randomSource, pos, player, new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange(), "none", ""));
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return;
            }

            // this is the first tick of a new night
            if (UndeadNights.serverState.getNightIsStarting()) {
                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (!(randomValue > (100 - UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getChanceForHordeNight()))) {
                    return;
                } else {
                    UndeadNights.serverState.setIsNaturalSpawningOk(true);
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setFirstWaveHasSpawned(false);
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendSystemMessage(Component.translatable("message.undeadnights.horde_night").withStyle(ChatFormatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && (normalizedTimeOfDay >= 12542 || allDayLong)) {
                if ((UndeadNights.serverState.getHordesCounter() != 0) && !UndeadNights.serverState.isSpawnBossHorde()) {
                    if ((UndeadNights.serverState.getHordesCounter() - 1) == 0) {
                        return;
                    }
                }

                if (UndeadNights.serverState.isSpawnBossHorde()) {
                    return;
                }

                int playerWithHordes = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getNumberOfPlayersToGetHordePerHordeEvent();
                boolean allPlayersGetHordes = (playerWithHordes == 0);
                List<ServerPlayer> players = level.getPlayers(LivingEntity::isAlive);
                Collections.shuffle(players);
                for (ServerPlayer player : players) {
                    if (!allPlayersGetHordes) {
                        if (playerWithHordes == 0) {
                            break;
                        }
                        playerWithHordes--;
                    }
                    UndeadNights.serverState.entitiesWithPendingHorde.put(player.getUUID(), player.getUUID().toString());
                    UndeadNights.serverState.entitiesWithPendingWave.put(player.getUUID(), player.getUUID().toString());
                    UndeadNights.serverState.entitiesWithReceivedHorde.remove(player.getUUID());
                    UndeadNights.serverState.setFirstWaveHasSpawned(true);
                    //bossHordeSpawned = true;
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
        } else { // it's day time
            // if it was a horde night, reset the counters and notify players
            //bossHordeSpawned = false;
            UndeadNights.serverState.setSpawnBossHorde(false);
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    if (!UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getAllDayLongHordeNights()) {
                        player.sendSystemMessage(Component.translatable("message.undeadnights.horde_night_over"));
                    } else {
                        player.sendSystemMessage(Component.translatable("message.undeadnights.horde_time_over"));
                    }
                    if (UndeadNights.serverState.entitiesWithPendingWave.containsKey(player.getUUID())) {
                        UndeadNights.serverState.entitiesWithPendingWave.remove(player.getUUID());
                        UndeadNights.serverState.entitiesWithPendingHorde.remove(player.getUUID());
                    }
                }
                UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
                if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() != 0) {
                    UndeadNights.serverState.setHordesCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() + 1);
                } else {
                    UndeadNights.serverState.setHordesCounter(0);
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", level.getOverworldClockTime(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }


                if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getAllDayLongHordeNights()) {
                    randomValue = randomSource.nextIntBetweenInclusive(10000, 10500); // default 10300
                } else {
                    randomValue = randomSource.nextIntBetweenInclusive(21500, 22000); // default 21800
                }
                HordeSpawner.bossHordeTime = randomValue;
            }

            if ((normalizedTimeOfDay >= 11500) && !level.getPlayers(LivingEntity::isAlive).isEmpty() && UndeadNights.automaticDifficultyProgressionActive) {
                boolean flag = false;
                if (UndeadNights.serverState.isPerformDifficultySwitchCheck()) {
                    flag = UndeadNights.difficultyConfig.checkForDifficultyLevelSwitch((int) (level.getOverworldClockTime() / 24000L)+1, randomSource);
                    UndeadNights.serverState.setPerformDifficultySwitchCheck(false);
                }
                if ((flag || !UndeadNights.serverState.isFirstDifficultyLevelPrinted()) &&
                        (UndeadNights.difficultyConfig.getDifficultyLevels().size() > 1)) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendSystemMessage(Component.literal(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName()).withStyle(ChatFormatting.YELLOW));
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
        return;
    }
}
