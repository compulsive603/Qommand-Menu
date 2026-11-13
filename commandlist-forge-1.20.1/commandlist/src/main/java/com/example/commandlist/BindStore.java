package com.example.commandlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Keyboard bindings for commands, stored in config/commandlist_binds.json. */
public final class BindStore {
    public static class Bind {
        public String command = "";
        public int key = -1;
        public int mods = 0;

        public Bind() {}

        public Bind(String command, int key, int mods) {
            this.command = command;
            this.key = key;
            this.mods = mods;
        }

        /** The first word of the command (no slash). */
        public String rootName() {
            String c = command.trim();
            if (c.startsWith("/")) c = c.substring(1);
            int sp = c.indexOf(' ');
            return sp < 0 ? c : c.substring(0, sp);
        }

        public boolean sameAs(Bind o) {
            return o != null && key == o.key && mods == o.mods && command.equals(o.command);
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<List<Bind>>() {}.getType();
    private static List<Bind> binds;

    private BindStore() {}

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("commandlist_binds.json");
    }

    private static void ensureLoaded() {
        if (binds != null) return;
        binds = new ArrayList<>();
        try {
            Path f = file();
            if (Files.exists(f)) {
                List<Bind> l = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), TYPE);
                if (l != null) binds.addAll(l);
            }
        } catch (Exception ignored) {
        }
    }

    private static void save() {
        try {
            Files.writeString(file(), GSON.toJson(binds, TYPE), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    public static synchronized List<Bind> all() {
        ensureLoaded();
        return new ArrayList<>(binds);
    }

    public static synchronized boolean hasBindFor(String root) {
        ensureLoaded();
        for (Bind b : binds) if (b.rootName().equals(root)) return true;
        return false;
    }

    /** Adds a bind, replacing {@code old} (if any) and any other bind on the same key combo. */
    public static synchronized void put(Bind old, Bind fresh) {
        ensureLoaded();
        if (old != null) binds.removeIf(b -> b.sameAs(old));
        binds.removeIf(b -> b.key == fresh.key && b.mods == fresh.mods);
        binds.add(fresh);
        save();
    }

    public static synchronized void remove(Bind bind) {
        ensureLoaded();
        binds.removeIf(b -> b.sameAs(bind));
        save();
    }

    public static String keyName(int key, int mods) {
        StringBuilder sb = new StringBuilder();
        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) sb.append("Ctrl+");
        if ((mods & GLFW.GLFW_MOD_ALT) != 0) sb.append("Alt+");
        if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) sb.append("Shift+");
        sb.append(InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString());
        return sb.toString();
    }
}
