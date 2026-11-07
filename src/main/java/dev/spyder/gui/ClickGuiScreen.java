package dev.spyder.gui;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings;
import dev.spyder.module.GuiModule;
import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SpyderClient ClickGUI - PRESTIGE STYLE, FULLY HAND-DRAWN.
 * - Left category sidebar: VISUALS / HUD / CLIENT (visual modules replace
 *   the combat categories of the reference client)
 * - Branded header: animated rainbow client name + version, draggable
 * - Module rows with ANIMATED SLIDING TOGGLE SWITCHES (Prestige look)
 * - Inline expandable settings (cycle chips, switches, - value + sliders)
 * - Cosmic starfield background, Right Shift close
 * - Lag-free: statics + reused arrays; switch animation state in one map
 *
 * Built for the 1.21.11 input API (MouseButtonEvent / KeyEvent).
 */
public class ClickGuiScreen extends Screen {
	private static final int SIDEBAR_W = 74;
	private static final int PANEL_W = 260;
	private static final int HEADER_H = 32;
	private static final int MODULE_H = 26;
	private static final int SETTING_H = 22;
	private static final int FOOTER_H = 15;
	private static final int CAT_H = 20;
	private static final int SWITCH_W = 24;
	private static final int SWITCH_H = 13;

	private static final String[] CATS = {"VISUALS", "HUD", "CLIENT"};

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

	private int activeCat = 0;

	private final Set<Module> expanded = new HashSet<>();
	private final Map<String, Float> switchAnim = new HashMap<>();

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

	private static String categoryOf(Module m) {
		String n = m.getName();
		if (n.equals("HUD")) {
			return "HUD";
		}
		if (n.equals("GUI Editor")) {
			return "CLIENT";
		}
		return "VISUALS";
	}

	private static int countInCat(String cat) {
		int c = 0;
		for (Module m : ModuleManager.modules()) {
			if (categoryOf(m).equals(cat)) {
				c++;
			}
		}
		return c;
	}

	private void rebuildRows() {
		rows.clear();
		String cat = CATS[activeCat];
		for (Module m : ModuleManager.modules()) {
			if (!categoryOf(m).equals(cat)) {
				continue;
			}
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
		int top = (int) panelY + HEADER_H + 6;
		int avail = this.height - top - FOOTER_H - 8;
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
		long time = System.currentTimeMillis();
		int px = (int) panelX;
		int py = (int) panelY;
		int x0 = px + SIDEBAR_W; // content left edge
		int x1 = px + PANEL_W;   // content right edge

		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int panelH = HEADER_H + 6 + contentH + FOOTER_H + 6;
		int accent = GuiTheme.accent();

		// Sidebar (darker) + main body
		g.fill(px, py, px + SIDEBAR_W, py + panelH, 0xF507050D);
		g.fill(x0, py, x1, py + panelH, 0xF20B0810);
		// Outer border + accent top edge (Prestige-style clean edge)
		g.fill(px, py, x1, py + 1, accent);
		g.fill(px, py + panelH - 1, x1, py + panelH, 0xFF14141A);
		g.fill(px, py, px + 1, py + panelH, 0xFF14141A);
		g.fill(x1 - 1, py, x1, py + panelH, 0xFF14141A);

		// Header over content area: animated brand + version + close x
		String title = GuiTheme.clientName;
		int tx = x0 + 8;
		for (int i = 0; i < title.length(); i++) {
			String ch = String.valueOf(title.charAt(i));
			g.drawString(this.font, ch, tx, py + 8, hue(time, i * 120));
			tx += this.font.width(ch);
		}
		g.drawString(this.font, " " + GuiTheme.version, tx, py + 8, 0xFF9A9A9A);
		g.drawString(this.font, "x", x1 - 14, py + 9, 0xFFAA6666);
		// Animated hue strip under the header (across content width)
		int stripW = PANEL_W - SIDEBAR_W;
		for (int i = 0; i < stripW - 4; i++) {
			g.fill(x0 + 2 + i, py + HEADER_H - 3, x0 + 3 + i, py + HEADER_H, hue(time, i));
		}

		// Sidebar: categories with counts, active one highlighted
		for (int i = 0; i < CATS.length; i++) {
			int cy = py + HEADER_H + 8 + i * CAT_H;
			boolean active = i == activeCat;
			if (active) {
				g.fill(px, cy, px + SIDEBAR_W, cy + CAT_H - 2, 0x22FFFFFF);
				g.fill(px, cy, px + 2, cy + CAT_H - 2, accent);
			}
			int color = active ? 0xFFFFFFFF : 0xFF8E8E96;
			g.drawString(this.font, CATS[i], px + 7, cy + 6, color);
			String cnt = String.valueOf(countInCat(CATS[i]));
			g.drawString(this.font, cnt, px + SIDEBAR_W - this.font.width(cnt) - 5, cy + 6, active ? accent : 0xFF55555F);
		}

		// Module / setting rows in the content area
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			if (r.isModule) {
				drawModuleRow(g, r.module, x0, x1, y, time);
			} else {
				drawSettingRow(g, r, x0, x1, y, time);
			}
		}

		// Footer: paging arrows + hint
		int footY = py + HEADER_H + 6 + contentH + 3;
		g.drawString(this.font, "tap row = toggle   + = settings", x0 + 6, footY, 0xFF6E6E76);
		if (pageStart > 0) {
			g.drawString(this.font, "<", x1 - 42, footY, accent);
		}
		if (visibleEnd < rows.size()) {
			g.drawString(this.font, ">", x1 - 18, footY, accent);
		}
	}

	private void drawModuleRow(GuiGraphics g, Module m, int x0, int x1, int y, long time) {
		g.fill(x0 + 3, y, x1 - 3, y + MODULE_H - 3, 0x14FFFFFF);
		g.drawString(this.font, m.getName(), x0 + 8, y + 9,
				m.isEnabled() ? 0xFFFFFFFF : 0xFFB0B0B8);
		// Prestige-style sliding toggle switch
		drawSwitch(g, x1 - 40, y + 6, m.isEnabled(), m.getName());
		// Expand indicator
		boolean open = expanded.contains(m);
		g.drawString(this.font, open ? "-" : "+", x1 - 14, y + 9, 0xFFDDDDDD);
	}

	private void drawSettingRow(GuiGraphics g, Row r, int x0, int x1, int y, long time) {
		int indent = x0 + 14;
		SpyderSettings.Setting s = r.setting;
		if (s instanceof SpyderSettings.CycleSetting cyc) {
			g.drawString(this.font, cyc.display(), indent, y + 7, 0xFFB8B8C0);
			String chip = "< " + cyc.value() + " >";
			int chipW = this.font.width(chip);
			int chipX = x1 - 10 - chipW;
			g.fill(chipX - 5, y + 3, chipX + chipW + 5, y + SETTING_H - 5, 0x30FFFFFF);
			g.drawString(this.font, chip, chipX, y + 7, hue(time, y * 16));
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), indent, y + 7, 0xFFB8B8C0);
			drawSwitch(g, x1 - 40, y + 4, tog.value(), tog.name());
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), indent, y + 7, 0xFFB8B8C0);
			String val = String.format("%.2f", rng.value());
			g.drawString(this.font, "-", x1 - 54, y + 7, 0xFFDDDDDD);
			g.drawString(this.font, val, x1 - 46, y + 7, hue(time, y * 16));
			g.drawString(this.font, "+", x1 - 14, y + 7, 0xFFDDDDDD);
		}
	}

	/** Animated sliding switch, Prestige-style. State cached per key. */
	private void drawSwitch(GuiGraphics g, int x, int y, boolean on, String key) {
		float f = switchAnim.getOrDefault(key, on ? 1f : 0f);
		float target = on ? 1f : 0f;
		f += (target - f) * 0.35f;
		if (Math.abs(target - f) < 0.02f) {
			f = target;
		}
		switchAnim.put(key, f);
		int track = f > 0.5f ? GuiTheme.accent() : 0xFF3A2A2E;
		g.fill(x, y, x + SWITCH_W, y + SWITCH_H, track);
		int knob = SWITCH_H - 3;
		int kx = x + 2 + (int) (f * (SWITCH_W - knob - 4));
		g.fill(kx, y + 1, kx + knob, y + SWITCH_H - 1, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		layout();
		int px = (int) panelX;
		int py = (int) panelY;
		int x0 = px + SIDEBAR_W;
		int x1 = px + PANEL_W;

		// Close zone (header far right)
		if (in(mx, my, x1 - 22, py + 3, 20, HEADER_H - 6)) {
			onClose();
			return true;
		}

		// Header drag
		if (in(mx, my, x0, py, PANEL_W - SIDEBAR_W, HEADER_H)) {
			dragging = true;
			dragOX = mx - panelX;
			dragOY = my - panelY;
			return true;
		}

		// Sidebar category selection
		if (in(mx, my, px, py + HEADER_H, SIDEBAR_W, panelHeight())) {
			for (int i = 0; i < CATS.length; i++) {
				int cy = py + HEADER_H + 8 + i * CAT_H;
				if (in(mx, my, px, cy, SIDEBAR_W, CAT_H - 2)) {
					if (activeCat != i) {
						activeCat = i;
						pageStart = 0;
						rebuildRows();
					}
					return true;
				}
			}
		}

		// Rows
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			if (!in(mx, my, x0, y, PANEL_W - SIDEBAR_W, h - 2)) {
				continue;
			}
			if (r.isModule) {
				// Expand zone (right 18px)
				if (in(mx, my, x1 - 18, y, 18, h - 2)) {
					if (expanded.contains(r.module)) {
						expanded.remove(r.module);
					} else {
						expanded.add(r.module);
					}
					rebuildRows();
					return true;
				}
				r.module.toggle();
				if (r.module instanceof GuiModule gm) {
					gm.syncToTheme();
				}
				announce(r.module.getName() + (r.module.isEnabled() ? " ON" : " OFF"));
				return true;
			} else {
				SpyderSettings.Setting s = r.setting;
				if (s instanceof SpyderSettings.CycleSetting cyc) {
					cyc.next();
					syncIfGuiEditor(r.module);
					announce(cyc.display() + ": " + cyc.value());
					return true;
				} else if (s instanceof SpyderSettings.ToggleSetting tog) {
					tog.toggle();
					syncIfGuiEditor(r.module);
					return true;
				} else if (s instanceof SpyderSettings.RangeSetting rng) {
					if (mx < x0 + (PANEL_W - SIDEBAR_W) / 2.0) {
						rng.decrement();
					} else {
						rng.increment();
					}
					syncIfGuiEditor(r.module);
					return true;
				}
			}
		}

		// Footer paging
		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int footY = py + HEADER_H + 6 + contentH + 3;
		if (pageStart > 0 && in(mx, my, x1 - 44, footY - 2, 22, FOOTER_H + 2)) {
			int pageSize = Math.max(1, visibleEnd - pageStart);
			pageStart = Math.max(0, pageStart - pageSize);
			layout();
			return true;
		}
		if (visibleEnd < rows.size() && in(mx, my, x1 - 22, footY - 2, 22, FOOTER_H + 2)) {
			pageStart = visibleEnd;
			layout();
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private int panelHeight() {
		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		return HEADER_H + 6 + contentH + FOOTER_H + 6;
	}

	private void syncIfGuiEditor(Module m) {
		if (m instanceof GuiModule gm) {
			gm.syncToTheme();
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (dragging) {
			panelX = event.x() - dragOX;
			panelY = event.y() - dragOY;
			clampPanel();
			layout();
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragging = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	private void announce(String message) {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.displayClientMessage(
					Component.literal("§a§l" + GuiTheme.clientName + " §7» §f" + message), false);
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
