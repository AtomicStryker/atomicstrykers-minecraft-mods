package atomicstryker.findercompass.common;

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

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T loadConfigWithDefault(Class<T> clazz, File file, T defaultInstance) throws IOException {
        if (file.createNewFile()) {
            String json = gson.toJson(JsonParser.parseString(gson.toJson(defaultInstance)));
            try (PrintWriter out = new PrintWriter(file)) {
                out.println(json);
            }
            return defaultInstance;
        } else {
            return gson.fromJson(new String(Files.readAllBytes(file.toPath())), clazz);
        }
    }

    public static <T> T loadConfigFromString(Class<T> clazz, String json) {
        return gson.fromJson(json, clazz);
    }

    public static void saveConfig(Object config, File file) throws IOException {
        if (file.createNewFile()) {
            String json = gson.toJson(JsonParser.parseString(gson.toJson(config)));
            try (PrintWriter out = new PrintWriter(file)) {
                out.println(json);
            }
        }
    }

    public static String jsonFromConfig(Object config) {
        return gson.toJson(JsonParser.parseString(gson.toJson(config)));
    }
}
