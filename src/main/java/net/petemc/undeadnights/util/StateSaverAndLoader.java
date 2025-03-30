package net.petemc.undeadnights.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
    private Integer daysCounter;
    private Integer lastMaxDaysCounter;
    private Integer tickCounter;
    private Boolean hordeNight;
    private Boolean spawnZombies;
    private Boolean respawnZombies;
    public Map<UUID, String> spawnedHordeMobs;
    public Map<UUID, String> hordeMobsToRemove;

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("daysCounter").forGetter(state -> state.daysCounter),
                    Codec.INT.fieldOf("lastMaxDaysCounter").forGetter(state -> state.lastMaxDaysCounter),
                    Codec.INT.fieldOf("tickCounter").forGetter(state -> state.tickCounter),
                    Codec.BOOL.fieldOf("hordeNight").forGetter(state -> state.hordeNight),
                    Codec.BOOL.fieldOf("spawnZombies").forGetter(state -> state.spawnZombies),
                    Codec.BOOL.fieldOf("respawnZombies").forGetter(state -> state.respawnZombies),
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.STRING).fieldOf("spawnedHordeMobs").forGetter(state -> state.spawnedHordeMobs),
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.STRING).fieldOf("hordeMobsToRemove").forGetter(state -> state.hordeMobsToRemove)
            ).apply(instance, StateSaverAndLoader::new)
    );

    public static SavedDataType<StateSaverAndLoader> createStateType() {
        return new SavedDataType<>(UndeadNights.MOD_ID + "_data", StateSaverAndLoader::new, CODEC, null);
    }

    public StateSaverAndLoader() {
        this(
                MainConfig.getDaysBetweenHordeNights(),
                MainConfig.getDaysBetweenHordeNights(),
                60,
                false,
                true,
                false,
                new HashMap<UUID, String>(),
                new HashMap<UUID,String>()
        );
    }

    public StateSaverAndLoader(
            Integer daysCounter,
            Integer lastMaxDaysCounter,
            Integer tickCounter,
            Boolean hordeNight,
            Boolean spawnZombies,
            Boolean respawnZombies,
            Map<UUID, String> spawnedHordeMobs,
            Map<UUID, String> hordeMobsToRemove
    )
    {
        this.daysCounter = daysCounter;
        this.lastMaxDaysCounter = lastMaxDaysCounter;
        this.tickCounter = tickCounter;
        this.hordeNight = hordeNight;
        this.spawnZombies = spawnZombies;
        this.respawnZombies = respawnZombies;
        this.spawnedHordeMobs = new HashMap<>(spawnedHordeMobs);
        this.hordeMobsToRemove = new HashMap<>(hordeMobsToRemove);
        this.setDirty();
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
}
