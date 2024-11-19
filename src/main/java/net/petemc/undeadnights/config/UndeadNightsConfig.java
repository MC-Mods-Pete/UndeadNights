package net.petemc.undeadnights.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.undeadnights.UndeadNights;

@Config(name = UndeadNights.MOD_ID)
public class UndeadNightsConfig implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static UndeadNightsConfig INSTANCE;

    public static void init() {
        AutoConfig.register(UndeadNightsConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(UndeadNightsConfig.class).getConfig();
        /*
        if (INSTANCE.infectionDuration < 0) {
            UndeadNights.LOGGER.warn("Value for infectionDuration is smaller than 0, using default.");
            INSTANCE.infectionDuration = 600;
        }
        if ((INSTANCE.baseInfectionChance < 0) || (INSTANCE.baseInfectionChance > 100)) {
            UndeadNights.LOGGER.warn("Value for baseInfectionChance not in range (valid range 0 - 100), using default.");
            INSTANCE.baseInfectionChance = 80;
        }
        if ((INSTANCE.minimumInfectionChance < 0) || (INSTANCE.minimumInfectionChance > 100)) {
            UndeadNights.LOGGER.warn("Value for minimumInfectionChance not in range (valid range 0 - 100), using default.");
            INSTANCE.minimumInfectionChance = 10;
        }
        if (INSTANCE.randomSymptomsDuration < 0) {
            UndeadNights.LOGGER.warn("Value for randomSymptomsDuration is smaller than 0, using default.");
            INSTANCE.randomSymptomsDuration = 30;
        }
        if ((INSTANCE.randomSymptomsChance < 0) || (INSTANCE.randomSymptomsChance > 100)) {
            UndeadNights.LOGGER.warn("Value for randomSymptomsChance not in range (valid range 0 - 100), using default.");
            INSTANCE.randomSymptomsChance = 3;
        }
        if (INSTANCE.immunityDuration < 0) {
            UndeadNights.LOGGER.warn("Value for immunityDuration is smaller than 0, using default.");
            INSTANCE.immunityDuration = 120;
        }*/
    }

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Nights of the Undead (horde nights) are enabled | default: true")
    public boolean undeadNightsEnabled = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Days between horde nights (1 = every night is a horde night) | default: 5")
    public int daysBetweenHordeNights = 5;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for a horde night | default: 100")
    public int chanceForHordeNight = 100;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Minimum distance a horde will spawn away from the player | default: 70")
    public int distanceMin = 70;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum distance a horde will spawn away from the player | default: 75")
    public int distanceMax = 75;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Size of a wave of zombies | default: 15")
    public int zombieHordeWaveSize = 15;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum amount of zombies that can be loaded in the world at the same time | default: 80")
    public int hordeZombiesSpawnCap = 80;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Time in seconds between possible additional waves in a horde night | default: 70")
    public int cooldownBetweenWaves = 70;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for another zombie wave | default: 5")
    public int chanceForAdditionalWaves = 5;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Time in seconds between checks for a new wave (see value above) | default: 30")
    public int timeBetweenChecksForNewWaves = 30;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde zombies will be persistent and not despawn | default: true")
    public boolean persistentZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde zombies will burn in daylight | default: false")
    public boolean zombiesBurnInDaylight = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, demolition zombies with TNT will spawn | default: true")
    public boolean spawnDemolitionZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for a demolition zombie to spawn | default: 6")
    public int chanceForDemolitionZombieToSpawn = 6;

    @ConfigEntry.Gui.Tooltip()
    @Comment("TNT Stack size a demolition zombie will spawn with (0 = unlimited) | default: 3")
    public int demolitionZombieTntStackSize = 3;
}
