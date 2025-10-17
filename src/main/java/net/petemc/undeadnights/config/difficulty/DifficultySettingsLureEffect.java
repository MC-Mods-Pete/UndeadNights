package net.petemc.undeadnights.config.difficulty;

public class DifficultySettingsLureEffect {
    private Boolean enableLureHordeEffect = null;
    private Boolean nonHordeZombiesCanCauseLureHordeEffect = null;
    private Double chanceForLureHordeEffect = null;
    private Integer durationForLureHordeEffect = null;
    private Boolean lureHordeEffectSpawnsHorde = null;
    private Double chanceForLureEffectToSpawnHorde = null;

    public DifficultySettingsLureEffect() {
        // Empty constructor
    }

    public DifficultySettingsLureEffect(Boolean enableLureHordeEffect, Boolean nonHordeZombiesCanCauseLureHordeEffect, Double chanceForLureHordeEffect, Integer durationForLureHordeEffect, Boolean lureHordeEffectSpawnsHorde, Double chanceForLureEffectToSpawnHorde) {
        this.enableLureHordeEffect = enableLureHordeEffect;
        this.nonHordeZombiesCanCauseLureHordeEffect = nonHordeZombiesCanCauseLureHordeEffect;
        this.chanceForLureHordeEffect = chanceForLureHordeEffect;
        this.durationForLureHordeEffect = durationForLureHordeEffect;
        this.lureHordeEffectSpawnsHorde = lureHordeEffectSpawnsHorde;
        this.chanceForLureEffectToSpawnHorde = chanceForLureEffectToSpawnHorde;
    }
    
    public Boolean isEnableLureHordeEffect() { return enableLureHordeEffect; }
    public void setEnableLureHordeEffect(Boolean enableLureHordeEffect) { this.enableLureHordeEffect = enableLureHordeEffect; }

    public Boolean isNonHordeZombiesCanCauseLureHordeEffect() { return nonHordeZombiesCanCauseLureHordeEffect; }
    public void setNonHordeZombiesCanCauseLureHordeEffect(Boolean nonHordeZombiesCanCauseLureHordeEffect) { this.nonHordeZombiesCanCauseLureHordeEffect = nonHordeZombiesCanCauseLureHordeEffect; }

    public Double getChanceForLureHordeEffect() { return chanceForLureHordeEffect; }
    public void setChanceForLureHordeEffect(Double chanceForLureHordeEffect) { this.chanceForLureHordeEffect = chanceForLureHordeEffect; }

    public Integer getDurationForLureHordeEffect() { return durationForLureHordeEffect; }
    public void setDurationForLureHordeEffect(Integer durationForLureHordeEffect) { this.durationForLureHordeEffect = durationForLureHordeEffect; }

    public Boolean isLureHordeEffectSpawnsHorde() { return lureHordeEffectSpawnsHorde; }
    public void setLureHordeEffectSpawnsHorde(Boolean lureHordeEffectSpawnsHorde) { this.lureHordeEffectSpawnsHorde = lureHordeEffectSpawnsHorde; }

    public Double getChanceForLureEffectToSpawnHorde() { return chanceForLureEffectToSpawnHorde; }
    public void setChanceForLureEffectToSpawnHorde(Double chanceForLureEffectToSpawnHorde) { this.chanceForLureEffectToSpawnHorde = chanceForLureEffectToSpawnHorde; }
}
