package net.petemc.undeadnights.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.petemc.undeadnights.UndeadNights;

@Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MainConfig
{
    public static boolean getUndeadNightsEnabled() {
        return undeadNightsEnabled;
    }

    public static int getGracePeriodBeforeFirstHordeNight() { return gracePeriodBeforeFirstHordeNight; }

    public static int getMaxHordesPerHordeNight() {
        return maxHordesPerHordeNight;
    }

    public static boolean getSendHordeNightsCountdownMessage() {
        return sendHordeNightsCountdownMessage;
    }

    public static boolean getHordeSpawnedMessageAndSound() { return hordeSpawnedMessageAndSound; }

    public static int getDistanceMin() {
        return distanceMin;
    }

    public static int getDistanceMax() {
        return distanceMax;
    }

    public static int getHordeMobsSpawnCap() {
        return hordeMobsSpawnCap;
    }

    public static boolean getIgnoreDoMobSpawningGamerule() {
        return ignoreDoMobSpawningGamerule;
    }

    public static boolean getHordeWavesCanSpawnInWater() {
        return hordeWavesCanSpawnInWater;
    }

    public static boolean getHordeWavesCanSpawnOnTrees() {
        return hordeWavesCanSpawnOnTrees;
    }

    public static boolean getHordeWavesCanSpawnInCaves() {
        return hordeWavesCanSpawnInCaves;
    }

    public static int getCaveSpawnDistance() { return caveSpawnDistance; }

    public static boolean getBlockLightLevelsInfluenceMonsterSpawns() {
        return blockLightLevelsInfluenceMonsterSpawns;
    }

    public static int getMaxBlockLightLevelForMonsterSpawns() {
        return maxBlockLightLevelForMonsterSpawns;
    }

    public static boolean getHordeNightsDisableSleeping() {
        return hordeNightsDisableSleeping;
    }

    public static boolean getEnableAutomaticDifficultyProgression() { return enableAutomaticDifficultyProgression; }

    public static boolean getPersistentMobs() {
        return persistentMobs;
    }

    public static boolean getSecurityCraftCompatibility() {
        return securityCraftCompatibility;
    }

    public static boolean getHordeZombiesSpawnNaturally() {
        return hordeZombiesSpawnNaturally;
    }

    public static boolean getEliteZombiesSpawnNaturally() {
        return eliteZombiesSpawnNaturally;
    }

    public static boolean getDemolitionZombiesSpawnNaturally() {
        return demolitionZombiesSpawnNaturally;
    }

    public static boolean getNoNaturalSpawningBeforeFirstHordeNight() { return noNaturalSpawningBeforeFirstHordeNight; }

    public static double getMaxHealthHordeZombies() {
        return maxHealthHordeZombies;
    }

    public static double getMaxHealthEliteZombies() {
        return maxHealthEliteZombies;
    }

    public static double getMaxHealthDemolitionZombies() {
        return maxHealthDemolitionZombies;
    }

    public static boolean getPrintDebugMessages() {
        return printDebugMessages;
    }

    public static boolean getDebugMakeHordeMobsGlow() { return debugMakeHordeMobsGlow; }

    // Server Config
    private static final ForgeConfigSpec.Builder BUILDER_SERVER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue UNDEAD_NIGHTS_ENABLED = BUILDER_SERVER
            .comment("If true, Nights of the Undead (horde nights) are enabled | default: true")
            .define("undeadNightsEnabled", true);

    private static final ForgeConfigSpec.IntValue GRACE_PERIOD = BUILDER_SERVER
            .comment("Grace period in days before the first horde night | default: 0")
            .comment("After the grace period the first horde night will happen and")
            .comment("after the first horde night only the config values daysBetweenHordeNights")
            .comment("and chanceForHordeNight will be taken into consideration.")
            .comment("Note: if the grace period is set to 0 the first horde night will occur after")
            .comment("the number of days set with daysBetweenHordeNights below.")
            .defineInRange("gracePeriod", 0, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue MAX_HORDES_PER_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Maximum number of hordes that can spawn per horde night (0 = unlimited) | default: 0")
            .defineInRange("maxHordesPerHordeNight", 0, 0, 512);

    private static final ForgeConfigSpec.BooleanValue SEND_HORDE_NIGHTS_COUNTDOWN_MESSAGE = BUILDER_SERVER
            .comment("If true, each night a message will be sent to the player with how many nights are left before the next Horde Night | default: false")
            .define("sendHordeNightsCountdownMessage", false);

    private static final ForgeConfigSpec.BooleanValue HORDE_SPAWNED_MESSAGE_AND_SOUND = BUILDER_SERVER
            .comment("If true, the horde has spawned message and the horde sound are enabled | default: true")
            .define("hordeSpawnedMessageAndSound", true);

    private static final ForgeConfigSpec.IntValue DISTANCE_MIN = BUILDER_SERVER
            .comment("Minimum distance a horde will spawn away from the player | default: 70")
            .defineInRange("distanceMin", 70, 10, 256);

    private static final ForgeConfigSpec.IntValue DISTANCE_MAX = BUILDER_SERVER
            .comment("Maximum distance a horde will spawn away from the player | default: 75")
            .defineInRange("distanceMax", 75, 10, 256);

    private static final ForgeConfigSpec.IntValue HORDE_MOBS_SPAWN_CAP = BUILDER_SERVER
            .comment("Maximum amount of horde mobs that can be loaded in the world at the same time | default: 80")
            .defineInRange("hordeMobsSpawnCap", 80, 1, 2048);

    private static final ForgeConfigSpec.BooleanValue IGNORE_DO_MOB_SPAWNING_GAMERULE = BUILDER_SERVER
            .comment("If true, the vanilla gamerune doMobSpawning will be ignored | default: true")
            .define("ignoreDoMobSpawningGamerule", true);

    private static final ForgeConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_IN_WATER = BUILDER_SERVER
            .comment("If true, horde waves can spawn in water | default: false")
            .define("hordeWavesCanSpawnInWater", false);

    private static final ForgeConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_ON_TREES = BUILDER_SERVER
            .comment("If true, horde waves can spawn on trees | default: false")
            .define("hordeWavesCanSpawnOnTrees", false);

    private static final ForgeConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_IN_CAVES = BUILDER_SERVER
            .comment("If true, horde waves can spawn in caves [EXPERIMENTAL]| default: false")
            .define("hordeWavesCanSpawnInCaves", false);

    private static final ForgeConfigSpec.IntValue CAVE_SPAWN_DISTANCE = BUILDER_SERVER
            .comment("Distance a horde will spawn away from the player when in a cave | default: 50")
            .defineInRange("caveSpawnDistance", 50, 10, 100);

    private static final ForgeConfigSpec.BooleanValue BLOCK_LIGHT_LEVELS_INFLUENCE_MONSTER_SPAWNS = BUILDER_SERVER
            .comment("If true, the light level of the block position can prevent horde mobs from spawning | default: false")
            .define("blockLightLevelsInfluenceMonsterSpawns", false);

    private static final ForgeConfigSpec.IntValue MAX_BLOCK_LIGHT_LEVEL_FOR_MONSTER_SPAWNS = BUILDER_SERVER
            .comment("Maximum block light level so a monster can spawn | default: 0")
            .defineInRange("maxBlockLightLevelForMonsterSpawns", 0, 0, 256);

    private static final ForgeConfigSpec.BooleanValue HORDE_NIGHTS_DISABLE_SLEEPING = BUILDER_SERVER
            .comment("If true, Players can't sleep through horde nights | default: true")
            .define("hordeNightsDisableSleeping", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_AUTOMATIC_DIFFICULTY_PROGRESSION = BUILDER_SERVER
            .comment("If true, the difficulty level will automatically progress over time | default: false")
            .define("enableAutomaticDifficultyProgression", true);

    private static final ForgeConfigSpec.BooleanValue PERSISTENT_MOBS = BUILDER_SERVER
            .comment("If true, the horde zombies will be persistent and not despawn | default: false")
            .define("persistentMobs", false);

    private static final ForgeConfigSpec.BooleanValue SECURITY_CRAFT_COMPATIBILITY = BUILDER_SERVER
            .comment("If true, Horde Zombies are not able to break reinforced blocks from the Security Craft mod | default: false")
            .define("securityCraftCompatibility", false);

    private static final ForgeConfigSpec.BooleanValue HORDE_ZOMBIES_SPAWN_NATURALLY = BUILDER_SERVER
            .comment("If true, Horde Zombies will spawn naturally | default: true")
            .define("hordeZombiesSpawnNaturally", true);

    private static final ForgeConfigSpec.BooleanValue ELITE_ZOMBIES_SPAWN_NATURALLY = BUILDER_SERVER
            .comment("If true, Elite Zombies will spawn naturally | default: true")
            .define("eliteZombiesSpawnNaturally", true);

    private static final ForgeConfigSpec.BooleanValue DEMOLITION_ZOMBIES_SPAWN_NATURALLY = BUILDER_SERVER
            .comment("If true, Demolition Zombies will spawn naturally | default: true")
            .define("demolitionZombiesSpawnNaturally", true);

    private static final ForgeConfigSpec.BooleanValue NO_NATURAL_SPAWNING_BEFORE_FIRST_HORDE_NIGHT = BUILDER_SERVER
            .comment("If true, Horde, Elite and Demolition zombies will not spawn naturally before the first horde night | default: true")
            .define("noNaturalSpawningBeforeFirstHordeNight", true);

    private static final ForgeConfigSpec.DoubleValue MAX_HEALTH_HORDE_ZOMBIES = BUILDER_SERVER
            .comment("Horde zombie max health | default: 40.0")
            .defineInRange("maxHealthHordeZombies", 40.0, 1.0, 2048.0);

    private static final ForgeConfigSpec.DoubleValue MAX_HEALTH_ELITE_ZOMBIES = BUILDER_SERVER
            .comment("Elite zombie max health | default: 40.0")
            .defineInRange("maxHealthEliteZombies", 40.0, 1.0, 2048.0);

    private static final ForgeConfigSpec.DoubleValue MAX_HEALTH_DEMOLITION_ZOMBIES = BUILDER_SERVER
            .comment("Demolition zombie max health | default: 40.0")
            .defineInRange("maxHealthDemolitionZombies", 40.0, 1.0, 2048.0);

    private static final ForgeConfigSpec.BooleanValue PRINT_DEBUG_MESSAGES = BUILDER_SERVER
            .comment("If true, debug messages will be logged out | default: false")
            .define("printDebugMessages", false);

    private static final ForgeConfigSpec.BooleanValue DEBUG_MAKE_HORDE_MOBS_GLOW = BUILDER_SERVER
            .comment("If true, horde mods that are added with a horde glow (for debug) | default: false")
            .define("debugMakeHordeMobsGlow", false);

   public static final ForgeConfigSpec SPEC_SERVER = BUILDER_SERVER.build();

    // Client Config
    private static final ForgeConfigSpec.Builder BUILDER_CLIENT = new ForgeConfigSpec.Builder();
    // no client config
    public static final ForgeConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();


    private static boolean undeadNightsEnabled = true;
    private static int gracePeriodBeforeFirstHordeNight = 0;
    private static int maxHordesPerHordeNight = 0;
    private static boolean sendHordeNightsCountdownMessage = false;
    private static boolean hordeSpawnedMessageAndSound = true;
    private static int distanceMin = 70;
    private static int distanceMax = 75;
    private static int hordeMobsSpawnCap = 100;
    private static boolean ignoreDoMobSpawningGamerule = true;
    private static boolean hordeWavesCanSpawnInWater = false;
    private static boolean hordeWavesCanSpawnOnTrees = false;
    private static boolean hordeWavesCanSpawnInCaves = false;
    private static int caveSpawnDistance = 50;
    private static boolean blockLightLevelsInfluenceMonsterSpawns = false;
    private static int maxBlockLightLevelForMonsterSpawns = 0;
    private static boolean hordeNightsDisableSleeping = true;
    private static boolean enableAutomaticDifficultyProgression = true;
    private static boolean persistentMobs = false;
    private static boolean securityCraftCompatibility = false;
    private static boolean hordeZombiesSpawnNaturally = true;
    private static boolean eliteZombiesSpawnNaturally = true;
    private static boolean demolitionZombiesSpawnNaturally = true;
    private static boolean noNaturalSpawningBeforeFirstHordeNight = true;
    private static double maxHealthHordeZombies = 40.0D;
    private static double maxHealthEliteZombies = 40.0D;
    private static double maxHealthDemolitionZombies = 40.0D;
    private static boolean printDebugMessages = false;
    private static boolean debugMakeHordeMobsGlow = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (SPEC_SERVER.isLoaded()) {
            UndeadNights.LOGGER.info("Loading {} server config", UndeadNights.MOD_ID);
            undeadNightsEnabled = UNDEAD_NIGHTS_ENABLED.get();
            gracePeriodBeforeFirstHordeNight = GRACE_PERIOD.get();
            maxHordesPerHordeNight = MAX_HORDES_PER_HORDE_NIGHTS.get();
            sendHordeNightsCountdownMessage = SEND_HORDE_NIGHTS_COUNTDOWN_MESSAGE.get();
            hordeSpawnedMessageAndSound = HORDE_SPAWNED_MESSAGE_AND_SOUND.get();
            distanceMin = DISTANCE_MIN.get();
            distanceMax = DISTANCE_MAX.get();
            hordeMobsSpawnCap = HORDE_MOBS_SPAWN_CAP.get();
            ignoreDoMobSpawningGamerule = IGNORE_DO_MOB_SPAWNING_GAMERULE.get();
            hordeWavesCanSpawnInWater = HORDE_WAVES_CAN_SPAWN_IN_WATER.get();
            hordeWavesCanSpawnOnTrees = HORDE_WAVES_CAN_SPAWN_ON_TREES.get();
            hordeWavesCanSpawnInCaves = HORDE_WAVES_CAN_SPAWN_IN_CAVES.get();
            caveSpawnDistance = CAVE_SPAWN_DISTANCE.get();
            blockLightLevelsInfluenceMonsterSpawns = BLOCK_LIGHT_LEVELS_INFLUENCE_MONSTER_SPAWNS.get();
            maxBlockLightLevelForMonsterSpawns = MAX_BLOCK_LIGHT_LEVEL_FOR_MONSTER_SPAWNS.get();
            hordeNightsDisableSleeping = HORDE_NIGHTS_DISABLE_SLEEPING.get();
            enableAutomaticDifficultyProgression = ENABLE_AUTOMATIC_DIFFICULTY_PROGRESSION.get();
	        persistentMobs = PERSISTENT_MOBS.get();
            securityCraftCompatibility = SECURITY_CRAFT_COMPATIBILITY.get();
            hordeZombiesSpawnNaturally = HORDE_ZOMBIES_SPAWN_NATURALLY.get();
            eliteZombiesSpawnNaturally = ELITE_ZOMBIES_SPAWN_NATURALLY.get();
            demolitionZombiesSpawnNaturally = DEMOLITION_ZOMBIES_SPAWN_NATURALLY.get();
            noNaturalSpawningBeforeFirstHordeNight = NO_NATURAL_SPAWNING_BEFORE_FIRST_HORDE_NIGHT.get();
            maxHealthHordeZombies = MAX_HEALTH_HORDE_ZOMBIES.get();
            maxHealthEliteZombies = MAX_HEALTH_ELITE_ZOMBIES.get();
            maxHealthDemolitionZombies = MAX_HEALTH_DEMOLITION_ZOMBIES.get();
            printDebugMessages = PRINT_DEBUG_MESSAGES.get();
            debugMakeHordeMobsGlow = DEBUG_MAKE_HORDE_MOBS_GLOW.get();
        }
        if (SPEC_CLIENT.isLoaded()) {
            UndeadNights.LOGGER.info("Loading {} client config", UndeadNights.MOD_ID);
           // no client config
        }
    }
}
