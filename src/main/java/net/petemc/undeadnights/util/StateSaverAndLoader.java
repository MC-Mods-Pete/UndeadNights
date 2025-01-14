package net.petemc.undeadnights.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.petemc.undeadnights.Config;
import net.petemc.undeadnights.UndeadNights;
import org.jetbrains.annotations.NotNull;

public class StateSaverAndLoader extends SavedData {
    private int daysCounter = Config.daysBetweenHordeNights;
    private int tickCounter = 60;
    private boolean hordeNight = false;
    private boolean spawnZombies = true;
    private boolean respawnZombies = false;

    // Setter and Getter functions
    public int getDaysCounter() {
        return this.daysCounter;
    }

    public void setDaysCounter(int val) {
        this.daysCounter = val;
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
    
    public static StateSaverAndLoader load(CompoundTag tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.daysCounter = tag.getInt("daysCounter");
        state.tickCounter = tag.getInt("tickCounter");
        state.hordeNight = tag.getBoolean("hordeNight");
        state.spawnZombies = tag.getBoolean("spawnZombies");
        state.respawnZombies = tag.getBoolean("respawnZombies");
        state.setDirty();
        return state;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.putInt("daysCounter", daysCounter);
        tag.putInt("tickCounter", tickCounter);
        tag.putBoolean("hordeNight", hordeNight);
        tag.putBoolean("spawnZombies", spawnZombies);
        tag.putBoolean("respawnZombies", respawnZombies);
        return tag;
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StateSaverAndLoader::load, StateSaverAndLoader::new, UndeadNights.MOD_ID);
    }

}
