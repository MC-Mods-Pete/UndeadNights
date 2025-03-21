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

    private int daysCounter = MainConfig.getDaysBetweenHordeNights();
    private int lastMaxDaysCounter = MainConfig.getDaysBetweenHordeNights();
    private int tickCounter = 60;
    private boolean hordeNight = false;
    private boolean spawnZombies = true;
    private boolean respawnZombies = false;
    public HashSet<UUID> spawnedHordeMobs = new HashSet<UUID>();
    public HashSet<UUID> hordeMobsToRemove = new HashSet<UUID>();

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

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.daysCounter = tag.getInt("daysCounter");
        state.lastMaxDaysCounter = tag.getInt("lastMaxDaysCounter");
        state.tickCounter = tag.getInt("tickCounter");
        state.hordeNight = tag.getBoolean("hordeNight");
        state.spawnZombies = tag.getBoolean("spawnZombies");
        state.respawnZombies = tag.getBoolean("respawnZombies");
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
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("daysCounter", daysCounter);
        nbt.putInt("lastMaxDaysCounter", lastMaxDaysCounter);
        nbt.putInt("tickCounter", tickCounter);
        nbt.putBoolean("hordeNight", hordeNight);
        nbt.putBoolean("spawnZombies", spawnZombies);
        nbt.putBoolean("respawnZombies", respawnZombies);
        NbtCompound mobUUIDs = new NbtCompound();
        spawnedHordeMobs.forEach((uuid) -> {
            mobUUIDs.putUuid(uuid.toString(), uuid);
        });
        nbt.put("spawnedHordeMobs", mobUUIDs);
        NbtCompound removeMobUUIDs = new NbtCompound();
        hordeMobsToRemove.forEach((uuid) -> {
            removeMobUUIDs.putUuid(uuid.toString(), uuid);
        });
        nbt.put("hordeMobsToRemove", removeMobUUIDs);
        return nbt;
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
