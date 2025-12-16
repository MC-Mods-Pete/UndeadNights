package net.petemc.undeadnights.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.HashSet;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
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

    public void setDirty() {
        this.markDirty();
    }

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

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
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

        NbtCompound mobUUIDs = tag.getCompound("spawnedHordeMobs");
        mobUUIDs.getKeys().forEach(key -> {
            UUID hordeMobUUID = mobUUIDs.getUuid(key);
            state.spawnedHordeMobs.add(hordeMobUUID);
        });

        NbtCompound removeMobUUIDs = tag.getCompound("hordeMobsToRemove");
        removeMobUUIDs.getKeys().forEach(key -> {
            UUID hordeMobUUID = removeMobUUIDs.getUuid(key);
            state.hordeMobsToRemove.add(hordeMobUUID);
        });

        NbtCompound pendingHordeUUIDs = tag.getCompound("entitiesWithPendingHorde");
        pendingHordeUUIDs.getKeys().forEach(key -> {
            UUID hordeMobUUID = pendingHordeUUIDs.getUuid(key);
            state.entitiesWithPendingHorde.add(hordeMobUUID);
        });

        NbtCompound pendingWaveUUIDs = tag.getCompound("entitiesWithPendingWave");
        pendingWaveUUIDs.getKeys().forEach(key -> {
            UUID hordeMobUUID = pendingWaveUUIDs.getUuid(key);
            state.entitiesWithPendingWave.add(hordeMobUUID);
        });

        NbtCompound receivedHordeUUIDs = tag.getCompound("entitiesWithReceivedHorde");
        receivedHordeUUIDs.getKeys().forEach(key -> {
            UUID hordeMobUUID = receivedHordeUUIDs.getUuid(key);
            state.entitiesWithReceivedHorde.add(hordeMobUUID);
        });

        state.markDirty();
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
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

        NbtCompound mobUUIDs = new NbtCompound();
        spawnedHordeMobs.forEach((uuid) -> {
            mobUUIDs.putUuid(uuid.toString(), uuid);
        });
        tag.put("spawnedHordeMobs", mobUUIDs);

        NbtCompound removeMobUUIDs = new NbtCompound();
        hordeMobsToRemove.forEach((uuid) -> {
            removeMobUUIDs.putUuid(uuid.toString(), uuid);
        });
        tag.put("hordeMobsToRemove", removeMobUUIDs);

        NbtCompound pendingHordeUUIDs = new NbtCompound();
        entitiesWithPendingHorde.forEach((uuid) -> {
            pendingHordeUUIDs.putUuid(uuid.toString(), uuid);
        });
        tag.put("entitiesWithPendingHorde", pendingHordeUUIDs);

        NbtCompound pendingWaveUUIDs = new NbtCompound();
        entitiesWithPendingWave.forEach((uuid) -> {
            pendingWaveUUIDs.putUuid(uuid.toString(), uuid);
        });
        tag.put("entitiesWithPendingWave", pendingWaveUUIDs);

        NbtCompound receivedHordeUUIDs = new NbtCompound();
        entitiesWithReceivedHorde.forEach((uuid) -> {
            receivedHordeUUIDs.putUuid(uuid.toString(), uuid);
        });
        tag.put("entitiesWithReceivedHorde", receivedHordeUUIDs);

        return tag;
    }

    /**
     * This function gets the 'PersistentStateManager' and creates or returns the filled in 'StateSaveAndLoader'.
     * It does this by calling 'StateSaveAndLoader::createFromNbt' passing it the previously saved 'NbtCompound' we wrote in 'writeNbt'.
     */
    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(
                StateSaverAndLoader::createFromNbt,
                StateSaverAndLoader::new,
                UndeadNights.MOD_ID
        );

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }
}
