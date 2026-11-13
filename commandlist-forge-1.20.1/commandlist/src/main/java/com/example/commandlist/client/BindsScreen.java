package com.example.commandlist.client;

import com.example.commandlist.BindStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Overview of all command keybinds with Edit / Remove buttons. */
public class BindsScreen extends Screen {
    private final Screen parent;
    private BindList list;
    private int count;

    public BindsScreen(Screen parent) {
        super(Component.literal("My Command Keybinds"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        list = new BindList(this.minecraft, this.width, this.height, 32, this.height - 36);
        List<BindStore.Bind> binds = BindStore.all();
        count = binds.size();
        for (BindStore.Bind b : binds) list.addBind(b);
        addRenderableWidget(list);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        if (count == 0) {
            g.drawCenteredString(this.font, "No keybinds yet.", this.width / 2, this.height / 2 - 10, 0xAAAAAA);
            g.drawCenteredString(this.font, "Press the Key button next to a command to add one.",
                    this.width / 2, this.height / 2 + 2, 0x777777);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private class BindList extends ObjectSelectionList<BindList.Row> {
        BindList(Minecraft mc, int w, int h, int top, int bottom) {
            super(mc, w, h, top, bottom, 28);
        }

        @Override
        public int getRowWidth() {
            return Math.min(this.width - 40, 440);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + getRowWidth() / 2 + 6;
        }

        void addBind(BindStore.Bind b) {
            this.addEntry(new Row(b));
        }

        private class Row extends ObjectSelectionList.Entry<Row> {
            private final BindStore.Bind bind;
            private final Button edit;
            private final Button remove;

            Row(BindStore.Bind bind) {
                this.bind = bind;
                this.edit = Button.builder(Component.literal("Edit"),
                                b -> BindsScreen.this.minecraft.setScreen(
                                        new BindScreen(BindsScreen.this, bind, bind.command)))
                        .bounds(0, 0, 40, 20).build();
                this.remove = Button.builder(Component.literal("Remove"), b -> {
                    BindStore.remove(bind);
                    BindsScreen.this.minecraft.setScreen(new BindsScreen(BindsScreen.this.parent));
                }).bounds(0, 0, 54, 20).build();
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                Font font = BindsScreen.this.font;
                int bx = left + width - 40 - 54 - 3 - 4;
                int by = top + (height - 20) / 2;
                edit.setX(bx);
                edit.setY(by);
                remove.setX(bx + 43);
                remove.setY(by);

                String keyText = BindStore.keyName(bind.key, bind.mods);
                g.drawString(font, keyText, left + 4, top + (height - 9) / 2, 0xFFD700, false);
                int kw = Math.max(70, font.width(keyText) + 10);
                g.drawString(font, font.plainSubstrByWidth("/" + bind.command, bx - left - kw - 12),
                        left + 4 + kw, top + (height - 9) / 2, 0xFFFFFF, false);

                edit.render(g, mouseX, mouseY, partialTick);
                remove.render(g, mouseX, mouseY, partialTick);
            }

            @Override
            public boolean mouseClicked(double x, double y, int button) {
                return edit.mouseClicked(x, y, button) || remove.mouseClicked(x, y, button);
            }

            @Override
            public Component getNarration() {
                return Component.literal(bind.command);
            }
        }
    }
}
