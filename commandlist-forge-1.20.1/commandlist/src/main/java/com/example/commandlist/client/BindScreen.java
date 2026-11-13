package com.example.commandlist.client;

import com.example.commandlist.BindStore;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Lets the player choose the exact command text and the key (with optional Ctrl/Alt/Shift). */
public class BindScreen extends Screen {
    private final Screen parent;
    private final BindStore.Bind existing;

    private String commandText;
    private int key;
    private int mods;
    private boolean listening = false;
    private String warning = "";

    private EditBox commandBox;
    private Button keyButton;

    public BindScreen(Screen parent, BindStore.Bind existing, String defaultCommand) {
        super(Component.literal("Bind Command to Key"));
        this.parent = parent;
        this.existing = existing;
        this.commandText = existing != null ? existing.command : defaultCommand;
        this.key = existing != null ? existing.key : -1;
        this.mods = existing != null ? existing.mods : 0;
        updateWarning();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        commandBox = new EditBox(this.font, cx - 130, cy - 32, 260, 20, Component.literal("Command"));
        commandBox.setMaxLength(256);
        commandBox.setValue(commandText);
        commandBox.setResponder(s -> commandText = s);
        addRenderableWidget(commandBox);

        keyButton = Button.builder(keyLabel(), b -> {
            listening = !listening;
            if (listening) this.setFocused(null);
            updateKeyButton();
        }).bounds(cx - 130, cy - 6, 260, 20).build();
        addRenderableWidget(keyButton);

        addRenderableWidget(Button.builder(Component.literal("Save"), b -> save())
                .bounds(cx - 130, cy + 34, 126, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(cx + 4, cy + 34, 126, 20).build());

        if (existing != null) {
            addRenderableWidget(Button.builder(Component.literal("Remove Keybind"), b -> {
                BindStore.remove(existing);
                this.minecraft.setScreen(parent);
            }).bounds(cx - 130, cy + 58, 260, 20).build());
        }
    }

    private Component keyLabel() {
        if (listening) return Component.literal("> Press a key (Esc to cancel) <");
        if (key >= 0) return Component.literal("Key: " + BindStore.keyName(key, mods));
        return Component.literal("Key: (click, then press a key)");
    }

    private void updateKeyButton() {
        if (keyButton != null) keyButton.setMessage(keyLabel());
    }

    private void updateWarning() {
        warning = "";
        if (key < 0) return;
        for (BindStore.Bind b : BindStore.all()) {
            if (b.key == key && b.mods == mods && !b.sameAs(existing)) {
                warning = "Replaces existing bind: /" + b.command;
                return;
            }
        }
        if (mods == 0 && this.minecraft != null) {
            for (KeyMapping km : this.minecraft.options.keyMappings) {
                if (km.getKey().getType() == InputConstants.Type.KEYSYM && km.getKey().getValue() == key) {
                    warning = "Also used by: " + I18n.get(km.getName()) + " (both will trigger)";
                    return;
                }
            }
        }
    }

    private void save() {
        String cmd = commandText.trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1).trim();
        if (cmd.isEmpty()) {
            warning = "Enter a command first.";
            return;
        }
        if (key < 0) {
            warning = "Click the key button and press a key first.";
            return;
        }
        BindStore.put(existing, new BindStore.Bind(cmd, key, mods));
        this.minecraft.setScreen(parent);
    }

    private static boolean isModifierKey(int k) {
        return k == GLFW.GLFW_KEY_UNKNOWN
                || (k >= GLFW.GLFW_KEY_LEFT_SHIFT && k <= GLFW.GLFW_KEY_RIGHT_SUPER);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listening = false;
            } else if (!isModifierKey(keyCode)) {
                key = keyCode;
                mods = modifiers & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT);
                listening = false;
                updateWarning();
            }
            updateKeyButton();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int cy = this.height / 2;
        g.drawCenteredString(this.font, this.title, cx, cy - 70, 0xFFFFFF);
        g.drawString(this.font, "Command to run (add arguments if it needs them):", cx - 130, cy - 44, 0xA0A0A0, false);
        super.render(g, mouseX, mouseY, partialTick);
        if (!warning.isEmpty()) {
            g.drawCenteredString(this.font, warning, cx, cy + 20, 0xFFAA00);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
