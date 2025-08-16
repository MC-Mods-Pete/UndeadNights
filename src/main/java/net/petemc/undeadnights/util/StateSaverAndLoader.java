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
    // All integer data elements in one class
    public static class IntegerCollection {
        private Integer daysCounter;
        private Integer lastMaxDaysCounter;
        private Integer gracePeriod;
        private Integer lastMaxGracePeriod;
        private Integer hordesCounter;
        private Integer lastMaxHordesCounter;
        private Integer tickCounter;

        public IntegerCollection() {
            this(
                    MainConfig.getDaysBetweenHordeNights(),
                    MainConfig.getDaysBetweenHordeNights(),
                    MainConfig.getGracePeriodBeforeFirstHordeNight(),
                    MainConfig.getGracePeriodBeforeFirstHordeNight(),
                    MainConfig.getDaysBetweenHordeNights() + 1,
                    MainConfig.getDaysBetweenHordeNights(),
                    60
            );
        }

        public IntegerCollection(
                Integer daysCounter,
                Integer lastMaxDaysCounter,
                Integer gracePeriod,
                Integer lastMaxGracePeriod,
                Integer hordesCounter,
                Integer lastMaxHordesCounter,
                Integer tickCounter
        ) {
            this.daysCounter = daysCounter;
            this.lastMaxDaysCounter = lastMaxDaysCounter;
            this.gracePeriod = gracePeriod;
            this.lastMaxGracePeriod = lastMaxGracePeriod;
            this.hordesCounter = hordesCounter;
            this.lastMaxHordesCounter = lastMaxHordesCounter;
            this.tickCounter = tickCounter;
        }
    }

    // All boolean data elements in one class
    public static class BooleanCollection {
        private Boolean hordeNight;
        private Boolean nightIsStarting;
        private Boolean firstWaveHasSpawned;
        private Boolean spawnZombies;
        private Boolean respawnZombies;
        private Boolean tryToSpawnRandomHorde;
        private Boolean isNaturalSpawningOk;
        private Boolean firstEliteZombieHasSpawned;
        private Boolean firstDemolitionZombieHasSpawned;

        public BooleanCollection() {
            this(
                    false,
                    false,
                    false,
                    true,
                    false,
                    true,
                    false,
                    false,
                    false
            );
        }

        public BooleanCollection(
                Boolean hordeNight,
                Boolean nightIsStarting,
                Boolean firstWaveHasSpawned,
                Boolean spawnZombies,
                Boolean respawnZombies,
                Boolean tryToSpawnRandomHorde,
                Boolean isNaturalSpawningOk,
                Boolean firstEliteZombieHasSpawned,
                Boolean firstDemolitionZombieHasSpawned
        ) {
            this.hordeNight = hordeNight;
            this.nightIsStarting = nightIsStarting;
            this.firstWaveHasSpawned = firstWaveHasSpawned;
            this.spawnZombies = spawnZombies;
            this.respawnZombies = respawnZombies;
            this.tryToSpawnRandomHorde = tryToSpawnRandomHorde;
            this.isNaturalSpawningOk = isNaturalSpawningOk;
            this.firstEliteZombieHasSpawned = firstEliteZombieHasSpawned;
            this.firstDemolitionZombieHasSpawned = firstDemolitionZombieHasSpawned;
        }
    }

    public static final Codec<IntegerCollection> INTEGER_COLLECTION_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("daysCounter").forGetter(state -> state.daysCounter),
                    Codec.INT.fieldOf("lastMaxDaysCounter").forGetter(state -> state.lastMaxDaysCounter),
                    Codec.INT.fieldOf("gracePeriod").forGetter(state -> state.gracePeriod),
                    Codec.INT.fieldOf("lastMaxGracePeriod").forGetter(state -> state.lastMaxGracePeriod),
                    Codec.INT.fieldOf("hordesCounter").forGetter(state -> state.hordesCounter),
                    Codec.INT.fieldOf("lastMaxHordesCounter").forGetter(state -> state.lastMaxHordesCounter),
                    Codec.INT.fieldOf("tickCounter").forGetter(state -> state.tickCounter)
            ).apply(instance, IntegerCollection::new)
    );

    public static final Codec<BooleanCollection> BOOLEAN_COLLECTION_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("hordeNight").forGetter(state -> state.hordeNight),
                    Codec.BOOL.fieldOf("nightIsStarting").forGetter(state -> state.nightIsStarting),
                    Codec.BOOL.fieldOf("firstWaveHasSpawned").forGetter(state -> state.firstWaveHasSpawned),
                    Codec.BOOL.fieldOf("spawnZombies").forGetter(state -> state.spawnZombies),
                    Codec.BOOL.fieldOf("respawnZombies").forGetter(state -> state.respawnZombies),
                    Codec.BOOL.fieldOf("tryToSpawnRandomHorde").forGetter(state -> state.tryToSpawnRandomHorde),
                    Codec.BOOL.fieldOf("isNaturalSpawningOk").forGetter(state -> state.isNaturalSpawningOk),
                    Codec.BOOL.fieldOf("firstEliteZombieHasSpawned").forGetter(state -> state.firstEliteZombieHasSpawned),
                    Codec.BOOL.fieldOf("firstDemolitionZombieHasSpawned").forGetter(state -> state.firstDemolitionZombieHasSpawned)
            ).apply(instance, BooleanCollection::new)
    );


    private final IntegerCollection integerCollection;
    private final BooleanCollection booleanCollection;
    private Long prevNormalizedTimeOfDay;
    public Map<UUID, String> spawnedHordeMobs;
    public Map<UUID, String> hordeMobsToRemove;
    public Map<UUID, String> entitiesWithPendingHorde;
    public Map<UUID, String> entitiesWithReceivedHorde;

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    INTEGER_COLLECTION_CODEC.fieldOf("integerCollection").forGetter(state -> state.integerCollection),
                    BOOLEAN_COLLECTION_CODEC.fieldOf("booleanCollection").forGetter(state -> state.booleanCollection),
                    Codec.LONG.fieldOf("prevNormalizedTimeOfDay").forGetter(state -> state.prevNormalizedTimeOfDay),
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.STRING).fieldOf("spawnedHordeMobs").forGetter(state -> state.spawnedHordeMobs),
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.STRING).fieldOf("hordeMobsToRemove").forGetter(state -> state.hordeMobsToRemove),
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.STRING).fieldOf("entitiesWithPendingHorde").forGetter(state -> state.entitiesWithPendingHorde),
                    Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC, Codec.STRING).fieldOf("entitiesWithReceivedHorde").forGetter(state -> state.entitiesWithReceivedHorde)
            ).apply(instance, StateSaverAndLoader::new)
    );

    public static SavedDataType<StateSaverAndLoader> createStateType() {
        return new SavedDataType<>(UndeadNights.MOD_ID + "_data", StateSaverAndLoader::new, CODEC, null);
    }

    public StateSaverAndLoader() {
        this(
                new IntegerCollection(),
                new BooleanCollection(),
                (long) 0,
                new HashMap<UUID, String>(),
                new HashMap<UUID, String>(),
                new HashMap<UUID, String>(),
                new HashMap<UUID, String>()
        );
    }

    public StateSaverAndLoader(
            IntegerCollection integerCollection,
            BooleanCollection booleanCollection,
            Long prevNormalizedTimeOfDay,
            Map<UUID, String> spawnedHordeMobs,
            Map<UUID, String> hordeMobsToRemove,
            Map<UUID, String> entitiesWithPendingHorde,
            Map<UUID, String> entitiesWithReceivedHorde
    ) {
        this.integerCollection = integerCollection;
        this.booleanCollection = booleanCollection;
        this.prevNormalizedTimeOfDay = prevNormalizedTimeOfDay;
        this.spawnedHordeMobs = new HashMap<>(spawnedHordeMobs);
        this.hordeMobsToRemove = new HashMap<>(hordeMobsToRemove);
        this.entitiesWithPendingHorde = new HashMap<>(entitiesWithPendingHorde);
        this.entitiesWithReceivedHorde = new HashMap<>(entitiesWithReceivedHorde);
        this.setDirty();
    }

    // Setter and Getter functions
    public int getDaysCounter() {
        return this.integerCollection.daysCounter;
    }

    public void setDaysCounter(int val) {
        this.integerCollection.daysCounter = val;
        this.setDirty();
    }

    public int getLastMaxDaysCounter() {
        return this.integerCollection.lastMaxDaysCounter;
    }

    public void setLastMaxDaysCounter(int val) {
        this.integerCollection.lastMaxDaysCounter = val;
        this.setDirty();
    }

    public int getGracePeriod() {
        return this.integerCollection.gracePeriod;
    }

    public void setGracePeriod(int val) {
        this.integerCollection.gracePeriod = val;
        this.setDirty();
    }

    public int getLastMaxGracePeriod() {
        return this.integerCollection.lastMaxGracePeriod;
    }

    public void setLastMaxGracePeriod(int val) {
        this.integerCollection.lastMaxGracePeriod = val;
        this.setDirty();
    }

    public int getHordesCounter() {
        return this.integerCollection.hordesCounter;
    }

    public void setHordesCounter(int val) {
        this.integerCollection.hordesCounter = val;
        this.setDirty();
    }

    public int getLastMaxHordesCounter() {
        return this.integerCollection.lastMaxHordesCounter;
    }

    public void setLastMaxHordesCounter(int val) {
        this.integerCollection.lastMaxHordesCounter = val;
        this.setDirty();
    }

    public int getTickCounter() {
        return this.integerCollection.tickCounter;
    }

    public void setTickCounter(int val) {
        this.integerCollection.tickCounter = val;
        this.setDirty();
    }


    public boolean getHordeNight() {
        return this.booleanCollection.hordeNight;
    }

    public void setHordeNight(boolean val) {
        this.booleanCollection.hordeNight = val;
        this.setDirty();
    }

    public boolean getNightIsStarting() {
        return this.booleanCollection.nightIsStarting;
    }

    public void setNightIsStarting(boolean val) {
        this.booleanCollection.nightIsStarting = val;
        this.setDirty();
    }

    public boolean getFirstWaveHasSpawned() {
        return this.booleanCollection.firstWaveHasSpawned;
    }

    public void setFirstWaveHasSpawned(boolean val) {
        this.booleanCollection.firstWaveHasSpawned = val;
        this.setDirty();
    }

    public boolean getSpawnZombies() {
        return this.booleanCollection.spawnZombies;
    }

    public void setSpawnZombies(boolean val) {
        this.booleanCollection.spawnZombies = val;
        this.setDirty();
    }

    public boolean getRespawnZombies() {
        return this.booleanCollection.respawnZombies;
    }

    public void setRespawnZombies(boolean val) {
        this.booleanCollection.respawnZombies = val;
        this.setDirty();
    }

    public boolean getTryToSpawnRandomHorde() {
        return this.booleanCollection.tryToSpawnRandomHorde;
    }

    public void setTryToSpawnRandomHorde(boolean val) {
        this.booleanCollection.tryToSpawnRandomHorde = val;
        this.setDirty();
    }

    public boolean getIsNaturalSpawningOk() {
        return this.booleanCollection.isNaturalSpawningOk;
    }

    public void setIsNaturalSpawningOk(boolean val) {
        this.booleanCollection.isNaturalSpawningOk = val;
        this.setDirty();
    }

    public boolean getFirstEliteZombieHasSpawned() {
        return this.booleanCollection.firstEliteZombieHasSpawned;
    }

    public void setFirstEliteZombieHasSpawned(boolean val) {
        this.booleanCollection.firstEliteZombieHasSpawned = val;
        this.setDirty();
    }

    public boolean getFirstDemolitionZombieHasSpawned() {
        return this.booleanCollection.firstDemolitionZombieHasSpawned;
    }

    public void setFirstDemolitionZombieHasSpawned(boolean val) {
        this.booleanCollection.firstDemolitionZombieHasSpawned = val;
        this.setDirty();
    }

    public long getPrevNormalizedTimeOfDay() {
        return this.prevNormalizedTimeOfDay;
    }

    public void setPrevNormalizedTimeOfDay(long val) {
        this.prevNormalizedTimeOfDay = val;
        this.setDirty();
    }
}
