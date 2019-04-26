package atomicstryker.infernalmobs.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * https://www.spigotmc.org/threads/easy-json-config-files-with-gson.222237/, but fixed to work
 */
public class GsonConfig {

    private static JsonParser parser = new JsonParser();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T loadConfigWithDefault(Class<T> clazz, File file, T defaultInstance) {
        try {
            if (file.createNewFile()) {
                String json = gson.toJson(parser.parse(gson.toJson(defaultInstance)));
                try (PrintWriter out = new PrintWriter(file)) {
                    out.println(json);
                }
                return defaultInstance;
            } else {
                return gson.fromJson(new String(Files.readAllBytes(file.toPath())), clazz);
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Config failed parsing json file somehow, read logfile");
        }
    }

    public static void saveConfig(Object config, File file) {
        try {
            String jsonNew = gson.toJson(parser.parse(gson.toJson(config)));
            if (file.exists()) {
                String jsonOld = new String(Files.readAllBytes(file.toPath()));
                if (jsonNew.equals(jsonOld)) {
                    // no change to config, dont torture the user SSD with pointless writes
                    return;
                }
                file.delete();
            }
            if (file.createNewFile()) {
                try (PrintWriter out = new PrintWriter(file)) {
                    out.println(jsonNew);
                }
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Config failed saving gson file somehow, read logfile");
        }
    }
}
