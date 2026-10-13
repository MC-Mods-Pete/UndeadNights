package net.petemc.undeadnights.config.difficulty;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;
import net.petemc.undeadnights.UndeadNights;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DifficultyConfigLoader {
    private static final File DIFFICULTY_CONFIG_FILE = new File("config/undeadnights_difficulty_config.json");
    public static boolean configWasNewlyCreated = false;

    public static void loadConfig() {
        UndeadNights.LOGGER.info("Loading {} difficulty config", UndeadNights.MOD_ID);

         if (!DIFFICULTY_CONFIG_FILE.exists()) {
             var defaultConfObject = DifficultyConfig.createDefaultConfigJsonObject();
             try (FileWriter writer = new FileWriter(DIFFICULTY_CONFIG_FILE)) {
                 Gson gson = new GsonBuilder().setPrettyPrinting().create();
                 gson.toJson(defaultConfObject, writer);  // store objects in JSON
                 configWasNewlyCreated = true;
             } catch (IOException e) {
                 UndeadNights.LOGGER.error("Failed to create difficulty config file with exception: {}", e.getLocalizedMessage());
             }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileReader reader = new FileReader(DIFFICULTY_CONFIG_FILE)) {
            UndeadNights.difficultyConfig = gson.fromJson(reader, DifficultyConfig.class);

            if (UndeadNights.difficultyConfig.getDifficultyLevels() != null && !UndeadNights.difficultyConfig.getDifficultyLevels().isEmpty()) {
                UndeadNights.difficultyConfig.setCurrentDifficultyLevel(UndeadNights.difficultyConfig.getDifficultyLevels().get(0));
                UndeadNights.LOGGER.info("Setting current difficulty level to: {}", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName());
            }
        } catch (Exception e) {
            UndeadNights.LOGGER.error("Failed to read difficulty config with exception: {}", e.getLocalizedMessage());
            if (e instanceof MalformedJsonException) {
                UndeadNights.LOGGER.error("The difficulty config file is malformed.");
            }
            UndeadNights.LOGGER.warn("Default difficulty values will be used.");
        }
        checkCompletenessOfDifficultyConfig();
    }

    public static void updateDifficultyConfigVersionInFile(int newVersion) {
        if (!DIFFICULTY_CONFIG_FILE.exists()) {
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileReader reader = new FileReader(DIFFICULTY_CONFIG_FILE)) {
            JsonObject configJson = gson.fromJson(reader, JsonObject.class);
            if (configJson == null) {
                return;
            }

            if (configJson.has("internalConfigVersion")) {
                configJson.remove("internalConfigVersion");
            }

            configJson.addProperty("difficultyConfigVersion", newVersion);

            try (FileWriter writer = new FileWriter(DIFFICULTY_CONFIG_FILE)) {
                gson.toJson(configJson, writer);
            }
        } catch (Exception e) {
            UndeadNights.LOGGER.error("Failed to update difficulty config version with exception: {}", e.getLocalizedMessage());
        }
    }

    public static boolean checkAndFillDynamicScalingAndFirstLevelInConfigFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileReader reader = new FileReader(DIFFICULTY_CONFIG_FILE)) {
            JsonObject configJson = gson.fromJson(reader, JsonObject.class);
            if (configJson == null) {
                configJson = new JsonObject();
            }

            boolean changed = checkAndFillDynamicScalingAndFirstLevel(configJson);
            if (changed) {
                try (FileWriter writer = new FileWriter(DIFFICULTY_CONFIG_FILE)) {
                    gson.toJson(configJson, writer);
                }
            }
            return changed;
        } catch (Exception e) {
            UndeadNights.LOGGER.error("Failed to update difficulty config structure with exception: {}", e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Checks the raw JSON config and only inserts missing keys from defaults for
     * {@code dynamicScaling} and the first difficulty level (index 0).
     * Existing keys and values are never overwritten.
     *
     * @param configJson parsed difficulty config JSON
     * @return {@code true} when the JSON was changed (missing keys inserted)
     */
    public static boolean checkAndFillDynamicScalingAndFirstLevel(JsonObject configJson) {
        JsonObject defaultConfig = DifficultyConfig.createDefaultConfigJsonObject();
        boolean changed = false;

        JsonObject defaultDynamicScaling = defaultConfig.getAsJsonObject("dynamicScaling");
        if (!configJson.has("dynamicScaling") || !configJson.get("dynamicScaling").isJsonObject()) {
            configJson.add("dynamicScaling", defaultDynamicScaling.deepCopy());
            changed = true;
        } else {
            changed |= addMissingKeysRecursively(configJson.getAsJsonObject("dynamicScaling"), defaultDynamicScaling);
        }

        JsonArray defaultDifficultyLevels = defaultConfig.getAsJsonArray("difficultyLevels");
        if (!configJson.has("difficultyLevels") || !configJson.get("difficultyLevels").isJsonArray()) {
            configJson.add("difficultyLevels", defaultDifficultyLevels.deepCopy());
            return true;
        }

        JsonArray difficultyLevels = configJson.getAsJsonArray("difficultyLevels");
        JsonObject defaultFirstLevel = defaultDifficultyLevels.get(0).getAsJsonObject();
        if (difficultyLevels.isEmpty()) {
            difficultyLevels.add(defaultFirstLevel.deepCopy());
            return true;
        }

        JsonElement firstLevel = difficultyLevels.get(0);
        if (!firstLevel.isJsonObject()) {
            difficultyLevels.set(0, defaultFirstLevel.deepCopy());
            return true;
        }
        changed |= addMissingKeysRecursively(firstLevel.getAsJsonObject(), defaultFirstLevel);
        return changed;
    }

    private static boolean addMissingKeysRecursively(JsonObject target, JsonObject defaults) {
        boolean changed = false;
        for (String key : defaults.keySet()) {
            JsonElement defaultValue = defaults.get(key);
            if (!target.has(key)) {
                target.add(key, defaultValue.deepCopy());
                changed = true;
                continue;
            }
            JsonElement targetValue = target.get(key);
            if (targetValue != null && targetValue.isJsonObject() && defaultValue.isJsonObject()) {
                changed |= addMissingKeysRecursively(targetValue.getAsJsonObject(), defaultValue.getAsJsonObject());
            }
        }
        return changed;
    }

    public static void checkCompletenessOfDifficultyConfig() {
        if (UndeadNights.difficultyConfig == null) {
            UndeadNights.difficultyConfig = new DifficultyConfig(
                    DifficultyConfig.defaultDynamicScalingValues,
                    List.of(DifficultyConfig.defaultDifficultyLevelValues),
                    DifficultyConfig.defaultDifficultyLevelValues,
                    DifficultyConfig.getCurrentDifficultyConfigVersion()
            );
            UndeadNights.difficultyConfig.setCurrentDifficultyLevel(UndeadNights.difficultyConfig.getDifficultyLevels().get(0));
        }

        if (UndeadNights.difficultyConfig != null) {
            UndeadNights.LOGGER.info("Loaded difficulty config with {} difficulty levels.", UndeadNights.difficultyConfig.getDifficultyLevels().size());

            if (UndeadNights.difficultyConfig.getDynamicScaling() == null) {
                UndeadNights.difficultyConfig.setDynamicScaling(DifficultyConfig.defaultDynamicScalingValues);
            } else {
                DifficultySettingDynamicScaling dynamicScaling = UndeadNights.difficultyConfig.getDynamicScaling();
                dynamicScaling.setDynamicScalingEnabled(dynamicScaling.isDynamicScalingEnabled() == null ? DifficultyConfig.defaultDynamicScalingValues.isDynamicScalingEnabled() : dynamicScaling.isDynamicScalingEnabled());
                dynamicScaling.setHealthScalePerPlayer(dynamicScaling.getHealthScalePerPlayer() == null ? DifficultyConfig.defaultDynamicScalingValues.getHealthScalePerPlayer() : dynamicScaling.getHealthScalePerPlayer());
                dynamicScaling.setDamageScalePerPlayer(dynamicScaling.getDamageScalePerPlayer() == null ? DifficultyConfig.defaultDynamicScalingValues.getDamageScalePerPlayer() : dynamicScaling.getDamageScalePerPlayer());
                dynamicScaling.setSpeedScalePerPlayer(dynamicScaling.getSpeedScalePerPlayer() == null ? DifficultyConfig.defaultDynamicScalingValues.getSpeedScalePerPlayer() : dynamicScaling.getSpeedScalePerPlayer());
                dynamicScaling.setArmorScalePerPlayer(dynamicScaling.getArmorScalePerPlayer() == null ? DifficultyConfig.defaultDynamicScalingValues.getArmorScalePerPlayer() : dynamicScaling.getArmorScalePerPlayer());
                dynamicScaling.setHordeScalePerPlayer(dynamicScaling.getHordeScalePerPlayer() == null ? DifficultyConfig.defaultDynamicScalingValues.getHordeScalePerPlayer() : dynamicScaling.getHordeScalePerPlayer());
                dynamicScaling.setDaysBetweenScaleIncreases(dynamicScaling.getDaysBetweenScaleIncreases() == null ? DifficultyConfig.defaultDynamicScalingValues.getDaysBetweenScaleIncreases() : dynamicScaling.getDaysBetweenScaleIncreases());
                dynamicScaling.setDaysHealthScaleIncrease(dynamicScaling.getDaysHealthScaleIncrease() == null ? DifficultyConfig.defaultDynamicScalingValues.getDaysHealthScaleIncrease() : dynamicScaling.getDaysHealthScaleIncrease());
                dynamicScaling.setDaysDamageScaleIncrease(dynamicScaling.getDaysDamageScaleIncrease() == null ? DifficultyConfig.defaultDynamicScalingValues.getDaysDamageScaleIncrease() : dynamicScaling.getDaysDamageScaleIncrease());
                dynamicScaling.setDaysSpeedScaleIncrease(dynamicScaling.getDaysSpeedScaleIncrease() == null ? DifficultyConfig.defaultDynamicScalingValues.getDaysSpeedScaleIncrease() : dynamicScaling.getDaysSpeedScaleIncrease());
                dynamicScaling.setDaysArmorScaleIncrease(dynamicScaling.getDaysArmorScaleIncrease() == null ? DifficultyConfig.defaultDynamicScalingValues.getDaysArmorScaleIncrease() : dynamicScaling.getDaysArmorScaleIncrease());
                dynamicScaling.setDaysHordeScaleIncrease(dynamicScaling.getDaysHordeScaleIncrease() == null ? DifficultyConfig.defaultDynamicScalingValues.getDaysHordeScaleIncrease() : dynamicScaling.getDaysHordeScaleIncrease());
                dynamicScaling.setMaxHealthScale(dynamicScaling.getMaxHealthScale() == null ? DifficultyConfig.defaultDynamicScalingValues.getMaxHealthScale() : dynamicScaling.getMaxHealthScale());
                dynamicScaling.setMaxDamageScale(dynamicScaling.getMaxDamageScale() == null ? DifficultyConfig.defaultDynamicScalingValues.getMaxDamageScale() : dynamicScaling.getMaxDamageScale());
                dynamicScaling.setMaxSpeedScale(dynamicScaling.getMaxSpeedScale() == null ? DifficultyConfig.defaultDynamicScalingValues.getMaxSpeedScale() : dynamicScaling.getMaxSpeedScale());
                dynamicScaling.setMaxArmorScale(dynamicScaling.getMaxArmorScale() == null ? DifficultyConfig.defaultDynamicScalingValues.getMaxArmorScale() : dynamicScaling.getMaxArmorScale());
                dynamicScaling.setMaxHordeScale(dynamicScaling.getMaxHordeScale() == null ? DifficultyConfig.defaultDynamicScalingValues.getMaxHordeScale() : dynamicScaling.getMaxHordeScale());
            }

            if (UndeadNights.difficultyConfig.getDifficultyLevels().isEmpty()) {
                UndeadNights.LOGGER.warn("The difficulty config contains no difficulty levels. Please add at least one difficulty level.");
            } else { //if (UndeadNights.difficultyConfig.getDifficultyLevels().size() >= 1) {
                for (int i = 0; UndeadNights.difficultyConfig.getDifficultyLevels().size() > i; i++) {
                    DifficultyLevel sourceDifficultyLevel;
                    if (i == 0) {
                        sourceDifficultyLevel = DifficultyConfig.defaultDifficultyLevelValues;
                    } else {
                        sourceDifficultyLevel = UndeadNights.difficultyConfig.getDifficultyLevels().get(i - 1);
                    }
                    DifficultyLevel targetDifficultyLevel = UndeadNights.difficultyConfig.getDifficultyLevels().get(i);

                    targetDifficultyLevel.setDifficultyName(targetDifficultyLevel.getDifficultyName() == null ? "Level " + (i) + "..." : targetDifficultyLevel.getDifficultyName());
                    targetDifficultyLevel.setMinStartDay(targetDifficultyLevel.getMinStartDay() == null ? sourceDifficultyLevel.getMinStartDay() : targetDifficultyLevel.getMinStartDay());
                    targetDifficultyLevel.setMaxStartDay(targetDifficultyLevel.getMaxStartDay() == null ? sourceDifficultyLevel.getMaxStartDay() : targetDifficultyLevel.getMaxStartDay());
                    targetDifficultyLevel.setChanceForDifficultyLevelSwitch(targetDifficultyLevel.getChanceForDifficultyLevelSwitch() == null ? sourceDifficultyLevel.getChanceForDifficultyLevelSwitch() : targetDifficultyLevel.getChanceForDifficultyLevelSwitch());

                    if (targetDifficultyLevel.getDifficultySettingsHordeMobs() == null) {
                        targetDifficultyLevel.setDifficultySettingsHordeMobs(sourceDifficultyLevel.getDifficultySettingsHordeMobs());
                    } else {
                        DifficultySettingsHordeMobs settingsHordeMobs = targetDifficultyLevel.getDifficultySettingsHordeMobs();
                        DifficultySettingsHordeMobs lowerSettingsHordeMobs = sourceDifficultyLevel.getDifficultySettingsHordeMobs();
                        settingsHordeMobs.setUpdateHordeMobAttributes(settingsHordeMobs.isUpdateHordeMobAttributes() == null ? lowerSettingsHordeMobs.isUpdateHordeMobAttributes() : settingsHordeMobs.isUpdateHordeMobAttributes());
                        settingsHordeMobs.setUpdateAttributesOfThirdPartyMobs(settingsHordeMobs.isUpdateAttributesOfThirdPartyMobs() == null ? lowerSettingsHordeMobs.isUpdateAttributesOfThirdPartyMobs() : settingsHordeMobs.isUpdateAttributesOfThirdPartyMobs());
                        settingsHordeMobs.setHealthAttributeScaleFactor(settingsHordeMobs.getHealthAttributeScaleFactor() == null ? lowerSettingsHordeMobs.getHealthAttributeScaleFactor() : settingsHordeMobs.getHealthAttributeScaleFactor());
                        settingsHordeMobs.setDamageAttributeScaleFactor(settingsHordeMobs.getDamageAttributeScaleFactor() == null ? lowerSettingsHordeMobs.getDamageAttributeScaleFactor() : settingsHordeMobs.getDamageAttributeScaleFactor());
                        settingsHordeMobs.setSpeedAttributeScaleFactor(settingsHordeMobs.getSpeedAttributeScaleFactor() == null ? lowerSettingsHordeMobs.getSpeedAttributeScaleFactor() : settingsHordeMobs.getSpeedAttributeScaleFactor());
                        settingsHordeMobs.setArmorAttributeScaleFactor(settingsHordeMobs.getArmorAttributeScaleFactor() == null ? lowerSettingsHordeMobs.getArmorAttributeScaleFactor() : settingsHordeMobs.getArmorAttributeScaleFactor());
                        settingsHordeMobs.setHordeMobsTrackingRange(settingsHordeMobs.getHordeMobsTrackingRange() == null ? lowerSettingsHordeMobs.getHordeMobsTrackingRange() : settingsHordeMobs.getHordeMobsTrackingRange());
                        settingsHordeMobs.setHordeZombiesBurnInTheSun(settingsHordeMobs.isHordeZombiesBurnInTheSun() == null ? lowerSettingsHordeMobs.isHordeZombiesBurnInTheSun() : settingsHordeMobs.isHordeZombiesBurnInTheSun());
                        settingsHordeMobs.setVanillaZombiesBurnInTheSun(settingsHordeMobs.isVanillaZombiesBurnInTheSun() == null ? lowerSettingsHordeMobs.isVanillaZombiesBurnInTheSun() : settingsHordeMobs.isVanillaZombiesBurnInTheSun());
                        settingsHordeMobs.setHordeZombiesAreFasterOnWater(settingsHordeMobs.isHordeZombiesAreFasterOnWater() == null ? lowerSettingsHordeMobs.isHordeZombiesAreFasterOnWater() : settingsHordeMobs.isHordeZombiesAreFasterOnWater());
                        settingsHordeMobs.setHordeMobsCanClimbEachOther(settingsHordeMobs.isHordeMobsCanClimbEachOther() == null ? lowerSettingsHordeMobs.isHordeMobsCanClimbEachOther() : settingsHordeMobs.isHordeMobsCanClimbEachOther());
                        settingsHordeMobs.setBlockBreakingTier(settingsHordeMobs.getBlockBreakingTier() == null ? lowerSettingsHordeMobs.getBlockBreakingTier() : settingsHordeMobs.getBlockBreakingTier());
                        settingsHordeMobs.setBlockBreaking(settingsHordeMobs.isBlockBreaking() == null ? lowerSettingsHordeMobs.isBlockBreaking() : settingsHordeMobs.isBlockBreaking());
                    }
                    if (targetDifficultyLevel.getDifficultySettingsHordeNights() == null) {
                        targetDifficultyLevel.setDifficultySettingsHordeNights(sourceDifficultyLevel.getDifficultySettingsHordeNights());
                    } else {
                        DifficultySettingsHordeNights settingsHordeNights = targetDifficultyLevel.getDifficultySettingsHordeNights();
                        DifficultySettingsHordeNights lowerSettingsHordeNights = sourceDifficultyLevel.getDifficultySettingsHordeNights();
                        settingsHordeNights.setDaysBetweenHordeNights(settingsHordeNights.getDaysBetweenHordeNights() == null ? lowerSettingsHordeNights.getDaysBetweenHordeNights() : settingsHordeNights.getDaysBetweenHordeNights());
                        settingsHordeNights.setChanceForHordeNight(settingsHordeNights.getChanceForHordeNight() == null ? lowerSettingsHordeNights.getChanceForHordeNight() : settingsHordeNights.getChanceForHordeNight());
                        settingsHordeNights.setSpawnAdditionalWaves(settingsHordeNights.isSpawnAdditionalWaves() == null ? lowerSettingsHordeNights.isSpawnAdditionalWaves() : settingsHordeNights.isSpawnAdditionalWaves());
                        settingsHordeNights.setRequirePreviousWaveClear(settingsHordeNights.isRequirePreviousWaveClear() == null ? lowerSettingsHordeNights.isRequirePreviousWaveClear() : settingsHordeNights.isRequirePreviousWaveClear());
                        settingsHordeNights.setCooldownBetweenWaves(settingsHordeNights.getCooldownBetweenWaves() == null ? lowerSettingsHordeNights.getCooldownBetweenWaves() : settingsHordeNights.getCooldownBetweenWaves());
                        settingsHordeNights.setChanceForAdditionalWave(settingsHordeNights.getChanceForAdditionalWave() == null ? lowerSettingsHordeNights.getChanceForAdditionalWave() : settingsHordeNights.getChanceForAdditionalWave());
                        settingsHordeNights.setAllDayLongHordeNights(settingsHordeNights.getAllDayLongHordeNights() == null ? lowerSettingsHordeNights.getAllDayLongHordeNights() : settingsHordeNights.getAllDayLongHordeNights());
                        settingsHordeNights.setMaxHordesPerHordeNight(settingsHordeNights.getMaxHordesPerHordeNight() == null ? lowerSettingsHordeNights.getMaxHordesPerHordeNight() : settingsHordeNights.getMaxHordesPerHordeNight());
                        settingsHordeNights.setNumberOfPlayersToGetHordePerHordeEvent(settingsHordeNights.getNumberOfPlayersToGetHordePerHordeEvent() == null ? lowerSettingsHordeNights.getNumberOfPlayersToGetHordePerHordeEvent() : settingsHordeNights.getNumberOfPlayersToGetHordePerHordeEvent());
                    }
                    if (targetDifficultyLevel.getDifficultySettingsHordes() == null) {
                        targetDifficultyLevel.setDifficultySettingsHordes(sourceDifficultyLevel.getDifficultySettingsHordes());
                    } else {
                        DifficultySettingsHordes settingsHordes = targetDifficultyLevel.getDifficultySettingsHordes();
                        DifficultySettingsHordes lowerSettingsHordes = sourceDifficultyLevel.getDifficultySettingsHordes();
                        settingsHordes.setEnableRandomHordes(settingsHordes.isEnableRandomHordes() == null ? lowerSettingsHordes.isEnableRandomHordes() : settingsHordes.isEnableRandomHordes());
                        settingsHordes.setChanceForRandomHorde(settingsHordes.getChanceForRandomHorde() == null ? lowerSettingsHordes.getChanceForRandomHorde() : settingsHordes.getChanceForRandomHorde());
                        settingsHordes.setHordeSizeScaleFactor(settingsHordes.getHordeSizeScaleFactor() == null ? lowerSettingsHordes.getHordeSizeScaleFactor() : settingsHordes.getHordeSizeScaleFactor());
                        settingsHordes.setHordeSelectionMode(settingsHordes.getHordeSelectionMode() == null ? lowerSettingsHordes.getHordeSelectionMode() : settingsHordes.getHordeSelectionMode());
                        settingsHordes.setListOfPossibleHordes(settingsHordes.getListOfPossibleHordes() == null ? lowerSettingsHordes.getListOfPossibleHordes() : settingsHordes.getListOfPossibleHordes());
                        settingsHordes.setBossHordeEnabled(settingsHordes.getBossHordeEnabled() == null ? lowerSettingsHordes.getBossHordeEnabled() : settingsHordes.getBossHordeEnabled());
                        settingsHordes.setBossHordeId(settingsHordes.getBossHordeId() == null ? lowerSettingsHordes.getBossHordeId() : settingsHordes.getBossHordeId());
                    }
                    if (targetDifficultyLevel.getDifficultySettingsLureEffect() == null) {
                        targetDifficultyLevel.setDifficultySettingsLureEffect(sourceDifficultyLevel.getDifficultySettingsLureEffect());
                    } else {
                        DifficultySettingsLureEffect settingsLureEffect = targetDifficultyLevel.getDifficultySettingsLureEffect();
                        DifficultySettingsLureEffect lowerSettingsLureEffect = sourceDifficultyLevel.getDifficultySettingsLureEffect();
                        settingsLureEffect.setEnableLureHordeEffect(settingsLureEffect.isEnableLureHordeEffect() == null ? lowerSettingsLureEffect.isEnableLureHordeEffect() : settingsLureEffect.isEnableLureHordeEffect());
                        settingsLureEffect.setNonHordeZombiesCanCauseLureHordeEffect(settingsLureEffect.isNonHordeZombiesCanCauseLureHordeEffect() == null ? lowerSettingsLureEffect.isNonHordeZombiesCanCauseLureHordeEffect() : settingsLureEffect.isNonHordeZombiesCanCauseLureHordeEffect());
                        settingsLureEffect.setChanceForLureHordeEffect(settingsLureEffect.getChanceForLureHordeEffect() == null ? lowerSettingsLureEffect.getChanceForLureHordeEffect() : settingsLureEffect.getChanceForLureHordeEffect());
                        settingsLureEffect.setDurationForLureHordeEffect(settingsLureEffect.getDurationForLureHordeEffect() == null ? lowerSettingsLureEffect.getDurationForLureHordeEffect() : settingsLureEffect.getDurationForLureHordeEffect());
                        settingsLureEffect.setLureHordeEffectSpawnsHorde(settingsLureEffect.isLureHordeEffectSpawnsHorde() == null ? lowerSettingsLureEffect.isLureHordeEffectSpawnsHorde() : settingsLureEffect.isLureHordeEffectSpawnsHorde());
                        settingsLureEffect.setChanceForLureEffectToSpawnHorde(settingsLureEffect.getChanceForLureEffectToSpawnHorde() == null ? lowerSettingsLureEffect.getChanceForLureEffectToSpawnHorde() : settingsLureEffect.getChanceForLureEffectToSpawnHorde());
                        settingsLureEffect.setStrongLureHordeEffectSpawnsHorde(settingsLureEffect.isStrongLureHordeEffectSpawnsHorde() == null ? lowerSettingsLureEffect.isStrongLureHordeEffectSpawnsHorde() : settingsLureEffect.isStrongLureHordeEffectSpawnsHorde());
                    }
                }
            }
        }
    }
}
