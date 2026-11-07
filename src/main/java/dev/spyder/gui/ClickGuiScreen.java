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
 * ClickGUI v4 - Prestige-inspired, fully hand-drawn, zero vanilla widgets.
 * - Chamfered (rounded) panels with drop shadow
 * - Row hover highlights with accent edge
 * - Glowing sliding switches (bigger, rounded knob)
 * - Nebula bands in the cosmic background
 * - Scaled bold animated title
 * - Interaction identical to v3 (proven 1.21.11 event API input handling).
 */
public class ClickGuiScreen extends Screen {
	private static final int SIDEBAR_W = 84;
	private static final int PANEL_W = 286;
	private static final int HEADER_H = 40;
	private static final int MODULE_H = 30;
	private static final int SETTING_H = 24;
	private static final int FOOTER_H = 18;
	private static final int CAT_H = 24;
	private static final int SWITCH_W = 30;
	private static final int SWITCH_H = 15;

	private static final String[] CATS = {"VISUALS", "HUD", "CLIENT"};

	// Starfield (allocated once, reused forever)
	private static final int STAR_COUNT = 90;
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
				starSp[i] = 0.00006f + (float) Math.random() * 0.00022f;
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
		int top = (int) panelY + HEADER_H + 8;
		int avail = this.height - top - FOOTER_H - 12;
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

	/** Chamfered ("rounded") rectangle via stepped fills - radius ~3. */
	private void rrect(GuiGraphics g, int x, int y, int w, int h, int color) {
		g.fill(x + 3, y, x + w - 3, y + h, color);
		g.fill(x, y + 3, x + w, y + h - 3, color);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
		g.fill(x + 2, y + 2, x + w - 2, y + h - 2, color);
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
		int x0 = px + SIDEBAR_W;
		int x1 = px + PANEL_W;
		int accent = GuiTheme.accent();

		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int panelH = HEADER_H + 8 + contentH + FOOTER_H + 8;

		// Drop shadow, then rounded panel body
		rrect(g, px + 4, py + 5, PANEL_W, panelH, 0x55000000);
		rrect(g, px, py, PANEL_W, panelH, 0xFF15151D);
		rrect(g, px + 1, py + 1, PANEL_W - 2, panelH - 2, 0xF60D0B14);
		// Accent top edge across rounded corners
		g.fill(px + 3, py, x1 - 3, py + 2, accent);
		g.fill(px + 2, py + 1, px + 3, py + 2, accent);
		g.fill(x1 - 3, py + 1, x1 - 2, py + 2, accent);

		// Scaled bold animated title with drop shadow + version
		float ts = 1.35F;
		g.pose().pushPose();
		g.pose().scale(ts, ts, 1.0F);
		String title = GuiTheme.clientName;
		float tx = (x0 + 10) / ts;
		for (int i = 0; i < title.length(); i++) {
			String ch = String.valueOf(title.charAt(i));
			g.drawString(this.font, ch, tx + 0.5F, (py + 9) / ts + 0.5F, 0x66000000);
			g.drawString(this.font, ch, tx, (py + 9) / ts, hue(time, i * 120));
			tx += this.font.width(ch);
		}
		g.pose().popPose();
		int titleEndX = (int) (tx * ts);
		g.drawString(this.font, "v" + GuiTheme.version, titleEndX + 4, py + 14, 0xFF8A8A96);
		g.drawString(this.font, "x", x1 - 15, py + 11, 0xFF995555);
		// Animated rainbow strip under header
		int stripW = PANEL_W - SIDEBAR_W;
		for (int i = 0; i < stripW - 10; i++) {
			g.fill(x0 + 5 + i, py + HEADER_H - 3, x0 + 6 + i, py + HEADER_H, hue(time, i));
		}
		// Divider between sidebar and content
		g.fill(x0, py + 2, x0 + 1, py + panelH - 2, 0x40FFFFFF);

		// Sidebar categories with glyph icons
		for (int i = 0; i < CATS.length; i++) {
			int cy = py + HEADER_H + 10 + i * CAT_H;
			boolean active = i == activeCat;
			if (active) {
				rrect(g, px + 5, cy, SIDEBAR_W - 10, CAT_H - 2, 0x28FFFFFF);
				g.fill(px + 6, cy + 4, px + 8, cy + CAT_H - 6, accent);
			}
			int gx = px + 12;
			int gy = cy + (CAT_H - 2) / 2 - 2;
			int gc = active ? accent : 0xFF70707C;
			if (i == 0) {
				g.fill(gx, gy, gx + 4, gy + 4, gc);
			} else if (i == 1) {
				g.fill(gx, gy, gx + 4, gy + 1, gc);
				g.fill(gx, gy + 3, gx + 4, gy + 4, gc);
				g.fill(gx, gy, gx + 1, gy + 4, gc);
				g.fill(gx + 3, gy, gx + 4, gy + 4, gc);
			} else {
				g.fill(gx + 1, gy, gx + 3, gy + 1, gc);
				g.fill(gx, gy + 1, gx + 4, gy + 3, gc);
				g.fill(gx + 1, gy + 3, gx + 3, gy + 4, gc);
			}
			g.drawString(this.font, CATS[i], px + 22, cy + 8,
					active ? 0xFFFFFFFF : 0xFF8E8E96);
		}

		// Rows with hover highlight
		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			boolean hovered = in(mouseX, mouseY, x0 + 2, y, PANEL_W - SIDEBAR_W - 4, h - 2);
			if (hovered) {
				rrect(g, x0 + 4, y, PANEL_W - SIDEBAR_W - 8, h - 2, 0x1CFFFFFF);
				g.fill(x0 + 5, y + 3, x0 + 7, y + h - 5, accent);
			}
			if (r.isModule) {
				drawModuleRow(g, r.module, x0, x1, y, time);
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
			g.drawString(this.font, ">", x1 - 20, footY, accent);
		}
		String wm = GuiTheme.clientName.toLowerCase() + " v" + GuiTheme.version;
		g.drawString(this.font, wm, x0 + 2 + this.font.width("tap row = toggle") * 0 / 1 + 8 + this.font.width("tap row = toggle   + = settings") + 8, footY, 0xFF4A4A55);
	}

	private void drawModuleRow(GuiGraphics g, Module m, int x0, int x1, int y, long time) {
		g.drawString(this.font, m.getName(), x0 + 10, y + 11,
				m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAB4);
		drawSwitch(g, x1 - 44, y + 7, m.isEnabled(), m.getName());
		boolean open = expanded.contains(m);
		g.drawString(this.font, open ? "-" : "+", x1 - 15, y + 11, 0xFFC8C8D0);
	}

	private void drawSettingRow(GuiGraphics g, Row r, int x0, int x1, int y, long time) {
		int indent = x0 + 16;
		SpyderSettings.Setting s = r.setting;
		if (s instanceof SpyderSettings.CycleSetting cyc) {
			g.drawString(this.font, cyc.display(), indent, y + 8, 0xFFB0B0BC);
			String chip = "< " + cyc.value() + " >";
			int chipW = this.font.width(chip);
			int chipX = x1 - 12 - chipW;
			rrect(g, chipX - 7, y + 4, chipW + 14, SETTING_H - 8, 0x24FFFFFF);
			g.drawString(this.font, chip, chipX, y + 8, hue(time, y * 16));
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), indent, y + 8, 0xFFB0B0BC);
			drawSwitch(g, x1 - 44, y + 4, tog.value(), tog.name());
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), indent, y + 8, 0xFFB0B0BC);
			String val = String.format("%.2f", rng.value());
			rrect(g, x1 - 62, y + 4, 12, SETTING_H - 12, 0x24FFFFFF);
			g.drawString(this.font, "-", x1 - 58, y + 8, 0xFFD8D8E0);
			int valX = x1 - 46;
			g.drawString(this.font, val, valX, y + 8, hue(time, y * 16));
			int plusX = valX + this.font.width(val) + 6;
			rrect(g, plusX - 2, y + 4, 12, SETTING_H - 12, 0x24FFFFFF);
			g.drawString(this.font, "+", plusX + 2, y + 8, 0xFFD8D8E0);
		}
	}

	/** Animated glowing sliding switch with rounded knob. */
	private void drawSwitch(GuiGraphics g, int x, int y, boolean on, String key) {
		float f = switchAnim.getOrDefault(key, on ? 1f : 0f);
		float target = on ? 1f : 0f;
		f += (target - f) * 0.35f;
		if (Math.abs(target - f) < 0.02f) {
			f = target;
		}
		switchAnim.put(key, f);
		if (f > 0.05f) {
			int glowA = (int) (0x40 * f);
			rrect(g, x - 2, y - 2, SWITCH_W + 4, SWITCH_H + 4,
					(glowA << 24) | (GuiTheme.accentR << 16) | (GuiTheme.accentG << 8) | GuiTheme.accentB);
		}
		int track = f > 0.5f ? GuiTheme.accent() : 0xFF3A3038;
		rrect(g, x, y, SWITCH_W, SWITCH_H, track);
		int knob = SWITCH_H - 5;
		int kx = x + 3 + (int) (f * (SWITCH_W - knob - 6));
		rrect(g, kx, y + 2, knob + 1, knob + 1, 0xFFFFFFFF);
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

		if (in(mx, my, x1 - 24, py + 4, 22, HEADER_H - 8)) {
			onClose();
			return true;
		}

		if (in(mx, my, x0, py, PANEL_W - SIDEBAR_W, HEADER_H)) {
			dragging = true;
			dragOX = mx - panelX;
			dragOY = my - panelY;
			return true;
		}

		for (int i = 0; i < CATS.length; i++) {
			int cy = py + HEADER_H + 10 + i * CAT_H;
			if (in(mx, my, px + 5, cy, SIDEBAR_W - 10, CAT_H - 2)) {
				if (activeCat != i) {
					activeCat = i;
					pageStart = 0;
					rebuildRows();
				}
				return true;
			}
		}

		for (int i = pageStart; i < visibleEnd; i++) {
			Row r = rows.get(i);
			int y = rowYs[i];
			int h = r.isModule ? MODULE_H : SETTING_H;
			if (!in(mx, my, x0, y, PANEL_W - SIDEBAR_W, h - 2)) {
				continue;
			}
			if (r.isModule) {
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

		int contentH = 0;
		for (int i = pageStart; i < visibleEnd; i++) {
			contentH += rows.get(i).isModule ? MODULE_H : SETTING_H;
		}
		int footY = py + HEADER_H + 8 + contentH + 4;
		if (pageStart > 0 && in(mx, my, x1 - 46, footY - 2, 24, FOOTER_H + 2)) {
			int pageSize = Math.max(1, visibleEnd - pageStart);
			pageStart = Math.max(0, pageStart - pageSize);
			layout();
			return true;
		}
		if (visibleEnd < rows.size() && in(mx, my, x1 - 24, footY - 2, 24, FOOTER_H + 2)) {
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
		for (int band = 0; band < 3; band++) {
			int bandH = this.height / 5;
			float phase = ((time % 30000L) / 30000.0F) + band * 0.33F;
			int by = (int) ((phase % 1.0F) * (this.height + bandH)) - bandH;
			int hueCol = hue(time, band * 900);
			int color = (hueCol & 0x00FFFFFF) | 0x14000000;
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
