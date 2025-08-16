package net.petemc.undeadnights.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.HashSet;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
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
    private boolean isNaturalSpawningOk = false;
    private boolean firstEliteZombieHasSpawned = false;
    private boolean firstDemolitionZombieHasSpawned = false;
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
        this.markDirty();
    }

    public int getLastMaxDaysCounter() {
        return this.lastMaxDaysCounter;
    }

    public void setLastMaxDaysCounter(int val) {
        this.lastMaxDaysCounter = val;
        this.markDirty();
    }

    public int getGracePeriod() {
        return this.gracePeriod;
    }

    public void setGracePeriod(int val) {
        this.gracePeriod = val;
        this.markDirty();
    }

    public int getLastMaxGracePeriod() {
        return this.lastMaxGracePeriod;
    }

    public void setLastMaxGracePeriod(int val) {
        this.lastMaxGracePeriod = val;
        this.markDirty();
    }

    public int getHordesCounter() {
        return this.hordesCounter;
    }

    public void setHordesCounter(int val) {
        this.hordesCounter = val;
        this.markDirty();
    }

    public int getLastMaxHordesCounter() {
        return this.lastMaxHordesCounter;
    }

    public void setLastMaxHordesCounter(int val) {
        this.lastMaxHordesCounter = val;
        this.markDirty();
    }

    public int getTickCounter() {
        return this.tickCounter;
    }

    public void setTickCounter(int val) {
        this.tickCounter = val;
        this.markDirty();
    }

    public boolean getHordeNight() {
        return this.hordeNight;
    }

    public void setHordeNight(boolean val) {
        this.hordeNight = val;
        this.markDirty();
    }

    public boolean getNightIsStarting() {
        return this.nightIsStarting;
    }

    public void setNightIsStarting(boolean val) {
        this.nightIsStarting = val;
        this.markDirty();
    }

    public boolean getFirstWaveHasSpawned() {
        return this.firstWaveHasSpawned;
    }

    public void setFirstWaveHasSpawned(boolean val) {
        this.firstWaveHasSpawned = val;
        this.markDirty();
    }

    public boolean getSpawnZombies() {
        return this.spawnZombies;
    }

    public void setSpawnZombies(boolean val) {
        this.spawnZombies = val;
        this.markDirty();
    }

    public boolean getRespawnZombies() {
        return this.respawnZombies;
    }

    public void setRespawnZombies(boolean val) {
        this.respawnZombies = val;
        this.markDirty();
    }

    public boolean getTryToSpawnRandomHorde() {
        return this.tryToSpawnRandomHorde;
    }

    public void setTryToSpawnRandomHorde(boolean val) {
        this.tryToSpawnRandomHorde = val;
        this.markDirty();
    }

    public boolean getIsNaturalSpawningOk() {
        return this.isNaturalSpawningOk;
    }

    public void setIsNaturalSpawningOk(boolean val) {
        this.isNaturalSpawningOk = val;
        this.markDirty();
    }

    public boolean getFirstEliteZombieHasSpawned() {
        return this.firstEliteZombieHasSpawned;
    }

    public void setFirstEliteZombieHasSpawned(boolean val) {
        this.firstEliteZombieHasSpawned = val;
        this.markDirty();
    }

    public boolean getFirstDemolitionZombieHasSpawned() {
        return this.firstDemolitionZombieHasSpawned;
    }

    public void setFirstDemolitionZombieHasSpawned(boolean val) {
        this.firstDemolitionZombieHasSpawned = val;
        this.markDirty();
    }

    public long getPrevNormalizedTimeOfDay() {
        return this.prevNormalizedTimeOfDay;
    }

    public void setPrevNormalizedTimeOfDay(long val) {
        this.prevNormalizedTimeOfDay = val;
        this.markDirty();
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
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
        state.isNaturalSpawningOk = tag.getBoolean("isNaturalSpawningOk");
        state.firstEliteZombieHasSpawned = tag.getBoolean("firstEliteZombieHasSpawned");
        state.firstDemolitionZombieHasSpawned = tag.getBoolean("firstDemolitionZombieHasSpawned");
        state.prevNormalizedTimeOfDay = tag.getLong("prevNormalizedTimeOfDay");

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

        NbtCompound receivedHordeUUIDs = tag.getCompound("entitiesWithReceivedHorde");
        receivedHordeUUIDs.getKeys().forEach(key -> {
            UUID hordeMobUUID = receivedHordeUUIDs.getUuid(key);
            state.entitiesWithReceivedHorde.add(hordeMobUUID);
        });

        state.markDirty();
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
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
        tag.putBoolean("isNaturalSpawningOk", isNaturalSpawningOk);
        tag.putBoolean("firstEliteZombieHasSpawned", firstEliteZombieHasSpawned);
        tag.putBoolean("firstDemolitionZombieHasSpawned", firstDemolitionZombieHasSpawned);
        tag.putLong("prevNormalizedTimeOfDay", prevNormalizedTimeOfDay);

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

        NbtCompound receivedHordeUUIDs = new NbtCompound();
        entitiesWithReceivedHorde.forEach((uuid) -> {
            receivedHordeUUIDs.putUuid(uuid.toString(), uuid);
        });
        tag.put("entitiesWithReceivedHorde", receivedHordeUUIDs);

        return tag;
    }

    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new, // If there's no 'StateSaverAndLoader' yet create one
            StateSaverAndLoader::createFromNbt, // If there is a 'StateSaverAndLoader' NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );

    /**
     * This function gets the 'PersistentStateManager' and creates or returns the filled in 'StateSaveAndLoader'.
     * It does this by calling 'StateSaveAndLoader::createFromNbt' passing it the previously saved 'NbtCompound' we wrote in 'writeNbt'.
     */
    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.
        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, UndeadNights.MOD_ID);

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }
}
