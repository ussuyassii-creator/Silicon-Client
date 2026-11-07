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
 * ClickGUI v6 "Nebula" - complete premium recode.
 *
 * NINE sections: SWORD, MACE, CRYSTAL, CART, SPEAR, UHC (reserved combat
 * sections, empty for now), VISUALS, CLIENT (live modules), SETTINGS
 * (theme presets with live color previews).
 *
 * Style: restrained premium - deep tinted panel (theme-matched), solid
 * white brand (no rainbow), accent gradient underline, glowing pill
 * switches, hover lift, slide-in settings. Zero vanilla widgets.
 * Zero per-frame allocations.
 */
public class ClickGuiScreen extends Screen {
	// Section ids in sidebar order
	private static final String[] SECTIONS = {
			"SWORD", "MACE", "CRYSTAL", "CART", "SPEAR", "UHC",
			"VISUALS", "CLIENT", "SETTINGS"
	};
	private static final int RESERVED_COUNT = 6; // sword..uhc
	private static final int SETTINGS_IDX = 8;

	private static final int SIDEBAR_W = 92;
	private static final int PANEL_W = 300;
	private static final int HEADER_H = 40;
	private static final int MODULE_H = 28;
	private static final int SETTING_H = 24;
	private static final int FOOTER_H = 18;
	private static final int SEC_H = 22;
	private static final int SWITCH_W = 32;
	private static final int SWITCH_H = 16;

	// Starfield (allocated once, reused forever)
	private static final int STAR_COUNT = 110;
	private static final float[] starX = new float[STAR_COUNT];
	private static final float[] starY = new float[STAR_COUNT];
	private static final float[] starSp = new float[STAR_COUNT];
	private static final float[] starTw = new float[STAR_COUNT];
	private static boolean starsInit = false;

	private double panelX = 40;
	private double panelY = 32;
	private boolean dragging;
	private double dragOX;
	private double dragOY;

	private int activeSec = 6; // VISUALS
	private float secGlow = 0f;

	private final Set<Module> expanded = new HashSet<>();
	private final Map<String, Float> switchAnim = new HashMap<>();
	private final Map<String, Float> expandAnim = new HashMap<>();
	private final float[] secAnim = new float[SECTIONS.length];
	private int hoverPreset = -1;

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
		clampPanel();
		rebuildRows();
	}

	private static boolean isReserved(String sec) {
		for (int i = 0; i < RESERVED_COUNT; i++) {
			if (SECTIONS[i].equals(sec)) {
				return true;
			}
		}
		return false;
	}

	private static String categoryOf(Module m) {
		String n = m.getName();
		if (n.equals("HUD")) {
			return "VISUALS";
		}
		if (n.equals("GUI Editor") || n.equals("FPS Boost")) {
			return "CLIENT";
		}
		return "VISUALS";
	}

	private void rebuildRows() {
		rows.clear();
		if (activeSec == SETTINGS_IDX) {
			return; // settings page draws its own content, not rows
		}
		String sec = SECTIONS[activeSec];
		for (Module m : ModuleManager.modules()) {
			if (!categoryOf(m).equals(sec)) {
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
		int top = (int) panelY + HEADER_H + 8;
		int avail = this.height - top - FOOTER_H - 14;
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

		if (activeSec != SETTINGS_IDX) {
			layout();
		}
		long time = System.currentTimeMillis();
		int px = (int) panelX;
		int py = (int) panelY;
		int x0 = px + SIDEBAR_W;
		int x1 = px + PANEL_W;
		int accent = GuiTheme.accent();

		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int panelH = HEADER_H + 8 + contentH + FOOTER_H + 10;

		// --- Panel: shadow, theme-tinted body, subtle border ---
		rrect(g, px + 5, py + 6, PANEL_W, panelH, 0x66000000);
		rrect(g, px, py, PANEL_W, panelH, GuiTheme.panelBody);
		// Hairline border (subtle light edge)
		rrect(g, px, py, PANEL_W, 1, 0x2EFFFFFF);
		rrect(g, px, py + panelH - 1, PANEL_W, 1, 0x14FFFFFF);
		// Sidebar slightly darker
		rrect(g, px + 1, py + 1, SIDEBAR_W - 2, panelH - 2, 0x22000000);
		g.fill(x0, py + 1, x0 + 1, py + panelH - 1, 0x26FFFFFF);

		// --- Header: solid white brand + accent gradient underline ---
		String title = GuiTheme.clientName;
		int tx = x0 + 10;
		int titleW = this.font.width(title);
		g.drawString(this.font, title, tx + 1, py + 10, 0x66000000);
		g.drawString(this.font, title, tx, py + 9, 0xFFFFFFFF);
		// Version chip
		String ver = "v" + GuiTheme.version;
		g.drawString(this.font, ver, tx + titleW + 6, py + 12, 0x8FFFFFFF);
		// Accent gradient underline under the whole header
		int uW = PANEL_W - SIDEBAR_W - 12;
		for (int i = 0; i < uW; i++) {
			float f = i / (float) uW;
			int r = (int) (GuiTheme.accentR + 60 * f);
			int gg = (int) (GuiTheme.accentG + 60 * f);
			int bb = (int) (GuiTheme.accentB + 60 * f);
			if (r > 255) r = 255;
			if (gg > 255) gg = 255;
			if (bb > 255) bb = 255;
			g.fill(x0 + 6 + i, py + HEADER_H - 4, x0 + 7 + i, py + HEADER_H - 1,
					0xFF000000 | (r << 16) | (gg << 8) | bb);
		}
		// Close button
		boolean closeHover = in(mouseX, mouseY, x1 - 24, py + 6, 18, HEADER_H - 12);
		rrect(g, x1 - 26, py + 8, 20, HEADER_H - 16, closeHover ? 0xFF3A2226 : 0x18FFFFFF);
		g.drawString(this.font, "x", x1 - 19, py + 13, closeHover ? 0xFFFF7777 : 0xFFAA6666);

		// --- Sidebar sections ---
		for (int i = 0; i < SECTIONS.length; i++) {
			int sy = py + HEADER_H + 8 + i * SEC_H;
			boolean active = i == activeSec;
			float target = active ? 1f : 0f;
			secAnim[i] += (target - secAnim[i]) * 0.25f;
			if (Math.abs(target - secAnim[i]) < 0.02f) {
				secAnim[i] = target;
			}
			boolean secHover = in(mouseX, mouseY, px + 6, sy, SIDEBAR_W - 12, SEC_H - 2);
			if (secAnim[i] > 0.03f) {
				glow(g, px + 6, sy, SIDEBAR_W - 12, SEC_H - 2, accentRgb(), secAnim[i] * 0.6f);
			}
			if (active || secHover) {
				rrect(g, px + 6, sy, SIDEBAR_W - 12, SEC_H - 2, active ? 0x24FFFFFF : 0x10FFFFFF);
			}
			if (active) {
				g.fill(px + 7, sy + 3, px + 9, sy + SEC_H - 5, accent);
			}
			boolean reserved = i < RESERVED_COUNT;
			int labelCol = active ? 0xFFFFFFFF : (reserved ? 0xFF5A5A66 : 0xFF9A9AA6);
			g.drawString(this.font, SECTIONS[i], px + 12, sy + 7, labelCol);
			if (reserved) {
				// "soon" tag
				g.drawString(this.font, "soon", px + SIDEBAR_W - 24, sy + 7, 0xFF4A4A56);
			}
		}

		// --- Content ---
		if (activeSec == SETTINGS_IDX) {
			drawSettingsPage(g, x0, x1, py + HEADER_H + 8, mouseX, mouseY);
		} else if (isReserved(SECTIONS[activeSec])) {
			drawReservedPage(g, x0, py + HEADER_H + 8, time);
		} else {
			for (int i = pageStart; i < visibleEnd; i++) {
				Row r = rows.get(i);
				int y = rowYs[i];
				int h = r.isModule ? MODULE_H : SETTING_H;
				boolean hovered = in(mouseX, mouseY, x0 + 3, y, PANEL_W - SIDEBAR_W - 6, h - 2);
				if (hovered) {
					rrect(g, x0 + 5, y - 1, PANEL_W - SIDEBAR_W - 10, h, 0x16FFFFFF);
					g.fill(x0 + 6, y + 3, x0 + 8, y + h - 4, accent);
				}
				if (r.isModule) {
					drawModuleRow(g, r.module, x0, x1, y, hovered);
				} else {
					drawSettingRow(g, r, x0, x1, y, time);
				}
			}
			// Footer
			int footY = py + HEADER_H + 8 + contentH + 4;
			g.drawString(this.font, "tap row = toggle   + = settings", x0 + 8, footY, 0xFF6E6E76);
			if (pageStart > 0) {
				g.drawString(this.font, "<", x1 - 44, footY, accent);
			}
			if (visibleEnd < rows.size()) {
				g.drawString(this.font, ">", x1 - 22, footY, accent);
			}
		}
	}

	private static int accentRgb() {
		return (GuiTheme.accentR << 16) | (GuiTheme.accentG << 8) | GuiTheme.accentB;
	}

	/** Reserved combat section: premium ghost empty-state. */
	private void drawReservedPage(GuiGraphics g, int x0, int y, long time) {
		int cx = x0 + (PANEL_W - SIDEBAR_W) / 2;
		int cy = y + 30;
		String name = SECTIONS[activeSec];
		// Big ghost wordmark
		g.drawString(this.font, name, cx - this.font.width(name) / 2, cy, 0xFF3C3C48);
		g.drawString(this.font, "coming soon", cx - this.font.width("coming soon") / 2, cy + 14, 0xFF565662);
		g.drawString(this.font, "combat modules reserved for this section", cx - this.font.width("combat modules reserved for this section") / 2, cy + 30, 0xFF4E4E5A);
		// Pulsing accent dot
		float pulse = 0.5f + 0.5f * (float) Math.sin(time / 400.0);
		int a = (int) (0x60 * pulse);
		g.fill(cx - 1, cy - 10, cx + 1, cy - 8, (a << 24) | accentRgb());
	}

	/** SETTINGS page: theme preset picker with live color swatches. */
	private void drawSettingsPage(GuiGraphics g, int x0, int x1, int y, int mouseX, int mouseY) {
		int cw = (PANEL_W - SIDEBAR_W - 20 - 3 * 4) / 4;
		int ch = 40;
		hoverPreset = -1;
		for (int i = 0; i < GuiTheme.PRESET_NAMES.length; i++) {
			int col = i % 4;
			int rowI = i / 4;
			int cx = x0 + 10 + col * (cw + 4);
			int cy = y + 4 + rowI * (ch + 4);
			int accentCol = GuiTheme.presetAccent(i);
			boolean hov = in(mouseX, mouseY, cx, cy, cw, ch);
			if (hov) {
				hoverPreset = i;
				rrect(g, cx - 2, cy - 2, cw + 4, ch + 4, 0x1AFFFFFF);
			}
			// Swatch card: dark body + accent bar preview
			rrect(g, cx, cy, cw, ch, 0xFF191922);
			rrect(g, cx + 1, cy + 1, cw - 2, 10, accentCol);
			g.drawString(this.font, GuiTheme.PRESET_NAMES[i], cx + 4, cy + 16, hov ? 0xFFFFFFFF : 0xFFB0B0BC);
			// Mini switch preview
			rrect(g, cx + cw - 18, cy + 26, 12, 6, accentCol);
			rrect(g, cx + cw - 16, cy + 27, 4, 4, 0xFFFFFFFF);
		}
		g.drawString(this.font, "pick a theme - the whole GUI recolors instantly", x0 + 10, y + 4 + 2 * (ch + 4) + 4, 0xFF6E6E76);
	}

	private void drawModuleRow(GuiGraphics g, Module m, int x0, int x1, int y, boolean hovered) {
		g.drawString(this.font, m.getName(), x0 + 12, y + 10,
				m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAB4);
		drawSwitch(g, x1 - 48, y + 6, m.isEnabled(), m.getName());
		if (m.getName().equals("Cosmetics")) {
			g.drawString(this.font, "3D", x1 - 17, y + 10, hovered ? 0xFFFFFFFF : GuiTheme.accent());
		} else {
			boolean open = expanded.contains(m);
			g.drawString(this.font, open ? "-" : "+", x1 - 17, y + 10, 0xFFC8C8D0);
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
			rrect(g, chipX - 8, y + 3, chipW + 16, SETTING_H - 6, 0x22FFFFFF);
			g.drawString(this.font, "<", chipX, y + 8, 0xFF8E8E9A);
			g.drawString(this.font, cyc.value(), chipX + this.font.width("< "), y + 8, GuiTheme.accent());
			g.drawString(this.font, ">", chipX + chipW - this.font.width(">"), y + 8, 0xFF8E8E9A);
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), indent, y + 8, 0xFFB0B0BC);
			drawSwitch(g, x1 - 48, y + 4, tog.value(), key + "." + tog.name());
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), indent, y + 8, 0xFFB0B0BC);
			String val = String.format("%.2f", rng.value());
			rrect(g, x1 - 66, y + 3, 14, SETTING_H - 10, 0x22FFFFFF);
			g.drawString(this.font, "-", x1 - 61, y + 8, 0xFFD8D8E0);
			int valX = x1 - 50;
			g.drawString(this.font, val, valX, y + 8, GuiTheme.accent());
			int plusX = valX + this.font.width(val) + 8;
			rrect(g, plusX - 3, y + 3, 14, SETTING_H - 10, 0x22FFFFFF);
			g.drawString(this.font, "+", plusX + 2, y + 8, 0xFFD8D8E0);
		}
	}

	private void drawSwitch(GuiGraphics g, int x, int y, boolean on, String key) {
		float f = switchAnim.getOrDefault(key, on ? 1f : 0f);
		float target = on ? 1f : 0f;
		f += (target - f) * 0.30f;
		if (Math.abs(target - f) < 0.02f) {
			f = target;
		}
		switchAnim.put(key, f);
		if (f > 0.05f) {
			glow(g, x, y, SWITCH_W, SWITCH_H, accentRgb(), f);
		}
		int track = f > 0.5f ? GuiTheme.accent() : 0xFF33333E;
		rrect(g, x, y, SWITCH_W, SWITCH_H, track);
		int knob = SWITCH_H - 6;
		int kx = x + 3 + (int) (f * (SWITCH_W - knob - 6));
		rrect(g, kx, y + 3, knob + 1, knob + 1, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		if (activeSec != SETTINGS_IDX) {
			layout();
		}
		int px = (int) panelX;
		int py = (int) panelY;
		int x0 = px + SIDEBAR_W;
		int x1 = px + PANEL_W;

		// Close button
		if (in(mx, my, x1 - 26, py + 8, 20, HEADER_H - 16)) {
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

		// Sidebar sections
		for (int i = 0; i < SECTIONS.length; i++) {
			int sy = py + HEADER_H + 8 + i * SEC_H;
			if (in(mx, my, px + 6, sy, SIDEBAR_W - 12, SEC_H - 2)) {
				if (activeSec != i) {
					activeSec = i;
					pageStart = 0;
					rebuildRows();
				}
				return true;
			}
		}

		// SETTINGS page: theme cards
		if (activeSec == SETTINGS_IDX) {
			int cw = (PANEL_W - SIDEBAR_W - 20 - 3 * 4) / 4;
			int ch = 40;
			int contentY = py + HEADER_H + 8;
			for (int i = 0; i < GuiTheme.PRESET_NAMES.length; i++) {
				int col = i % 4;
				int rowI = i / 4;
				int cx = x0 + 10 + col * (cw + 4);
				int cy = contentY + 4 + rowI * (ch + 4);
				if (in(mx, my, cx, cy, cw, ch)) {
					GuiTheme.applyPreset(i);
					return true;
				}
			}
			return super.mouseClicked(event, doubleClick);
		}

		// Module rows
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			if (!in(mx, my, x0, y, PANEL_W - SIDEBAR_W, h - 2)) {
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
		int footY = py + HEADER_H + 8 + contentH + 4;
		if (pageStart > 0 && in(mx, my, x1 - 46, footY - 2, 22, FOOTER_H + 2)) {
			int pageSize = Math.max(1, visibleEnd - pageStart);
			pageStart = Math.max(0, pageStart - pageSize);
			layout();
			return true;
		}
		if (visibleEnd < rows.size() && in(mx, my, x1 - 24, footY - 2, 22, FOOTER_H + 2)) {
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
		long time = System.currentTimeMillis();
		// Deep nebula bands - subdued, tinted with the accent
		for (int band = 0; band < 2; band++) {
			int bandH = this.height / 4;
			float phase = ((time % 52000L) / 52000.0F) + band * 0.5F;
			int by = (int) ((phase % 1.0F) * (this.height + bandH)) - bandH;
			int color = (accentRgb() & 0x00FFFFFF) | 0x0E000000;
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

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
