/* idea and original code provided by: https://github.com/BazeBro1337
*  repository: https://github.com/BazeBro1337/Hotwaves
* */

package net.petemc.undeadnights.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.petemc.undeadnights.UndeadNights;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HordeConfig {
    private static final File CONFIG_FILE = new File("config/undeadnights_horde_mobs_config.json");
    private static int configVariant = 1;
    private static int maxWaveSize = 15;
    private static MobSpawnData defaultHordeMob = null;
    private static final List<MobSpawnData> hordeMobs = new ArrayList<>();

    private static final List<HordesData> hordes = new ArrayList<>();
    private static int numberOfHordes = 0;
    private static int defaultHorde = 1;
    private static String dimension = "minecraft:overworld";

    private static final int currentConfigVersion = 1;
    private static boolean readingConfigFailed = false;
    private static String errorMessage = null;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            var defaultConfObject = getJsonObject();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(defaultConfObject, writer);  // store objects in JSON
            } catch (IOException e) {
                readingConfigFailed = true;
                errorMessage = e.getMessage();
            }
        }

        UndeadNights.LOGGER.info("Loading {} horde mobs config", UndeadNights.MOD_ID);

        int configVersionReadFromFile = 0;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            configVersionReadFromFile = json.get("internalConfigVersion").getAsInt();
            if (configVersionReadFromFile < currentConfigVersion) {
                UndeadNights.LOGGER.warn("Horde mobs config file from older version, backing up and replacing with new config file!");
                try (FileWriter writer = new FileWriter(CONFIG_FILE + "_back-" + configVersionReadFromFile)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(json, writer);
                } catch (Exception e) {
                    readingConfigFailed = true;
                    errorMessage = e.getMessage();
                }
                var defaultConfObject = getJsonObject();
                try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(defaultConfObject, writer);
                } catch (Exception e) {
                    readingConfigFailed = true;
                    errorMessage = e.getMessage();
                }
            }
        } catch (Exception e) {
            readingConfigFailed = true;
            errorMessage = e.getMessage();
        }

        if (!readingConfigFailed) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                configVariant = json.get("configVariant").getAsInt();
                if (configVariant == 1) {
                    maxWaveSize = json.get("maxWaveSize").getAsInt();
                    try {
                        dimension = json.get("dimension").getAsString();
                    } catch (Exception e) {
                        dimension = "minecraft:overworld";
                    }
                    String defaultMobId = json.get("defaultMobId").getAsString();
                    String defaultMobExtraInfo = json.get("extraSpawnInfo").getAsString();
                    defaultHordeMob = new MobSpawnData(defaultMobId, 100, 0, 0, defaultMobExtraInfo);
                    UndeadNights.LOGGER.info("Default horde mob {} was read from config.", defaultMobId);

                    hordeMobs.clear();
                    JsonArray mobsArray = json.getAsJsonArray("hordeMobs");
                    for (int i = 0; i < mobsArray.size(); i++) {
                        JsonObject mobObj = mobsArray.get(i).getAsJsonObject();
                        String mobId = mobObj.get("mobId").getAsString();
                        int mobChance = mobObj.get("spawnChance").getAsInt();
                        String mobExtraInfo = mobObj.get("extraSpawnInfo").getAsString();
                        UndeadNights.LOGGER.info("Horde mob {} with chance {}% was read from config.", mobId, mobChance);
                        hordeMobs.add(new MobSpawnData(mobId, mobChance, 0, 0, mobExtraInfo));
                    }
                    numberOfHordes = 1;
                    defaultHorde = 1;
                } else {
                    defaultHorde = json.get("defaultHordeId").getAsInt();
                    JsonArray hordesArray = json.getAsJsonArray("hordes");
                    numberOfHordes = hordesArray.size();
                    for (int j = 0; j < hordesArray.size(); j++) {
                        List<MobSpawnData> subHorde = new ArrayList<>();
                        JsonObject hordeObj = hordesArray.get(j).getAsJsonObject();
                        String hordeName = hordeObj.get("hordeName").getAsString();
                        String dimension = null;
                        try {
                            dimension = hordeObj.get("dimension").getAsString();
                        } catch (Exception e) {
                            dimension = "minecraft:overworld";
                        }
                        JsonArray mobsArray = hordeObj.getAsJsonArray("horde"+(j+1));
                        for (int i = 0; i < mobsArray.size(); i++) {
                            JsonObject mobObj = mobsArray.get(i).getAsJsonObject();
                            String mobId = mobObj.get("mobId").getAsString();
                            int mobChance = 0;
                            try {
                                mobChance = mobObj.get("spawnChance").getAsInt();
                            } catch (Exception e) {
                                mobChance = 100;
                            }
                            int mobCountMin = mobObj.get("countMin").getAsInt();
                            int mobCountMax = mobObj.get("countMax").getAsInt();
                            String mobExtraInfo = mobObj.get("extraSpawnInfo").getAsString();
                            UndeadNights.LOGGER.info("Horde mob {} with count range {}-{} was read from config.", mobId, mobCountMin, mobCountMax);
                            subHorde.add(new MobSpawnData(mobId, mobChance, mobCountMin, mobCountMax, mobExtraInfo));
                        }
                        hordes.add(new HordesData(j+1, hordeName, dimension, subHorde));
                    }
                    if (hordes.isEmpty()) {
                        readingConfigFailed = true;
                        errorMessage = "hordeMobs array is empty!";
                    }
                }
            } catch (Exception e) {
                readingConfigFailed = true;
                errorMessage = e.getMessage();
            }
        }

        if (readingConfigFailed) {
            UndeadNights.LOGGER.error("Reading horde mob config file {} failed with error: {}", CONFIG_FILE, errorMessage);
            configVariant = 1;
            maxWaveSize = 15;
            defaultHordeMob = new MobSpawnData("undeadnights:horde_zombie", 100, 0, 0, "none");
            UndeadNights.LOGGER.info("Horde config file will be ignored, a wave of {} {} will be spawned\nPlease check: https://github.com/MC-Mods-Pete/UndeadNights/wiki", maxWaveSize, defaultHordeMob.mobId);
            hordeMobs.clear();
        }

    }

    private static @NotNull JsonObject getJsonObject() {
        var waveMobsJson = new JsonArray();

        JsonObject mob = new JsonObject();
        mob.addProperty("mobId", "undeadnights:elite_zombie");
        mob.addProperty("spawnChance", "3");
        mob.addProperty("extraSpawnInfo", "none");
        waveMobsJson.add(mob);

        mob = new JsonObject();
        mob.addProperty("mobId", "undeadnights:demolition_zombie");
        mob.addProperty("spawnChance", "6");
        mob.addProperty("extraSpawnInfo", "tnt:5");
        waveMobsJson.add(mob);

        var defaultConfObject = new JsonObject();
        defaultConfObject.addProperty("configVariant", 1);
        defaultConfObject.addProperty("maxWaveSize", 15);
        defaultConfObject.addProperty("defaultMobId", "undeadnights:horde_zombie");
        defaultConfObject.addProperty("extraSpawnInfo", "none");
        defaultConfObject.add("hordeMobs", waveMobsJson);
        defaultConfObject.addProperty("internalConfigVersion", currentConfigVersion);
        return defaultConfObject;
    }

    public static int getConfigVariant() {
        return configVariant;
    }

    public static int getMaxWaveSize() {
        return maxWaveSize;
    }

    public static MobSpawnData getDefaultHordeMob() {
        return defaultHordeMob;
    }

    public static String getDimension() {
        return dimension;
    }

    public static List<MobSpawnData> getHordeMobs() {
        return hordeMobs;
    }

    public static List<HordesData> getHordes() {
        return hordes;
    }

    public static int getNumberOfHordes() {
        return numberOfHordes;
    }

    public static int getDefaultHorde() {
        return defaultHorde;
    }

    public record MobSpawnData(String mobId, int chance, int countMin, int countMax, String extra) {
    }

    public record HordesData(int hordeId, String hordeName, String dimension, List<MobSpawnData> hordeMobs) {
    }

    public static boolean getReadingConfigFailed() {
        return readingConfigFailed;
    }

    public static String getErrorMessage() {
        return errorMessage;
    }
}
