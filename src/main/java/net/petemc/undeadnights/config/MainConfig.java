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

    public static void loadConfig() {
        AutoConfig.register(MainConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(MainConfig.class).getConfig();
    }

    public static boolean getUndeadNightsEnabled() { return INSTANCE.undeadNightsEnabled; }

    public static int getGracePeriodBeforeFirstHordeNight() { return INSTANCE.gracePeriodBeforeFirstHordeNight; }

    public static boolean getSendHordeNightsCountdownMessage() {
        return INSTANCE.sendHordeNightsCountdownMessage;
    }

    public static boolean getHordeSpawnedMessageAndSound() { return INSTANCE.hordeSpawnedMessageAndSound; }

    public static int getDistanceMin() {
        return INSTANCE.distanceMin;
    }

    public static int getDistanceMax() {
        return INSTANCE.distanceMax;
    }

    public static int getHordeMobsSpawnCap() {
        return INSTANCE.hordeMobsSpawnCap;
    }

    public static boolean getIgnoreDoMobSpawningGamerule() {
        return INSTANCE.ignoreDoMobSpawningGamerule;
    }

    public static boolean getHordeWavesCanSpawnInWater() {
        return INSTANCE.hordeWavesCanSpawnInWater;
    }

    public static boolean getHordeWavesCanSpawnOnTrees() {
        return INSTANCE.hordeWavesCanSpawnOnTrees;
    }

    public static boolean getHordeWavesCanSpawnInCaves() {
        return INSTANCE.hordeWavesCanSpawnInCaves;
    }

    public static int getCaveSpawnDistance() { return INSTANCE.caveSpawnDistance; }

    public static boolean getBlockLightLevelsInfluenceMonsterSpawns() { return INSTANCE.blockLightLevelsInfluenceMonsterSpawns; }

    public static int getMaxBlockLightLevelForMonsterSpawns() { return INSTANCE.maxBlockLightLevelForMonsterSpawns; }

    public static boolean getHordeNightsDisableSleeping() { return INSTANCE.hordeNightsDisableSleeping; }

    public static boolean getEnableAutomaticDifficultyProgression() { return INSTANCE.enableAutomaticDifficultyProgression; }

    public static boolean getPreventDespawnWhenInsideMaxTrackingRange() { return INSTANCE.preventDespawnWhenInsideMaxTrackingRange; }

    public static boolean getPersistentMobs() { return INSTANCE.persistentMobs; }

    public static boolean getHordeZombiesSpawnNaturally() { return INSTANCE.hordeZombiesSpawnNaturally; }

    public static boolean getEliteZombiesSpawnNaturally() { return INSTANCE.eliteZombiesSpawnNaturally; }

    public static boolean getDemolitionZombiesSpawnNaturally() { return INSTANCE.demolitionZombiesSpawnNaturally; }

    public static boolean getNoNaturalSpawningBeforeFirstHordeNight() { return INSTANCE.noNaturalSpawningBeforeFirstHordeNight; }

    public static double getMaxHealthHordeZombies() { return INSTANCE.maxHealthHordeZombies; }

    public static double getMaxHealthEliteZombies() { return INSTANCE.maxHealthEliteZombies; }

    public static double getMaxHealthDemolitionZombies() { return INSTANCE.maxHealthDemolitionZombies; }

    public static boolean getPrintDebugMessages() { return INSTANCE.printDebugMessages; }

    public static boolean getDebugMakeHordeMobsGlow() { return INSTANCE.debugMakeHordeMobsGlow; }


    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Nights of the Undead (horde nights) are enabled | default: true")
    private boolean undeadNightsEnabled = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("""
            Grace period in days before the first horde night | default: 0)
            After the grace period the first horde night will happen and)
            after the first horde night only the config values daysBetweenHordeNights")
            and chanceForHordeNight will be taken into consideration.")
            Note: if the grace period is set to 0 the first horde night will occur after")
            the number of days set with daysBetweenHordeNights below.")""")
    private int gracePeriodBeforeFirstHordeNight = 0;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, each night a message will be sent to the player with how many nights are left before the next Horde Night | default: false")
    private boolean sendHordeNightsCountdownMessage = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde has spawned message and the horde sound are enabled | default: true")
    private boolean hordeSpawnedMessageAndSound = true;

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
    @Comment("If true, the DoMobSpawning gamerule will be ignored for horde mob spawning | default: true")
    private boolean ignoreDoMobSpawningGamerule = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde waves can spawn in water | default: false")
    private boolean hordeWavesCanSpawnInWater = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde waves can spawn on trees | default: false")
    private boolean hordeWavesCanSpawnOnTrees = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde waves can spawn in caves | default: false")
    private boolean hordeWavesCanSpawnInCaves = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Minimum distance from player for cave spawns (only used if hordeWavesCanSpawnInCaves is true) | default: 50")
    private int caveSpawnDistance = 50;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the light level of the block position can prevent horde mobs from spawning | default: false")
    private boolean blockLightLevelsInfluenceMonsterSpawns = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum block light level so a monster can spawn | default: 0")
    private int maxBlockLightLevelForMonsterSpawns = 0;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Players can't sleep through horde nights | default: true")
    private boolean hordeNightsDisableSleeping = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the automatic difficulty progression is enabled | default: true")
    private boolean enableAutomaticDifficultyProgression = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde mobs will not despawn when inside the max tracking range of a player | default: false")
    private boolean preventDespawnWhenInsideMaxTrackingRange = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the horde mobs will be persistent and not despawn | default: false")
    private boolean persistentMobs = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Horde Zombies will spawn naturally | default: true")
    private boolean hordeZombiesSpawnNaturally = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Elite Zombies will spawn naturally | default: true")
    private boolean eliteZombiesSpawnNaturally = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Demolition Zombies will spawn naturally | default: true")
    private boolean demolitionZombiesSpawnNaturally = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Horde, Elite and Demolition zombies will not spawn naturally before the first horde night | default: true")
    private boolean noNaturalSpawningBeforeFirstHordeNight = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Horde zombie max health | default: 40.0")
    private double maxHealthHordeZombies = 40.0D;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Elite zombie max health | default: 40.0")
    private double maxHealthEliteZombies = 40.0D;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Demolition zombie max health | default: 40.0")
    private double maxHealthDemolitionZombies = 40.0D;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, debug messages will be logged out | default: false")
    private boolean printDebugMessages = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, horde mobs will glow (for debugging purposes) | default: false")
    private boolean debugMakeHordeMobsGlow = false;
}
