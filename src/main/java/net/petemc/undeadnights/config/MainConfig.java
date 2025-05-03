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

    public static boolean getHordeNightsDisableSleeping() {
        return hordeNightsDisableSleeping;
    }

    public static boolean getPersistentMobs() {
        return persistentMobs;
    }

    public static boolean getHordeZombiesBurnInDaylight() {
        return hordeZombiesBurnInDaylight;
    }

    public static boolean getHordeZombiesCanBreakBlocks() {
        return hordeZombiesCanBreakBlocks;
    }

    public static int getHordeZombiesBlockBreakTier() {
        return hordeZombiesBlockBreakTier;
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

    private static final ModConfigSpec.BooleanValue HORDE_NIGHTS_DISABLE_SLEEPING = BUILDER_SERVER
            .comment("If true, Players can't sleep through horde nights | default: true")
            .define("hordeNightsDisableSleeping", true);

    private static final ModConfigSpec.BooleanValue PERSISTENT_MOBS = BUILDER_SERVER
            .comment("If true, the horde zombies will be persistent and not despawn | default: false")
            .define("persistentMobs", false);

   private static final ModConfigSpec.BooleanValue HORDE_ZOMBIES_BURN_IN_DAYLIGHT = BUILDER_SERVER
            .comment("If true, the horde zombies (only those added by this mod) will burn in daylight | default: false")
            .define("hordeZombiesBurnInDaylight", false);

    private static final ModConfigSpec.BooleanValue HORDE_ZOMBIES_CAN_BREAK_BLOCKS = BUILDER_SERVER
            .comment("If true, Horde Zombies can break blocks | default: false")
            .define("hordeZombiesCanBreakBlocks", false);

    private static final ModConfigSpec.IntValue HORDE_ZOMBIES_BLOCK_BREAK_TIER = BUILDER_SERVER
            .comment("Horde Zombie block break tier (0-3) | default: 1")
            .defineInRange("hordeZombieBlockBreakTier", 1, 0, 3);

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
    private static int distanceMin = 70;
    private static int distanceMax = 75;
    private static int hordeMobsSpawnCap = 80;
    private static boolean spawnAdditionalWaves = true;
    private static int cooldownBetweenWaves = 45;
    private static int chanceForAdditionalWaves = 7;
    private static boolean hordeWavesCanSpawnInWater =false;
    private static boolean hordeWavesCanSpawnOnTrees = false;
    private static boolean hordeNightsDisableSleeping = true;
    private static boolean persistentMobs = false;
    private static boolean hordeZombiesBurnInDaylight = false;
    private static boolean hordeZombiesCanBreakBlocks = false;
    private static int hordeZombiesBlockBreakTier = 2;
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
            distanceMin = DISTANCE_MIN.get();
            distanceMax = DISTANCE_MAX.get();
            hordeMobsSpawnCap = HORDE_MOBS_SPAWN_CAP.get();
            spawnAdditionalWaves = SPAWN_ADDITIONAL_WAVES.get();
            cooldownBetweenWaves = COOLDOWN_BETWEEN_WAVES.get();
            chanceForAdditionalWaves = CHANCE_FOR_ADDITIONAL_WAVES.get();
            hordeWavesCanSpawnInWater = HORDE_WAVES_CAN_SPAWN_IN_WATER.get();
            hordeWavesCanSpawnOnTrees = HORDE_WAVES_CAN_SPAWN_ON_TREES.get();
            hordeNightsDisableSleeping = HORDE_NIGHTS_DISABLE_SLEEPING.get();
	        persistentMobs = PERSISTENT_MOBS.get();
            hordeZombiesBurnInDaylight = HORDE_ZOMBIES_BURN_IN_DAYLIGHT.get();
            hordeZombiesCanBreakBlocks = HORDE_ZOMBIES_CAN_BREAK_BLOCKS.get();
            hordeZombiesBlockBreakTier = HORDE_ZOMBIES_BLOCK_BREAK_TIER.get();
            spawnStrayHordeZombies = SPAWN_STRAY_HORDE_ZOMBIES.get();
            printDebugMessages = PRINT_DEBUG_MESSAGES.get();
        }
        if (SPEC_CLIENT.isLoaded()) {
            UndeadNights.LOGGER.info("Loading {} client config", UndeadNights.MOD_ID);
           // no client config
        }
    }
}
