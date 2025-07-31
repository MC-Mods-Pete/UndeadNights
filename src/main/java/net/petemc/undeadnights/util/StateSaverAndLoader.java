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
    private int daysCounter = MainConfig.getDaysBetweenHordeNights();
    private int lastMaxDaysCounter = MainConfig.getDaysBetweenHordeNights();
    private int gracePeriod = MainConfig.getGracePeriodBeforeFirstHordeNight();
    private int lastMaxGracePeriod = MainConfig.getGracePeriodBeforeFirstHordeNight();
    private int hordesCounter = MainConfig.getDaysBetweenHordeNights() + 1;
    private int lastMaxHordesCounter = MainConfig.getDaysBetweenHordeNights();
    private int tickCounter = 60;
    private boolean hordeNight = false;
    private boolean nightIsStarting = false;
    private boolean firstWaveHasSpawned = false;
    private boolean spawnZombies = true;
    private boolean respawnZombies = false;
    private boolean tryToSpawnRandomHorde = true;
    private long prevNormalizedTimeOfDay = 0;
    public HashSet<UUID> spawnedHordeMobs = new HashSet<UUID>();
    public HashSet<UUID> hordeMobsToRemove = new HashSet<UUID>();
    public HashSet<UUID> entitiesWithPendingHorde = new HashSet<UUID>();
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

    public long getPrevNormalizedTimeOfDay() {
        return this.prevNormalizedTimeOfDay;
    }

    public void setPrevNormalizedTimeOfDay(long val) {
        this.prevNormalizedTimeOfDay = val;
        this.setDirty();
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
        state.hordeNight = tag.getBoolean("hordeNight");
        state.nightIsStarting = tag.getBoolean("nightIsStarting");
        state.firstWaveHasSpawned = tag.getBoolean("firstWaveHasSpawned");
        state.spawnZombies = tag.getBoolean("spawnZombies");
        state.respawnZombies = tag.getBoolean("respawnZombies");
        state.tryToSpawnRandomHorde = tag.getBoolean("tryToSpawnRandomHorde");
        state.prevNormalizedTimeOfDay = tag.getLong("prevNormalizedTimeOfDay");

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
        tag.putBoolean("hordeNight", hordeNight);
        tag.putBoolean("nightIsStarting", nightIsStarting);
        tag.putBoolean("firstWaveHasSpawned", firstWaveHasSpawned);
        tag.putBoolean("spawnZombies", spawnZombies);
        tag.putBoolean("respawnZombies", respawnZombies);
        tag.putBoolean("tryToSpawnRandomHorde", tryToSpawnRandomHorde);
        tag.putLong("prevNormalizedTimeOfDay", prevNormalizedTimeOfDay);

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

        CompoundTag receivedHordeUUIDs = new CompoundTag();
        entitiesWithReceivedHorde.forEach((uuid) -> {
            receivedHordeUUIDs.putUUID(uuid.toString(), uuid);
        });
        tag.put("entitiesWithReceivedHorde", receivedHordeUUIDs);

        return tag;
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StateSaverAndLoader::load, StateSaverAndLoader::new, UndeadNights.MOD_ID);
    }
}
