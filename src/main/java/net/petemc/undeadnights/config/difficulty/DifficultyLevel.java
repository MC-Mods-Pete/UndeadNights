package net.petemc.undeadnights.config.difficulty;

import java.util.ArrayList;
import java.util.List;

public class DifficultyLevel {
    private String difficultyName = null;//"Phase 1";
    private Integer minStartDay = null;
    private Integer maxStartDay = null;
    private Integer chanceForDifficultyLevelSwitch = null;
    private DifficultySettingsHordeMobs difficultySettingsHordeMobs = new DifficultySettingsHordeMobs();
    private DifficultySettingsHordeNights difficultySettingsHordeNights = new DifficultySettingsHordeNights();
    private DifficultySettingsHordes difficultySettingsHordes = new DifficultySettingsHordes();
    private DifficultySettingsLureEffect difficultySettingsLureEffect = new DifficultySettingsLureEffect();

    public DifficultyLevel() {
        // Empty constructor
    }

    public DifficultyLevel(String difficultyName, Integer minStartDay, Integer maxStartDay, Integer chanceForDifficultyLevelSwitch,
                           DifficultySettingsHordeMobs difficultySettingsHordeMobs, DifficultySettingsHordeNights difficultySettingsHordeNights,
                           DifficultySettingsHordes difficultySettingsHordes, DifficultySettingsLureEffect difficultySettingsLureEffect) {
        this.difficultyName = difficultyName;
        this.minStartDay = minStartDay;
        this.maxStartDay = maxStartDay;
        this.chanceForDifficultyLevelSwitch = chanceForDifficultyLevelSwitch;
        this.difficultySettingsHordeMobs = difficultySettingsHordeMobs;
        this.difficultySettingsHordeNights = difficultySettingsHordeNights;
        this.difficultySettingsHordes = difficultySettingsHordes;
        this.difficultySettingsLureEffect = difficultySettingsLureEffect;
    }

    public String getDifficultyName() { return difficultyName; }
    public void setDifficultyName(String difficultyName) { this.difficultyName = difficultyName; }

    public Integer getMinStartDay() { return minStartDay; }
    public void setMinStartDay(Integer minStartDay) { this.minStartDay = minStartDay; }

    public Integer getMaxStartDay() { return maxStartDay; }
    public void setMaxStartDay(Integer maxStartDay) { this.maxStartDay = maxStartDay; }

    public Integer getChanceForDifficultyLevelSwitch() { return chanceForDifficultyLevelSwitch; }
    public void setChanceForDifficultyLevelSwitch(Integer chanceForDifficultyLevelSwitch) { this.chanceForDifficultyLevelSwitch = chanceForDifficultyLevelSwitch; }

    public DifficultySettingsHordeMobs getDifficultySettingsHordeMobs() { return difficultySettingsHordeMobs; }
    public void setDifficultySettingsHordeMobs(DifficultySettingsHordeMobs difficultySettingsHordeMobs) { this.difficultySettingsHordeMobs = difficultySettingsHordeMobs; }

    public DifficultySettingsHordeNights getDifficultySettingsHordeNights() { return difficultySettingsHordeNights; }
    public void setDifficultySettingsHordeNights(DifficultySettingsHordeNights difficultySettingsHordeNights) { this.difficultySettingsHordeNights = difficultySettingsHordeNights; }

    public DifficultySettingsHordes getDifficultySettingsHordes() { return difficultySettingsHordes; }
    public void setDifficultySettingsHordes(DifficultySettingsHordes difficultySettingsHordes) { this.difficultySettingsHordes = difficultySettingsHordes; }

    public DifficultySettingsLureEffect getDifficultySettingsLureEffect() {
        return difficultySettingsLureEffect;
    }

    public void setDifficultySettingsLureEffect(DifficultySettingsLureEffect difficultySettingsLureEffect) {
        this.difficultySettingsLureEffect = difficultySettingsLureEffect;
    }
}
