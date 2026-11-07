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
 * ClickGUI v6 "GLASS" - a real OS-style window:
 * - Draggable by the header (window title bar)
 * - RESIZABLE by dragging the corner grip (bottom-right)
 * - Glass transparency controlled live from GUI Editor > Glass opacity
 * - Window chrome: title bar, accent edge, close button, resize grip
 * - Rounded panels, glow switches, hover highlights, slide-in settings,
 *   nebula + star background - the full premium set
 * - Cosmetics module opens the 3D player-preview editor
 *
 * Only proven API: fill + int drawString + 1.21.11 input events.
 */
public class ClickGuiScreen extends Screen {
	private static final int MIN_W = 240;
	private static final int MIN_H = 180;
	private static final int SIDEBAR_W = 86;
	private static final int HEADER_H = 42;
	private static final int MODULE_H = 30;
	private static final int SETTING_H = 24;
	private static final int FOOTER_H = 18;
	private static final int CAT_H = 26;
	private static final int SWITCH_W = 32;
	private static final int SWITCH_H = 16;
	private static final int GRIP_SIZE = 12;

	private static final String[] CATS = {"VISUALS", "HUD", "CLIENT"};

	// Starfield (allocated once, reused forever)
	private static final int STAR_COUNT = 110;
	private static final float[] starX = new float[STAR_COUNT];
	private static final float[] starY = new float[STAR_COUNT];
	private static final float[] starSp = new float[STAR_COUNT];
	private static final float[] starTw = new float[STAR_COUNT];
	private static boolean starsInit = false;

	// Window state
	private int panelX = 40;
	private int panelY = 32;
	private int panelW = 292;
	private int panelH = 320;
	private boolean dragging;
	private int dragOX;
	private int dragOY;
	private boolean resizing;
	private int resizeOX;
	private int resizeOY;

	private int activeCat = 0;

	private final Set<Module> expanded = new HashSet<>();
	private final Map<String, Float> switchAnim = new HashMap<>();
	private final Map<String, Float> expandAnim = new HashMap<>();
	private final float[] catAnim = new float[CATS.length];

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
				starSp[i] = 0.00006f + (float) Math.random() * 0.00020f;
				starTw[i] = (float) Math.random() * 6.2831855f;
			}
			starsInit = true;
		}
		clampWindow();
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
		int top = panelY + HEADER_H + 10;
		int avail = panelH - HEADER_H - FOOTER_H - 26;
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

	private void clampWindow() {
		panelW = Math.max(MIN_W, Math.min(panelW, this.width));
		panelH = Math.max(MIN_H, Math.min(panelH, this.height));
		panelX = Math.max(0, Math.min(panelX, this.width - panelW));
		panelY = Math.max(0, Math.min(panelY, this.height - HEADER_H - 30));
	}

	/** Rounded rectangle (radius ~4) via layered fills. */
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

	/** Soft outer glow: 3 fading rings around a rect. */
	private void glow(GuiGraphics g, int x, int y, int w, int h, int rgb, float strength) {
		int a1 = (int) (0x30 * strength);
		int a2 = (int) (0x18 * strength);
		int a3 = (int) (0x0C * strength);
		g.fill(x - 6, y - 6, x + w + 6, y + h + 6, (a3 << 24) | rgb);
		g.fill(x - 4, y - 4, x + w + 4, y + h + 4, (a2 << 24) | rgb);
		g.fill(x - 2, y - 2, x + w + 2, y + h + 2, (a1 << 24) | rgb);
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
		int px = panelX;
		int py = panelY;
		int x1 = px + panelW;
		int accent = GuiTheme.accent();
		int x0 = px + SIDEBAR_W;

		// --- Window chrome: shadow + GLASS body + accent edge ---
		rrect(g, px + 5, py + 6, panelW, panelH, 0x66000000);
		rrect(g, px, py, panelW, panelH, GuiTheme.glass(0xFF161620));
		rrect(g, px + 1, py + 1, panelW - 2, panelH - 2, GuiTheme.glass(0xF50C0A13));
		g.fill(px + 4, py, x1 - 4, py + 2, accent);
		g.fill(px + 2, py + 1, px + 4, py + 2, accent);
		g.fill(px + 1, py + 2, px + 2, py + 2, accent);
		g.fill(x1 - 4, py + 1, x1 - 2, py + 2, accent);
		g.fill(x1 - 2, py + 2, x1 - 1, py + 2, accent);
		// Glass shine: a faint diagonal light band across the top-left of the window
		for (int i = 0; i < panelW / 3 && i < 40; i++) {
			int sy = py + 3 + i;
			if (sy > py + panelH / 3) break;
			g.fill(px + 3 + i * 2, sy, px + 4 + i * 2, sy + 1, 0x10FFFFFF);
		}
		// Divider between sidebar and content
		g.fill(x0, py + 2, x0 + 1, py + panelH - 2, 0x38FFFFFF);

		// --- Title bar: bold animated title + version + close ---
		String title = GuiTheme.clientName;
		int tx = x0 + 10;
		for (int i = 0; i < title.length(); i++) {
			String ch = String.valueOf(title.charAt(i));
			int c = hue(time, i * 120);
			g.drawString(this.font, ch, tx + 1, py + 12, 0x66000000);
			g.drawString(this.font, ch, tx, py + 11, c);
			g.drawString(this.font, ch, tx, py + 12, c);
			tx += this.font.width(ch);
		}
		g.drawString(this.font, "v" + GuiTheme.version, tx + 5, py + 14, 0xFF8A8A96);
		boolean closeHover = in(mouseX, mouseY, x1 - 26, py + 8, 20, HEADER_H - 16);
		rrect(g, x1 - 26, py + 8, 20, HEADER_H - 16, closeHover ? 0xFF441B1B : GuiTheme.glass(0xFF1E1620));
		g.drawString(this.font, "x", x1 - 19, py + 14, closeHover ? 0xFFFF7777 : 0xFFAA6666);

		// --- Gradient-lit strip under the header ---
		int stripW = panelW - SIDEBAR_W;
		for (int i = 0; i < stripW - 12; i++) {
			g.fill(x0 + 6 + i, py + HEADER_H - 5, x0 + 7 + i, py + HEADER_H - 2, hue(time, i));
		}

		// --- Sidebar: glyph icons + labels + counts, active glow ---
		for (int i = 0; i < CATS.length; i++) {
			int cy = py + HEADER_H + 12 + i * CAT_H;
			boolean active = i == activeCat;
			float target = active ? 1f : 0f;
			catAnim[i] += (target - catAnim[i]) * 0.25f;
			if (Math.abs(target - catAnim[i]) < 0.02f) {
				catAnim[i] = target;
			}
			if (catAnim[i] > 0.03f) {
				glow(g, px + 6, cy - 1, SIDEBAR_W - 12, CAT_H - 2,
						(GuiTheme.accentR << 16) | (GuiTheme.accentG << 8) | GuiTheme.accentB,
						catAnim[i] * 0.8f);
			}
			rrect(g, px + 6, cy - 1, SIDEBAR_W - 12, CAT_H - 2,
					active ? 0x2EFFFFFF : 0x10FFFFFF);
			if (active) {
				g.fill(px + 7, cy + 3, px + 9, cy + CAT_H - 6, accent);
			}
			int gx = px + 14;
			int gy = cy + 8;
			int gc = active ? accent : 0xFF6E6E7A;
			if (i == 0) { // VISUALS: eye-ish diamond
				g.fill(gx + 2, gy, gx + 4, gy + 1, gc);
				g.fill(gx, gy + 1, gx + 2, gy + 2, gc);
				g.fill(gx + 4, gy + 1, gx + 6, gy + 2, gc);
				g.fill(gx + 2, gy + 2, gx + 4, gy + 3, gc);
				g.fill(gx, gy + 3, gx + 2, gy + 4, gc);
				g.fill(gx + 4, gy + 3, gx + 6, gy + 4, gc);
				g.fill(gx + 2, gy + 4, gx + 4, gy + 5, gc);
			} else if (i == 1) { // HUD: screen outline
				g.fill(gx, gy, gx + 6, gy + 1, gc);
				g.fill(gx, gy + 4, gx + 6, gy + 5, gc);
				g.fill(gx, gy, gx + 1, gy + 5, gc);
				g.fill(gx + 5, gy, gx + 6, gy + 5, gc);
				g.fill(gx + 2, gy + 2, gx + 4, gy + 3, gc);
			} else { // CLIENT: gear-ish block
				g.fill(gx + 1, gy, gx + 4, gy + 1, gc);
				g.fill(gx, gy + 1, gx + 5, gy + 4, gc);
				g.fill(gx + 1, gy + 4, gx + 4, gy + 5, gc);
				g.fill(gx + 2, gy + 2, gx + 3, gy + 3, 0xFF0C0A13);
			}
			g.drawString(this.font, CATS[i], px + 26, cy + 8,
					active ? 0xFFFFFFFF : 0xFF8E8E96);
			String cnt = String.valueOf(countInCat(CATS[i]));
			g.drawString(this.font, cnt, px + SIDEBAR_W - this.font.width(cnt) - 8, cy + 8,
					active ? accent : 0xFF55555F);
		}

		// --- Rows: hover highlight + accent bar ---
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			boolean hovered = in(mouseX, mouseY, x0 + 3, y, panelW - SIDEBAR_W - 6, h - 2);
			if (hovered) {
				rrect(g, x0 + 5, y - 1, panelW - SIDEBAR_W - 10, h, 0x1EFFFFFF);
				g.fill(x0 + 6, y + 3, x0 + 8, y + h - 4, accent);
			}
			if (r.isModule) {
				drawModuleRow(g, r.module, x0, x1, y, time, hovered);
			} else {
				drawSettingRow(g, r, x0, x1, y, time);
			}
		}

		// --- Footer: hint + paging ---
		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int footY = py + panelH - FOOTER_H - 8;
		g.drawString(this.font, "tap row = toggle   + = settings", x0 + 8, footY, 0xFF6E6E76);
		if (pageStart > 0) {
			boolean hovL = in(mouseX, mouseY, x1 - 48, footY - 2, 24, FOOTER_H + 2);
			g.drawString(this.font, "<", x1 - 44, footY, hovL ? 0xFFFFFFFF : accent);
		}
		if (visibleEnd < rows.size()) {
			boolean hovR = in(mouseX, mouseY, x1 - 26, footY - 2, 24, FOOTER_H + 2);
			g.drawString(this.font, ">", x1 - 22, footY, hovR ? 0xFFFFFFFF : accent);
		}

		// --- Resize grip (bottom-right corner, OS-style diagonal lines) ---
		int gpx = x1 - GRIP_SIZE - 2;
		int gpy = py + panelH - GRIP_SIZE - 2;
		boolean gripHover = in(mouseX, mouseY, gpx - 4, gpy - 4, GRIP_SIZE + 8, GRIP_SIZE + 8);
		int gc = gripHover || resizing ? accent : 0xFF5A5A66;
		for (int i = 2; i < GRIP_SIZE - 1; i += 3) {
			g.fill(gpx + i, gpy + GRIP_SIZE - 2, gpx + i + 1, gpy + GRIP_SIZE - 1, gc);
			g.fill(gpx + i + 3, gpy + GRIP_SIZE - 5, gpx + i + 4, gpy + GRIP_SIZE - 4, gc);
		}
	}

	private void drawModuleRow(GuiGraphics g, Module m, int x0, int x1, int y, long time, boolean hovered) {
		boolean hasEditor = m.getName().equals("Cosmetics");
		g.drawString(this.font, m.getName(), x0 + 12, y + 11,
				m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAB4);
		drawSwitch(g, x1 - 48, y + 7, m.isEnabled(), m.getName());
		if (hasEditor) {
			g.drawString(this.font, "3D", x1 - 17, y + 11, hovered ? 0xFFFFFFFF : GuiTheme.accent());
		} else {
			boolean open = expanded.contains(m);
			g.drawString(this.font, open ? "-" : "+", x1 - 17, y + 11,
					hovered ? 0xFFFFFFFF : 0xFFC8C8D0);
		}
	}

	private void drawSettingRow(GuiGraphics g, Row r, int x0, int x1, int y, long time) {
		String key = r.module.getName();
		float target = expanded.contains(r.module) ? 1f : 0f;
		float a = expandAnim.getOrDefault(key, target);
		a += (target - a) * 0.30f;
		if (Math.abs(target - a) < 0.02f) {
			a = target;
		}
		expandAnim.put(key, a);
		int slide = (int) ((1.0f - a) * 14.0f);

		int indent = x0 + 16 + slide;
		SpyderSettings.Setting s = r.setting;
		if (s instanceof SpyderSettings.CycleSetting cyc) {
			g.drawString(this.font, cyc.display(), indent, y + 8, 0xFFB0B0BC);
			String chip = "< " + cyc.value() + " >";
			int chipW = this.font.width(chip);
			int chipX = x1 - 14 - chipW;
			rrect(g, chipX - 8, y + 3, chipW + 16, SETTING_H - 6, 0x26FFFFFF);
			g.drawString(this.font, "<", chipX, y + 8, 0xFF8E8E9A);
			g.drawString(this.font, cyc.value(), chipX + this.font.width("< "), y + 8, hue(time, y * 16));
			g.drawString(this.font, ">", chipX + chipW - this.font.width(">"), y + 8, 0xFF8E8E9A);
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), indent, y + 8, 0xFFB0B0BC);
			drawSwitch(g, x1 - 48, y + 4, tog.value(), key + "." + tog.name());
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), indent, y + 8, 0xFFB0B0BC);
			String val = String.format("%.2f", rng.value());
			rrect(g, x1 - 66, y + 3, 14, SETTING_H - 10, 0x26FFFFFF);
			g.drawString(this.font, "-", x1 - 61, y + 8, 0xFFD8D8E0);
			int valX = x1 - 50;
			g.drawString(this.font, val, valX, y + 8, hue(time, y * 16));
			int plusX = valX + this.font.width(val) + 8;
			rrect(g, plusX - 3, y + 3, 14, SETTING_H - 10, 0x26FFFFFF);
			g.drawString(this.font, "+", plusX + 2, y + 8, 0xFFD8D8E0);
		}
	}

	/** Glowing animated sliding switch - premium look, zero allocations. */
	private void drawSwitch(GuiGraphics g, int x, int y, boolean on, String key) {
		float f = switchAnim.getOrDefault(key, on ? 1f : 0f);
		float target = on ? 1f : 0f;
		f += (target - f) * 0.30f;
		if (Math.abs(target - f) < 0.02f) {
			f = target;
		}
		switchAnim.put(key, f);
		int rgb = (GuiTheme.accentR << 16) | (GuiTheme.accentG << 8) | GuiTheme.accentB;
		if (f > 0.05f) {
			glow(g, x, y, SWITCH_W, SWITCH_H, rgb, f);
		}
		int track = f > 0.5f ? GuiTheme.accent() : 0xFF3A3038;
		rrect(g, x, y, SWITCH_W, SWITCH_H, track);
		int knob = SWITCH_H - 6;
		int kx = x + 3 + (int) (f * (SWITCH_W - knob - 6));
		rrect(g, kx, y + 3, knob + 1, knob + 1, 0xFFFFFFFF);
		if (f > 0.9f) {
			g.fill(kx + 2, y + 5, kx + 3, y + 6, track);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int mx = (int) event.x();
		int my = (int) event.y();
		layout();
		int px = panelX;
		int py = panelY;
		int x1 = px + panelW;
		int x0 = px + SIDEBAR_W;

		// Resize grip (corner) - checked BEFORE everything else
		int gpx = x1 - GRIP_SIZE - 2;
		int gpy = py + panelH - GRIP_SIZE - 2;
		if (in(mx, my, gpx - 4, gpy - 4, GRIP_SIZE + 8, GRIP_SIZE + 8)) {
			resizing = true;
			resizeOX = mx - panelW;
			resizeOY = my - panelH;
			return true;
		}

		// Close button
		if (in(mx, my, x1 - 26, py + 8, 20, HEADER_H - 16)) {
			onClose();
			return true;
		}

		// Title bar drag
		if (in(mx, my, px, py, panelW, HEADER_H)) {
			dragging = true;
			dragOX = mx - panelX;
			dragOY = my - panelY;
			return true;
		}

		// Sidebar categories
		for (int i = 0; i < CATS.length; i++) {
			int cy = py + HEADER_H + 12 + i * CAT_H;
			if (in(mx, my, px + 6, cy - 1, SIDEBAR_W - 12, CAT_H - 2)) {
				if (activeCat != i) {
					activeCat = i;
					pageStart = 0;
					rebuildRows();
				}
				return true;
			}
		}

		// Rows
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			if (!in(mx, my, x0, y, panelW - SIDEBAR_W, h - 2)) {
				continue;
			}
			if (r.isModule) {
				if (!r.module.getName().equals("Cosmetics") && in(mx, my, x1 - 22, y, 22, h - 2)) {
					if (expanded.contains(r.module)) {
						expanded.remove(r.module);
					} else {
						expanded.add(r.module);
					}
					rebuildRows();
					return true;
				}
				if (r.module.getName().equals("Cosmetics")) {
					if (this.minecraft != null) {
						this.minecraft.setScreen(new CosmeticsScreen());
					}
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
					if (mx < x0 + (panelW - SIDEBAR_W) / 2.0) {
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
		int footY = py + panelH - FOOTER_H - 8;
		if (pageStart > 0 && in(mx, my, x1 - 48, footY - 2, 24, FOOTER_H + 2)) {
			int pageSize = Math.max(1, visibleEnd - pageStart);
			pageStart = Math.max(0, pageStart - pageSize);
			layout();
			return true;
		}
		if (visibleEnd < rows.size() && in(mx, my, x1 - 26, footY - 2, 24, FOOTER_H + 2)) {
			pageStart = visibleEnd;
			layout();
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private void syncIfGuiEditor(Module m) {
		if (m instanceof GuiModule gm) {
			gm.syncToTheme();
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		int mx = (int) event.x();
		int my = (int) event.y();
		if (resizing) {
			panelW = Math.max(MIN_W, (int) Math.min(this.width - panelX, mx - resizeOX));
			panelH = Math.max(MIN_H, (int) Math.min(this.height - panelY, my - resizeOY));
			clampWindow();
			layout();
			return true;
		}
		if (dragging) {
			panelX = mx - dragOX;
			panelY = my - dragOY;
			clampWindow();
			layout();
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragging = false;
		resizing = false;
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
		long time = System.currentTimeMillis();
		for (int band = 0; band < 3; band++) {
			int bandH = this.height / 5;
			float phase = ((time % 42000L) / 42000.0F) + band * 0.33F;
			int by = (int) ((phase % 1.0F) * (this.height + bandH)) - bandH;
			int hueCol = hue(time, band * 1200);
			int color = (hueCol & 0x00FFFFFF) | 0x12000000;
			g.fill(0, by, this.width, by + bandH, color);
			g.fill(0, by + bandH / 3, this.width, by + bandH * 2 / 3, color);
		}
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
