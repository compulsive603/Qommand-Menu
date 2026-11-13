package com.example.commandlist.client;

import com.example.commandlist.CommandInfo;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommandsScreen extends Screen implements CommandList.Actions {
    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    /** Commands that deserve a confirmation even though they need no arguments. */
    private static final Set<String> DANGEROUS = Set.of("stop", "kill", "reload", "save-off");

    private enum SortMode {
        AZ("Sort: A-Z"), ZA("Sort: Z-A"), NEWEST("Sort: Newest"), OLDEST("Sort: Oldest"), MOD("Sort: By Mod");

        final String label;

        SortMode(String label) {
            this.label = label;
        }

        SortMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        boolean byDate() {
            return this == NEWEST || this == OLDEST;
        }
    }

    private final Screen parent;
    private List<CommandInfo> all;
    private SortMode mode = SortMode.AZ;
    private String query = "";
    private int shown = 0;
    private double savedScroll = 0;
    private String status = "";
    private long statusUntil = 0;

    private EditBox search;
    private CommandList list;

    public CommandsScreen(Screen parent) {
        super(Component.literal("All Commands"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (all == null) all = CommandCollector.collect();

        int cx = this.width / 2;
        int rowW = Math.min(this.width - 40, 440);
        int left = cx - rowW / 2;

        search = new EditBox(this.font, left, 22, rowW - 114, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search name, mod or description..."));
        search.setMaxLength(64);
        search.setValue(query);
        search.setResponder(s -> {
            query = s;
            refresh();
        });
        addRenderableWidget(search);

        addRenderableWidget(Button.builder(Component.literal(mode.label), b -> {
            mode = mode.next();
            b.setMessage(Component.literal(mode.label));
            refresh();
        }).bounds(left + rowW - 110, 22, 110, 20).build());

        list = new CommandList(this.minecraft, this.width, this.height, 50, this.height - 52, this);
        addRenderableWidget(list);

        addRenderableWidget(Button.builder(Component.literal("My Keybinds"),
                        b -> leaveTo(new BindsScreen(this)))
                .bounds(cx - 148, this.height - 28, 140, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(cx + 8, this.height - 28, 140, 20).build());

        setInitialFocus(search);
        refresh();
        list.setScrollAmount(savedScroll);
        savedScroll = 0;
    }

    private void refresh() {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.startsWith("/")) q = q.substring(1);

        List<CommandInfo> filtered = new ArrayList<>();
        for (CommandInfo c : all) {
            if (q.isEmpty()
                    || c.name().toLowerCase(Locale.ROOT).contains(q)
                    || c.modName().toLowerCase(Locale.ROOT).contains(q)
                    || c.description().toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(c);
            }
        }

        Comparator<CommandInfo> byName = Comparator.comparing(c -> c.name().toLowerCase(Locale.ROOT));
        switch (mode) {
            case AZ -> filtered.sort(byName);
            case ZA -> filtered.sort(byName.reversed());
            case NEWEST -> filtered.sort(Comparator.comparingLong(CommandInfo::firstSeen).reversed().thenComparing(byName));
            case OLDEST -> filtered.sort(Comparator.comparingLong(CommandInfo::firstSeen).thenComparing(byName));
            case MOD -> filtered.sort(Comparator
                    .comparingInt((CommandInfo c) -> modRank(c.modName()))
                    .thenComparing(c -> c.modName().toLowerCase(Locale.ROOT))
                    .thenComparing(byName));
        }

        shown = filtered.size();
        list.setItems(filtered, c -> switch (mode) {
            case NEWEST, OLDEST -> DAY.format(Instant.ofEpochMilli(c.firstSeen()));
            case MOD -> c.modName();
            default -> {
                char ch = Character.toUpperCase(c.name().charAt(0));
                yield Character.isLetter(ch) ? String.valueOf(ch) : "#";
            }
        });
    }

    private static int modRank(String modName) {
        return switch (modName) {
            case "Minecraft" -> 0;
            case "Forge" -> 1;
            case "Unknown mod" -> 3;
            default -> 2;
        };
    }

    // ---- row actions ----

    @Override
    public void use(CommandInfo c) {
        if (c.needsArgs()) {
            this.minecraft.setScreen(new ChatScreen("/" + c.name() + " "));
            return;
        }
        String base = c.name().contains(":") ? c.name().substring(c.name().indexOf(':') + 1) : c.name();
        if (DANGEROUS.contains(base)) {
            savedScroll = list.getScrollAmount();
            this.minecraft.setScreen(new ConfirmScreen(yes -> {
                if (yes) {
                    this.minecraft.setScreen(null);
                    CommandRunner.run(c.name());
                } else {
                    this.minecraft.setScreen(this);
                }
            }, Component.literal("Run /" + c.name() + "?"),
                    Component.literal("This command can stop the server, kill you or change the world. Run it anyway?")));
            return;
        }
        this.minecraft.setScreen(null);
        CommandRunner.run(c.name());
    }

    @Override
    public void copy(CommandInfo c) {
        this.minecraft.keyboardHandler.setClipboard("/" + c.name());
        flash("Copied /" + c.name() + " to clipboard");
    }

    @Override
    public void bind(CommandInfo c) {
        leaveTo(new BindScreen(this, null, c.name()));
    }

    private void leaveTo(Screen next) {
        savedScroll = list.getScrollAmount();
        this.minecraft.setScreen(next);
    }

    private void flash(String text) {
        status = text;
        statusUntil = Util.getMillis() + 2500;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font,
                Component.literal("All Commands (" + shown + ")"), this.width / 2, 8, 0xFFFFFF);
        if (Util.getMillis() < statusUntil) {
            g.drawCenteredString(this.font, status, this.width / 2, this.height - 44, 0x55FF55);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
