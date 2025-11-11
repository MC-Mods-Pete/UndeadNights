package net.petemc.undeadnights.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
    private int daysCounter = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights();
    private int lastMaxDaysCounter = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights();
    private int gracePeriod = MainConfig.getGracePeriodBeforeFirstHordeNight();
    private int lastMaxGracePeriod = MainConfig.getGracePeriodBeforeFirstHordeNight();
    private int hordesCounter = 0;
    private int lastMaxHordesCounter = 0;
    private int tickCounter = 60;
    private int possibleHordesIndex = -1;
    private int currentDifficultyLevelIndex = 0;
    private int currentDayScaleCounter = 0;
    private boolean hordeNight = false;
    private boolean nightIsStarting = false;
    private boolean firstWaveHasSpawned = false;
    private boolean spawnZombies = true;
    private boolean respawnZombies = false;
    private boolean tryToSpawnRandomHorde = true;
    private boolean firstDifficultyLevelPrinted = false;
    private boolean performDifficultySwitchCheck = true;
    private boolean isNaturalSpawningOk = false;
    private boolean firstEliteZombieHasSpawned = false;
    private boolean firstDemolitionZombieHasSpawned = false;
    private long prevNormalizedTimeOfDay = 0;
    private double currentHealthScale = 0.0;
    private double currentSpeedScale = 0.0;
    private double currentDamageScale = 0.0;
    private double currentArmorScale = 0.0;
    public HashSet<UUID> spawnedHordeMobs = new HashSet<UUID>();
    public HashSet<UUID> hordeMobsToRemove = new HashSet<UUID>();
    public HashSet<UUID> entitiesWithPendingHorde = new HashSet<UUID>();
    public HashSet<UUID> entitiesWithPendingWave = new HashSet<UUID>();
    public HashSet<UUID> entitiesWithReceivedHorde = new HashSet<UUID>();


    // Setter and Getter functions
    public int getDaysCounter() {
        return this.daysCounter;
    }
    public void setDaysCounter(int val) {
        this.daysCounter = val;
        this.setDirty();
    }

    public int getLastMaxDaysCounter() {
        return this.lastMaxDaysCounter;
    }
    public void setLastMaxDaysCounter(int val) {
        this.lastMaxDaysCounter = val;
        this.setDirty();
    }

    public int getGracePeriod() {
        return this.gracePeriod;
    }
    public void setGracePeriod(int val) {
        this.gracePeriod = val;
        this.setDirty();
    }

    public int getLastMaxGracePeriod() {
        return this.lastMaxGracePeriod;
    }
    public void setLastMaxGracePeriod(int val) {
        this.lastMaxGracePeriod = val;
        this.setDirty();
    }

    public int getHordesCounter() {
        return this.hordesCounter;
    }
    public void setHordesCounter(int val) {
        this.hordesCounter = val;
        this.setDirty();
    }

    public int getLastMaxHordesCounter() {
        return this.lastMaxHordesCounter;
    }
    public void setLastMaxHordesCounter(int val) {
        this.lastMaxHordesCounter = val;
        this.setDirty();
    }

    public int getTickCounter() {
        return this.tickCounter;
    }
    public void setTickCounter(int val) {
        this.tickCounter = val;
        this.setDirty();
    }

    public int getPossibleHordesIndex() { return possibleHordesIndex; }
    public void setPossibleHordesIndex(int possibleHordesIndex) {
        this.possibleHordesIndex = possibleHordesIndex;
        this.setDirty();
    }

    public int getCurrentDifficultyLevelIndex() { return currentDifficultyLevelIndex; }
    public void setCurrentDifficultyLevelIndex(int currentDifficultyLevelIndex) {
        this.currentDifficultyLevelIndex = currentDifficultyLevelIndex;
        this.setDirty();
    }

    public int getCurrentDayScaleCounter() { return currentDayScaleCounter; }
    public void setCurrentDayScaleCounter(int currentDayScaleCounter) {
        this.currentDayScaleCounter = currentDayScaleCounter;
        this.setDirty();
    }

    public boolean getHordeNight() {
        return this.hordeNight;
    }
    public void setHordeNight(boolean val) {
        this.hordeNight = val;
        this.setDirty();
    }

    public boolean getNightIsStarting() {
        return this.nightIsStarting;
    }
    public void setNightIsStarting(boolean val) {
        this.nightIsStarting = val;
        this.setDirty();
    }

    public boolean getFirstWaveHasSpawned() {
        return this.firstWaveHasSpawned;
    }
    public void setFirstWaveHasSpawned(boolean val) {
        this.firstWaveHasSpawned = val;
        this.setDirty();
    }

    public boolean getSpawnZombies() {
        return this.spawnZombies;
    }
    public void setSpawnZombies(boolean val) {
        this.spawnZombies = val;
        this.setDirty();
    }

    public boolean getRespawnZombies() {
        return this.respawnZombies;
    }
    public void setRespawnZombies(boolean val) {
        this.respawnZombies = val;
        this.setDirty();
    }

    public boolean getTryToSpawnRandomHorde() {
        return this.tryToSpawnRandomHorde;
    }
    public void setTryToSpawnRandomHorde(boolean val) {
        this.tryToSpawnRandomHorde = val;
        this.setDirty();
    }

    public boolean isFirstDifficultyLevelPrinted() {
        return firstDifficultyLevelPrinted;
    }
    public void setFirstDifficultyLevelPrinted(boolean firstDifficultyLevelPrinted) {
        this.firstDifficultyLevelPrinted = firstDifficultyLevelPrinted;
        this.setDirty();
    }

    public boolean isPerformDifficultySwitchCheck() {
        return performDifficultySwitchCheck;
    }
    public void setPerformDifficultySwitchCheck(boolean performDifficultySwitchCheck) {
        this.performDifficultySwitchCheck = performDifficultySwitchCheck;
        this.setDirty();
    }

    public boolean getIsNaturalSpawningOk() {
        return this.isNaturalSpawningOk;
    }
    public void setIsNaturalSpawningOk(boolean val) {
        this.isNaturalSpawningOk = val;
        this.setDirty();
    }

    public boolean getFirstEliteZombieHasSpawned() {
        return this.firstEliteZombieHasSpawned;
    }
    public void setFirstEliteZombieHasSpawned(boolean val) {
        this.firstEliteZombieHasSpawned = val;
        this.setDirty();
    }

    public boolean getFirstDemolitionZombieHasSpawned() {
        return this.firstDemolitionZombieHasSpawned;
    }
    public void setFirstDemolitionZombieHasSpawned(boolean val) {
        this.firstDemolitionZombieHasSpawned = val;
        this.setDirty();
    }

    public long getPrevNormalizedTimeOfDay() {
        return this.prevNormalizedTimeOfDay;
    }
    public void setPrevNormalizedTimeOfDay(long val) {
        this.prevNormalizedTimeOfDay = val;
        this.setDirty();
    }

    public double getCurrentHealthScale() { return currentHealthScale; }
    public void setCurrentHealthScale(double currentHealthScale) {
        this.currentHealthScale = currentHealthScale;
        this.setDirty();
    }

    public double getCurrentSpeedScale() { return currentSpeedScale; }
    public void setCurrentSpeedScale(double currentSpeedScale) {
        this.currentSpeedScale = currentSpeedScale;
        this.setDirty();
    }

    public double getCurrentDamageScale() { return currentDamageScale; }
    public void setCurrentDamageScale(double currentDamageScale) {
        this.currentDamageScale = currentDamageScale;
        this.setDirty();
    }

    public double getCurrentArmorScale() { return currentArmorScale; }
    public void setCurrentArmorScale(double currentArmorScale) {
        this.currentArmorScale = currentArmorScale;
        this.setDirty();
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StateSaverAndLoader::load, StateSaverAndLoader::new, UndeadNights.MOD_ID);
    }

    public static StateSaverAndLoader load(CompoundTag tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.daysCounter = tag.getInt("daysCounter");
        state.lastMaxDaysCounter = tag.getInt("lastMaxDaysCounter");
        state.gracePeriod = tag.getInt("gracePeriod");
        state.lastMaxGracePeriod = tag.getInt("lastMaxGracePeriod");
        state.hordesCounter = tag.getInt("hordesCounter");
        state.lastMaxHordesCounter = tag.getInt("lastMaxHordesCounter");
        state.tickCounter = tag.getInt("tickCounter");
        state.possibleHordesIndex = tag.getInt("lastHordeIndex");
        state.currentDifficultyLevelIndex = tag.getInt("currentDifficultyLevel");
        state.currentDayScaleCounter = tag.getInt("currentDayScaleCounter");
        state.hordeNight = tag.getBoolean("hordeNight");
        state.nightIsStarting = tag.getBoolean("nightIsStarting");
        state.firstWaveHasSpawned = tag.getBoolean("firstWaveHasSpawned");
        state.spawnZombies = tag.getBoolean("spawnZombies");
        state.respawnZombies = tag.getBoolean("respawnZombies");
        state.tryToSpawnRandomHorde = tag.getBoolean("tryToSpawnRandomHorde");
        state.performDifficultySwitchCheck = tag.getBoolean("performDifficultySwitchCheck");
        state.firstDifficultyLevelPrinted = tag.getBoolean("firstDifficultyLevelPrinted");
        state.isNaturalSpawningOk = tag.getBoolean("isNaturalSpawningOk");
        state.firstEliteZombieHasSpawned = tag.getBoolean("firstEliteZombieHasSpawned");
        state.firstDemolitionZombieHasSpawned = tag.getBoolean("firstDemolitionZombieHasSpawned");
        state.prevNormalizedTimeOfDay = tag.getLong("prevNormalizedTimeOfDay");
        state.currentHealthScale = tag.getDouble("currentHealthScale");
        state.currentSpeedScale = tag.getDouble("currentSpeedScale");
        state.currentDamageScale = tag.getDouble("currentDamageScale");
        state.currentArmorScale = tag.getDouble("currentArmorScale");

        CompoundTag mobUUIDs = tag.getCompound("spawnedHordeMobs");
        mobUUIDs.getAllKeys().forEach(key -> {
            UUID hordeMobUUID = mobUUIDs.getUUID(key);
            state.spawnedHordeMobs.add(hordeMobUUID);
        });

        CompoundTag removeMobUUIDs = tag.getCompound("hordeMobsToRemove");
        removeMobUUIDs.getAllKeys().forEach(key -> {
            UUID hordeMobUUID = removeMobUUIDs.getUUID(key);
            state.hordeMobsToRemove.add(hordeMobUUID);
        });

        CompoundTag pendingHordeUUIDs = tag.getCompound("entitiesWithPendingHorde");
        pendingHordeUUIDs.getAllKeys().forEach(key -> {
            UUID hordeMobUUID = pendingHordeUUIDs.getUUID(key);
            state.entitiesWithPendingHorde.add(hordeMobUUID);
        });

        CompoundTag pendingWaveUUIDs = tag.getCompound("entitiesWithPendingWave");
        pendingWaveUUIDs.getAllKeys().forEach(key -> {
            UUID hordeMobUUID = pendingWaveUUIDs.getUUID(key);
            state.entitiesWithPendingWave.add(hordeMobUUID);
        });

        CompoundTag receivedHordeUUIDs = tag.getCompound("entitiesWithReceivedHorde");
        receivedHordeUUIDs.getAllKeys().forEach(key -> {
            UUID hordeMobUUID = receivedHordeUUIDs.getUUID(key);
            state.entitiesWithReceivedHorde.add(hordeMobUUID);
        });

        state.setDirty();
        return state;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.putInt("daysCounter", daysCounter);
        tag.putInt("lastMaxDaysCounter", lastMaxDaysCounter);
        tag.putInt("gracePeriod", gracePeriod);
        tag.putInt("lastMaxGracePeriod", lastMaxGracePeriod);
        tag.putInt("hordesCounter", hordesCounter);
        tag.putInt("lastMaxHordesCounter", lastMaxHordesCounter);
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("lastHordeIndex", possibleHordesIndex);
        tag.putInt("currentDifficultyLevel", currentDifficultyLevelIndex);
        tag.putInt("currentDayScaleCounter", currentDayScaleCounter);
        tag.putBoolean("hordeNight", hordeNight);
        tag.putBoolean("nightIsStarting", nightIsStarting);
        tag.putBoolean("firstWaveHasSpawned", firstWaveHasSpawned);
        tag.putBoolean("spawnZombies", spawnZombies);
        tag.putBoolean("respawnZombies", respawnZombies);
        tag.putBoolean("tryToSpawnRandomHorde", tryToSpawnRandomHorde);
        tag.putBoolean("performDifficultySwitchCheck", performDifficultySwitchCheck);
        tag.putBoolean("firstDifficultyLevelPrinted", firstDifficultyLevelPrinted);
        tag.putBoolean("isNaturalSpawningOk", isNaturalSpawningOk);
        tag.putBoolean("firstEliteZombieHasSpawned", firstEliteZombieHasSpawned);
        tag.putBoolean("firstDemolitionZombieHasSpawned", firstDemolitionZombieHasSpawned);
        tag.putLong("prevNormalizedTimeOfDay", prevNormalizedTimeOfDay);
        tag.putDouble("currentHealthScale", currentHealthScale);
        tag.putDouble("currentSpeedScale", currentSpeedScale);
        tag.putDouble("currentDamageScale", currentDamageScale);
        tag.putDouble("currentArmorScale", currentArmorScale);

        CompoundTag mobUUIDs = new CompoundTag();
        spawnedHordeMobs.forEach((uuid) -> {
            mobUUIDs.putUUID(uuid.toString(), uuid);
        });
        tag.put("spawnedHordeMobs", mobUUIDs);

        CompoundTag removeMobUUIDs = new CompoundTag();
        hordeMobsToRemove.forEach((uuid) -> {
            removeMobUUIDs.putUUID(uuid.toString(), uuid);
        });
        tag.put("hordeMobsToRemove", removeMobUUIDs);

        CompoundTag pendingHordeUUIDs = new CompoundTag();
        entitiesWithPendingHorde.forEach((uuid) -> {
            pendingHordeUUIDs.putUUID(uuid.toString(), uuid);
        });
        tag.put("entitiesWithPendingHorde", pendingHordeUUIDs);

        CompoundTag pendingWaveUUIDs = new CompoundTag();
        entitiesWithPendingWave.forEach((uuid) -> {
            pendingWaveUUIDs.putUUID(uuid.toString(), uuid);
        });
        tag.put("entitiesWithPendingWave", pendingWaveUUIDs);

        CompoundTag receivedHordeUUIDs = new CompoundTag();
        entitiesWithReceivedHorde.forEach((uuid) -> {
            receivedHordeUUIDs.putUUID(uuid.toString(), uuid);
        });
        tag.put("entitiesWithReceivedHorde", receivedHordeUUIDs);

        return tag;
    }
}
