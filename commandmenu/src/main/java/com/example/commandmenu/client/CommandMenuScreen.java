package com.example.commandmenu.client;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommandMenuScreen extends Screen {
    private EditBox searchBox;
    private Button sortButton;
    private final List<CommandEntry> allCommands = new ArrayList<>();
    private final List<CommandEntry> filteredCommands = new ArrayList<>();
    private int scrollOffset = 0;
    private boolean sortAlphabetical = true; // true = A-Z, false = Default order

    public CommandMenuScreen() {
        super(Component.literal("In-Game Commands Menu"));
    }

    @Override
    protected void init() {
        super.init();

        // Search bar widget
        this.searchBox = new EditBox(this.font, this.width / 2 - 150, 40, 300, 20, Component.literal("Search Commands"));
        this.searchBox.setResponder(this::filterCommands);
        this.addRenderableWidget(this.searchBox);

        // Sorting toggle button
        this.sortButton = this.addRenderableWidget(Button.builder(
                Component.literal("Sort: " + (sortAlphabetical ? "A-Z" : "Default")),
                button -> {
                    sortAlphabetical = !sortAlphabetical;
                    button.setMessage(Component.literal("Sort: " + (sortAlphabetical ? "A-Z" : "Default")));
                    sortAndFilterCommands();
                })
                .bounds(this.width / 2 - 150, 65, 140, 20)
                .build());

        // Close button
        this.addRenderableWidget(Button.builder(
                Component.literal("Close"),
                button -> this.onClose())
                .bounds(this.width / 2 + 10, 65, 140, 20)
                .build());

        loadCommands();
    }

    private void loadCommands() {
        allCommands.clear();
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null && connection.getCommands() != null) {
            var root = connection.getCommands().getRoot();
            int index = 0;
            for (CommandNode<?> node : root.getChildren()) {
                if (node.canUse(Minecraft.getInstance().player.createCommandSourceStack())) {
                    String name = "/" + node.getName();
                    allCommands.add(new CommandEntry(name, index++));
                }
            }
        }
        sortAndFilterCommands();
    }

    private void sortAndFilterCommands() {
        if (sortAlphabetical) {
            allCommands.sort(Comparator.comparing(e -> e.name));
        } else {
            allCommands.sort(Comparator.comparingInt(e -> e.originalIndex));
        }
        filterCommands(searchBox != null ? searchBox.getValue() : "");
    }

    private void filterCommands(String query) {
        filteredCommands.clear();
        String lowerQuery = query.toLowerCase();
        for (CommandEntry entry : allCommands) {
            if (entry.name.toLowerCase().contains(lowerQuery)) {
                filteredCommands.add(entry);
            }
        }
        scrollOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, filteredCommands.size() - 12);
        if (delta > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (delta < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        
        // Title banner
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // Background box for command list
        guiGraphics.fill(this.width / 2 - 160, 95, this.width / 2 + 160, this.height - 30, 0x80000000);

        int startY = 100;
        int visibleCount = 12;
        int endIndex = Math.min(scrollOffset + visibleCount, filteredCommands.size());

        for (int i = scrollOffset; i < endIndex; i++) {
            CommandEntry entry = filteredCommands.get(i);
            int y = startY + (i - scrollOffset) * 20;

            // Hover highlight
            if (mouseX >= this.width / 2 - 150 && mouseX <= this.width / 2 + 150 && mouseY >= y && mouseY < y + 18) {
                guiGraphics.fill(this.width / 2 - 150, y, this.width / 2 + 150, y + 18, 0x55FFFFFF);
            }

            guiGraphics.drawString(this.font, entry.name, this.width / 2 - 142, y + 5, 0xFFFFFF);
            guiGraphics.drawString(this.font, "[Click to insert]", this.width / 2 + 55, y + 5, 0xAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startY = 100;
            int visibleCount = 12;
            int endIndex = Math.min(scrollOffset + visibleCount, filteredCommands.size());

            for (int i = scrollOffset; i < endIndex; i++) {
                int y = startY + (i - scrollOffset) * 20;
                if (mouseX >= this.width / 2 - 150 && mouseX <= this.width / 2 + 150 && mouseY >= y && mouseY < y + 18) {
                    CommandEntry entry = filteredCommands.get(i);
                    // Open chat screen with command pre-filled
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ChatScreen(entry.name + " "));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class CommandEntry {
        private final String name;
        private final int originalIndex;

        public CommandEntry(String name, int originalIndex) {
            this.name = name;
            this.originalIndex = originalIndex;
        }
    }
}
