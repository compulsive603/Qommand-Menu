package com.example.commandlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Optional user-editable file config/commandlist_info.json that lets you set the mod name
 * and description for any command, e.g. for commands the mod could not identify.
 */
public final class UserOverrides {
    public static class Info {
        public String mod;
        public String description;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, Info>>() {}.getType();

    private UserOverrides() {}

    public static Map<String, Info> load() {
        Map<String, Info> result = new HashMap<>();
        try {
            Path f = FMLPaths.CONFIGDIR.get().resolve("commandlist_info.json");
            if (!Files.exists(f)) {
                Files.writeString(f, """
                        {
                          "examplecommand": {
                            "mod": "Example Mod",
                            "description": "What this command does. Replace this example with your own entries."
                          }
                        }
                        """, StandardCharsets.UTF_8);
                return result;
            }
            Map<String, Info> m = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), TYPE);
            if (m != null) {
                for (Map.Entry<String, Info> e : m.entrySet()) {
                    if (e.getValue() != null) result.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }
}
