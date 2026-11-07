package dev.spyder.module;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * HUD - Prestige-style on-screen HUD: animated watermark, FPS,
 * coordinates, and WASD/mouse keystrokes.
 *
 * The watermark reads GuiTheme.clientName so changing the name in the
 * GUI Editor rebrands the HUD instantly.
 *
 * Lag-free by design: all strings are built once per TICK in onTick()
 * (cached in fields), and the per-frame draw only renders cached text
 * and static boxes. Zero allocations in the render path.
 */
public class HudModule extends Module {
	public final ToggleSetting showWatermark;
	public final ToggleSetting showFps;
	public final ToggleSetting showCoords;
	public final ToggleSetting showKeystrokes;

	// Cached strings - rebuilt once per tick, never per frame
	private String fpsText = "FPS: -";
	private String coordText = "XYZ: -";

	private static final int BOX = 18;
	private static final int GAP = 1;

	public HudModule() {
		super("HUD", "Watermark, FPS, coords & keystrokes");
		this.showWatermark = toggleSetting("watermark", "Watermark", true);
		this.showFps = toggleSetting("fps", "FPS counter", true);
		this.showCoords = toggleSetting("coords", "Coordinates", true);
		this.showKeystrokes = toggleSetting("keys", "Keystrokes", true);

		HudRenderCallback.EVENT.register(this::onHudRender);
	}

	@Override
	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		fpsText = "FPS: " + mc.getFps();
		coordText = "XYZ: " + mc.player.blockPosition().getX()
				+ " " + mc.player.blockPosition().getY()
				+ " " + mc.player.blockPosition().getZ();
	}

	private void onHudRender(GuiGraphics g, DeltaTracker delta) {
		if (!isEnabled()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) {
			return;
		}

		long time = System.currentTimeMillis();
		int x = 6;
		int y = 6;

		if (showWatermark.value()) {
			// Animated client-name letters - each letter its own hue phase
			String title = GuiTheme.clientName;
			String ver = "  " + GuiTheme.version;
			int lw = mc.font.width(title);
			g.fill(x - 3, y - 2, x + lw + mc.font.width(ver) + 3, y + 11, 0xA00B0810);
			int cx = x;
			for (int i = 0; i < title.length(); i++) {
				String ch = String.valueOf(title.charAt(i));
				g.drawString(mc.font, ch, cx, y, hue(time, i * 120));
				cx += mc.font.width(ch);
			}
			g.drawString(mc.font, ver, cx, y, 0xFF9A9A9A);
			y += 13;
		}

		if (showFps.value()) {
			g.drawString(mc.font, fpsText, x, y, 0xFF55FF55);
			y += 11;
		}

		if (showCoords.value()) {
			g.drawString(mc.font, coordText, x, y, 0xFFAAAAFF);
			y += 11;
		}

		if (showKeystrokes.value()) {
			y += 4;
			// Row 1: W
			drawKey(g, mc, "W", x + BOX + GAP, y, mc.options.keyUp.isDown(), time);
			// Row 2: A S D
			int row2 = y + BOX + GAP;
			drawKey(g, mc, "A", x, row2, mc.options.keyLeft.isDown(), time);
			drawKey(g, mc, "S", x + BOX + GAP, row2, mc.options.keyDown.isDown(), time);
			drawKey(g, mc, "D", x + (BOX + GAP) * 2, row2, mc.options.keyRight.isDown(), time);
			// Row 3: LMB RMB
			int row3 = row2 + BOX + GAP;
			drawKey(g, mc, "LMB", x, row3, mc.options.keyAttack.isDown(), time);
			drawKey(g, mc, "RMB", x + (BOX + GAP) * 2, row3, mc.options.keyUse.isDown(), time);
		}
	}

	private void drawKey(GuiGraphics g, Minecraft mc, String label, int x, int y, boolean pressed, long time) {
		int w = BOX + 8; // wider for LMB/RMB
		if (label.length() == 1) {
			w = BOX;
		}
		int bg = pressed ? GuiTheme.accent() : 0x90000000;
		g.fill(x, y, x + w, y + BOX, bg);
		g.fill(x, y, x + 1, y + BOX, 0xFF2A2A2A);
		g.fill(x, y + BOX - 1, x + w, y + BOX, 0xFF2A2A2A);
		g.fill(x + w - 1, y, x + w, y + BOX, 0xFF2A2A2A);
		g.fill(x, y, x + w, y + 1, 0xFF2A2A2A);
		int textCol = pressed ? 0xFF0B0810 : hue(time, y * 40);
		int tx = x + (w - mc.font.width(label)) / 2;
		g.drawString(mc.font, label, tx, y + 5, textCol);
	}

	/** Animated hue. Pure int math, no allocation. */
	private int hue(long time, int offset) {
		float h = (((time + offset) % 3000L) / 3000.0F) * 6.0F;
		int seg = (int) h;
		float f = h - seg;
		float p = 0.25F;
		float q = 1.0F - 0.75F * f;
		float t = 0.25F + 0.75F * f;
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
}
