package net.petemc.undeadnights.config.difficulty;

public class DifficultySettingDynamicScaling {
    private Boolean dynamicScalingEnabled = null;
    private Double healthScalePerPlayer = null;
    private Double damageScalePerPlayer = null;
    private Double speedScalePerPlayer = null;
    private Double armorScalePerPlayer = null;
    private Double daysBetweenScaleIncreases = null;
    private Double daysHealthScaleIncrease = null;
    private Double daysDamageScaleIncrease = null;
    private Double daysSpeedScaleIncrease = null;
    private Double daysArmorScaleIncrease = null;
    private Double maxHealthScale = null;
    private Double maxDamageScale = null;
    private Double maxSpeedScale = null;
    private Double maxArmorScale = null;
    
    public DifficultySettingDynamicScaling(){}
    
    public DifficultySettingDynamicScaling(Boolean dynamicScalingEnabled, Double healthScalePerPlayer, Double damageScalePerPlayer, Double speedScalePerPlayer, Double armorScalePerPlayer, Double daysBetweenScaleIncreases, Double daysHealthScaleIncrease, Double daysDamageScaleIncrease, Double daysSpeedScaleIncrease, Double daysArmorScaleIncrease, Double maxHealthScale, Double maxDamageScale, Double maxSpeedScale, Double maxArmorScale) {
        this.dynamicScalingEnabled = dynamicScalingEnabled;
        this.healthScalePerPlayer = healthScalePerPlayer;
        this.damageScalePerPlayer = damageScalePerPlayer;
        this.speedScalePerPlayer = speedScalePerPlayer;
        this.armorScalePerPlayer = armorScalePerPlayer;
        this.daysBetweenScaleIncreases = daysBetweenScaleIncreases;
        this.daysHealthScaleIncrease = daysHealthScaleIncrease;
        this.daysDamageScaleIncrease = daysDamageScaleIncrease;
        this.daysSpeedScaleIncrease = daysSpeedScaleIncrease;
        this.daysArmorScaleIncrease = daysArmorScaleIncrease;
        this.maxHealthScale = maxHealthScale;
        this.maxDamageScale = maxDamageScale;
        this.maxSpeedScale = maxSpeedScale;
        this.maxArmorScale = maxArmorScale;
    }

    public Boolean isDynamicScalingEnabled() { return dynamicScalingEnabled; }
    public void setDynamicScalingEnabled(Boolean dynamicScalingEnabled) { this.dynamicScalingEnabled = dynamicScalingEnabled; }

    public Double getHealthScalePerPlayer() { return healthScalePerPlayer; }
    public void setHealthScalePerPlayer(Double healthScalePerPlayer) { this.healthScalePerPlayer = healthScalePerPlayer; }

    public Double getDamageScalePerPlayer() { return damageScalePerPlayer; }
    public void setDamageScalePerPlayer(Double damageScalePerPlayer) { this.damageScalePerPlayer = damageScalePerPlayer; }

    public Double getSpeedScalePerPlayer() { return speedScalePerPlayer; }
    public void setSpeedScalePerPlayer(Double speedScalePerPlayer) { this.speedScalePerPlayer = speedScalePerPlayer; }

    public Double getArmorScalePerPlayer() { return armorScalePerPlayer; }
    public void setArmorScalePerPlayer(Double armorScalePerPlayer) { this.armorScalePerPlayer = armorScalePerPlayer; }

    public Double getDaysBetweenScaleIncreases() { return daysBetweenScaleIncreases; }
    public void setDaysBetweenScaleIncreases(Double daysBetweenScaleIncreases) { this.daysBetweenScaleIncreases = daysBetweenScaleIncreases; }

    public Double getDaysHealthScaleIncrease() { return daysHealthScaleIncrease; }
    public void setDaysHealthScaleIncrease(Double daysHealthScaleIncrease) { this.daysHealthScaleIncrease = daysHealthScaleIncrease; }

    public Double getDaysDamageScaleIncrease() { return daysDamageScaleIncrease; }
    public void setDaysDamageScaleIncrease(Double daysDamageScaleIncrease) { this.daysDamageScaleIncrease = daysDamageScaleIncrease; }

    public Double getDaysSpeedScaleIncrease() { return daysSpeedScaleIncrease; }
    public void setDaysSpeedScaleIncrease(Double daysSpeedScaleIncrease) { this.daysSpeedScaleIncrease = daysSpeedScaleIncrease; }

    public Double getDaysArmorScaleIncrease() { return daysArmorScaleIncrease; }
    public void setDaysArmorScaleIncrease(Double daysArmorScaleIncrease) { this.daysArmorScaleIncrease = daysArmorScaleIncrease; }

    public Double getMaxHealthScale() { return maxHealthScale; }
    public void setMaxHealthScale(Double maxHealthScale) { this.maxHealthScale = maxHealthScale; }

    public Double getMaxDamageScale() { return maxDamageScale; }
    public void setMaxDamageScale(Double maxDamageScale) { this.maxDamageScale = maxDamageScale; }

    public Double getMaxSpeedScale() { return maxSpeedScale; }
    public void setMaxSpeedScale(Double maxSpeedScale) { this.maxSpeedScale = maxSpeedScale; }

    public Double getMaxArmorScale() { return maxArmorScale; }
    public void setMaxArmorScale(Double maxArmorScale) { this.maxArmorScale = maxArmorScale; }
}
