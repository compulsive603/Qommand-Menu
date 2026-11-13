package com.example.commandlist.client;

import com.example.commandlist.BindStore;
import com.example.commandlist.CommandListMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
    public static final KeyMapping OPEN_KEY = new KeyMapping(
            "key.commandlist.open",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.commandlist");

    private ClientSetup() {}

    @Mod.EventBusSubscriber(modid = CommandListMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = CommandListMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            while (OPEN_KEY.consumeClick()) {
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new MenuScreen());
                }
            }
        }

        /** Runs the commands the player bound to keys. Only active while no screen is open. */
        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            if (event.getAction() != GLFW.GLFW_PRESS) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;

            int mods = event.getModifiers()
                    & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT);
            for (BindStore.Bind b : BindStore.all()) {
                if (b.key == event.getKey() && b.mods == mods) {
                    CommandRunner.run(b.command);
                }
            }
        }
    }
}
