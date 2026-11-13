package com.example.commandlist.client;

import com.example.commandlist.Descriptions;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Locale;

/**
 * Best-effort guess of which mod a command belongs to. Minecraft does not store this,
 * so several strategies are tried in order of reliability.
 */
public final class ModResolver {
    public record Source(String modId, String modName, String modDescription) {}

    private static final Source MINECRAFT = new Source("minecraft", "Minecraft", null);
    private static final Source FORGE = new Source("forge", "Forge", null);
    private static final Source UNKNOWN = new Source("?", "Unknown mod", null);

    private ModResolver() {}

    /** @param serverNode the node from the integrated server's dispatcher, or null on multiplayer */
    public static Source resolve(String commandName, CommandNode<CommandSourceStack> serverNode) {
        // 1. "modid:command" namespace
        int colon = commandName.indexOf(':');
        if (colon > 0) {
            String ns = commandName.substring(0, colon);
            if (ns.equals("minecraft")) return MINECRAFT;
            Source s = byId(ns);
            return s != null ? s : new Source(ns, ns, null);
        }
        // 2. known vanilla / Forge commands
        if (Descriptions.isVanilla(commandName)) return MINECRAFT;
        if (Descriptions.isForge(commandName)) return FORGE;

        // 3. Singleplayer only: look at the package of the code that runs the command
        if (serverNode != null) {
            Class<?> cls = findExecutorClass(serverNode, 8);
            if (cls != null) {
                Source s = fromClass(cls);
                if (s != null) return s;
            }
        }
        // 4. Command name matches (or starts with) a mod id
        Source loose = byLooseId(commandName);
        return loose != null ? loose : UNKNOWN;
    }

    private static Class<?> findExecutorClass(CommandNode<?> node, int depth) {
        if (node.getCommand() != null) return node.getCommand().getClass();
        if (depth <= 0) return null;
        for (CommandNode<?> child : node.getChildren()) {
            Class<?> c = findExecutorClass(child, depth - 1);
            if (c != null) return c;
        }
        return null;
    }

    private static Source fromClass(Class<?> cls) {
        String pkg = cls.getPackageName();
        if (pkg.startsWith("net.minecraft.") || pkg.startsWith("net.minecraftforge.")) return null;
        String[] segments = pkg.split("\\.");
        for (IModInfo info : ModList.get().getMods()) {
            String id = norm(info.getModId());
            if (id.length() < 3 || id.equals("minecraft") || id.equals("forge")) continue;
            for (String seg : segments) {
                if (norm(seg).equals(id)) return from(info);
            }
        }
        return null;
    }

    private static Source byId(String id) {
        for (IModInfo info : ModList.get().getMods()) {
            if (info.getModId().equals(id)) return from(info);
        }
        return null;
    }

    private static Source byLooseId(String commandName) {
        String n = norm(commandName);
        if (n.length() < 4) return null;
        for (IModInfo info : ModList.get().getMods()) {
            String id = norm(info.getModId());
            if (id.length() < 4 || id.equals("minecraft") || id.equals("forge")) continue;
            if (id.equals(n) || id.startsWith(n) || n.startsWith(id)) return from(info);
        }
        return null;
    }

    private static Source from(IModInfo info) {
        return new Source(info.getModId(), info.getDisplayName(), info.getDescription());
    }

    private static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /** First sentence / line of a mod description, shortened for display. */
    public static String firstSentence(String text) {
        String t = text.strip().replace("\r", "");
        int nl = t.indexOf('\n');
        if (nl > 0) t = t.substring(0, nl);
        int dot = t.indexOf(". ");
        if (dot > 0) t = t.substring(0, dot + 1);
        if (t.length() > 160) t = t.substring(0, 157) + "...";
        return t.strip();
    }
}
