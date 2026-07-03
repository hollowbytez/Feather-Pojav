package net.featherpojav.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.featherpojav.client.config.FeatherConfig;
import net.featherpojav.client.gui.FeatherSettingsScreen;
import net.featherpojav.client.hud.FeatherHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class FeatherPojavModClient implements ClientModInitializer {
    public static KeyBinding openSettingsKey;
    public static KeyBinding openHudEditorKey;
    public static KeyBinding zoomKey;
    public static KeyBinding freelookKey;
    public static KeyBinding stopwatchKey;
    public static KeyBinding autoTextKey;
    public static KeyBinding toggleSprintKey;
    public static double originalGamma = -1.0;

    // Zoom state
    public static boolean isZooming = false;
    public static float zoomLevel = 1.0f;
    public static float targetZoomLevel = 1.0f;

    // Freelook state
    public static boolean isFreelooking = false;
    public static float freelookYaw = 0.0f;
    public static float freelookPitch = 0.0f;
    public static net.minecraft.client.option.Perspective originalPerspective = net.minecraft.client.option.Perspective.FIRST_PERSON;

    @Override
    public void onInitializeClient() {
        // Load configurations
        FeatherConfig.load();

        // Cull Logs on Startup
        if (FeatherConfig.INSTANCE.cullLogs) {
            try {
                File logsDir = new File(MinecraftClient.getInstance().runDirectory, "logs");
                if (logsDir.exists() && logsDir.isDirectory()) {
                    File[] files = logsDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".log") || file.getName().endsWith(".log.gz")) {
                                file.delete();
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // Keybindings Registration
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.featherpojav"
        ));

        openHudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                "category.featherpojav"
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.featherpojav"
        ));

        freelookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.freelook",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.featherpojav"
        ));

        stopwatchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.stopwatch",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.featherpojav"
        ));

        autoTextKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.autotext",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.featherpojav"
        ));

        toggleSprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.featherpojav.togglesprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.featherpojav"
        ));

        // Register HUD Render Callback
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            FeatherHudRenderer.renderHUD(context, tickCounter.getTickDelta(false));
        });

        // Register Attack Entity Callback (for Combo & Reach tracker)
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) {
                FeatherHudRenderer.onHit();
                FeatherHudRenderer.onAttackEntity(entity);
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        // Client tick event handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Handle Hitbox outlines toggle in real-time
            if (client.getEntityRenderDispatcher() != null) {
                client.getEntityRenderDispatcher().setRenderHitboxes(FeatherConfig.INSTANCE.hitbox);
            }

            // Handle Menu keybind
            if (openSettingsKey.wasPressed()) {
                client.setScreen(new FeatherSettingsScreen(null));
            }

            // Handle HUD Editor keybind
            if (openHudEditorKey.wasPressed()) {
                client.setScreen(new net.featherpojav.client.gui.FeatherHudEditorScreen(null));
            }

            // Handle Stopwatch keybind
            if (stopwatchKey.wasPressed() && FeatherConfig.INSTANCE.stopwatch) {
                FeatherHudRenderer.toggleStopwatch();
            }

            // Handle Auto Text macro keybind (with click consumption)
            if (FeatherConfig.INSTANCE.autoText) {
                while (autoTextKey.wasPressed()) {
                    if (client.player.networkHandler != null) {
                        client.player.networkHandler.sendChatMessage(FeatherConfig.INSTANCE.autoTextCommand);
                        client.player.sendMessage(Text.of("§a[Hollow] Executed Auto Text Macro (" + FeatherConfig.INSTANCE.autoTextCommand + ")"), true);
                    }
                }
            }

            // Handle Toggle Sprint keypress
            while (toggleSprintKey.wasPressed()) {
                FeatherConfig.INSTANCE.toggleSprint = !FeatherConfig.INSTANCE.toggleSprint;
                FeatherConfig.save();
                client.player.sendMessage(Text.of("§a[Hollow] Toggle Sprint: " + (FeatherConfig.INSTANCE.toggleSprint ? "§2Enabled" : "§4Disabled")), true);
            }

            // Handle Zoom status
            if (FeatherConfig.INSTANCE.zoom) {
                isZooming = zoomKey.isPressed();
            } else {
                isZooming = false;
            }

            // Handle Freelook status
            if (FeatherConfig.INSTANCE.freelook) {
                if (freelookKey.isPressed()) {
                    if (!isFreelooking) {
                        isFreelooking = true;
                        freelookYaw = client.player.getYaw();
                        freelookPitch = client.player.getPitch();
                        if (client.options != null) {
                            originalPerspective = client.options.getPerspective();
                            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                        }
                    }
                } else {
                    if (isFreelooking) {
                        isFreelooking = false;
                        if (client.options != null) {
                            client.options.setPerspective(originalPerspective);
                        }
                    }
                }
            } else {
                if (isFreelooking) {
                    isFreelooking = false;
                    if (client.options != null) {
                        client.options.setPerspective(originalPerspective);
                    }
                }
            }

            // Force lightmap update so Fullbright applies instantly in-game
            if (client.gameRenderer != null && client.gameRenderer.getLightmapTextureManager() != null) {
                ((net.featherpojav.mixin.client.LightmapTextureManagerAccessor) client.gameRenderer.getLightmapTextureManager()).setDirty(true);
            }
        });

        // Register client command /feather for mobile players on PojavLauncher
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("feather")
                .executes(context -> {
                    context.getSource().getClient().execute(() -> {
                        context.getSource().getClient().setScreen(new FeatherSettingsScreen(null));
                    });
                    context.getSource().sendFeedback(Text.of("Opening Hollow Settings..."));
                    return 1;
                })
            );
        });
    }
}
