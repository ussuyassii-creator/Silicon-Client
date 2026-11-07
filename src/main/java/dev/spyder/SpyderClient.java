package dev.spyder;

import com.mojang.blaze3d.platform.InputConstants;
import dev.spyder.gui.ClickGuiScreen;
import dev.spyder.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * SpyderClient - entrypoint.
 * R = proof-of-life ping. Right Shift = open ClickGUI.
 */
public class SpyderClient implements ClientModInitializer {
	public static final String MOD_ID = "spyderclient";

	// Newer MC versions renamed ResourceLocation to Identifier.
	// Translation key for this category: key.category.spyderclient.main
	public static final KeyMapping.Category SPYDER_CATEGORY =
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

	private static KeyMapping pingKey;
	private static KeyMapping guiKey;
	private boolean wasPressed = false;
	private boolean wasGuiPressed = false;

	@Override
	public void onInitializeClient() {
		ModuleManager.init();

		pingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.spyderclient.ping",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				SPYDER_CATEGORY));

		guiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.spyderclient.gui",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				SPYDER_CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean pressed = pingKey.isDown();
			if (pressed && !wasPressed && client.player != null) {
				client.player.displayClientMessage(
						Component.literal("§a§lSpyderClient §7» §fClient is alive! §7(build 2)"),
						false);
			}
			wasPressed = pressed;

			boolean guiPressed = guiKey.isDown();
			if (guiPressed && !wasGuiPressed && client.player != null) {
				client.setScreen(new ClickGuiScreen());
			}
			wasGuiPressed = guiPressed;
		});
	}
}
