package net.petemc.undeadnights.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.petemc.undeadnights.UndeadNights;

@EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MainConfig
{
    public static boolean getUndeadNightsEnabled() {
        return undeadNightsEnabled;
    }

    public static int getDaysBetweenHordeNights() {
        return daysBetweenHordeNights;
    }

    public static int getChanceForHordeNight() {
        return chanceForHordeNight;
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

    public static boolean getSpawnAdditionalWaves() {
        return spawnAdditionalWaves;
    }

    public static int getCooldownBetweenWaves() {
        return cooldownBetweenWaves;
    }

    public static int getChanceForAdditionalWaves() {
        return chanceForAdditionalWaves;
    }

    public static boolean getHordeWavesCanSpawnInWater() {
        return hordeWavesCanSpawnInWater;
    }

    public static boolean getHordeWavesCanSpawnOnTrees() {
        return hordeWavesCanSpawnOnTrees;
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

    public static boolean getPersistentMobs() {
        return persistentMobs;
    }

    public static boolean getHordeZombiesBurnInDaylight() {
        return hordeZombiesBurnInDaylight;
    }

    public static boolean getVanillaZombiesBurnInDaylight() {
        return vanillaZombiesBurnInDaylight;
    }

    public static boolean getHordeZombiesCanBreakBlocks() {
        return hordeZombiesCanBreakBlocks;
    }

    public static boolean getVanillaZombiesCanBreakBlocks() {
        return vanillaZombiesCanBreakBlocks;
    }

    public static int getZombiesBlockBreakTier() {
        return zombiesBlockBreakTier;
    }

    public static double getHordeZombieWaterMovementEfficiency() { return hordeZombieWaterMovementEfficiency; }

    public static boolean getSecurityCraftCompatibility() {
        return securityCraftCompatibility;
    }

    public static boolean getSpawnStrayHordeZombies() {
        return spawnStrayHordeZombies;
    }

    public static boolean getPrintDebugMessages() {
        return printDebugMessages;
    }

    // Server Config
    private static final ModConfigSpec.Builder BUILDER_SERVER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue UNDEAD_NIGHTS_ENABLED = BUILDER_SERVER
            .comment("If true, Nights of the Undead (horde nights) are enabled | default: true")
            .define("undeadNightsEnabled", true);

    private static final ModConfigSpec.IntValue DAYS_BETWEEN_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Days between horde nights (1 = every night is a horde night) | default: 5")
            .defineInRange("daysBetweenHordeNights", 5, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CHANCE_FOR_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Chance in % for a horde night | default: 100")
            .defineInRange("chanceForHordeNight", 100, 1, 100);

    private static final ModConfigSpec.BooleanValue SEND_HORDE_NIGHTS_COUNTDOWN_MESSAGE = BUILDER_SERVER
            .comment("If true, each night a message will be sent to the player with how many nights are left before the next Horde Night | default: false")
            .define("sendHordeNightsCountdownMessage", false);

    private static final ModConfigSpec.BooleanValue HORDE_SPAWNED_MESSAGE_AND_SOUND = BUILDER_SERVER
            .comment("If true, the horde has spawned message and the horde sound are enabled | default: true")
            .define("hordeSpawnedMessageAndSound", true);

    private static final ModConfigSpec.IntValue DISTANCE_MIN = BUILDER_SERVER
            .comment("Minimum distance a horde will spawn away from the player | default: 70")
            .defineInRange("distanceMin", 70, 10, 256);

    private static final ModConfigSpec.IntValue DISTANCE_MAX = BUILDER_SERVER
            .comment("Maximum distance a horde will spawn away from the player | default: 75")
            .defineInRange("distanceMax", 75, 10, 256);

    private static final ModConfigSpec.IntValue HORDE_MOBS_SPAWN_CAP = BUILDER_SERVER
            .comment("Maximum amount of horde mobs that can be loaded in the world at the same time | default: 80")
            .defineInRange("hordeMobsSpawnCap", 80, 1, 256);

    private static final ModConfigSpec.BooleanValue SPAWN_ADDITIONAL_WAVES = BUILDER_SERVER
            .comment("If true, additional waves can spawn in a horde night | default: true")
            .define("spawnAdditionalWaves", true);

    private static final ModConfigSpec.IntValue COOLDOWN_BETWEEN_WAVES = BUILDER_SERVER
            .comment("Time in seconds between check for next possible wave in a horde night | default: 45")
            .defineInRange("cooldownBetweenWaves", 45, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CHANCE_FOR_ADDITIONAL_WAVES = BUILDER_SERVER
            .comment("Chance in % for another zombie wave (checked after every wave cooldown) | default: 7")
            .defineInRange("chanceForAdditionalWaves", 7, 1, 100);

    private static final ModConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_IN_WATER = BUILDER_SERVER
            .comment("If true, horde waves can spawn in water | default: false")
            .define("hordeWavesCanSpawnInWater", false);

    private static final ModConfigSpec.BooleanValue HORDE_WAVES_CAN_SPAWN_ON_TREES = BUILDER_SERVER
            .comment("If true, horde waves can spawn on trees | default: false")
            .define("hordeWavesCanSpawnOnTrees", false);

    private static final ModConfigSpec.BooleanValue BLOCK_LIGHT_LEVELS_INFLUENCE_MONSTER_SPAWNS = BUILDER_SERVER
            .comment("If true, the light level of the block position can prevent horde mobs from spawning | default: false")
            .define("blockLightLevelsInfluenceMonsterSpawns", false);

    private static final ModConfigSpec.IntValue MAX_BLOCK_LIGHT_LEVEL_FOR_MONSTER_SPAWNS = BUILDER_SERVER
            .comment("Maximum block light level so a monster can spawn | default: 0")
            .defineInRange("maxBlockLightLevelForMonsterSpawns", 0, 0, 256);

    private static final ModConfigSpec.BooleanValue HORDE_NIGHTS_DISABLE_SLEEPING = BUILDER_SERVER
            .comment("If true, Players can't sleep through horde nights | default: true")
            .define("hordeNightsDisableSleeping", true);

    private static final ModConfigSpec.BooleanValue PERSISTENT_MOBS = BUILDER_SERVER
            .comment("If true, the horde zombies will be persistent and not despawn | default: false")
            .define("persistentMobs", false);

   private static final ModConfigSpec.BooleanValue HORDE_ZOMBIES_BURN_IN_DAYLIGHT = BUILDER_SERVER
            .comment("If true, the horde zombies (only those added by this mod) will burn in daylight | default: false")
            .define("hordeZombiesBurnInDaylight", false);

    private static final ModConfigSpec.BooleanValue VANILLA_ZOMBIES_BURN_IN_DAYLIGHT = BUILDER_SERVER
            .comment("If true, the vanilla zombies will burn in daylight | default: false")
            .define("vanillaZombiesBurnInDaylight", true);

    private static final ModConfigSpec.BooleanValue HORDE_ZOMBIES_CAN_BREAK_BLOCKS = BUILDER_SERVER
            .comment("If true, Horde Zombies can break blocks | default: false")
            .define("hordeZombiesCanBreakBlocks", false);

    private static final ModConfigSpec.BooleanValue VANILLA_ZOMBIES_CAN_BREAK_BLOCKS = BUILDER_SERVER
            .comment("If true, Vanilla Zombies can break blocks | default: false")
            .define("vanillaZombiesCanBreakBlocks", false);

    private static final ModConfigSpec.IntValue ZOMBIES_BLOCK_BREAK_TIER = BUILDER_SERVER
            .comment("Zombie block break tier (0-4) | default: 1")
            .defineInRange("zombieBlockBreakTier", 1, 0, 3);

    private static final ModConfigSpec.DoubleValue HORDE_ZOMBIES_WATER_MOVEMENT_EFFICIENCY = BUILDER_SERVER
            .comment("Horde Zombie water movement efficiency (0.0D = vanilla) | default: 0.5D")
            .defineInRange("hordeZombieWaterMovementEfficiency", 0.5D, 0.0D, 1.0D);

    private static final ModConfigSpec.BooleanValue SECURITY_CRAFT_COMPATIBILITY = BUILDER_SERVER
            .comment("If true, Horde Zombies are not able to break reinforced blocks from the Security Craft mod | default: false")
            .define("securityCraftCompatibility", false);

   private static final ModConfigSpec.BooleanValue SPAWN_STRAY_HORDE_ZOMBIES = BUILDER_SERVER
            .comment("If true, single stray horde zombies can spawn on normal nights | default: true")
            .define("spawnStrayHordeZombies", true);

   private static final ModConfigSpec.BooleanValue PRINT_DEBUG_MESSAGES = BUILDER_SERVER
            .comment("If true, debug messages will be logged out | default: false")
            .define("printDebugMessages", false);

   public static final ModConfigSpec SPEC_SERVER = BUILDER_SERVER.build();

    // Client Config
    private static final ModConfigSpec.Builder BUILDER_CLIENT = new ModConfigSpec.Builder();
    // no client config
    public static final ModConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();


    private static boolean undeadNightsEnabled = true;
    private static int daysBetweenHordeNights = 5;
    private static int chanceForHordeNight = 100;
    private static boolean sendHordeNightsCountdownMessage = false;
    private static boolean hordeSpawnedMessageAndSound = true;
    private static int distanceMin = 70;
    private static int distanceMax = 75;
    private static int hordeMobsSpawnCap = 80;
    private static boolean spawnAdditionalWaves = true;
    private static int cooldownBetweenWaves = 45;
    private static int chanceForAdditionalWaves = 7;
    private static boolean hordeWavesCanSpawnInWater = false;
    private static boolean hordeWavesCanSpawnOnTrees = false;
    private static boolean blockLightLevelsInfluenceMonsterSpawns = false;
    private static int maxBlockLightLevelForMonsterSpawns = 0;
    private static boolean hordeNightsDisableSleeping = true;
    private static boolean persistentMobs = false;
    private static boolean hordeZombiesBurnInDaylight = false;
    private static boolean vanillaZombiesBurnInDaylight = true;
    private static boolean hordeZombiesCanBreakBlocks = false;
    private static boolean vanillaZombiesCanBreakBlocks = false;
    private static int zombiesBlockBreakTier = 2;
    private static double hordeZombieWaterMovementEfficiency = 0.5D;
    private static boolean securityCraftCompatibility = false;
    private static boolean spawnStrayHordeZombies = true;
    private static boolean printDebugMessages = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (SPEC_SERVER.isLoaded()) {
            UndeadNights.LOGGER.info("Loading {} server config", UndeadNights.MOD_ID);
            undeadNightsEnabled = UNDEAD_NIGHTS_ENABLED.get();
            daysBetweenHordeNights = DAYS_BETWEEN_HORDE_NIGHTS.get();
            chanceForHordeNight = CHANCE_FOR_HORDE_NIGHTS.get();
            sendHordeNightsCountdownMessage = SEND_HORDE_NIGHTS_COUNTDOWN_MESSAGE.get();
            hordeSpawnedMessageAndSound = HORDE_SPAWNED_MESSAGE_AND_SOUND.get();
            distanceMin = DISTANCE_MIN.get();
            distanceMax = DISTANCE_MAX.get();
            hordeMobsSpawnCap = HORDE_MOBS_SPAWN_CAP.get();
            spawnAdditionalWaves = SPAWN_ADDITIONAL_WAVES.get();
            cooldownBetweenWaves = COOLDOWN_BETWEEN_WAVES.get();
            chanceForAdditionalWaves = CHANCE_FOR_ADDITIONAL_WAVES.get();
            hordeWavesCanSpawnInWater = HORDE_WAVES_CAN_SPAWN_IN_WATER.get();
            hordeWavesCanSpawnOnTrees = HORDE_WAVES_CAN_SPAWN_ON_TREES.get();
            blockLightLevelsInfluenceMonsterSpawns = BLOCK_LIGHT_LEVELS_INFLUENCE_MONSTER_SPAWNS.get();
            maxBlockLightLevelForMonsterSpawns = MAX_BLOCK_LIGHT_LEVEL_FOR_MONSTER_SPAWNS.get();
            hordeNightsDisableSleeping = HORDE_NIGHTS_DISABLE_SLEEPING.get();
	        persistentMobs = PERSISTENT_MOBS.get();
            hordeZombiesBurnInDaylight = HORDE_ZOMBIES_BURN_IN_DAYLIGHT.get();
            vanillaZombiesBurnInDaylight = VANILLA_ZOMBIES_BURN_IN_DAYLIGHT.get();
            hordeZombiesCanBreakBlocks = HORDE_ZOMBIES_CAN_BREAK_BLOCKS.get();
            vanillaZombiesCanBreakBlocks = VANILLA_ZOMBIES_CAN_BREAK_BLOCKS.get();
            zombiesBlockBreakTier = ZOMBIES_BLOCK_BREAK_TIER.get();
            hordeZombieWaterMovementEfficiency = HORDE_ZOMBIES_WATER_MOVEMENT_EFFICIENCY.get();
            securityCraftCompatibility = SECURITY_CRAFT_COMPATIBILITY.get();
            spawnStrayHordeZombies = SPAWN_STRAY_HORDE_ZOMBIES.get();
            printDebugMessages = PRINT_DEBUG_MESSAGES.get();
        }
        if (SPEC_CLIENT.isLoaded()) {
            UndeadNights.LOGGER.info("Loading {} client config", UndeadNights.MOD_ID);
           // no client config
        }
    }
}
