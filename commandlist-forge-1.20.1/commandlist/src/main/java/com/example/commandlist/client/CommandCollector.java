package com.example.commandlist.client;

import com.example.commandlist.CommandInfo;
import com.example.commandlist.SeenStore;
import com.example.commandlist.UserOverrides;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import com.example.commandlist.Descriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Collects every root command the server sent us and enriches it with mod + description. */
public final class CommandCollector {
    private CommandCollector() {}

    public static List<CommandInfo> collect() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener conn = mc.getConnection();
        if (conn == null) return List.of();

        CommandDispatcher<SharedSuggestionProvider> dispatcher = conn.getCommands();
        Map<CommandNode<SharedSuggestionProvider>, String> usages = Map.of();
        try {
            usages = dispatcher.getSmartUsage(dispatcher.getRoot(), conn.getSuggestionsProvider());
        } catch (Exception ignored) {
        }

        CommandDispatcher<CommandSourceStack> serverDispatcher = null;
        IntegratedServer server = mc.getSingleplayerServer();
        if (server != null) serverDispatcher = server.getCommands().getDispatcher();

        Map<String, Long> seen = SeenStore.load();
        Map<String, UserOverrides.Info> overrides = UserOverrides.load();
        long now = System.currentTimeMillis();
        boolean changed = false;

        List<CommandInfo> out = new ArrayList<>();
        for (CommandNode<SharedSuggestionProvider> node : dispatcher.getRoot().getChildren()) {
            String name = node.getName();

            Long t = seen.get(name);
            if (t == null) {
                t = now;
                seen.put(name, t);
                changed = true;
            }

            String usage = usages.getOrDefault(node, name);
            CommandNode<CommandSourceStack> serverNode =
                    serverDispatcher == null ? null : serverDispatcher.getRoot().getChild(name);
            ModResolver.Source src = ModResolver.resolve(name, serverNode);

            UserOverrides.Info ov = overrides.get(name.toLowerCase(Locale.ROOT));
            String modName = src.modName();
            String desc = null;
            if (ov != null) {
                if (ov.mod != null && !ov.mod.isBlank()) modName = ov.mod;
                if (ov.description != null && !ov.description.isBlank()) desc = ov.description;
            }

            String base = name.contains(":") ? name.substring(name.indexOf(':') + 1) : name;
            if (desc == null && src.modId().equals("minecraft")) desc = Descriptions.vanilla(base);
            if (desc == null && src.modId().equals("forge")) desc = Descriptions.forge(base);
            if (desc == null && src.modDescription() != null && !src.modDescription().isBlank()) {
                desc = "Mod info: " + ModResolver.firstSentence(src.modDescription());
            }
            if (desc == null) desc = "No description available for this command.";

            out.add(new CommandInfo(name, usage, t, src.modId(), modName, desc, needsArgs(name, usage)));
        }
        if (changed) SeenStore.save(seen);
        return out;
    }

    /** True if the command has required arguments (ignoring [optional] parts) or is an alias. */
    private static boolean needsArgs(String name, String usage) {
        String rest = usage.startsWith(name) ? usage.substring(name.length()) : usage;
        String prev;
        do {
            prev = rest;
            rest = rest.replaceAll("\\[[^\\[\\]]*\\]", "");
        } while (!rest.equals(prev));
        return rest.contains("<") || rest.contains("(") || rest.contains("->") || rest.contains("...");
    }
}
