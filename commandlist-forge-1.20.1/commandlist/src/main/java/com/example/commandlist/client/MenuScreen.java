package com.example.commandlist.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The simple menu: a title and a single button. */
public class MenuScreen extends Screen {
    public MenuScreen() {
        super(Component.literal("Command List"));
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Show All Commands"),
                        b -> this.minecraft.setScreen(new CommandsScreen(this)))
                .bounds(this.width / 2 - 100, this.height / 2 - 10, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 35, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }
}
