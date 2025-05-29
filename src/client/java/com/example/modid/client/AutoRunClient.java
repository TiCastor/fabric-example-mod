package com.example.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoRunClient implements ClientModInitializer {
    private static KeyBinding toggleAutoRunKey;
    private static boolean autoRunEnabled = false;
    private static boolean wasSimulatingForward = false;

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

            KeyBinding forwardKey = client.options.forwardKey;
            KeyBinding backKey = client.options.backKey;

            // Toggle auto-run with the hotkey
            while (toggleAutoRunKey.wasPressed()) {
                autoRunEnabled = !autoRunEnabled;

                if (!autoRunEnabled && wasSimulatingForward) {
                    // We were simulating forward, so clear it
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }

                client.player.sendMessage(
                        Text.of(autoRunEnabled ? "Auto-Run Enabled" : "Auto-Run Disabled"),
                        true
                );
            }

            // Cancel auto-run if player presses back
            if (autoRunEnabled && backKey.isPressed()) {
                autoRunEnabled = false;
                if (wasSimulatingForward) {
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }
                client.player.sendMessage(Text.of("Auto-Run Cancelled"), true);
            }

            // Simulate pressing forward if auto-run is on and player isn't pressing forward
            if (autoRunEnabled) {
                if (!forwardKey.isPressed()) {
                    forwardKey.setPressed(true);
                    wasSimulatingForward = true;
                }
            } else {
                // Make sure we clean up only our simulated key press
                if (wasSimulatingForward) {
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }
            }
        });
    }
}
