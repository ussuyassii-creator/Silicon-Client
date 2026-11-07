package dev.spyder.gui;

import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * SpyderClient ClickGUI: one toggle button per registered module.
 * Opens with the GUI keybind while in-game.
 */
public class ClickGuiScreen extends Screen {
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_SPACING = 24;

	public ClickGuiScreen() {
		super(Component.literal("SpyderClient"));
	}

	@Override
	protected void init() {
		super.init();

		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = 70;

		for (Module module : ModuleManager.modules()) {
			addRenderableWidget(Button.builder(labelFor(module), button -> {
				module.toggle();
				button.setMessage(labelFor(module));
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(
							Component.literal("§a§lSpyderClient §7» §f" + module.getName()
									+ (module.isEnabled() ? " §aON" : " §cOFF")),
							false);
				}
			}).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
			y += BUTTON_SPACING;
		}

		y += 12;
		addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
	}

	private Component labelFor(Module module) {
		return Component.literal(module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF"));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font,
				Component.literal("§7" + ModuleManager.modules().size() + " modules"),
				this.width / 2, 52, 0xAAAAAA);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
