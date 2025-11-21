package net.petemc.undeadnights.config.difficulty;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.RandomSource;
import net.petemc.undeadnights.UndeadNights;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DifficultyConfig {

    private DifficultySettingDynamicScaling dynamicScaling = new DifficultySettingDynamicScaling();
    private List<DifficultyLevel> difficultyLevels;
    private DifficultyLevel currentDifficultyLevel;

    public DifficultyConfig() {
        // Empty constructor
    }

    public DifficultyConfig(DifficultySettingDynamicScaling dynamicScaling, List<DifficultyLevel> difficultyLevels, DifficultyLevel currentDifficultyLevel, int internalConfigVersion) {
        this.dynamicScaling = dynamicScaling;
        this.difficultyLevels = difficultyLevels;
        this.currentDifficultyLevel = currentDifficultyLevel;
        this.internalConfigVersion = internalConfigVersion;
    }

    public static final DifficultySettingDynamicScaling defaultDynamicScalingValues =
            new DifficultySettingDynamicScaling(
                    false,
                    0.05,
                    0.05,
                    0.0,
                    0.0,
                    10.0,
                    0.05,
                    0.05,
                    0.0,
                    0.0,
                    2.0,
                    2.0,
                    2.0,
                    2.0);

    public static final DifficultyLevel defaultDifficultyLevelValues =
            new DifficultyLevel(
                    "Level 1",
                    0,
                    0,
                    100,
                    new DifficultySettingsHordeMobs(
                            false,
                            false,
                            1.0,
                            1.0,
                            1.0,
                            1.0,
                            false,
                            true,
                            false,
                            true,
                            false,
                            1),
                    new DifficultySettingsHordeNights(
                            5,
                            100,
                            true,
                            45,
                            7,
                            false,
                            0),
                    new DifficultySettingsHordes(
                            false,
                            15,
                            List.of(1),
                            1.0,
                            "sequential"),
                    new DifficultySettingsLureEffect(
                            true,
                            true,
                            0.07,
                            60,
                            true,
                            0.2));

    private int internalConfigVersion;

    // Getter and setter functions
    public List<DifficultyLevel> getDifficultyLevels() { return difficultyLevels; }
    public void setDifficultyLevels(List<DifficultyLevel> difficultyLevels) { this.difficultyLevels = difficultyLevels; }

    public DifficultyLevel getCurrentDifficultyLevel() {
        return currentDifficultyLevel;
    }
    public void setCurrentDifficultyLevel(DifficultyLevel currentDifficultyLevel) { this.currentDifficultyLevel = currentDifficultyLevel; }

    public DifficultySettingDynamicScaling getDynamicScaling() { return dynamicScaling; }
    public void setDynamicScaling(DifficultySettingDynamicScaling dynamicScaling) { this.dynamicScaling = dynamicScaling; }


    public boolean checkForDifficultyLevelSwitch(int currentDay, RandomSource randomSource) {
        UndeadNights.serverState.setCurrentDayScaleCounter(UndeadNights.serverState.getCurrentDayScaleCounter()+1);
        if (UndeadNights.serverState.getCurrentDayScaleCounter() == UndeadNights.difficultyConfig.getDynamicScaling().getDaysBetweenScaleIncreases()) {
            UndeadNights.serverState.setCurrentDayScaleCounter(0);
            UndeadNights.serverState.setCurrentHealthScale(UndeadNights.serverState.getCurrentHealthScale() + UndeadNights.difficultyConfig.getDynamicScaling().getDaysHealthScaleIncrease());
            UndeadNights.serverState.setCurrentDamageScale(UndeadNights.serverState.getCurrentDamageScale() + UndeadNights.difficultyConfig.getDynamicScaling().getDaysDamageScaleIncrease());
            UndeadNights.serverState.setCurrentSpeedScale(UndeadNights.serverState.getCurrentSpeedScale() + UndeadNights.difficultyConfig.getDynamicScaling().getDaysSpeedScaleIncrease());
            UndeadNights.serverState.setCurrentArmorScale(UndeadNights.serverState.getCurrentArmorScale() + UndeadNights.difficultyConfig.getDynamicScaling().getDaysArmorScaleIncrease());
            if (UndeadNights.serverState.getCurrentHealthScale() > UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale()) {
                UndeadNights.serverState.setCurrentHealthScale(UndeadNights.difficultyConfig.getDynamicScaling().getMaxHealthScale());
            }
            if (UndeadNights.serverState.getCurrentDamageScale() > UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale()) {
                UndeadNights.serverState.setCurrentDamageScale(UndeadNights.difficultyConfig.getDynamicScaling().getMaxDamageScale());
            }
            if (UndeadNights.serverState.getCurrentSpeedScale() > UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale()) {
                UndeadNights.serverState.setCurrentSpeedScale(UndeadNights.difficultyConfig.getDynamicScaling().getMaxSpeedScale());
            }
            if (UndeadNights.serverState.getCurrentArmorScale() > UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale()) {
                UndeadNights.serverState.setCurrentArmorScale(UndeadNights.difficultyConfig.getDynamicScaling().getMaxArmorScale());
            }
        }

        if (UndeadNights.serverState.getCurrentDifficultyLevelIndex()+1 < UndeadNights.difficultyConfig.getDifficultyLevels().size()) {
            DifficultyLevel nextDifficultyLevel = UndeadNights.difficultyConfig.getDifficultyLevels().get(UndeadNights.serverState.getCurrentDifficultyLevelIndex()+1);
            if (currentDay >= nextDifficultyLevel.getMaxStartDay()) {
                UndeadNights.difficultyConfig.setCurrentDifficultyLevel(nextDifficultyLevel);
                UndeadNights.serverState.setCurrentDifficultyLevelIndex(UndeadNights.serverState.getCurrentDifficultyLevelIndex()+1);
                return true;
            }
            if ((currentDay >= nextDifficultyLevel.getMinStartDay()) &&
                    (currentDay < nextDifficultyLevel.getMaxStartDay())) {
                int randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (randomValue > (100 - nextDifficultyLevel.getChanceForDifficultyLevelSwitch())) {
                    UndeadNights.difficultyConfig.setCurrentDifficultyLevel(nextDifficultyLevel);
                    UndeadNights.serverState.setCurrentDifficultyLevelIndex(UndeadNights.serverState.getCurrentDifficultyLevelIndex()+1);
                    UndeadNights.LOGGER.info("Switching to new difficulty level: {}", nextDifficultyLevel.getDifficultyName());
                    return true;
                }
            }
        }
        return false;
    }

    public static @NotNull JsonObject createDefaultConfigJsonObject() {
        var difficultyLevelArray = new JsonArray();
        var listOfPossibleHordes = new JsonArray();
        JsonObject difficultySettingsHordeMobs;
        JsonObject difficultySettingsHordeNights;
        JsonObject difficultySettingsHordes;
        JsonObject difficultySettingsLureEffect;
        JsonObject difficultySettingsDynamicScaling;

        JsonObject difficultyLevel = new JsonObject();
        difficultyLevel.addProperty("difficultyName", defaultDifficultyLevelValues.getDifficultyName());
        difficultyLevel.addProperty("minStartDay", defaultDifficultyLevelValues.getMinStartDay());
        difficultyLevel.addProperty("maxStartDay", defaultDifficultyLevelValues.getMaxStartDay());
        difficultyLevel.addProperty("chanceForDifficultyLevelSwitch", defaultDifficultyLevelValues.getChanceForDifficultyLevelSwitch());

        DifficultySettingsHordeNights hordeNightsValues = defaultDifficultyLevelValues.getDifficultySettingsHordeNights();
        difficultySettingsHordeNights = new JsonObject();
        difficultySettingsHordeNights.addProperty("daysBetweenHordeNights", hordeNightsValues.getDaysBetweenHordeNights());
        difficultySettingsHordeNights.addProperty("chanceForHordeNight", hordeNightsValues.getChanceForHordeNight());
        difficultySettingsHordeNights.addProperty("spawnAdditionalWaves", hordeNightsValues.isSpawnAdditionalWaves());
        difficultySettingsHordeNights.addProperty("cooldownBetweenWaves", hordeNightsValues.getCooldownBetweenWaves());
        difficultySettingsHordeNights.addProperty("chanceForAdditionalWave", hordeNightsValues.getChanceForAdditionalWave());
        difficultySettingsHordeNights.addProperty("allDayLongHordeNights", hordeNightsValues.getAllDayLongHordeNights());
        difficultySettingsHordeNights.addProperty("maxHordesPerHordeNight", hordeNightsValues.getMaxHordesPerHordeNight());
        difficultyLevel.add("difficultySettingsHordeNights", difficultySettingsHordeNights);

        DifficultySettingsHordeMobs hordeMobsValues = defaultDifficultyLevelValues.getDifficultySettingsHordeMobs();
        difficultySettingsHordeMobs = new JsonObject();
        difficultySettingsHordeMobs.addProperty("updateHordeMobAttributes", hordeMobsValues.isUpdateHordeMobAttributes());
        difficultySettingsHordeMobs.addProperty("updateAttributesOfThirdPartyMobs", hordeMobsValues.isUpdateAttributesOfThirdPartyMobs());
        difficultySettingsHordeMobs.addProperty("healthAttributeScaleFactor", hordeMobsValues.getHealthAttributeScaleFactor());
        difficultySettingsHordeMobs.addProperty("damageAttributeScaleFactor", hordeMobsValues.getDamageAttributeScaleFactor());
        difficultySettingsHordeMobs.addProperty("speedAttributeScaleFactor", hordeMobsValues.getSpeedAttributeScaleFactor());
        difficultySettingsHordeMobs.addProperty("armorAttributeScaleFactor", hordeMobsValues.getArmorAttributeScaleFactor());
        difficultySettingsHordeMobs.addProperty("hordeZombiesBurnInTheSun", hordeMobsValues.isHordeZombiesBurnInTheSun());
        difficultySettingsHordeMobs.addProperty("vanillaZombiesBurnInTheSun", hordeMobsValues.isVanillaZombiesBurnInTheSun());
        difficultySettingsHordeMobs.addProperty("hordeZombiesAreFasterOnWater", hordeMobsValues.isHordeZombiesAreFasterOnWater());
        difficultySettingsHordeMobs.addProperty("hordeMobsCanClimbEachOther", hordeMobsValues.isHordeMobsCanClimbEachOther());
        difficultySettingsHordeMobs.addProperty("blockBreaking", hordeMobsValues.isBlockBreaking());
        difficultySettingsHordeMobs.addProperty("blockBreakingTier", hordeMobsValues.getBlockBreakingTier());
        difficultyLevel.add("difficultySettingsHordeMobs", difficultySettingsHordeMobs);

        DifficultySettingsHordes hordesValues = defaultDifficultyLevelValues.getDifficultySettingsHordes();
        difficultySettingsHordes = new JsonObject();
        difficultySettingsHordes.addProperty("hordeSizeScaleFactor", hordesValues.getHordeSizeScaleFactor());
        difficultySettingsHordes.addProperty("enableRandomHordes", hordesValues.isEnableRandomHordes());
        difficultySettingsHordes.addProperty("chanceForRandomHorde", hordesValues.getChanceForRandomHorde());
        difficultySettingsHordes.addProperty("hordeSelectionMode", hordesValues.getHordeSelectionMode());
        listOfPossibleHordes = new JsonArray();
        if (hordesValues.getListOfPossibleHordes() != null) {
            for (Integer hordeId : hordesValues.getListOfPossibleHordes()) {
                listOfPossibleHordes.add(hordeId);
            }
        }
        difficultySettingsHordes.add("listOfPossibleHordes", listOfPossibleHordes);
        difficultyLevel.add("difficultySettingsHordes", difficultySettingsHordes);

        DifficultySettingsLureEffect lureEffectValues = defaultDifficultyLevelValues.getDifficultySettingsLureEffect();
        difficultySettingsLureEffect = new JsonObject();
        difficultySettingsLureEffect.addProperty("enableLureHordeEffect", lureEffectValues.isEnableLureHordeEffect());
        difficultySettingsLureEffect.addProperty("nonHordeZombiesCanCauseLureHordeEffect", lureEffectValues.isNonHordeZombiesCanCauseLureHordeEffect());
        difficultySettingsLureEffect.addProperty("chanceForLureHordeEffect", lureEffectValues.getChanceForLureHordeEffect());
        difficultySettingsLureEffect.addProperty("durationForLureHordeEffect", lureEffectValues.getDurationForLureHordeEffect());
        difficultySettingsLureEffect.addProperty("lureHordeEffectSpawnsHorde", lureEffectValues.isLureHordeEffectSpawnsHorde());
        difficultySettingsLureEffect.addProperty("chanceForLureEffectToSpawnHorde", lureEffectValues.getChanceForLureEffectToSpawnHorde());
        difficultyLevel.add("difficultySettingsLureEffect", difficultySettingsLureEffect);
        difficultyLevelArray.add(difficultyLevel);

        difficultySettingsDynamicScaling = new JsonObject();
        difficultySettingsDynamicScaling.addProperty("dynamicScalingEnabled", defaultDynamicScalingValues.isDynamicScalingEnabled());
        difficultySettingsDynamicScaling.addProperty("healthScalePerPlayer", defaultDynamicScalingValues.getHealthScalePerPlayer());
        difficultySettingsDynamicScaling.addProperty("damageScalePerPlayer", defaultDynamicScalingValues.getDamageScalePerPlayer());
        difficultySettingsDynamicScaling.addProperty("speedScalePerPlayer", defaultDynamicScalingValues.getSpeedScalePerPlayer());
        difficultySettingsDynamicScaling.addProperty("armorScalePerPlayer", defaultDynamicScalingValues.getArmorScalePerPlayer());
        difficultySettingsDynamicScaling.addProperty("daysBetweenScaleIncreases", defaultDynamicScalingValues.getDaysBetweenScaleIncreases());
        difficultySettingsDynamicScaling.addProperty("daysHealthScaleIncrease", defaultDynamicScalingValues.getDaysHealthScaleIncrease());
        difficultySettingsDynamicScaling.addProperty("daysDamageScaleIncrease", defaultDynamicScalingValues.getDaysDamageScaleIncrease());
        difficultySettingsDynamicScaling.addProperty("daysSpeedScaleIncrease", defaultDynamicScalingValues.getDaysSpeedScaleIncrease());
        difficultySettingsDynamicScaling.addProperty("daysArmorScaleIncrease", defaultDynamicScalingValues.getDaysArmorScaleIncrease());
        difficultySettingsDynamicScaling.addProperty("maxHealthScale", defaultDynamicScalingValues.getMaxHealthScale());
        difficultySettingsDynamicScaling.addProperty("maxDamageScale", defaultDynamicScalingValues.getMaxDamageScale());
        difficultySettingsDynamicScaling.addProperty("maxSpeedScale", defaultDynamicScalingValues.getMaxSpeedScale());
        difficultySettingsDynamicScaling.addProperty("maxArmorScale", defaultDynamicScalingValues.getMaxArmorScale());

        var defaultConfObject = new JsonObject();
        defaultConfObject.add("dynamicScaling", difficultySettingsDynamicScaling);
        defaultConfObject.add("difficultyLevels", difficultyLevelArray);
        defaultConfObject.addProperty("internalConfigVersion", 1);
        return defaultConfObject;
    }
}
