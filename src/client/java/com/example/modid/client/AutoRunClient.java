package com.example.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
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

            // Toggle Auto-Run
            while (toggleAutoRunKey.wasPressed()) {
                autoRunEnabled = !autoRunEnabled;

                if (autoRunEnabled) {
                    autoSprintEnabled = false;
                    if (wasSimulatingSprint) {
                        sprintKey.setPressed(false);
                        wasSimulatingSprint = false;
                    }
                    client.player.sendMessage(Text.of("Auto-Run Enabled"), true);
                } else {
                    client.player.sendMessage(Text.of("Auto-Run Disabled"), true);
                }

                if (!autoRunEnabled && wasSimulatingForward) {
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }
            }

            // Toggle Auto-Sprint
            while (toggleAutoSprintKey.wasPressed()) {
                autoSprintEnabled = !autoSprintEnabled;

                if (autoSprintEnabled) {
                    autoRunEnabled = false;
                    if (wasSimulatingForward) {
                        forwardKey.setPressed(false);
                        wasSimulatingForward = false;
                    }
                    client.player.sendMessage(Text.of("Auto-Sprint Enabled"), true);
                } else {
                    client.player.sendMessage(Text.of("Auto-Sprint Disabled"), true);
                }

                if (!autoSprintEnabled && wasSimulatingSprint) {
                    sprintKey.setPressed(false);
                    wasSimulatingSprint = false;
                }
            }

            // Cancel both if pressing back
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

            // Handle Auto-Run
            if (autoRunEnabled && !autoSprintEnabled) {
                if (!forwardKey.isPressed()) {
                    forwardKey.setPressed(true);
                    wasSimulatingForward = true;
                }
            }

            // Handle Auto-Sprint
            if (autoSprintEnabled) {
                if (!sprintKey.isPressed()) {
                    sprintKey.setPressed(true);
                    wasSimulatingSprint = true;
                }
                if (!forwardKey.isPressed()) {
                    forwardKey.setPressed(true);
                    wasSimulatingForward = true;
                }
            }

            // Reset keys when neither mode is active
            if (!autoRunEnabled && !autoSprintEnabled) {
                if (wasSimulatingForward) {
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }
                if (wasSimulatingSprint) {
                    sprintKey.setPressed(false);
                    wasSimulatingSprint = false;
                }
            }

        });
    }
}
