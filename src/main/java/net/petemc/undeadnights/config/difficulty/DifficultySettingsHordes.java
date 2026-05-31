package net.petemc.undeadnights.config.difficulty;

import java.util.ArrayList;
import java.util.List;

public class DifficultySettingsHordes {
    private Boolean enableRandomHordes = null;
    private Integer chanceForRandomHorde = null;
    private Double hordeSizeScaleFactor = null;
    private String hordeSelectionMode = null;//"sequential";
    private List<Integer> listOfPossibleHordes = null;//new ArrayList<>(List.of(1));
    private Boolean bossHordeEnabled = false;
    private Integer bossHordeId = 1;

    public Boolean isEnableRandomHordes() { return enableRandomHordes; }
    public void setEnableRandomHordes(Boolean enableRandomHordes) { this.enableRandomHordes = enableRandomHordes; }

    public Integer getChanceForRandomHorde() { return chanceForRandomHorde; }
    public void setChanceForRandomHorde(Integer chanceForRandomHorde) { this.chanceForRandomHorde = chanceForRandomHorde; }

    public List<Integer> getListOfPossibleHordes() { return listOfPossibleHordes; }
    public void setListOfPossibleHordes(List<Integer> listOfPossibleHordes) { this.listOfPossibleHordes = listOfPossibleHordes; }

    public Double getHordeSizeScaleFactor() { return hordeSizeScaleFactor; }
    public void setHordeSizeScaleFactor(Double hordeSizeScaleFactor) { this.hordeSizeScaleFactor = hordeSizeScaleFactor; }

    public String getHordeSelectionMode() { return hordeSelectionMode; }
    public void setHordeSelectionMode(String hordeSelectionMode) { this.hordeSelectionMode = hordeSelectionMode; }

    public Boolean getBossHordeEnabled() { return bossHordeEnabled; }
    public void setBossHordeEnabled(Boolean bossHordeEnabled) { this.bossHordeEnabled = bossHordeEnabled; }

    public Integer getBossHordeId() { return bossHordeId; }
    public void setBossHordeId(Integer bossHordeId) { this.bossHordeId = bossHordeId; }

    public DifficultySettingsHordes() {
        // Empty constructor
    }

    public DifficultySettingsHordes(Boolean enableRandomHordes, Integer chanceForRandomHorde, List<Integer> listOfPossibleHordes, Double hordeSizeScaleFactor, String hordeSelectionMode, Boolean bossHordeEnabled, Integer bossHordeId) {
        this.enableRandomHordes = enableRandomHordes;
        this.chanceForRandomHorde = chanceForRandomHorde;
        this.hordeSizeScaleFactor = hordeSizeScaleFactor;
        this.hordeSelectionMode = hordeSelectionMode;
        this.listOfPossibleHordes = listOfPossibleHordes;
        this.bossHordeEnabled = bossHordeEnabled;
        this.bossHordeId = bossHordeId;
    }
}
