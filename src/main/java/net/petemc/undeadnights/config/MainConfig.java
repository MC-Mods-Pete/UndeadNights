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

    public static int getGracePeriodBeforeFirstHordeNight() { return INSTANCE.gracePeriodBeforeFirstHordeNight; }

    public static int getDaysBetweenHordeNights() {
        return INSTANCE.daysBetweenHordeNights;
    }

    public static int getChanceForHordeNight() {
        return INSTANCE.chanceForHordeNight;
    }

    public static int getMaxHordesPerHordeNight() {
        return INSTANCE.maxHordesPerHordeNight;
    }

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

    public static boolean getBlockLightLevelsInfluenceMonsterSpawns() {
        return INSTANCE.blockLightLevelsInfluenceMonsterSpawns;
    }

    public static int getMaxBlockLightLevelForMonsterSpawns() {
        return INSTANCE.maxBlockLightLevelForMonsterSpawns;
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

    public static boolean getVanillaZombiesBurnInDaylight() {
        return INSTANCE.vanillaZombiesBurnInDaylight;
    }

    public static boolean getHordeZombiesCanPushEachOtherUp() {
        return INSTANCE.hordeZombiesCanPushEachOtherUp;
    }

    public static boolean getHordeZombiesCanBreakBlocks() {
        return INSTANCE.hordeZombiesCanBreakBlocks;
    }

    public static int getZombiesBlockBreakTier() {
        return INSTANCE.zombiesBlockBreakTier;
    }

    public static boolean getHordeZombiesHaveIncreasedWaterMovementSpeed() { return INSTANCE.hordeZombiesHaveIncreasedWaterMovementSpeed; }

    public static boolean getEnableRandomHordes() {
        return INSTANCE.enableRandomHordes;
    }

    public static int getChanceForRandomHordes() {
        return INSTANCE.chanceForRandomHordes;
    }

    public static boolean getEnableLureHordeEffect() {
        return INSTANCE.enableLureHordeEffect;
    }

    public static boolean getNonHordeZombiesCanCauseLureHordeEffect() { return INSTANCE.nonHordeZombiesCanCauseLureHordeEffect; }

    public static boolean getLureHordeEffectSpawnsHorde() { return INSTANCE.lureHordeEffectSpawnsHorde; }

    public static double getChanceForLureHordeEffect() {
        return INSTANCE.chanceForLureHordeEffect;
    }

    public static int getDurationForLureHordeEffect() {
        return INSTANCE.durationForLureHordeEffect;
    }

    public static double getChanceForLureEffectToSpawnHorde() {
        return INSTANCE.chanceForLureEffectToSpawnHorde;
    }

    public static boolean getHordeZombiesSpawnNaturally() {
        return INSTANCE.hordeZombiesSpawnNaturally;
    }

    public static boolean getEliteZombiesSpawnNaturally() {
        return INSTANCE.eliteZombiesSpawnNaturally;
    }

    public static boolean getDemolitionZombiesSpawnNaturally() {
        return INSTANCE.demolitionZombiesSpawnNaturally;
    }

    public static boolean getNoNaturalSpawningBeforeFirstHordeNight() { return INSTANCE.noNaturalSpawningBeforeFirstHordeNight; }

    public static double getMaxHealthHordeZombies() {
        return INSTANCE.maxHealthHordeZombies;
    }

    public static double getMaxHealthEliteZombies() {
        return INSTANCE.maxHealthEliteZombies;
    }

    public static double getMaxHealthDemolitionZombies() {
        return INSTANCE.maxHealthDemolitionZombies;
    }

    public static boolean getPrintDebugMessages() {
        return INSTANCE.printDebugMessages;
    }


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
    @Comment("Days between horde nights (1 = every night is a horde night) | default: 5")
    private int daysBetweenHordeNights = 5;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for a horde night | default: 100")
    private int chanceForHordeNight = 100;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum number of hordes that can spawn per horde night (0 = unlimited) | default: 0")
    private int maxHordesPerHordeNight = 0;

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
    @Comment("If true, the light level of the block position can prevent horde mobs from spawning | default: false")
    private boolean blockLightLevelsInfluenceMonsterSpawns = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Maximum block light level so a monster can spawn | default: 0")
    private int maxBlockLightLevelForMonsterSpawns = 0;

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
    @Comment("If true, the vanilla zombies will burn in daylight | default: true")
    private boolean vanillaZombiesBurnInDaylight = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, (Horde, Elite and vanilla) zombies can push each other up (WWZ style) | default: true")
    private boolean hordeZombiesCanPushEachOtherUp = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, (Horde, Elite and vanilla) zombies can break blocks | default: false")
    private boolean hordeZombiesCanBreakBlocks = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Horde Zombie block break tier (0-4) | default: 1")
    private int zombiesBlockBreakTier = 1;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, Horde Zombies (incl. vanilla) have increased water movement speed | default: false")
    private boolean hordeZombiesHaveIncreasedWaterMovementSpeed = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a random horde can spawn on none-horde nights | default: false")
    private boolean enableRandomHordes = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance in % for a random horde (checked once per night, on none-horde nights) | default: 15")
    private int chanceForRandomHordes = 15;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, killing a horde mob can give the player the lure horde effect (this will attract horde mobs in the area) | default: true")
    private boolean enableLureHordeEffect = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, killing zombies that were not spawned in a horde can also give the player the lure horde effect | default: true")
    private boolean nonHordeZombiesCanCauseLureHordeEffect = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance to get the lure horde effect, when the player kills a horde mob (1.0 = 100%) | default: 0.07")
    private double chanceForLureHordeEffect = 0.07;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Duration in seconds for the lure horde effect | default: 60")
    private int durationForLureHordeEffect = 60;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, getting the lure horde effect can spawn a horde | default: true")
    private boolean lureHordeEffectSpawnsHorde = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Chance for the lure horde effect to spawn a horde (1.0 = 100%) | default: 0.2")
    private double chanceForLureEffectToSpawnHorde = 0.2;

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
}
