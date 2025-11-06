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

    public static boolean getHordeWavesCanSpawnInWater() {
        return hordeWavesCanSpawnInWater;
    }

    public static boolean getHordeWavesCanSpawnOnTrees() {
        return hordeWavesCanSpawnOnTrees;
    }

    public static boolean getHordeWavesCanSpawnInCaves() {
        return hordeWavesCanSpawnInCaves;
    }

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

    private static final ForgeConfigSpec.IntValue DAYS_BETWEEN_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Days between horde nights (1 = every night is a horde night) | default: 5")
            .defineInRange("daysBetweenHordeNights", 5, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CHANCE_FOR_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Chance in % for a horde night | default: 100")
            .defineInRange("chanceForHordeNight", 100, 1, 100);

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

    private static final ForgeConfigSpec.BooleanValue SPAWN_ADDITIONAL_WAVES = BUILDER_SERVER
            .comment("If true, additional waves can spawn in a horde night | default: true")
            .define("spawnAdditionalWaves", true);

    private static final ForgeConfigSpec.IntValue COOLDOWN_BETWEEN_WAVES = BUILDER_SERVER
            .comment("Time in seconds between check for next possible wave in a horde night | default: 45")
            .defineInRange("cooldownBetweenWaves", 45, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CHANCE_FOR_ADDITIONAL_WAVES = BUILDER_SERVER
            .comment("Chance in % for another zombie wave (checked after every wave cooldown) | default: 7")
            .defineInRange("chanceForAdditionalWaves", 7, 1, 100);

    private static final ForgeConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_IN_WATER = BUILDER_SERVER
            .comment("If true, horde waves can spawn in water | default: false")
            .define("hordeWavesCanSpawnInWater", false);

    private static final ForgeConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_ON_TREES = BUILDER_SERVER
            .comment("If true, horde waves can spawn on trees | default: false")
            .define("hordeWavesCanSpawnOnTrees", false);

    private static final ForgeConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_IN_CAVES = BUILDER_SERVER
            .comment("If true, horde waves can spawn in caves | default: false")
            .define("hordeWavesCanSpawnInCaves", false);

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

    private static final ForgeConfigSpec.BooleanValue HORDE_ZOMBIES_BURN_IN_DAYLIGHT = BUILDER_SERVER
            .comment("If true, the horde zombies (only those added by this mod) will burn in daylight | default: false")
            .define("hordeZombiesBurnInDaylight", false);

    private static final ForgeConfigSpec.BooleanValue VANILLA_ZOMBIES_BURN_IN_DAYLIGHT = BUILDER_SERVER
            .comment("If true, the vanilla zombies will burn in daylight | default: true")
            .define("vanillaZombiesBurnInDaylight", true);

    private static final ForgeConfigSpec.BooleanValue HORDE_ZOMBIES_CAN_PUSH_EACH_OTHER_UP = BUILDER_SERVER
            .comment("If true, (Horde, Elite and vanilla) zombies can push each other up (WWZ style) | default: true")
            .define("hordeZombiesCanPushEachOtherUp", true);

    private static final ForgeConfigSpec.BooleanValue HORDE_ZOMBIES_CAN_BREAK_BLOCKS = BUILDER_SERVER
            .comment("If true, (Horde, Elite and vanilla) zombies can break blocks | default: false")
            .define("hordeZombiesCanBreakBlocks", false);

    private static final ForgeConfigSpec.IntValue ZOMBIES_BLOCK_BREAK_TIER = BUILDER_SERVER
            .comment("Zombie block break tier (0-4) | default: 1")
            .defineInRange("zombieBlockBreakTier", 1, 0, 4);

    private static final ForgeConfigSpec.BooleanValue HORDE_ZOMBIES_HAVE_INCREASED_WATER_MOVEMENT_SPEED = BUILDER_SERVER
            .comment("If true, Horde Zombies (incl. vanilla) have increased water movement speed | default: false")
            .define("hordeZombiesHaveIncreasedWaterMovementSpeed", false);

    private static final ForgeConfigSpec.BooleanValue SECURITY_CRAFT_COMPATIBILITY = BUILDER_SERVER
            .comment("If true, Horde Zombies are not able to break reinforced blocks from the Security Craft mod | default: false")
            .define("securityCraftCompatibility", false);

    private static final ForgeConfigSpec.BooleanValue ENABLE_RANDOM_HORDES = BUILDER_SERVER
            .comment("If true, a random horde can spawn on none-horde nights | default: false")
            .define("enableRandomHordes", false);

    private static final ForgeConfigSpec.IntValue CHANCE_FOR_RANDOM_HORDES = BUILDER_SERVER
            .comment("Chance in % for a random horde (checked once per night, on none-horde nights) | default: 15")
            .defineInRange("chanceForRandomHordes", 15, 1, 100);

    private static final ForgeConfigSpec.BooleanValue ENABLE_LURE_HORDE_EFFECT = BUILDER_SERVER
            .comment("If true, killing a horde mob can give the player the lure horde effect | default: true")
            .comment("Getting this effect will attract horde mobs in the area.")
            .define("enableLureHordeEffect", true);

    private static final ForgeConfigSpec.BooleanValue NON_HORDE_ZOMBIES_CAN_CAUSE_LURE_HORDE_EFFECT = BUILDER_SERVER
            .comment("If true, killing zombies that were not spawned in a horde can also give the player the lure horde effect | default: true")
            .define("nonHordeZombiesCanCauseLureHordeEffect", true);

    private static final ForgeConfigSpec.DoubleValue CHANCE_FOR_LURE_HORDE_EFFECT = BUILDER_SERVER
            .comment("Chance to get the lure horde effect, when the player kills a horde mob (1.0 = 100%) | default: 0.07")
            .defineInRange("chanceForLureHordeEffect", 0.07, 0.0, 1.0);

    private static final ForgeConfigSpec.IntValue DURATION_FOR_LURE_HORDE_EFFECT = BUILDER_SERVER
            .comment("Duration in seconds for the lure horde effect | default: 60")
            .defineInRange("durationForLureHordeEffect", 60, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue LURE_HORDE_EFFECT_SPAWNS_HORDE = BUILDER_SERVER
            .comment("If true, getting the lure horde effect can spawn a horde | default: true")
            .define("lureHordeEffectSpawnsHorde", true);

    private static final ForgeConfigSpec.DoubleValue CHANCE_FOR_LURE_EFFECT_HORDE = BUILDER_SERVER
            .comment("Chance for the lure horde effect to spawn a horde (1.0 = 100%) | default: 0.2")
            .defineInRange("chanceForLureEffectToSpawnHorde", 0.2, 0.0, 1.0);

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
    private static boolean hordeWavesCanSpawnInWater = false;
    private static boolean hordeWavesCanSpawnOnTrees = false;
    private static boolean hordeWavesCanSpawnInCaves = false;
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
            hordeWavesCanSpawnInWater = HORDE_WAVES_CAN_SPAWN_IN_WATER.get();
            hordeWavesCanSpawnOnTrees = HORDE_WAVES_CAN_SPAWN_ON_TREES.get();
            hordeWavesCanSpawnInCaves = HORDE_WAVES_CAN_SPAWN_IN_CAVES.get();
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
        }
        if (SPEC_CLIENT.isLoaded()) {
            UndeadNights.LOGGER.info("Loading {} client config", UndeadNights.MOD_ID);
           // no client config
        }
    }
}
