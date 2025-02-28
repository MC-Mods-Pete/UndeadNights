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
    private static final int currentConfigVersion = 1;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            var defaultConfObject = getJsonObject();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(defaultConfObject, writer);  // store objects in JSON
            } catch (IOException e) {
                e.printStackTrace();
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

                }
                var defaultConfObject = getJsonObject();
                try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(defaultConfObject, writer);
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {

        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            configVariant = json.get("configVariant").getAsInt();
            if (configVariant == 1) {
                maxWaveSize = json.get("maxWaveSize").getAsInt();
                String defaultMobId = json.get("defaultMobId").getAsString();
                String defaultMobExtraInfo = json.get("extraSpawnInfo").getAsString();
                //configVersionReadFromFile = json.get("internalConfigVersion").getAsInt();
                defaultHordeMob = new MobSpawnData(defaultMobId, 100, 0, 0, defaultMobExtraInfo);

                hordeMobs.clear();
                JsonArray mobsArray = json.getAsJsonArray("hordeMobs");
                for (int i = 0; i < mobsArray.size(); i++) {
                    JsonObject mobObj = mobsArray.get(i).getAsJsonObject();
                    String mobId = mobObj.get("mobId").getAsString();
                    int mobChance = mobObj.get("spawnChance").getAsInt();
                    String mobExtraInfo = mobObj.get("extraSpawnInfo").getAsString();
                    hordeMobs.add(new MobSpawnData(mobId, mobChance, 0, 0,mobExtraInfo));
                }
            } else {
                hordeMobs.clear();
                JsonArray mobsArray = json.getAsJsonArray("hordeMobs");
                for (int i = 0; i < mobsArray.size(); i++) {
                    JsonObject mobObj = mobsArray.get(i).getAsJsonObject();
                    String mobId = mobObj.get("mobId").getAsString();
                    int mobCountMin = mobObj.get("countMin").getAsInt();
                    int mobCountMax = mobObj.get("countMax").getAsInt();
                    String mobExtraInfo = mobObj.get("extraSpawnInfo").getAsString();
                    hordeMobs.add(new MobSpawnData(mobId, 0, mobCountMin, mobCountMax, mobExtraInfo));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static List<MobSpawnData> getHordeMobs() {
        return hordeMobs;
    }

    public record MobSpawnData(String mobId, int chance, int countMin, int countMax, String extra) {
    }
}
