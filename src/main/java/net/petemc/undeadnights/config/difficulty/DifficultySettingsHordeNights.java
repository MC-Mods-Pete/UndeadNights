package net.petemc.undeadnights.config.difficulty;

public class DifficultySettingsHordeNights {
    private Integer daysBetweenHordeNights = null;
    private Integer chanceForHordeNight = null;
    private Boolean spawnAdditionalWaves = null;
    private Integer cooldownBetweenWaves = null;
    private Integer chanceForAdditionalWave = null;
    private Boolean allDayLongHordeNights = null;
    private Integer maxHordesPerHordeNight = null;

    public Integer getDaysBetweenHordeNights() { return daysBetweenHordeNights; }
    public void setDaysBetweenHordeNights(Integer daysBetweenHordeNights) { this.daysBetweenHordeNights = daysBetweenHordeNights; }

    public Integer getChanceForHordeNight() { return chanceForHordeNight; }
    public void setChanceForHordeNight(Integer chanceForHordeNight) { this.chanceForHordeNight = chanceForHordeNight; }

    public Boolean isSpawnAdditionalWaves() { return spawnAdditionalWaves; }
    public void setSpawnAdditionalWaves(Boolean spawnAdditionalWaves) { this.spawnAdditionalWaves = spawnAdditionalWaves; }

    public Integer getCooldownBetweenWaves() { return cooldownBetweenWaves; }
    public void setCooldownBetweenWaves(Integer cooldownBetweenWaves) { this.cooldownBetweenWaves = cooldownBetweenWaves; }

    public Integer getChanceForAdditionalWave() { return chanceForAdditionalWave; }
    public void setChanceForAdditionalWave(Integer chanceForAdditionalWave) { this.chanceForAdditionalWave = chanceForAdditionalWave; }

    public Boolean getAllDayLongHordeNights() { return allDayLongHordeNights; }
    public void setAllDayLongHordeNights(Boolean allDayLongHordeNights) { this.allDayLongHordeNights = allDayLongHordeNights; }

    public Integer getMaxHordesPerHordeNight() { return maxHordesPerHordeNight; }
    public void setMaxHordesPerHordeNight(Integer maxHordesPerHordeNight) { this.maxHordesPerHordeNight = maxHordesPerHordeNight; }

    public DifficultySettingsHordeNights() {
        // Empty constructor
    }

    public DifficultySettingsHordeNights(Integer daysBetweenHordeNights, Integer chanceForHordeNight, Boolean spawnAdditionalWaves, Integer cooldownBetweenWaves, Integer chanceForAdditionalWave, Boolean allDayLongHordeNights, Integer maxHordesPerHordeNight) {
        this.daysBetweenHordeNights = daysBetweenHordeNights;
        this.chanceForHordeNight = chanceForHordeNight;
        this.spawnAdditionalWaves = spawnAdditionalWaves;
        this.cooldownBetweenWaves = cooldownBetweenWaves;
        this.chanceForAdditionalWave = chanceForAdditionalWave;
        this.allDayLongHordeNights = allDayLongHordeNights;
        this.maxHordesPerHordeNight = maxHordesPerHordeNight;
    }
}
