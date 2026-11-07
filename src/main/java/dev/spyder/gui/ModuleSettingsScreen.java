package dev.spyder.gui;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings;
import dev.spyder.module.GuiModule;
import dev.spyder.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Per-module settings screen, auto-generated from the module's declared
 * settings. Hand-drawn rows only - zero vanilla widgets, matching the
 * ClickGUI v5 style. Only proven 1.21.11 API calls.
 */
public class ModuleSettingsScreen extends Screen {
	private static final int PANEL_W = 260;
	private static final int ROW_H = 22;

	private final Module module;

	public ModuleSettingsScreen(Module module, Screen parent) {
		super(Component.literal(module.getName()));
		this.module = module;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		g.fill(0, 0, this.width, this.height, 0x90000000);
		long time = System.currentTimeMillis();
		int rows = 2 + module.settings().size();
		int panelH = 40 + rows * ROW_H + 16;
		int px = (this.width - PANEL_W) / 2;
		int py = (this.height - panelH) / 2;
		int accent = GuiTheme.accent();

		rrect(g, px, py, PANEL_W, panelH, 0xFF12121B);
		g.fill(px + 4, py, px + PANEL_W - 4, py + 2, accent);
		g.fill(px + 2, py + 1, px + 4, py + 2, accent);
		g.fill(px + PANEL_W - 4, py + 1, px + PANEL_W - 2, py + 2, accent);

		// Animated title
		String title = module.getName().toUpperCase();
		int tx = px + 10;
		for (int i = 0; i < title.length(); i++) {
			String ch = String.valueOf(title.charAt(i));
			g.drawString(this.font, ch, tx, py + 10, hue(time, i * 120));
			tx += this.font.width(ch);
		}
		g.drawString(this.font, "x", px + PANEL_W - 14, py + 10, 0xFFAA6666);

		int rx = px + 12;
		int rw = PANEL_W - 24;
		int row = 0;

		// Module on/off row
		int y = py + 34 + row * ROW_H;
		g.drawString(this.font, "Module", rx, y + 7, 0xFFB0B0BC);
		g.drawString(this.font, module.isEnabled() ? "ON" : "OFF", rx + rw - 20, y + 7,
				module.isEnabled() ? GuiTheme.accent() : 0xFF7A2A2A);
		row++;

		for (SpyderSettings.Setting s : module.settings()) {
			y = py + 34 + row * ROW_H;
			if (s instanceof SpyderSettings.CycleSetting cyc) {
				g.drawString(this.font, cyc.display(), rx, y + 7, 0xFFB0B0BC);
				String chip = "< " + cyc.value() + " >";
				int chipW = this.font.width(chip);
				g.drawString(this.font, chip, rx + rw - chipW, y + 7, hue(time, y * 16));
			} else if (s instanceof SpyderSettings.ToggleSetting tog) {
				g.drawString(this.font, tog.display(), rx, y + 7, 0xFFB0B0BC);
				g.drawString(this.font, tog.value() ? "ON" : "OFF", rx + rw - 20, y + 7,
						tog.value() ? GuiTheme.accent() : 0xFF7A2A2A);
			} else if (s instanceof SpyderSettings.RangeSetting rng) {
				g.drawString(this.font, rng.display(), rx, y + 7, 0xFFB0B0BC);
				String val = String.format("%.1f", rng.value());
				g.drawString(this.font, "-", rx + rw - 46, y + 7, 0xFFDDDDDD);
				g.drawString(this.font, val, rx + rw - 38, y + 7, hue(time, y * 16));
				g.drawString(this.font, "+", rx + rw - 14, y + 7, 0xFFDDDDDD);
			}
			row++;
		}

		// Back row
		y = py + 34 + row * ROW_H;
		g.drawString(this.font, "< back", rx, y + 7, 0xFFB0B0BC);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		int rows = 2 + module.settings().size();
		int panelH = 40 + rows * ROW_H + 16;
		int px = (this.width - PANEL_W) / 2;
		int py = (this.height - panelH) / 2;

		if (in(mx, my, px + PANEL_W - 22, py + 4, 20, 18)) {
			onClose();
			return true;
		}

		int rx = px + 12;
		int rw = PANEL_W - 24;
		int row = 0;
		// Module toggle row
		if (in(mx, my, rx, py + 34 + row * ROW_H, rw, ROW_H - 2)) {
			module.toggle();
			if (module instanceof GuiModule gm) {
				gm.syncToTheme();
			}
			return true;
		}
		row++;

		for (SpyderSettings.Setting s : module.settings()) {
			if (in(mx, my, rx, py + 34 + row * ROW_H, rw, ROW_H - 2)) {
				if (s instanceof SpyderSettings.CycleSetting cyc) {
					cyc.next();
				} else if (s instanceof SpyderSettings.ToggleSetting tog) {
					tog.toggle();
				} else if (s instanceof SpyderSettings.RangeSetting rng) {
					if (mx < rx + rw / 2.0) {
						rng.decrement();
					} else {
						rng.increment();
					}
				}
				if (module instanceof GuiModule gm) {
					gm.syncToTheme();
				}
				return true;
			}
			row++;
		}

		// Back row
		if (in(mx, my, rx, py + 34 + row * ROW_H, rw, ROW_H - 2)) {
			onClose();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	private boolean in(double mx, double my, double x, double y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private void rrect(GuiGraphics g, int x, int y, int w, int h, int color) {
		if (w < 8 || h < 8) {
			g.fill(x, y, x + w, y + h, color);
			return;
		}
		g.fill(x + 4, y, x + w - 4, y + h, color);
		g.fill(x, y + 4, x + w, y + h - 4, color);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
		g.fill(x + 2, y + 1, x + w - 2, y + h - 1, color);
		g.fill(x + 1, y + 2, x + w - 1, y + h - 2, color);
		g.fill(x + 3, y + 1, x + w - 3, y + h - 1, color);
	}

	private int hue(long time, int offset) {
		float h = (((time + offset) % 4000L) / 4000.0F) * 6.0F;
		int seg = (int) h;
		float f = h - seg;
		float p = 0.2F;
		float q = 1.0F - 0.8F * f;
		float t = 0.2F + 0.8F * f;
		float r;
		float gr;
		float b;
		switch (seg % 6) {
			case 0 -> { r = 1.0F; gr = t; b = p; }
			case 1 -> { r = q; gr = 1.0F; b = p; }
			case 2 -> { r = p; gr = 1.0F; b = t; }
			case 3 -> { r = p; gr = q; b = 1.0F; }
			case 4 -> { r = t; gr = p; b = 1.0F; }
			default -> { r = 1.0F; gr = p; b = q; }
		}
		int ri = (int) (r * 255.0F);
		int gi = (int) (gr * 255.0F);
		int bi = (int) (b * 255.0F);
		return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
