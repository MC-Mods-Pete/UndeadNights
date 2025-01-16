package net.petemc.undeadnights;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
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

    public static int getZombieHordeWaveSize() {
        return zombieHordeWaveSize;
    }

    public static int getHordeZombiesSpawnCap() {
        return hordeZombiesSpawnCap;
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

    public static boolean getHordeNightsDisableSleeping() {
        return hordeNightsDisableSleeping;
    }

    public static boolean getPersistentZombies() {
        return persistentZombies;
    }

    public static boolean getZombiesBurnInDaylight() {
        return zombiesBurnInDaylight;
    }

    public static boolean getSpawnDemolitionZombies() {
        return spawnDemolitionZombies;
    }

    public static int getChanceForDemolitionZombieToSpawn() {
        return chanceForDemolitionZombieToSpawn;
    }

    public static int getDemolitionZombieTntStackSize() {
        return demolitionZombieTntStackSize;
    }

    public static boolean getSpawnEliteZombies() {
        return spawnEliteZombies;
    }

    public static int getChanceForEliteZombieToSpawn() {
        return chanceForEliteZombieToSpawn;
    }

    public static boolean getSpawnStrayHordeZombies() {
        return spawnStrayHordeZombies;
    }

    public static boolean getPrintDebugMessages() {
        return printDebugMessages;
    }

    // Server Config
    private static final ForgeConfigSpec.Builder BUILDER_SERVER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue UNDEAD_NIGHTS_ENABLED = BUILDER_SERVER
            .comment("If true, Nights of the Undead (horde nights) are enabled | default: true")
            .define("undeadNightsEnabled", true);

    private static final ForgeConfigSpec.IntValue DAYS_BETWEEN_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Days between horde nights (1 = every night is a horde night) | default: 5")
            .defineInRange("daysBetweenHordeNights", 5, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CHANCE_FOR_HORDE_NIGHTS = BUILDER_SERVER
            .comment("Chance in % for a horde night | default: 100")
            .defineInRange("chanceForHordeNight", 100, 1, 100);

    private static final ForgeConfigSpec.BooleanValue SEND_HORDE_NIGHTS_COUNTDOWN_MESSAGE = BUILDER_SERVER
            .comment("If true, each night a message will be sent to the player with how many nights are left before the next Horde Night | default: false")
            .define("sendHordeNightsCountdownMessage", false);

    private static final ForgeConfigSpec.IntValue DISTANCE_MIN = BUILDER_SERVER
            .comment("Minimum distance a horde will spawn away from the player | default: 70")
            .defineInRange("distanceMin", 70, 10, 256);

    private static final ForgeConfigSpec.IntValue DISTANCE_MAX = BUILDER_SERVER
            .comment("Maximum distance a horde will spawn away from the player | default: 75")
            .defineInRange("distanceMax", 75, 10, 256);

    private static final ForgeConfigSpec.IntValue ZOMBIE_HORDE_WAVE_SIZE = BUILDER_SERVER
            .comment("Size of a wave of zombies | default: 15")
            .defineInRange("zombieHordeWaveSize", 15, 1, 256);

    private static final ForgeConfigSpec.IntValue HORDE_ZOMBIE_SPAWN_CAP = BUILDER_SERVER
            .comment("Maximum amount of zombies that can be loaded in the world at the same time | default: 80")
            .defineInRange("hordeZombiesSpawnCap", 80, 1, 256);

    private static final ForgeConfigSpec.BooleanValue SPAWN_ADDITIONAL_WAVES = BUILDER_SERVER
            .comment("If true, additional waves can spawn in a horde night | default: true")
            .define("spawnAdditionalWaves", true);

    private static final ForgeConfigSpec.IntValue COOLDOWN_BETWEEN_WAVES = BUILDER_SERVER
            .comment("Time in seconds between check for next possible wave in a horde night | default: 45")
            .defineInRange("cooldownBetweenWaves", 45, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue CHANCE_FOR_ADDITIONAL_WAVES = BUILDER_SERVER
            .comment("Chance in % for another zombie wave (checked after every wave cooldown) | default: 7")
            .defineInRange("chanceForAdditionalWaves", 7, 1, 100);

    private static final ForgeConfigSpec.BooleanValue HORDE_NIGHTS_DISABLE_SLEEPING = BUILDER_SERVER
            .comment("If true, Players can't sleep through horde nights | default: true")
            .define("hordeNightsDisableSleeping", true);

    private static final ForgeConfigSpec.BooleanValue PERSISTENT_ZOMBIES = BUILDER_SERVER
            .comment("If true, the horde zombies will be persistent and not despawn | default: false")
            .define("persistentZombies", false);

   private static final ForgeConfigSpec.BooleanValue ZOMBIES_BURN_IN_DAYLIGHT = BUILDER_SERVER
            .comment("If true, the horde zombies will burn in daylight | default: false")
            .define("zombiesBurnInDaylight", false);

   private static final ForgeConfigSpec.BooleanValue SPAWN_DEMOLITION_ZOMBIES = BUILDER_SERVER
            .comment("If true, demolition zombies with TNT will spawn | default: true")
            .define("spawnDemolitionZombies", true);

   private static final ForgeConfigSpec.IntValue CHANCE_FOR_DEMOLITION_ZOMBIE_TO_SPAWN = BUILDER_SERVER
            .comment("Chance in % for a demolition zombie to spawn | default: 6")
            .defineInRange("chanceForDemolitionZombieToSpawn", 6, 1, 100);

   private static final ForgeConfigSpec.IntValue DEMOLITION_ZOMBIE_TNT_STACK_SIZE = BUILDER_SERVER
            .comment("TNT Stack size a demolition zombie will spawn with (0 = unlimited) | default: 3")
            .defineInRange("demolitionZombieTntStackSize", 3, 1, 64);

   private static final ForgeConfigSpec.BooleanValue SPAWN_ELITE_ZOMBIES = BUILDER_SERVER
            .comment("If true, elite zombies will spawn | default: true")
            .define("spawnEliteZombies", true);

   private static final ForgeConfigSpec.IntValue CHANCE_FOR_ELITE_ZOMBIE_TO_SPAWN = BUILDER_SERVER
            .comment("Chance in % for an elite zombie to spawn | default: 3")
            .defineInRange("chanceForEliteZombieToSpawn", 3, 1, 64);

   private static final ForgeConfigSpec.BooleanValue SPAWN_STRAY_HORDE_ZOMBIES = BUILDER_SERVER
            .comment("If true, single stray horde zombies can spawn on normal nights | default: true")
            .define("spawnStrayHordeZombies", true);

   private static final ForgeConfigSpec.BooleanValue PRINT_DEBUG_MESSAGES = BUILDER_SERVER
            .comment("If true, debug messages will be logged out | default: false")
            .define("printDebugMessages", false);

    static final ForgeConfigSpec SPEC_SERVER = BUILDER_SERVER.build();


    // Client Config
    private static final ForgeConfigSpec.Builder BUILDER_CLIENT = new ForgeConfigSpec.Builder();
    // no client config
    static final ForgeConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();


    private static boolean undeadNightsEnabled = true;
    private static int daysBetweenHordeNights = 5;
    private static int chanceForHordeNight = 100;
    private static boolean sendHordeNightsCountdownMessage = false;
    private static int distanceMin = 70;
    private static int distanceMax = 75;
    private static int zombieHordeWaveSize = 15;
    private static int hordeZombiesSpawnCap = 80;
    private static boolean spawnAdditionalWaves = true;
    private static int cooldownBetweenWaves = 45;
    private static int chanceForAdditionalWaves = 7;
    private static boolean hordeNightsDisableSleeping = true;
    private static boolean persistentZombies = false;
    private static boolean zombiesBurnInDaylight = false;
    private static boolean spawnDemolitionZombies = true;
    private static int chanceForDemolitionZombieToSpawn = 6;
    private static int demolitionZombieTntStackSize = 3;
    private static boolean spawnEliteZombies = true;
    private static int chanceForEliteZombieToSpawn = 3;
    private static boolean spawnStrayHordeZombies = true;
    private static boolean printDebugMessages = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        UndeadNights.LOGGER.info("Loading Config");
        if (SPEC_SERVER.isLoaded()) {
            undeadNightsEnabled = UNDEAD_NIGHTS_ENABLED.get();
            daysBetweenHordeNights = DAYS_BETWEEN_HORDE_NIGHTS.get();
            chanceForHordeNight = CHANCE_FOR_HORDE_NIGHTS.get();
            sendHordeNightsCountdownMessage = SEND_HORDE_NIGHTS_COUNTDOWN_MESSAGE.get();
            distanceMin = DISTANCE_MIN.get();
            distanceMax = DISTANCE_MAX.get();
            zombieHordeWaveSize = ZOMBIE_HORDE_WAVE_SIZE.get();
            hordeZombiesSpawnCap = HORDE_ZOMBIE_SPAWN_CAP.get();
            spawnAdditionalWaves = SPAWN_ADDITIONAL_WAVES.get();
            cooldownBetweenWaves = COOLDOWN_BETWEEN_WAVES.get();
            chanceForAdditionalWaves = CHANCE_FOR_ADDITIONAL_WAVES.get();
            hordeNightsDisableSleeping = HORDE_NIGHTS_DISABLE_SLEEPING.get();
	        persistentZombies = PERSISTENT_ZOMBIES.get();
            zombiesBurnInDaylight = ZOMBIES_BURN_IN_DAYLIGHT.get();
            spawnDemolitionZombies = SPAWN_DEMOLITION_ZOMBIES.get();
            chanceForDemolitionZombieToSpawn = CHANCE_FOR_DEMOLITION_ZOMBIE_TO_SPAWN.get();
            demolitionZombieTntStackSize = DEMOLITION_ZOMBIE_TNT_STACK_SIZE.get();
            spawnEliteZombies = SPAWN_ELITE_ZOMBIES.get();
	        chanceForEliteZombieToSpawn = CHANCE_FOR_ELITE_ZOMBIE_TO_SPAWN.get();
            spawnStrayHordeZombies = SPAWN_STRAY_HORDE_ZOMBIES.get();
            printDebugMessages = PRINT_DEBUG_MESSAGES.get();
        }
        if (SPEC_CLIENT.isLoaded()) {
           // no client config
        }
    }
}
