package net.petemc.undeadnights.config.difficulty;

public class DifficultySettingsHordeMobs {
    private Boolean updateHordeMobAttributes = null;
    private Boolean updateAttributesOfThirdPartyMobs = null;
    private Double healthAttributeScaleFactor = null;
    private Double damageAttributeScaleFactor = null;
    private Double speedAttributeScaleFactor = null;
    private Double armorAttributeScaleFactor = null;
    private Double hordeMobsTrackingRange = null;
    private Boolean hordeZombiesBurnInTheSun = null;
    private Boolean vanillaZombiesBurnInTheSun = null;
    private Boolean hordeZombiesAreFasterOnWater = null;
    private Boolean hordeMobsCanClimbEachOther = null;
    private Boolean blockBreaking = null;
    private Integer blockBreakingTier = null;

    public Boolean isUpdateHordeMobAttributes() { return updateHordeMobAttributes; }
    public void setUpdateHordeMobAttributes(Boolean updateHordeMobAttributes) { this.updateHordeMobAttributes = updateHordeMobAttributes; }

    public Boolean isUpdateAttributesOfThirdPartyMobs() { return updateAttributesOfThirdPartyMobs; }
    public void setUpdateAttributesOfThirdPartyMobs(Boolean updateAttributesOfThirdPartyMobs) { this.updateAttributesOfThirdPartyMobs = updateAttributesOfThirdPartyMobs; }

    public Double getHealthAttributeScaleFactor() { return healthAttributeScaleFactor; }
    public void setHealthAttributeScaleFactor(Double healthAttributeScaleFactor) { this.healthAttributeScaleFactor = healthAttributeScaleFactor; }

    public Double getDamageAttributeScaleFactor() { return damageAttributeScaleFactor; }
    public void setDamageAttributeScaleFactor(Double damageAttributeScaleFactor) { this.damageAttributeScaleFactor = damageAttributeScaleFactor; }

    public Double getSpeedAttributeScaleFactor() { return speedAttributeScaleFactor; }
    public void setSpeedAttributeScaleFactor(Double speedAttributeScaleFactor) { this.speedAttributeScaleFactor = speedAttributeScaleFactor; }

    public Double getArmorAttributeScaleFactor() { return armorAttributeScaleFactor; }
    public void setArmorAttributeScaleFactor(Double armorAttributeScaleFactor) { this.armorAttributeScaleFactor = armorAttributeScaleFactor; }

    public Double getHordeMobsTrackingRange() { return hordeMobsTrackingRange; }
    public void setHordeMobsTrackingRange(Double hordeMobsTrackingRange) { this.hordeMobsTrackingRange = hordeMobsTrackingRange; }

    public Boolean isHordeZombiesBurnInTheSun() { return hordeZombiesBurnInTheSun; }
    public void setHordeZombiesBurnInTheSun(Boolean hordeZombiesBurnInTheSun) { this.hordeZombiesBurnInTheSun = hordeZombiesBurnInTheSun; }

    public Boolean isHordeZombiesAreFasterOnWater() { return hordeZombiesAreFasterOnWater; }
    public void setHordeZombiesAreFasterOnWater(Boolean hordeZombiesAreFasterOnWater) { this.hordeZombiesAreFasterOnWater = hordeZombiesAreFasterOnWater; }

    public Boolean isHordeMobsCanClimbEachOther() { return hordeMobsCanClimbEachOther; }
    public void setHordeMobsCanClimbEachOther(Boolean hordeMobsCanClimbEachOther) { this.hordeMobsCanClimbEachOther = hordeMobsCanClimbEachOther; }

    public Boolean isBlockBreaking() { return blockBreaking; }
    public void setBlockBreaking(Boolean blockBreaking) { this.blockBreaking = blockBreaking; }

    public Integer getBlockBreakingTier() { return blockBreakingTier; }
    public void setBlockBreakingTier(Integer blockBreakingTier) { this.blockBreakingTier = blockBreakingTier; }

    public Boolean isVanillaZombiesBurnInTheSun() { return vanillaZombiesBurnInTheSun; }
    public void setVanillaZombiesBurnInTheSun(Boolean vanillaZombiesBurnInTheSun) { this.vanillaZombiesBurnInTheSun = vanillaZombiesBurnInTheSun; }

    public DifficultySettingsHordeMobs() {
        // Empty constructor
    }

    public DifficultySettingsHordeMobs(Boolean updateHordeMobAttributes, Boolean updateAttributesOfThirdPartyMobs, Double healthAttributeScaleFactor, Double damageAttributeScaleFactor, Double speedAttributeScaleFactor, Double armorAttributeScaleFactor, Double hordeMobsTrackingRange, Boolean hordeZombiesBurnInTheSun, Boolean vanillaZombiesBurnInTheSun, Boolean hordeZombiesAreFasterOnWater, Boolean hordeMobsCanClimbEachOther, Boolean blockBreaking, Integer blockBreakingTier) {
        this.updateHordeMobAttributes = updateHordeMobAttributes;
        this.updateAttributesOfThirdPartyMobs = updateAttributesOfThirdPartyMobs;
        this.healthAttributeScaleFactor = healthAttributeScaleFactor;
        this.damageAttributeScaleFactor = damageAttributeScaleFactor;
        this.speedAttributeScaleFactor = speedAttributeScaleFactor;
        this.armorAttributeScaleFactor = armorAttributeScaleFactor;
        this.hordeMobsTrackingRange = hordeMobsTrackingRange;
        this.hordeZombiesBurnInTheSun = hordeZombiesBurnInTheSun;
        this.vanillaZombiesBurnInTheSun = vanillaZombiesBurnInTheSun;
        this.hordeZombiesAreFasterOnWater = hordeZombiesAreFasterOnWater;
        this.hordeMobsCanClimbEachOther = hordeMobsCanClimbEachOther;
        this.blockBreaking = blockBreaking;
        this.blockBreakingTier = blockBreakingTier;
    }
}