package com.example.commandlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Remembers when each command was first seen, in config/commandlist_seen.json. */
public final class SeenStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, Long>>() {}.getType();

    private SeenStore() {}

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("commandlist_seen.json");
    }

    public static Map<String, Long> load() {
        try {
            Path f = file();
            if (Files.exists(f)) {
                Map<String, Long> m = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), TYPE);
                if (m != null) return new HashMap<>(m);
            }
        } catch (Exception ignored) {
        }
        return new HashMap<>();
    }

    public static void save(Map<String, Long> map) {
        try {
            Files.writeString(file(), GSON.toJson(map, TYPE), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
