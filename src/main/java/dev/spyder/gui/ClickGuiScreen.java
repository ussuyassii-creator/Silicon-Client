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
 * ClickGUI v7 "Orchard" - premium violet interface.
 *
 * ELEVEN sections with hand-drawn pixel icons:
 * SWORD, MACE, CART, UHC, SPEAR, CRYSTAL (reserved, ghost pages),
 * VISUALS (live modules), SILENT (reserved), COSMETICS (opens the 3D
 * player-model editor), SETTINGS (theme preset cards), PLAYER (routes
 * to the cosmetics editor too).
 *
 * Orchard style: light purple + deep violet, rounded glass panels with
 * soft shadows, clean spacing, touch-friendly row heights for
 * PojavLauncher, smooth hover/click animations, zero rainbow.
 */
public class ClickGuiScreen extends Screen {
	private static final String[] SECTIONS = {
			"SWORD", "MACE", "CART", "UHC", "SPEAR", "CRYSTAL",
			"VISUALS", "SILENT", "COSMETICS", "SETTINGS", "PLAYER"
	};
	private static final int RESERVED_COUNT = 6; // sword..crystal
	private static final int VISUALS_IDX = 6;
	private static final int SILENT_IDX = 7;
	private static final int COSMETICS_IDX = 8;
	private static final int SETTINGS_IDX = 9;
	private static final int PLAYER_IDX = 10;

	private static final int SIDEBAR_W = 96;
	private static final int PANEL_W = 308;
	private static final int HEADER_H = 42;
	private static final int MODULE_H = 30;   // touch-friendly
	private static final int SETTING_H = 26; // touch-friendly
	private static final int FOOTER_H = 18;
	private static final int SEC_H = 24;      // touch-friendly
	private static final int SWITCH_W = 34;
	private static final int SWITCH_H = 17;

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

	private int activeSec = VISUALS_IDX;

	private final Set<Module> expanded = new HashSet<>();
	private final Map<String, Float> switchAnim = new HashMap<>();
	private final Map<String, Float> expandAnim = new HashMap<>();
	private final float[] secAnim = new float[SECTIONS.length];

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

	private static String categoryOf(Module m) {
		String n = m.getName();
		if (n.equals("HUD") || n.equals("Swing") || n.equals("HandShader")
				|| n.equals("Skybox") || n.equals("Ambient")) {
			return "VISUALS";
		}
		if (n.equals("GUI Editor") || n.equals("FPS Boost")) {
			return "CLIENT";
		}
		if (n.equals("Cosmetics")) {
			return "COSMETICS";
		}
		return "VISUALS";
	}

	private void rebuildRows() {
		rows.clear();
		if (activeSec == SETTINGS_IDX || activeSec == PLAYER_IDX) {
			return;
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
		int top = (int) panelY + HEADER_H + 10;
		int avail = this.height - top - FOOTER_H - 16;
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
		g.fill(x + 5, y, x + w - 5, y + h, color);
		g.fill(x, y + 5, x + w, y + h - 5, color);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
		g.fill(x + 2, y + 1, x + w - 2, y + h - 1, color);
		g.fill(x + 1, y + 2, x + w - 1, y + h - 2, color);
		g.fill(x + 3, y + 1, x + w - 3, y + h - 1, color);
		g.fill(x + 4, y + 1, x + w - 4, y + h - 1, color);
		g.fill(x + 1, y + 3, x + w - 1, y + h - 3, color);
		g.fill(x + 1, y + 4, x + w - 1, y + h - 4, color);
	}

	private void glow(GuiGraphics g, int x, int y, int w, int h, int rgb, float strength) {
		int a1 = (int) (0x30 * strength);
		int a2 = (int) (0x18 * strength);
		int a3 = (int) (0x0C * strength);
		g.fill(x - 6, y - 6, x + w + 6, y + h + 6, (a3 << 24) | rgb);
		g.fill(x - 4, y - 4, x + w + 4, y + h + 4, (a2 << 24) | rgb);
		g.fill(x - 2, y - 2, x + w + 2, y + h + 2, (a1 << 24) | rgb);
	}

	private static int accentRgb() {
		return (GuiTheme.accentR << 16) | (GuiTheme.accentG << 8) | GuiTheme.accentB;
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

		if (activeSec != SETTINGS_IDX && activeSec != PLAYER_IDX) {
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
		int panelH;
		if (activeSec == SETTINGS_IDX) {
			panelH = HEADER_H + 150;
		} else if (activeSec == PLAYER_IDX) {
			panelH = HEADER_H + 90;
		} else if (activeSec == COSMETICS_IDX) {
			panelH = HEADER_H + 150;
		} else {
			panelH = HEADER_H + 10 + contentH + FOOTER_H + 12;
		}

		// --- Panel: soft shadow, glass body, hairline border ---
		rrect(g, px + 5, py + 6, PANEL_W, panelH, 0x55000000);
		rrect(g, px, py, PANEL_W, panelH, GuiTheme.glass(GuiTheme.panelBody));
		// Hairline top + bottom light edges
		g.fill(px + 5, py, x1 - 5, py + 1, 0x30FFFFFF);
		g.fill(px + 5, py + panelH - 1, x1 - 5, py + panelH, 0x18FFFFFF);
		// Sidebar tint
		rrect(g, px + 1, py + 1, SIDEBAR_W - 2, panelH - 2, 0x20000000);
		g.fill(x0, py + 1, x0 + 1, py + panelH - 1, 0x22FFFFFF);

		// --- Header: white brand + version, violet gradient underline ---
		String title = GuiTheme.clientName;
		int tx = x0 + 10;
		int titleW = this.font.width(title);
		g.drawString(this.font, title, tx + 1, py + 11, 0x66000000);
		g.drawString(this.font, title, tx, py + 10, 0xFFFFFFFF);
		g.drawString(this.font, "v" + GuiTheme.version, tx + titleW + 6, py + 13, 0x8FFFFFFF);
		int uW = PANEL_W - SIDEBAR_W - 12;
		for (int i = 0; i < uW; i++) {
			float f = i / (float) uW;
			// accent -> lighter accent gradient
			int r = Math.min(255, (int) (GuiTheme.accentR + 50 * f));
			int gg = Math.min(255, (int) (GuiTheme.accentG + 50 * f));
			int bb = Math.min(255, (int) (GuiTheme.accentB + 50 * f));
			g.fill(x0 + 6 + i, py + HEADER_H - 4, x0 + 7 + i, py + HEADER_H - 1,
					0xFF000000 | (r << 16) | (gg << 8) | bb);
		}
		// Close button
		boolean closeHover = in(mouseX, mouseY, x1 - 26, py + 8, 20, HEADER_H - 16);
		rrect(g, x1 - 26, py + 8, 20, HEADER_H - 16, closeHover ? 0xFF3A2226 : 0x18FFFFFF);
		g.drawString(this.font, "x", x1 - 19, py + 14, closeHover ? 0xFFFF7777 : 0xFFAA6666);

		// --- Sidebar: sections with pixel icons ---
		for (int i = 0; i < SECTIONS.length; i++) {
			int sy = py + HEADER_H + 10 + i * SEC_H;
			boolean active = i == activeSec;
			float target = active ? 1f : 0f;
			secAnim[i] += (target - secAnim[i]) * 0.25f;
			if (Math.abs(target - secAnim[i]) < 0.02f) {
				secAnim[i] = target;
			}
			boolean secHover = in(mouseX, mouseY, px + 5, sy, SIDEBAR_W - 10, SEC_H - 2);
			if (secAnim[i] > 0.03f) {
				glow(g, px + 5, sy, SIDEBAR_W - 10, SEC_H - 2, accentRgb(), secAnim[i] * 0.5f);
			}
			if (active || secHover) {
				rrect(g, px + 5, sy, SIDEBAR_W - 10, SEC_H - 2, active ? 0x24FFFFFF : 0x10FFFFFF);
			}
			if (active) {
				g.fill(px + 6, sy + 3, px + 8, sy + SEC_H - 5, accent);
			}
			boolean reserved = i < RESERVED_COUNT || i == SILENT_IDX;
			drawSectionIcon(g, i, px + 12, sy + 6,
					active ? accent : (reserved ? 0xFF5A5A66 : 0xFF9A9AA6));
			int labelCol = active ? 0xFFFFFFFF : (reserved ? 0xFF6A6A76 : 0xFFC4C4CE);
			g.drawString(this.font, SECTIONS[i], px + 28, sy + 8, labelCol);
		}

		// --- Content pages ---
		if (activeSec == SETTINGS_IDX) {
			drawSettingsPage(g, x0, py + HEADER_H + 10, mouseX, mouseY);
		} else if (activeSec == PLAYER_IDX || activeSec == COSMETICS_IDX) {
			drawCosmeticsEntryPage(g, x0, x1, py + HEADER_H + 10, time);
		} else if (activeSec == SILENT_IDX || activeSec < RESERVED_COUNT) {
			drawReservedPage(g, x0, py + HEADER_H + 10, time);
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
			int footY = py + HEADER_H + 10 + contentH + 6;
			g.drawString(this.font, "tap row = toggle   + = settings", x0 + 8, footY, 0xFF6E6E78);
			if (pageStart > 0) {
				g.drawString(this.font, "<", x1 - 44, footY, accent);
			}
			if (visibleEnd < rows.size()) {
				g.drawString(this.font, ">", x1 - 22, footY, accent);
			}
		}
	}

	/** Hand-drawn 8x8 pixel icons per section - Minecraft-inspired, not dated. */
	private void drawSectionIcon(GuiGraphics g, int sec, int x, int y, int color) {
		switch (sec) {
			case 0 -> { // SWORD: diagonal blade + crossguard
				g.fill(x + 5, y, x + 7, y + 1, color);
				g.fill(x + 4, y + 1, x + 6, y + 2, color);
				g.fill(x + 3, y + 2, x + 5, y + 3, color);
				g.fill(x + 1, y + 4, x + 4, y + 5, color);
				g.fill(x + 2, y + 3, x + 4, y + 4, color);
				g.fill(x, y + 5, x + 2, y + 6, color);
				g.fill(x + 1, y + 6, x + 3, y + 7, color);
				g.fill(x, y + 7, x + 1, y + 8, color);
			}
			case 1 -> { // MACE: heavy head + handle
				g.fill(x + 1, y, x + 5, y + 2, color);
				g.fill(x, y + 1, x + 6, y + 3, color);
				g.fill(x + 2, y + 3, x + 4, y + 4, color);
				g.fill(x + 2, y + 4, x + 4, y + 8, color);
			}
			case 2 -> { // CART: minecart box + wheels
				g.fill(x, y + 1, x + 8, y + 2, color);
				g.fill(x, y + 2, x + 2, y + 4, color);
				g.fill(x + 6, y + 2, x + 8, y + 4, color);
				g.fill(x + 1, y + 4, x + 7, y + 5, color);
				g.fill(x + 1, y + 5, x + 3, y + 7, color);
				g.fill(x + 5, y + 5, x + 7, y + 7, color);
			}
			case 3 -> { // UHC: heart
				g.fill(x + 1, y + 1, x + 3, y + 2, color);
				g.fill(x + 5, y + 1, x + 7, y + 2, color);
				g.fill(x, y + 2, x + 8, y + 4, color);
				g.fill(x + 1, y + 4, x + 7, y + 5, color);
				g.fill(x + 2, y + 5, x + 6, y + 6, color);
				g.fill(x + 3, y + 6, x + 5, y + 7, color);
				g.fill(x + 4, y + 7, x + 5, y + 8, color);
			}
			case 4 -> { // SPEAR: shaft + triangular tip
				g.fill(x + 6, y, x + 8, y + 1, color);
				g.fill(x + 5, y + 1, x + 7, y + 2, color);
				g.fill(x + 4, y + 2, x + 6, y + 3, color);
				g.fill(x + 3, y + 3, x + 5, y + 4, color);
				g.fill(x + 2, y + 4, x + 4, y + 5, color);
				g.fill(x + 1, y + 5, x + 3, y + 6, color);
				g.fill(x, y + 6, x + 2, y + 7, color);
				g.fill(x, y + 7, x + 1, y + 8, color);
			}
			case 5 -> { // CRYSTAL: end crystal diamond on base
				g.fill(x + 3, y, x + 5, y + 1, color);
				g.fill(x + 2, y + 1, x + 6, y + 2, color);
				g.fill(x + 1, y + 2, x + 7, y + 4, color);
				g.fill(x + 2, y + 4, x + 6, y + 5, color);
				g.fill(x + 3, y + 5, x + 5, y + 6, color);
				g.fill(x + 1, y + 6, x + 7, y + 7, color);
				g.fill(x, y + 7, x + 8, y + 8, color);
			}
			case 6 -> { // VISUALS: eye
				g.fill(x + 2, y + 2, x + 6, y + 3, color);
				g.fill(x + 1, y + 3, x + 7, y + 5, color);
				g.fill(x + 3, y + 3, x + 5, y + 5, 0xFF0B0B12);
				g.fill(x + 2, y + 5, x + 6, y + 6, color);
			}
			case 7 -> { // SILENT: ghost
				g.fill(x + 1, y + 1, x + 7, y + 2, color);
				g.fill(x, y + 2, x + 8, y + 6, color);
				g.fill(x, y + 6, x + 2, y + 8, color);
				g.fill(x + 3, y + 6, x + 5, y + 8, color);
				g.fill(x + 6, y + 6, x + 8, y + 8, color);
				g.fill(x + 2, y + 3, x + 3, y + 4, 0xFF0B0B12);
				g.fill(x + 5, y + 3, x + 6, y + 4, 0xFF0B0B12);
			}
			case 8 -> { // COSMETICS: star
				g.fill(x + 3, y, x + 5, y + 2, color);
				g.fill(x, y + 2, x + 8, y + 4, color);
				g.fill(x + 1, y + 4, x + 7, y + 5, color);
				g.fill(x + 2, y + 5, x + 6, y + 6, color);
				g.fill(x + 1, y + 6, x + 3, y + 8, color);
				g.fill(x + 5, y + 6, x + 7, y + 8, color);
			}
			case 9 -> { // SETTINGS: gear
				g.fill(x + 2, y, x + 6, y + 1, color);
				g.fill(x, y + 1, x + 8, y + 2, color);
				g.fill(x, y + 2, x + 2, y + 6, color);
				g.fill(x + 6, y + 2, x + 8, y + 6, color);
				g.fill(x, y + 6, x + 8, y + 7, color);
				g.fill(x + 2, y + 7, x + 6, y + 8, color);
				g.fill(x + 3, y + 3, x + 5, y + 5, 0xFF0B0B12);
			}
			case 10 -> { // PLAYER: head + shoulders silhouette
				g.fill(x + 2, y, x + 6, y + 4, color);
				g.fill(x + 1, y + 4, x + 7, y + 5, color);
				g.fill(x, y + 5, x + 8, y + 8, color);
				g.fill(x + 3, y + 1, x + 5, y + 3, 0xFF0B0B12);
			}
			default -> {
			}
		}
	}

	/** Reserved section ghost page - premium empty state. */
	private void drawReservedPage(GuiGraphics g, int x0, int y, long time) {
		int cx = x0 + (PANEL_W - SIDEBAR_W) / 2;
		int cy = y + 34;
		String name = SECTIONS[activeSec];
		// Ghost icon, large
		drawSectionIcon(g, activeSec, cx - 4, cy - 16, 0xFF3A3A46);
		g.drawString(this.font, name, cx - this.font.width(name) / 2, cy + 2, 0xFF565664);
		g.drawString(this.font, "reserved - modules coming soon", cx - this.font.width("reserved - modules coming soon") / 2, cy + 16, 0xFF4E4E5C);
		float pulse = 0.5f + 0.5f * (float) Math.sin(time / 400.0);
		int a = (int) (0x60 * pulse);
		g.fill(cx - 1, cy - 22, cx + 1, cy - 20, (a << 24) | accentRgb());
	}

	/** Cosmetics / Player page: entry card that opens the 3D editor. */
	private void drawCosmeticsEntryPage(GuiGraphics g, int x0, int x1, int y, long time) {
		int cw = PANEL_W - SIDEBAR_W - 16;
		int ch = 90;
		int cx = x0 + 8;
		int cy = y + 8;
		// Card
		rrect(g, cx, cy, cw, ch, 0x1EFFFFFF);
		rrect(g, cx + 1, cy + 1, cw - 2, ch - 2, 0x14FFFFFF);
		// Star icon + title
		drawSectionIcon(g, 8, cx + 8, cy + 8, GuiTheme.accent());
		g.drawString(this.font, "Cosmetics Editor", cx + 24, cy + 10, 0xFFFFFFFF);
		g.drawString(this.font, "your player model, live 3D preview,", cx + 24, cy + 22, 0xFF9A9AA8);
		g.drawString(this.font, "drag to rotate, cape / wings / aura settings", cx + 24, cy + 32, 0xFF9A9AA8);
		// Pulsing open button
		float pulse = 0.5f + 0.5f * (float) Math.sin(time / 300.0);
		int btnCol = accentDimBlend(pulse);
		rrect(g, cx + 8, cy + 48, 92, 22, btnCol);
		String open = "OPEN 3D EDITOR";
		g.drawString(this.font, open, cx + 8 + (92 - this.font.width(open)) / 2, cy + 55, 0xFFFFFFFF);
		g.drawString(this.font, "star rating: premium", cx + 24, cy + 76, 0xFF6E6E78);
	}

	private int accentDimBlend(float f) {
		int r = (int) (GuiTheme.accentR * (0.7f + 0.3f * f));
		int gg = (int) (GuiTheme.accentG * (0.7f + 0.3f * f));
		int bb = (int) (GuiTheme.accentB * (0.7f + 0.3f * f));
		return 0xFF000000 | (r << 16) | (gg << 8) | bb;
	}

	/** SETTINGS page: preset cards grid with live color previews. */
	private void drawSettingsPage(GuiGraphics g, int x0, int y, int mouseX, int mouseY) {
		int cw = (PANEL_W - SIDEBAR_W - 20 - 3 * 4) / 4;
		int ch = 44;
		for (int i = 0; i < GuiTheme.PRESET_NAMES.length; i++) {
			int col = i % 4;
			int rowI = i / 4;
			int cx = x0 + 10 + col * (cw + 4);
			int cy = y + 4 + rowI * (ch + 4);
			int accentCol = GuiTheme.presetAccent(i);
			boolean hov = in(mouseX, mouseY, cx, cy, cw, ch);
			if (hov) {
				rrect(g, cx - 2, cy - 2, cw + 4, ch + 4, 0x1AFFFFFF);
			}
			rrect(g, cx, cy, cw, ch, 0xFF191922);
			// Accent gradient bar preview
			for (int j = 0; j < cw - 4; j++) {
				float f = j / (float) (cw - 4);
				int pr = (accentCol >> 16 & 0xFF);
				int pg = (accentCol >> 8 & 0xFF);
				int pb = (accentCol & 0xFF);
				int r = Math.min(255, (int) (pr + 40 * f));
				int gg = Math.min(255, (int) (pg + 40 * f));
				int bb = Math.min(255, (int) (pb + 40 * f));
				g.fill(cx + 2 + j, cy + 2, cx + 3 + j, cy + 10, 0xFF000000 | (r << 16) | (gg << 8) | bb);
			}
			String nm = GuiTheme.PRESET_NAMES[i];
			g.drawString(this.font, nm, cx + 3, cy + 16, hov ? 0xFFFFFFFF : 0xFFC4C4CE);
			// Mini switch preview
			rrect(g, cx + 2, cy + 28, 14, 7, accentCol);
			rrect(g, cx + 9, cy + 29, 5, 5, 0xFFFFFFFF);
			g.drawString(this.font, "tap", cx + cw - 22, cy + 28, 0xFF6E6E78);
		}
		g.drawString(this.font, "themes recolor the whole GUI instantly", x0 + 10, y + 4 + 2 * (ch + 4) + 6, 0xFF6E6E78);
	}

	private void drawModuleRow(GuiGraphics g, Module m, int x0, int x1, int y, boolean hovered) {
		g.drawString(this.font, m.getName(), x0 + 12, y + 11,
				m.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAB8);
		drawSwitch(g, x1 - 50, y + 7, m.isEnabled(), m.getName());
		if (m.getName().equals("Cosmetics")) {
			g.drawString(this.font, "3D", x1 - 17, y + 11, hovered ? 0xFFFFFFFF : GuiTheme.accent());
		} else {
			boolean open = expanded.contains(m);
			g.drawString(this.font, open ? "-" : "+", x1 - 17, y + 11, 0xFFC8C8D4);
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
			g.drawString(this.font, cyc.display(), indent, y + 8, 0xFFB0B0BE);
			String chip = "< " + cyc.value() + " >";
			int chipW = this.font.width(chip);
			int chipX = x1 - 14 - chipW;
			rrect(g, chipX - 8, y + 3, chipW + 16, SETTING_H - 6, 0x22FFFFFF);
			g.drawString(this.font, "<", chipX, y + 8, 0xFF8E8E9C);
			g.drawString(this.font, cyc.value(), chipX + this.font.width("< "), y + 8, GuiTheme.accent());
			g.drawString(this.font, ">", chipX + chipW - this.font.width(">"), y + 8, 0xFF8E8E9C);
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), indent, y + 8, 0xFFB0B0BE);
			drawSwitch(g, x1 - 50, y + 5, tog.value(), key + "." + tog.name());
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), indent, y + 8, 0xFFB0B0BE);
			String val = String.format("%.2f", rng.value());
			rrect(g, x1 - 68, y + 3, 16, SETTING_H - 10, 0x22FFFFFF);
			g.drawString(this.font, "-", x1 - 63, y + 8, 0xFFD8D8E4);
			int valX = x1 - 46;
			g.drawString(this.font, val, valX, y + 8, GuiTheme.accent());
			int plusX = valX + this.font.width(val) + 8;
			rrect(g, plusX - 3, y + 3, 16, SETTING_H - 10, 0x22FFFFFF);
			g.drawString(this.font, "+", plusX + 3, y + 8, 0xFFD8D8E4);
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
		int track = f > 0.5f ? GuiTheme.accent() : 0xFF34343E;
		rrect(g, x, y, SWITCH_W, SWITCH_H, track);
		int knob = SWITCH_H - 7;
		int kx = x + 3 + (int) (f * (SWITCH_W - knob - 7));
		rrect(g, kx, y + 3, knob + 1, knob + 1, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		if (activeSec != SETTINGS_IDX && activeSec != PLAYER_IDX) {
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
			int sy = py + HEADER_H + 10 + i * SEC_H;
			if (in(mx, my, px + 5, sy, SIDEBAR_W - 10, SEC_H - 2)) {
				if (activeSec != i) {
					activeSec = i;
					pageStart = 0;
					rebuildRows();
				}
				return true;
			}
		}

		// Cosmetics/Player entry page: open 3D editor button
		if (activeSec == COSMETICS_IDX || activeSec == PLAYER_IDX) {
			int cw = PANEL_W - SIDEBAR_W - 16;
			int cy = py + HEADER_H + 18;
			if (in(mx, my, x0 + 16, cy, 92, 22)) {
				if (this.minecraft != null) {
					this.minecraft.setScreen(new CosmeticsScreen());
				}
				return true;
			}
			return super.mouseClicked(event, doubleClick);
		}

		// SETTINGS page: theme cards
		if (activeSec == SETTINGS_IDX) {
			int cw = (PANEL_W - SIDEBAR_W - 20 - 3 * 4) / 4;
			int ch = 44;
			int contentY = py + HEADER_H + 10;
			for (int i = 0; i < GuiTheme.PRESET_NAMES.length; i++) {
				int col = i % 4;
				int rowI = i / 4;
				int cx = x0 + 10 + col * (cw + 4);
				int cyy = contentY + 4 + rowI * (ch + 4);
				if (in(mx, my, cx, cyy, cw, ch)) {
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
		int footY = py + HEADER_H + 10 + contentH + 6;
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
		g.fill(0, 0, this.width, this.height, 0xFF070710);
		long time = System.currentTimeMillis();
		// Subtle violet nebula bands
		for (int band = 0; band < 2; band++) {
			int bandH = this.height / 4;
			float phase = ((time % 56000L) / 56000.0F) + band * 0.5F;
			int by = (int) ((phase % 1.0F) * (this.height + bandH)) - bandH;
			int color = (accentRgb() & 0x00FFFFFF) | 0x0D000000;
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
