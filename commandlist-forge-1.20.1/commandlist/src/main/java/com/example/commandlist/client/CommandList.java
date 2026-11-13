package com.example.commandlist.client;

import com.example.commandlist.BindStore;
import com.example.commandlist.CommandInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Scrolling list: group headers + command rows with Use / Copy / Key buttons. */
public class CommandList extends ObjectSelectionList<CommandList.Entry> {

    public interface Actions {
        void use(CommandInfo c);
        void copy(CommandInfo c);
        void bind(CommandInfo c);
    }

    private static final int ROW_HEIGHT = 40;
    private final Actions actions;

    public CommandList(Minecraft mc, int width, int height, int top, int bottom, Actions actions) {
        super(mc, width, height, top, bottom, ROW_HEIGHT);
        this.actions = actions;
    }

    @Override
    public int getRowWidth() {
        return Math.min(this.width - 40, 440);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + getRowWidth() / 2 + 6;
    }

    /** Fills the list. A header row is inserted whenever {@code groupKey} changes. */
    public void setItems(List<CommandInfo> sorted, Function<CommandInfo, String> groupKey) {
        this.clearEntries();
        String last = null;
        for (CommandInfo info : sorted) {
            String key = groupKey.apply(info);
            if (!key.equals(last)) {
                this.addEntry(new HeaderEntry(key));
                last = key;
            }
            this.addEntry(new CommandEntry(info));
        }
        this.setScrollAmount(0);
    }

    static int modColor(String modName) {
        return switch (modName) {
            case "Minecraft" -> 0x55FF55;
            case "Forge" -> 0xFFAA00;
            case "Unknown mod" -> 0xAAAAAA;
            default -> 0x55FFFF;
        };
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
    }

    public static class HeaderEntry extends Entry {
        private final Component label;

        public HeaderEntry(String text) {
            this.label = Component.literal(text).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = Minecraft.getInstance().font;
            g.drawString(font, label, left + 4, top + height - 14, 0xFFFFFF, false);
            g.fill(left, top + height - 3, left + width, top + height - 2, 0x66FFD700);
        }

        @Override
        public Component getNarration() {
            return label;
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return false;
        }
    }

    public class CommandEntry extends Entry {
        private static final int GAP = 3;
        private final CommandInfo info;
        private final Button use;
        private final Button copy;
        private final Button key;

        public CommandEntry(CommandInfo info) {
            this.info = info;
            this.use = Button.builder(Component.literal("Use"), b -> actions.use(info))
                    .bounds(0, 0, 34, 20)
                    .tooltip(Tooltip.create(Component.literal(info.needsArgs()
                            ? "Needs arguments: opens chat with /" + info.name() + " ready to finish"
                            : "Run /" + info.name() + " right now")))
                    .build();
            this.copy = Button.builder(Component.literal("Copy"), b -> actions.copy(info))
                    .bounds(0, 0, 36, 20)
                    .tooltip(Tooltip.create(Component.literal("Copy /" + info.name() + " to the clipboard")))
                    .build();
            this.key = Button.builder(
                            Component.literal(BindStore.hasBindFor(info.name()) ? "Key*" : "Key"),
                            b -> actions.bind(info))
                    .bounds(0, 0, 34, 20)
                    .tooltip(Tooltip.create(Component.literal("Bind this command to a keyboard key")))
                    .build();
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = CommandList.this.minecraft.font;

            if (hovering) g.fill(left, top, left + width, top + height, 0x22FFFFFF);

            int buttonsWidth = use.getWidth() + copy.getWidth() + key.getWidth() + GAP * 2;
            int bx = left + width - buttonsWidth - 4;
            int by = top + (height - 20) / 2;
            use.setX(bx);
            use.setY(by);
            copy.setX(bx + use.getWidth() + GAP);
            copy.setY(by);
            key.setX(bx + use.getWidth() + copy.getWidth() + GAP * 2);
            key.setY(by);

            int textLeft = left + 4;
            int textWidth = bx - textLeft - 6;

            String nameText = "/" + info.name();
            g.drawString(font, font.plainSubstrByWidth(nameText, textWidth), textLeft, top + 2, 0xFFFFFF, false);
            int nameW = font.width(nameText);
            if (nameW + 8 < textWidth) {
                String tag = "[" + info.modName() + "]";
                g.drawString(font, font.plainSubstrByWidth(tag, textWidth - nameW - 8),
                        textLeft + nameW + 8, top + 2, modColor(info.modName()), false);
            }
            g.drawString(font, font.plainSubstrByWidth(info.description(), textWidth),
                    textLeft, top + 13, 0xBBBBBB, false);
            g.drawString(font, font.plainSubstrByWidth("/" + info.usage(), textWidth),
                    textLeft, top + 24, 0x777777, false);

            use.render(g, mouseX, mouseY, partialTick);
            copy.render(g, mouseX, mouseY, partialTick);
            key.render(g, mouseX, mouseY, partialTick);

            // Full details in a tooltip when hovering the text part of the row
            Screen screen = CommandList.this.minecraft.screen;
            if (hovering && screen != null && mouseX < bx - 4) {
                List<FormattedCharSequence> lines = new ArrayList<>();
                lines.addAll(font.split(Component.literal("/" + info.name())
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD), 280));
                lines.addAll(font.split(Component.literal("Mod: " + info.modName())
                        .withStyle(ChatFormatting.AQUA), 280));
                lines.addAll(font.split(Component.literal(info.description()), 280));
                lines.addAll(font.split(Component.literal("/" + info.usage())
                        .withStyle(ChatFormatting.GRAY), 280));
                screen.setTooltipForNextRenderPass(lines);
            }
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (use.mouseClicked(x, y, button)) return true;
            if (copy.mouseClicked(x, y, button)) return true;
            return key.mouseClicked(x, y, button);
        }

        @Override
        public Component getNarration() {
            return Component.literal(info.name());
        }
    }
}
