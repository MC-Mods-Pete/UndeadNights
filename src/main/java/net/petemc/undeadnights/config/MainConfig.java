package net.petemc.undeadnights.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.undeadnights.UndeadNights;

@me.shedaniel.autoconfig.annotation.Config(name = UndeadNights.MOD_ID)
public class MainConfig implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static MainConfig INSTANCE;

    public static void init() {
        AutoConfig.register(MainConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(MainConfig.class).getConfig();
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

    public static int getHordeMobsSpawnCap() {
        return INSTANCE.hordeMobsSpawnCap;
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

    public static boolean getHordeWavesCanSpawnInWater() {
        return INSTANCE.hordeWavesCanSpawnInWater;
    }

    public static boolean getHordeWavesCanSpawnOnTrees() {
        return INSTANCE.hordeWavesCanSpawnOnTrees;
    }

    public static boolean getHordeNightsDisableSleeping() {
        return INSTANCE.hordeNightsDisableSleeping;
    }

    public static boolean getPersistentMobs() {
        return INSTANCE.persistentMobs;
    }

    public static boolean getHordeZombiesBurnInDaylight() {
        return INSTANCE.hordeZombiesBurnInDaylight;
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
    @Comment("Maximum amount of horde mobs that can be loaded in the world at the same time | default: 80")
    private int hordeMobsSpawnCap = 80;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, additional waves can spawn in a horde night | default: true")
    private boolean spawnAdditionalWaves = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Time in seconds between check for next possible wave in a horde night | default: 45")
    private int cooldownBetweenWaves = 45;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for another horde wave | default: 7")
    private int chanceForAdditionalWaves = 7;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde waves can spawn in water | default: false")
    private boolean hordeWavesCanSpawnInWater = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde waves can spawn on trees | default: false")
    private boolean hordeWavesCanSpawnOnTrees = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Players can't sleep through horde nights | default: true")
    private boolean hordeNightsDisableSleeping = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde mobs will be persistent and not despawn | default: false")
    private boolean persistentMobs = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde zombies will burn in daylight (only those added by this mod) | default: false")
    private boolean hordeZombiesBurnInDaylight = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, single stray horde zombies can spawn on normal nights | default: true")
    private boolean spawnStrayHordeZombies = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, debug messages will be logged out | default: false")
    private boolean printDebugMessages = false;
}
