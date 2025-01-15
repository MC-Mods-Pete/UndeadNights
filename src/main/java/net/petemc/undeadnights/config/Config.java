package net.petemc.undeadnights.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.undeadnights.UndeadNights;

@me.shedaniel.autoconfig.annotation.Config(name = UndeadNights.MOD_ID)
public class Config implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static Config INSTANCE;

    public static void init() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    public static boolean getUndeadNightsEnabled() {
        return INSTANCE.undeadNightsEnabled;
    }

    public static int getDaysBetweenHordeNights() {
        return INSTANCE.daysBetweenHordeNights;
    }

    public static int getChanceForHordeNight() {
        return INSTANCE.chanceForHordeNight;
    }

    public static boolean getSendHordeNightsCountdownMessage() {
        return INSTANCE.sendHordeNightsCountdownMessage;
    }

    public static int getDistanceMin() {
        return INSTANCE.distanceMin;
    }

    public static int getDistanceMax() {
        return INSTANCE.distanceMax;
    }

    public static int getZombieHordeWaveSize() {
        return INSTANCE.zombieHordeWaveSize;
    }

    public static int getHordeZombiesSpawnCap() {
        return INSTANCE.hordeZombiesSpawnCap;
    }

    public static boolean getSpawnAdditionalWaves() {
        return INSTANCE.spawnAdditionalWaves;
    }

    public static int getCooldownBetweenWaves() {
        return INSTANCE.cooldownBetweenWaves;
    }

    public static int getChanceForAdditionalWaves() {
        return INSTANCE.chanceForAdditionalWaves;
    }

    public static boolean getHordeNightsDisableSleeping() {
        return INSTANCE.hordeNightsDisableSleeping;
    }

    public static boolean getPersistentZombies() {
        return INSTANCE.persistentZombies;
    }

    public static boolean getZombiesBurnInDaylight() {
        return INSTANCE.zombiesBurnInDaylight;
    }

    public static boolean getSpawnDemolitionZombies() {
        return INSTANCE.spawnDemolitionZombies;
    }

    public static int getChanceForDemolitionZombieToSpawn() {
        return INSTANCE.chanceForDemolitionZombieToSpawn;
    }

    public static int getDemolitionZombieTntStackSize() {
        return INSTANCE.demolitionZombieTntStackSize;
    }

    public static boolean getSpawnEliteZombies() {
        return INSTANCE.spawnEliteZombies;
    }

    public static int getChanceForEliteZombieToSpawn() {
        return INSTANCE.chanceForEliteZombieToSpawn;
    }

    public static boolean getSpawnStrayHordeZombies() {
        return INSTANCE.spawnStrayHordeZombies;
    }

    public static boolean getPrintDebugMessages() {
        return INSTANCE.printDebugMessages;
    }

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Nights of the Undead (horde nights) are enabled | default: true")
    private boolean undeadNightsEnabled = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Days between horde nights (1 = every night is a horde night) | default: 5")
    private int daysBetweenHordeNights = 5;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for a horde night | default: 100")
    private int chanceForHordeNight = 100;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, each night a message will be sent to the player with how many nights are left before the next Horde Night | default: false")
    private boolean sendHordeNightsCountdownMessage = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Minimum distance a horde will spawn away from the player | default: 70")
    private int distanceMin = 70;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum distance a horde will spawn away from the player | default: 75")
    private int distanceMax = 75;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Size of a wave of zombies | default: 15")
    private int zombieHordeWaveSize = 15;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum amount of zombies that can be loaded in the world at the same time | default: 80")
    private int hordeZombiesSpawnCap = 80;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, additional waves can spawn in a horde night | default: true")
    private boolean spawnAdditionalWaves = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Time in seconds between check for next possible wave in a horde night | default: 45")
    private int cooldownBetweenWaves = 45;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for another zombie wave | default: 7")
    private int chanceForAdditionalWaves = 7;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Players can't sleep through horde nights | default: true")
    private boolean hordeNightsDisableSleeping = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde zombies will be persistent and not despawn | default: true")
    private boolean persistentZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde zombies will burn in daylight | default: false")
    private boolean zombiesBurnInDaylight = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, demolition zombies with TNT will spawn | default: true")
    private boolean spawnDemolitionZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for a demolition zombie to spawn | default: 6")
    private int chanceForDemolitionZombieToSpawn = 6;

    @ConfigEntry.Gui.Tooltip()
    @Comment("TNT Stack size a demolition zombie will spawn with (0 = unlimited) | default: 3")
    private int demolitionZombieTntStackSize = 3;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, elite zombies will spawn | default: true")
    private boolean spawnEliteZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for an elite zombie to spawn | default: 3")
    private int chanceForEliteZombieToSpawn = 3;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, single stray horde zombies can spawn on normal nights | default: true")
    private boolean spawnStrayHordeZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, debug messages will be logged out | default: false")
    private boolean printDebugMessages = false;
}
