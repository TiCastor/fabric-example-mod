package com.example.modid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
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
                "Toggle Auto-Run", // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "Auto-Movement"    // category in controls menu
        ));

        toggleAutoSprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle Auto-Sprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "Auto-Movement"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            GameOptions options = client.options;
            KeyBinding forwardKey = options.forwardKey;
            KeyBinding sprintKey = options.sprintKey;

            // Toggle Auto-Run
            if (toggleAutoRunKey.wasPressed()) {
                autoRunEnabled = !autoRunEnabled;

                if (autoRunEnabled) {
                    autoSprintEnabled = false;

                    // Release sprint key if switching from sprint to run
                    if (wasSimulatingSprint) {
                        sprintKey.setPressed(false);
                        wasSimulatingSprint = false;
                    }

                    client.player.sendMessage(Text.of("Auto-Run Enabled"), true);
                } else {
                    client.player.sendMessage(Text.of("Auto-Run Disabled"), true);
                }
            }

            // Toggle Auto-Sprint
            if (toggleAutoSprintKey.wasPressed()) {
                autoSprintEnabled = !autoSprintEnabled;

                if (autoSprintEnabled) {
                    autoRunEnabled = false;

                    // Release forward key if switching from run to sprint
                    if (wasSimulatingForward) {
                        forwardKey.setPressed(false);
                        wasSimulatingForward = false;
                    }

                    client.player.sendMessage(Text.of("Auto-Sprint Enabled"), true);
                } else {
                    client.player.sendMessage(Text.of("Auto-Sprint Disabled"), true);
                }
            }

            // Cancel auto modes if moving backward
            if ((autoRunEnabled || autoSprintEnabled) && options.backKey.isPressed()) {
                autoRunEnabled = false;
                autoSprintEnabled = false;

                if (wasSimulatingForward) {
                    forwardKey.setPressed(false);
                    wasSimulatingForward = false;
                }

                if (wasSimulatingSprint) {
                    sprintKey.setPressed(false);
                    wasSimulatingSprint = false;
                }

                client.player.sendMessage(Text.of("Auto-Movement stopped"), true);
            }

            // Auto-Run Logic
            if (autoRunEnabled && !autoSprintEnabled) {
                if (!forwardKey.isPressed()) {
                    forwardKey.setPressed(true);
                    wasSimulatingForward = true;
                }
            }

            // Auto-Sprint Logic
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

            // Reset keys if no auto modes are active
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
