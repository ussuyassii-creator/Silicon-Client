package dev.spyder.gui;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings;
import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * Cosmetics Editor - live 3D preview of YOUR player model with your real skin,
 * draggable to rotate (auto-spins gently otherwise), with all Cosmetics
 * module settings (cape, back item, aura, fist FX, scale) right beside it.
 *
 * Uses the verified 1.21.11 path: entityRenderDispatcher.getRenderer(player)
 * -> createRenderState -> g.submitEntityRenderState(...). Same recipe as
 * working 1.21.11 cosmetics screens.
 */
public class CosmeticsScreen extends Screen {
	private static final int PREVIEW_W = 150;
	private static final int PREVIEW_H = 190;
	private static final int ROW_H = 22;

	private final Minecraft mc = Minecraft.getInstance();

	private float manualRotY = 0f;
	private float manualRotX = -6f;
	private boolean draggingPreview = false;
	private double lastDragX;
	private double lastDragY;

	public CosmeticsScreen() {
		super(Component.literal("Cosmetics"));
	}

	private Module cosmetics() {
		return ModuleManager.byName("Cosmetics");
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		// Dim background over the world (GUI stays see-through to the game)
		g.fill(0, 0, this.width, this.height, 0x88050510);

		long time = System.currentTimeMillis();
		int px = (this.width - 470) / 2;
		int py = (this.height - PREVIEW_H - 60) / 2;
		int panelW = 470;
		int panelH = PREVIEW_H + 60;
		int accent = GuiTheme.accent();

		// Panel with shadow + rounded body (same style as ClickGUI v5)
		rrect(g, px + 4, py + 5, panelW, panelH, 0x55000000);
		rrect(g, px, py, panelW, panelH, 0xFF12121B);
		rrect(g, px + 1, py + 1, panelW - 2, panelH - 2, 0xF70C0A13);
		g.fill(px + 4, py, px + panelW - 4, py + 2, accent);
		g.fill(px + 2, py + 1, px + 4, py + 2, accent);
		g.fill(px + panelW - 4, py + 1, px + panelW - 2, py + 2, accent);

		// Title
		String title = GuiTheme.clientName + " COSMETICS";
		int tx = px + 10;
		for (int i = 0; i < title.length(); i++) {
			String ch = String.valueOf(title.charAt(i));
			int c = hue(time, i * 100);
			g.drawString(this.font, ch, tx + 1, py + 12, 0x66000000);
			g.drawString(this.font, ch, tx, py + 11, c);
			tx += this.font.width(ch);
		}
		// Close
		g.drawString(this.font, "x", px + panelW - 14, py + 11, 0xFFAA6666);

		// Preview card (left)
		int pvx = px + 10;
		int pvy = py + 26;
		rrect(g, pvx, pvy, PREVIEW_W, PREVIEW_H, 0x22000000);
		rrect(g, pvx + 1, pvy + 1, PREVIEW_W - 2, PREVIEW_H - 2, 0xFF16121E);
		// Glow ring accent
		glowRing(g, pvx, pvy, PREVIEW_W, PREVIEW_H);
		// Draw the player!
		drawPlayerPreview(g, pvx, pvy, PREVIEW_W, PREVIEW_H, time);

		// Settings rows (right column)
		Module m = cosmetics();
		int rx = pvx + PREVIEW_W + 14;
		int rw = px + panelW - rx - 10;
		int ry = pvy;
		int row = 0;
		if (m != null) {
			for (SpyderSettings.Setting s : m.settings()) {
				drawSettingRow(g, s, rx, ry + row * ROW_H, rw, time);
				row++;
			}
		}
		// Footer hint
		g.drawString(this.font, "drag the preview to rotate   x / Right Shift = close", px + 10, py + panelH - 16, 0xFF6E6E76);
	}

	/** The live 3D player model with your skin. Drag to rotate. */
	private void drawPlayerPreview(GuiGraphics g, int x, int y, int w, int h, long time) {
		if (this.mc.player == null) {
			g.drawString(this.font, "join a world first", x + 8, y + h / 2 - 4, 0xFF6E6E76);
			return;
		}
		float autoSpin = (time % 20000L) / 20000.0f * 360.0f;
		float spin = manualRotY + autoSpin;

		var renderer = this.mc.entityRenderDispatcher.getRenderer(this.mc.player);
		var state = renderer.createRenderState(this.mc.player, 1f);
		if (!(state instanceof LivingEntityRenderState live)) {
			g.drawString(this.font, "preview unavailable", x + 8, y + h / 2 - 4, 0xFF6E6E76);
			return;
		}
		live.bodyRot = 180f + spin;
		live.yRot = 0f;
		live.xRot = 0f;
		live.pose = Pose.STANDING;
		live.lightCoords = 0xF000F0;
		live.isDiscrete = false;
		live.distanceToCameraSq = 0.0;
		live.nameTag = null;

		// Player name above the model, drawn as normal text (fixed size)
		Component label = this.mc.player.getDisplayName();
		int lw = this.font.width(label);
		g.drawString(this.font, label, x + (w - lw) / 2, y + 5, 0xFFFFFFFF);

		// Submit to the deferred entity pipeline (1.21.11 picture-in-picture path)
		var tilt = new Quaternionf().rotateX((float) Math.toRadians(manualRotX));
		var pose = new Quaternionf().rotateZ((float) Math.PI).mul(tilt);
		float scale = h / 3.1f;
		g.submitEntityRenderState(
				state, scale,
				new Vector3f(0f, this.mc.player.getBbHeight() / 2f + 0.3f, 0f),
				pose, tilt,
				x + 4, y + 14, x + w - 4, y + h - 4
		);
	}

	private void drawSettingRow(GuiGraphics g, SpyderSettings.Setting s, int x, int y, int w, long time) {
		if (s instanceof SpyderSettings.CycleSetting cyc) {
			g.drawString(this.font, cyc.display(), x, y + 7, 0xFFB0B0BC);
			String chip = "< " + cyc.value() + " >";
			int chipW = this.font.width(chip);
			rrect(g, x + w - chipW - 16, y + 3, chipW + 12, ROW_H - 6, 0x24FFFFFF);
			g.drawString(this.font, chip, x + w - chipW - 10, y + 7, hue(time, y * 16));
		} else if (s instanceof SpyderSettings.ToggleSetting tog) {
			g.drawString(this.font, tog.display(), x, y + 7, 0xFFB0B0BC);
			g.drawString(this.font, tog.value() ? "ON" : "OFF", x + w - 20, y + 7,
					tog.value() ? GuiTheme.accent() : 0xFF7A2A2A);
		} else if (s instanceof SpyderSettings.RangeSetting rng) {
			g.drawString(this.font, rng.display(), x, y + 7, 0xFFB0B0BC);
			String val = String.format("%.1f", rng.value());
			g.drawString(this.font, "-", x + w - 44, y + 7, 0xFFDDDDDD);
			g.drawString(this.font, val, x + w - 36, y + 7, hue(time, y * 16));
			g.drawString(this.font, "+", x + w - 14, y + 7, 0xFFDDDDDD);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		int px = (this.width - 470) / 2;
		int py = (this.height - PREVIEW_H - 60) / 2;
		int pvx = px + 10;
		int pvy = py + 26;
		int x1 = px + 470;

		// Close button
		if (in(mx, my, x1 - 24, py + 4, 20, 20)) {
			onClose();
			return true;
		}

		// Preview drag start
		if (in(mx, my, pvx, pvy, PREVIEW_W, PREVIEW_H)) {
			draggingPreview = true;
			lastDragX = mx;
			lastDragY = my;
			return true;
		}

		// Setting rows
		Module m = cosmetics();
		if (m != null) {
			int rx = pvx + PREVIEW_W + 14;
			int rw = x1 - rx - 10;
			int row = 0;
			for (SpyderSettings.Setting s : m.settings()) {
				if (in(mx, my, rx, py + 26 + row * ROW_H, rw, ROW_H - 2)) {
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
					return true;
				}
				row++;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (draggingPreview) {
			manualRotY += (float) dx;
			manualRotX = Math.max(-60f, Math.min(60f, manualRotX - (float) dy));
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		draggingPreview = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT
				|| event.key() == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	private boolean in(double mx, double my, double x, double y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
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

	/** Soft accent glow ring around the preview card. */
	private void glowRing(GuiGraphics g, int x, int y, int w, int h) {
		int rgb = (GuiTheme.accentR << 16) | (GuiTheme.accentG << 8) | GuiTheme.accentB;
		g.fill(x - 2, y - 2, x + w + 2, y + h + 2, (0x30 << 24) | rgb);
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
