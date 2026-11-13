package com.example.commandlist.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

/** Runs a command exactly as if the player had typed it into chat. */
public final class CommandRunner {
    private CommandRunner() {}

    public static void run(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String cmd = command.trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        if (cmd.isEmpty()) return;

        try {
            // Going through ChatScreen keeps Forge client-side command handling and chat history working.
            ChatScreen helper = new ChatScreen("");
            helper.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            try {
                helper.handleChatInput("/" + cmd, true);
            } finally {
                helper.removed();
            }
        } catch (Exception e) {
            mc.player.connection.sendCommand(cmd);
        }
    }
}
