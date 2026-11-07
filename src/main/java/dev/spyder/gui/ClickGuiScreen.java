package dev.spyder.gui;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings;
import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SpyderClient ClickGUI v3 - FULLY HAND-DRAWN.
 * Zero vanilla widgets: no Buttons, no EditBoxes. Every pixel is ours.
 * - Draggable panel with animated accent strip
 * - Module rows with colored ON/OFF pills
 * - Inline expandable settings (cycle/toggle/range) right under each module
 * - Cosmic starfield background with twinkle
 * - Right Shift closes the GUI too
 * - Lag-free: statics + reused arrays, nothing allocated per frame
 */
public class ClickGuiScreen extends Screen {
	private static final int PANEL_W = 240;
	private static final int HEADER_H = 34;
	private static final int MODULE_H = 26;
	private static final int SETTING_H = 22;
	private static final int FOOTER_H = 16;

	// Starfield (allocated once, reused forever)
	private static final int STAR_COUNT = 70;
	private static final float[] starX = new float[STAR_COUNT];
	private static final float[] starY = new float[STAR_COUNT];
	private static final float[] starSp = new float[STAR_COUNT];
	private static final float[] starTw = new float[STAR_COUNT];
	private static boolean starsInit = false;

	private double panelX = 24;
	private double panelY = 24;
	private boolean dragging;
	private double dragOX;
	private double dragOY;

	private final Set<Module> expanded = new HashSet<>();

	private static final class Row {
		final Module module;
		final SpyderSettings.Setting setting;
		final boolean isModule;

		Row(Module m) {
			module = m;
			setting = null;
			isModule = true;
		}

		Row(Module m, SpyderSettings.Setting s) {
			module = m;
			setting = s;
			isModule = false;
		}
	}

	private final List<Row> rows = new ArrayList<>();
	private int[] rowYs = new int[0];
	private int pageStart = 0;
	private int visibleEnd = 0;

	public ClickGuiScreen() {
		super(Component.literal("SpyderClient"));
	}

	@Override
	protected void init() {
		if (!starsInit) {
			for (int i = 0; i < STAR_COUNT; i++) {
				starX[i] = (float) Math.random();
				starY[i] = (float) Math.random();
				starSp[i] = 0.00008f + (float) Math.random() * 0.00025f;
				starTw[i] = (float) Math.random() * 6.2831855f;
			}
			starsInit = true;
		}
		clampPanel();
		rebuildRows();
	}

	private void rebuildRows() {
		rows.clear();
		for (Module m : ModuleManager.modules()) {
			rows.add(new Row(m));
			if (expanded.contains(m)) {
				for (SpyderSettings.Setting s : m.settings()) {
					rows.add(new Row(m, s));
				}
			}
		}
		rowYs = new int[rows.size()];
		pageStart = Math.min(pageStart, Math.max(0, rows.size() - 1));
		layout();
	}

	private void layout() {
		int top = (int) panelY + HEADER_H + 4;
		int avail = this.height - top - FOOTER_H - 6;
		if (avail < MODULE_H) {
			avail = MODULE_H;
		}
		int y = top;
		int i = pageStart;
		while (i < rows.size()) {
			int h = rows.get(i).isModule ? MODULE_H : SETTING_H;
			if (i > pageStart && y + h > top + avail) {
				break;
			}
			rowYs[i] = y;
			y += h;
			i++;
		}
		visibleEnd = i;
	}

	private void clampPanel() {
		panelX = Math.max(0, Math.min(panelX, Math.max(0, this.width - PANEL_W)));
		panelY = Math.max(0, Math.min(panelY, Math.max(0, this.height - HEADER_H - 60)));
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		switch (GuiTheme.background) {
			case COSMIC -> drawCosmic(g);
			case DARK -> g.fill(0, 0, this.width, this.height, 0xB0000000);
			case BLUR -> g.fill(0, 0, this.width, this.height, 0x60000000);
			case NONE -> {
			}
		}

		layout();
		long time = Util.getMillis();
		int px = (int) panelX;
		int py = (int) panelY;

		// Panel height from visible rows
		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int panelH = HEADER_H + 4 + contentH + FOOTER_H + 4;
		int accent = GuiTheme.accent();

		// Panel body + animated accent border
		g.fill(px, py, px + PANEL_W, py + panelH, 0xF20B0810);
		g.fill(px, py, px + PANEL_W, py + 1, accent);
		g.fill(px, py + panelH - 1, px + PANEL_W, py + panelH, accent);
		g.fill(px, py, px + 1, py + panelH, accent);
		g.fill(px + PANEL_W - 1, py, px + PANEL_W, py + panelH, accent);

		// Header: title + close + animated strip
		g.drawString(this.font, "SPYDER", px + 10, py + 7, accent);
		g.drawString(this.font, "CLIENT", px + 10 + this.font.width("SPYDER "), py + 7, 0xFFFFFFFF);
		g.drawString(this.font, "x", px + PANEL_W - 16, py + 8, 0xFFAA6666);
		for (int i = 0; i < PANEL_W - 4; i++) {
			g.fill(px + 2 + i, py + HEADER_H - 4, px + 3 + i, py + HEADER_H - 1, hue(time, i));
		}

		// Rows
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			if (r.isModule) {
				drawModuleRow(g, r.module, px, y, time);
			} else {
				drawSettingRow(g, r, px, y, time);
			}
		}

		// Footer: paging arrows + hint
		int footY = py + HEADER_H + 4 + contentH + 2;
		g.drawString(this.font, "tap row = toggle   +/- = settings", px + 8, footY, 0xFF777777);
		if (pageStart > 0) {
			g.drawString(this.font, "<", px + PANEL_W - 40, footY, accent);
		}
		if (visibleEnd < rows.size()) {
			g.drawString(this.font, ">", px + PANEL_W - 16, footY, accent);
		}
	}

	private void drawModuleRow(GuiGraphics g, Module m, int px, int y, long time) {
		// Row background
		g.fill(px + 4, y, px + PANEL_W - 4, y + MODULE_H - 3, 0x16FFFFFF);
		// Accent edge (green when ON, dark when OFF)
		g.fill(px + 4, y, px + 6, y + MODULE_H - 3, m.isEnabled() ? GuiTheme.accent() : 0xFF3A3A3A);
		// Name
		g.drawString(this.font, m.getName(), px + 12, y + 9, m.isEnabled() ? 0xFFFFFFFF : 0xFFB0B0B0);
		// State pill
		String st = m.isEnabled() ? "ON" : "OFF";
		int pillW = this.font.width(st) + 8;
		int pillX = px + PANEL_W - 22 - pillW;
		g.fill(pillX, y + 5, pillX + pillW, y + MODULE_H - 8, m.isEnabled() ? GuiTheme.accent() : 0xFF7A2A2A);
		g.drawString(this.font, st, pillX + 4, y + 9, 0xFF0B0810);
		// Expand indicator
		boolean open = expanded.contains(m);
		g.drawString(this.font, open ? "-" : "+", px + PANEL_W - 14, y + 9, 0xFFDDDDDD);
	}

	private void drawSettingRow(GuiGraphics g, Row r, int px, int y, long time) {
		int indent = px + 16;
		SpyderSettings.Setting s = r.setting;
		if (s instanceof SpyderSettings.CycleSetting cyc) {
			g.drawString(this.font, cyc.display(), indent, y + 7, 0xFFB8B8B8);
			String chip = "< " + cyc.value() + " >";
			int chipW = this.font.width(chip);
			int chipX = px + PANEL_W - 14 - chipW;
			g.fill(chipX - 4, y + 3, chipX + chipW + 4, y + SETTING_H - 5, 0x30FFFFFF);
			g.drawString(this.font, chip, chipX, y + 7, hue(time, y * 16));
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), indent, y + 7, 0xFFB8B8B8);
			String st = tog.value() ? "ON" : "OFF";
			int pillW = this.font.width(st) + 8;
			int pillX = px + PANEL_W - 22 - pillW;
			g.fill(pillX, y + 3, pillX + pillW, y + SETTING_H - 6, tog.value() ? GuiTheme.accent() : 0xFF7A2A2A);
			g.drawString(this.font, st, pillX + 4, y + 7, 0xFF0B0810);
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), indent, y + 7, 0xFFB8B8B8);
			String val = String.format("%.2f", rng.value());
			g.drawString(this.font, "-", px + PANEL_W - 56, y + 7, 0xFFDDDDDD);
			g.drawString(this.font, val, px + PANEL_W - 48, y + 7, hue(time, y * 16));
			g.drawString(this.font, "+", px + PANEL_W - 16, y + 7, 0xFFDDDDDD);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		layout();
		int px = (int) panelX;
		int py = (int) panelY;

		// Close zone
		if (in(mx, my, px + PANEL_W - 24, py + 3, 22, HEADER_H - 6)) {
			onClose();
			return true;
		}

		// Header drag
		if (in(mx, my, px, py, PANEL_W, HEADER_H)) {
			dragging = true;
			dragOX = mx - panelX;
			dragOY = my - panelY;
			return true;
		}

		// Rows
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			if (!in(mx, my, px, y, PANEL_W, h - 2)) {
				continue;
			}
			if (r.isModule) {
				// Expand zone on the far right
				if (in(mx, my, px + PANEL_W - 18, y, 18, h - 2)) {
					if (expanded.contains(r.module)) {
						expanded.remove(r.module);
					} else {
						expanded.add(r.module);
					}
					rebuildRows();
					return true;
				}
				r.module.toggle();
				announce(r.module.getName() + (r.module.isEnabled() ? " ON" : " OFF"));
				return true;
			} else {
				SpyderSettings.Setting s = r.setting;
				if (s instanceof SpyderSettings.CycleSetting cyc) {
					cyc.next();
					announce(cyc.display() + ": " + cyc.value());
					return true;
				} else if (s instanceof SpyderSettings.ToggleSetting tog) {
					tog.toggle();
					return true;
				} else if (s instanceof SpyderSettings.RangeSetting rng) {
					if (mx < px + PANEL_W / 2.0) {
						rng.decrement();
					} else {
						rng.increment();
					}
					return true;
				}
			}
		}

		// Footer paging
		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int footY = py + HEADER_H + 4 + contentH + 2;
		if (pageStart > 0 && in(mx, my, px + PANEL_W - 44, footY - 2, 22, FOOTER_H + 2)) {
			int pageSize = Math.max(1, visibleEnd - pageStart);
			pageStart = Math.max(0, pageStart - pageSize);
			layout();
			return true;
		}
		if (visibleEnd < rows.size() && in(mx, my, px + PANEL_W - 22, footY - 2, 22, FOOTER_H + 2)) {
			pageStart = visibleEnd;
			layout();
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging) {
			panelX = mx - dragOX;
			panelY = my - dragOY;
			clampPanel();
			layout();
			return true;
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		dragging = false;
		return super.mouseReleased(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void announce(String message) {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.displayClientMessage(
					Component.literal("§a§lSpyderClient §7» §f" + message), false);
		}
	}

	private boolean in(double mx, double my, double x, double y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private void drawCosmic(GuiGraphics g) {
		g.fill(0, 0, this.width, this.height, 0xFF050510);
		int bandAlpha = (int) (25 * GuiTheme.cosmicIntensity);
		int bandColor = (bandAlpha << 24) | 0x1A1A2E;
		g.fill(0, 0, this.width, this.height / 4, bandColor);
		g.fill(0, this.height / 2, this.width, (this.height * 3) / 4, bandColor);

		if (GuiTheme.showStars) {
			for (int i = 0; i < STAR_COUNT; i++) {
				starY[i] += starSp[i];
				starTw[i] += 0.05f;
				if (starY[i] > 1.0f) {
					starY[i] = 0f;
					starX[i] = (float) Math.random();
				}
				int px = (int) (starX[i] * this.width);
				int py = (int) (starY[i] * this.height);
				float twinkle = 0.6f + 0.4f * (float) Math.sin(starTw[i]);
				int b = (int) (0x99 * twinkle) + 0x33;
				int color = 0xFF000000 | (b << 16) | (b << 8) | b;
				if (i % 7 == 0) {
					g.fill(px, py, px + 2, py + 2, color);
				} else {
					g.fill(px, py, px + 1, py + 1, color);
				}
			}
		}
	}

	/** Animated hue-shifted color. Pure math, no allocation. */
	private int hue(long time, int offset) {
		float h = (((time + offset * 6L) % 4000L) / 4000.0F) * 6.0F;
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
