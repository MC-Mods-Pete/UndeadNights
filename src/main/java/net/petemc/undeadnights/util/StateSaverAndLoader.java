package net.petemc.undeadnights.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.UndeadNightsConfig;

public class StateSaverAndLoader extends PersistentState {

    public int daysCounter = UndeadNightsConfig.INSTANCE.daysBetweenHordeNights;
    public int hordeSpawnCounter = 0;
    public int globalSpawnCountLastWave = 0;
    public int tickCounter = 60;
    public boolean hordeNight = false;
    public boolean spawnZombies = true;
    public boolean respawnZombies = false;
    public boolean lastWasDay;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("daysCounter", daysCounter);
        nbt.putInt("hordeSpawnCounter", hordeSpawnCounter);
        nbt.putInt("globalSpawnCountLastWave", globalSpawnCountLastWave);
        nbt.putInt("tickCounter", tickCounter);
        nbt.putBoolean("hordeNight", hordeNight);
        nbt.putBoolean("spawnZombies", spawnZombies);
        nbt.putBoolean("respawnZombies", respawnZombies);
        nbt.putBoolean("lastWasDay", lastWasDay);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        state.daysCounter = tag.getInt("daysCounter");
        state.hordeSpawnCounter = tag.getInt("hordeSpawnCounter");
        state.globalSpawnCountLastWave = tag.getInt("globalSpawnCountLastWave");
        state.tickCounter = tag.getInt("tickCounter");
        state.hordeNight = tag.getBoolean("hordeNight");
        state.spawnZombies = tag.getBoolean("spawnZombies");
        state.respawnZombies = tag.getBoolean("respawnZombies");
        state.lastWasDay = tag.getBoolean("lastWasDay");
        return state;
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
