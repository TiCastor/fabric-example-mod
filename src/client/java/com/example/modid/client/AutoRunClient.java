package com.example.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoRunClient implements ClientModInitializer {
    private static KeyBinding toggleAutoRunKey;
    private static boolean autoRunEnabled = false;

    @Override
    public void onInitializeClient() {
        // Register the keybinding
        toggleAutoRunKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.modid.autorun",  // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,      // Default key: R
                "category.modid.controls"  // Translation key for category
        ));

        // Check for key press every tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleAutoRunKey.wasPressed()) {
                autoRunEnabled = !autoRunEnabled;
                client.player.sendMessage(
                    autoRunEnabled ? 
                    net.minecraft.text.Text.of("Auto-Run Enabled") : 
                    net.minecraft.text.Text.of("Auto-Run Disabled"), true);
            }

            if (autoRunEnabled && client.player != null && client.currentScreen == null) {
                client.options.forwardKey.setPressed(true);
            } else {
                client.options.forwardKey.setPressed(false);
            }
        });
    }
}
// This code is a client-side mod for Minecraft using Fabric.