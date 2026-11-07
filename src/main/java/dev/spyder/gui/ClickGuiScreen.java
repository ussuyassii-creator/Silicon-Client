package dev.spyder.gui;

import dev.spyder.core.GuiTheme;
import dev.spyder.module.GuiModule;
import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * SpyderClient ClickGUI v2.
 * - Animated cosmic background (stars drawn in one pass, zero allocation)
 * - Accent-colored module list with ON/OFF state
 * - Per-module settings screens
 * - Lag-free: all drawing is static math; nothing new'ed per frame.
 */
public class ClickGuiScreen extends Screen {
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;
	private static final int MAX_VISIBLE = 7;

	// Cosmic background stars (pre-allocated once, reused every frame - zero GC)
	private static final int STAR_COUNT = 50;
	private static final float[] starX = new float[STAR_COUNT];
	private static final float[] starY = new float[STAR_COUNT];
	private static final float[] starSpeed = new float[STAR_COUNT];
	private static boolean starsInitialized = false;

	private int scrollIndex = 0;

	public ClickGuiScreen() {
		super(Component.literal("SpyderClient"));
	}

	@Override
	protected void init() {
		super.init();

		if (!starsInitialized) {
			for (int i = 0; i < STAR_COUNT; i++) {
				starX[i] = (float) Math.random();
				starY[i] = (float) Math.random();
				starSpeed[i] = 0.0001f + (float) Math.random() * 0.0003f;
			}
			starsInitialized = true;
		}

		int centerX = this.width / 2 - BUTTON_WIDTH / 2;
		int topY = 50;

		int total = ModuleManager.modules().size();
		int visible = Math.min(MAX_VISIBLE, total);

		for (int i = 0; i < visible; i++) {
			Module module = ModuleManager.modules().get(i + scrollIndex);
			int y = topY + i * SPACING;

			// Toggle button
			addRenderableWidget(Button.builder(labelFor(module), button -> {
				module.toggle();
				if (module instanceof GuiModule guiModule) guiModule.syncToTheme();
				button.setMessage(labelFor(module));
			}).bounds(centerX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

			// Settings button (right of toggle)
			addRenderableWidget(Button.builder(Component.literal("⚙"), button -> {
				if (this.minecraft != null) {
					this.minecraft.setScreen(new ModuleSettingsScreen(module, this));
				}
			}).bounds(centerX + BUTTON_WIDTH + 8, y, 24, BUTTON_HEIGHT).build());
		}

		int bottomY = topY + visible * SPACING + 10;

		// Scroll controls
		if (total > MAX_VISIBLE) {
			addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
				if (scrollIndex > 0) scrollIndex--;
				rebuildWidgets();
			}).bounds(centerX, bottomY, 30, BUTTON_HEIGHT).build());

			addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
				if (scrollIndex < total - MAX_VISIBLE) scrollIndex++;
				rebuildWidgets();
			}).bounds(centerX + 34, bottomY, 30, BUTTON_HEIGHT).build());

			addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
					.bounds(centerX + 72, bottomY, BUTTON_WIDTH - 72, BUTTON_HEIGHT).build());
		} else {
			addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
					.bounds(centerX, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
		}
	}

	private Component labelFor(Module module) {
		return Component.literal(module.getName() + ": " + (module.isEnabled() ? "ON" : "OFF"));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		switch (GuiTheme.background) {
			case COSMIC -> renderCosmic(guiGraphics);
			case DARK -> guiGraphics.fill(0, 0, this.width, this.height, 0xB0000000);
			case BLUR -> guiGraphics.fill(0, 0, this.width, this.height, 0x60000000);
			case NONE -> {
			}
		}

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, GuiTheme.accent());
		int showing = Math.min(MAX_VISIBLE, ModuleManager.modules().size());
		guiGraphics.drawCenteredString(this.font,
				Component.literal("§7" + ModuleManager.modules().size() + " modules"),
				this.width / 2, 32, 0x999999);
		if (showing > 0 && ModuleManager.modules().size() > MAX_VISIBLE) {
			guiGraphics.drawCenteredString(this.font,
					Component.literal("§8" + (scrollIndex + 1) + "/" + ModuleManager.modules().size()),
					this.width / 2, this.height - 12, 0x777777);
		}
	}

	/** Animated cosmic background: deep space fill + drifting stars. Single pass, no allocations. */
	private void renderCosmic(GuiGraphics guiGraphics) {
		guiGraphics.fill(0, 0, this.width, this.height, 0xFF050510);

		// Cheap nebula bands
		int bandAlpha = (int) (25 * GuiTheme.cosmicIntensity);
		int bandColor = (bandAlpha << 24) | 0x1A1A2E;
		guiGraphics.fill(0, 0, this.width, this.height / 4, bandColor);
		guiGraphics.fill(0, this.height / 2, this.width, (this.height * 3) / 4, bandColor);

		if (GuiTheme.showStars) {
			for (int i = 0; i < STAR_COUNT; i++) {
				starY[i] += starSpeed[i];
				if (starY[i] > 1.0f) {
					starY[i] = 0f;
					starX[i] = (float) Math.random();
				}
				int px = (int) (starX[i] * this.width);
				int py = (int) (starY[i] * this.height);
				int brightness = 0x88 + (i % 4) * 0x1F;
				int color = 0xFF000000 | (brightness << 16) | (brightness << 8) | brightness;
				guiGraphics.fill(px, py, px + 1, py + 1, color);
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
