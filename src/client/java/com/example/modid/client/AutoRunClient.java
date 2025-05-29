package com.example.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
// import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoRunClient implements ClientModInitializer {
    private static KeyBinding toggleAutoRunKey;
    private static boolean autoRunEnabled = false;

    @Override
    public void onInitializeClient() {
        toggleAutoRunKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.modid.autorun",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.modid.controls"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            // Toggle auto-run on key press
            while (toggleAutoRunKey.wasPressed()) {
                autoRunEnabled = !autoRunEnabled;
                client.player.sendMessage(
                        Text.of(autoRunEnabled ? "Auto-Run Enabled" : "Auto-Run Disabled"),
                        true
                );
            }

            // Cancel auto-run if player moves backward manually
            if (autoRunEnabled && client.options.backKey.isPressed()) {
                autoRunEnabled = false;
                client.player.sendMessage(Text.of("Auto-Run Cancelled"), true);
            }

            // If auto-run is enabled and player isn't holding W, simulate forward movement
            if (autoRunEnabled) {
                if (!client.options.forwardKey.isPressed()) {
                    client.options.forwardKey.setPressed(true);
                }
            } else {
                // If auto-run is off, don't force the key anymore
                if (!client.options.forwardKey.isPressed()) {
                    client.options.forwardKey.setPressed(false);
                }
            }
        });
    }
}
