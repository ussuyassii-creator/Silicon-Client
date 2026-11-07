package dev.spyder.gui;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings;
import dev.spyder.module.GuiModule;
import dev.spyder.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Per-module settings screen, auto-generated from the module's declared
 * settings. Every cycle/toggle/range setting gets a control row.
 */
public class ModuleSettingsScreen extends Screen {
	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Module module;
	private final Screen parent;

	public ModuleSettingsScreen(Module module, Screen parent) {
		super(Component.literal(module.getName()));
		this.module = module;
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2 - BUTTON_WIDTH / 2;
		int y = 55;

		// Toggle the module on/off from here too
		addRenderableWidget(Button.builder(labelFor(module), button -> {
			module.toggle();
			if (module instanceof GuiModule guiModule) guiModule.syncToTheme();
			button.setMessage(labelFor(module));
		}).bounds(centerX, 30, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		for (SpyderSettings.Setting setting : module.settings()) {
			if (setting instanceof SpyderSettings.CycleSetting cycle) {
				addRenderableWidget(Button.builder(
						Component.literal(cycle.display() + ": " + cycle.value()),
						button -> {
							cycle.next();
							if (module instanceof GuiModule guiModule) guiModule.syncToTheme();
							button.setMessage(Component.literal(cycle.display() + ": " + cycle.value()));
						}).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
				y += SPACING;
			} else if (setting instanceof SpyderSettings.ToggleSetting toggle) {
				addRenderableWidget(Button.builder(
						Component.literal(toggle.display() + ": " + (toggle.value() ? "ON" : "OFF")),
						button -> {
							toggle.toggle();
							if (module instanceof GuiModule guiModule) guiModule.syncToTheme();
							button.setMessage(Component.literal(toggle.display() + ": " + (toggle.value() ? "ON" : "OFF")));
						}).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
				y += SPACING;
			} else if (setting instanceof SpyderSettings.RangeSetting range) {
				// Minus button on the left
				addRenderableWidget(Button.builder(Component.literal("-"), button -> {
					range.decrement();
					if (module instanceof GuiModule guiModule) guiModule.syncToTheme();
					button.setMessage(Component.literal(range.display() + ": " + String.format("%.1f", range.value())));
				}).bounds(centerX - 30, y, 24, BUTTON_HEIGHT).build());

				// Main button shows the value and increments
				addRenderableWidget(Button.builder(
						Component.literal(range.display() + ": " + String.format("%.1f", range.value())),
						button -> {
							range.increment();
							if (module instanceof GuiModule guiModule) guiModule.syncToTheme();
							button.setMessage(Component.literal(range.display() + ": " + String.format("%.1f", range.value())));
						}).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
				y += SPACING;
			}
		}

		addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
			if (this.minecraft != null) this.minecraft.setScreen(this.parent);
		}).bounds(centerX, y + 8, BUTTON_WIDTH, BUTTON_HEIGHT).build());
	}

	private Component labelFor(Module module) {
		return Component.literal(module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF"));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, GuiTheme.accent());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
