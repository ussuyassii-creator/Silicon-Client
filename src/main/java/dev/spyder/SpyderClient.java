package dev.spyder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * SpyderClient - entrypoint.
 * Milestone 1: press R in-game to prove the client is loaded and running.
 */
public class SpyderClient implements ClientModInitializer {
	public static final String MOD_ID = "spyderclient";

	// 1.21.9+ requires a KeyMapping.Category object, not a String.
	// Translation key for this category: key.category.spyderclient.main
	public static final KeyMapping.Category SPYDER_CATEGORY =
			new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"));

	private static KeyMapping pingKey;
	private boolean wasPressed = false;

	@Override
	public void onInitializeClient() {
		// Register the R key under our own category
		pingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.spyderclient.ping",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				SPYDER_CATEGORY));

		// Fired once per client tick (20x/sec) - only checks a boolean, so cost is ~zero
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean pressed = pingKey.isDown();
			if (pressed && !wasPressed && client.player != null) {
				client.player.displayClientMessage(
						Component.literal("§a§lSpyderClient §7» §fClient is alive! §7(build 1)"),
						false);
			}
			wasPressed = pressed;
		});
	}
}
