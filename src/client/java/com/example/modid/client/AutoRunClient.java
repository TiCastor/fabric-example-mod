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
    private static KeyBinding toggleAutoSprintKey;

    private static boolean autoRunEnabled = false;
    private static boolean autoSprintEnabled = false;

    private static boolean wasSimulatingForward = false;
    private static boolean wasSimulatingSprint = false;

    @Override
    public void onInitializeClient() {
        toggleAutoRunKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.modid.autorun",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.modid.controls"
        ));

        toggleAutoSprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.modid.autosprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.modid.controls"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            KeyBinding forwardKey = client.options.forwardKey;
            KeyBinding backKey = client.options.backKey;
            KeyBinding sprintKey = client.options.sprintKey;

            // Toggle auto-run
            while (toggleAutoRunKey.wasPressed()) {
                autoRunEnabled = !autoRunEnabled;
                if (!autoRunEnabled && wasSimulatingForward) {
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }
                client.player.sendMessage(
                        Text.of(autoRunEnabled ? "Auto-Run Enabled" : "Auto-Run Disabled"), true
                );
            }

            // Toggle auto-sprint
            while (toggleAutoSprintKey.wasPressed()) {
                autoSprintEnabled = !autoSprintEnabled;
                if (!autoSprintEnabled && wasSimulatingSprint) {
                    sprintKey.setPressed(false);
                    wasSimulatingSprint = false;
                }
                client.player.sendMessage(
                        Text.of(autoSprintEnabled ? "Auto-Sprint Enabled" : "Auto-Sprint Disabled"), true
                );
            }

            // Cancel both if back is pressed
            if ((autoRunEnabled || autoSprintEnabled) && backKey.isPressed()) {
                if (autoRunEnabled) {
                    autoRunEnabled = false;
                    if (wasSimulatingForward) {
                        forwardKey.setPressed(false);
                        wasSimulatingForward = false;
                    }
                }
                if (autoSprintEnabled) {
                    autoSprintEnabled = false;
                    if (wasSimulatingSprint) {
                        sprintKey.setPressed(false);
                        wasSimulatingSprint = false;
                    }
                }
                client.player.sendMessage(Text.of("Auto-Movement Cancelled"), true);
            }

            // Handle auto-run simulation
            if (autoRunEnabled) {
                if (!forwardKey.isPressed()) {
                    forwardKey.setPressed(true);
                    wasSimulatingForward = true;
                }
            } else if (wasSimulatingForward) {
                forwardKey.setPressed(false);
                wasSimulatingForward = false;
            }

            // Handle auto-sprint simulation
            if (autoSprintEnabled) {
                if (!sprintKey.isPressed()) {
                    sprintKey.setPressed(true);
                    wasSimulatingSprint = true;
                }
            } else if (wasSimulatingSprint) {
                sprintKey.setPressed(false);
                wasSimulatingSprint = false;
            }
        });
    }
}
