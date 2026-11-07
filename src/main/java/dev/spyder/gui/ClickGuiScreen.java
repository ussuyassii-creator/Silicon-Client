package dev.spyder.gui;

import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import dev.spyder.module.SwingModule;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * SpyderClient ClickGUI v2 - custom-drawn, animated, colorful.
 * No vanilla widgets: everything is drawn by hand each frame.
 * - Animated rainbow accent bar
 * - Semi-transparent dark panel with animated border
 * - Draggable by the header
 * - Tap a module row to toggle, tap the style chip to cycle swing styles
 */
public class ClickGuiScreen extends Screen {
	private static final int PANEL_WIDTH = 220;
	private static final int ROW_HEIGHT = 22;
	private static final int HEADER_HEIGHT = 30;

	private double panelX = 40;
	private double panelY = 40;

	private boolean dragging = false;
	private double dragOffX;
	private double dragOffY;

	public ClickGuiScreen() {
		super(Component.literal("SpyderClient"));
	}

	@Override
	protected void init() {
		// Keep the panel on-screen if the window is small (phones).
		if (panelX + PANEL_WIDTH > width) panelX = Math.max(0, width - PANEL_WIDTH);
		if (panelY + HEADER_HEIGHT > height) panelY = 0;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);

		long time = Util.getMillis();
		int moduleCount = ModuleManager.modules().size();
		int panelH = HEADER_HEIGHT + 14 + moduleCount * ROW_HEIGHT + 36;

		// Dim the world behind the menu
		g.fill(0, 0, width, height, 0x66000000);

		int px = (int) panelX;
		int py = (int) panelY;

		// Panel background
		g.fill(px, py, px + PANEL_WIDTH, py + panelH, 0xE6120507);
		// Animated border
		int border = rgb(time, 0);
		g.fill(px, py, px + PANEL_WIDTH, py + 1, border);
		g.fill(px, py + panelH - 1, px + PANEL_WIDTH, py + panelH, border);
		g.fill(px, py, px + 1, py + panelH, border);
		g.fill(px + PANEL_WIDTH - 1, py, px + PANEL_WIDTH, py + panelH, border);

		// Header text + animated rainbow accent under the title
		g.drawString(font, "SpyderClient", px + 10, py + 9, 0xFFFFFFFF);
		g.drawString(font, moduleCount + " mods", px + PANEL_WIDTH - 52, py + 9, 0xFF9A9A9A);
		for (int i = 0; i < PANEL_WIDTH - 20; i++) {
			g.fill(px + 10 + i, py + HEADER_HEIGHT - 6, px + 11 + i, py + HEADER_HEIGHT - 2, rgb(time, i));
		}

		// Module rows
		int y = py + HEADER_HEIGHT + 14;
		for (Module module : ModuleManager.modules()) {
			boolean hovered = inRect(mouseX, mouseY, px + 6, y, PANEL_WIDTH - 12, ROW_HEIGHT - 4);
			g.fill(px + 6, y, px + PANEL_WIDTH - 6, y + ROW_HEIGHT - 4,
					hovered ? 0x40FFFFFF : 0x14000000);

			g.drawString(font, module.getName(), px + 14, y + 7, 0xFFFFFFFF);
			String state = module.isEnabled() ? "ON" : "OFF";
			int stateColor = module.isEnabled() ? 0xFF4BE04B : 0xFFD04B4B;
			g.drawString(font, state, px + PANEL_WIDTH - 36, y + 7, stateColor);

			// Swing style chip
			if (module instanceof SwingModule swing) {
				String chip = "< " + swing.getStyle().getLabel() + " >";
				int chipW = font.width(chip);
				int chipX = px + PANEL_WIDTH - 48 - chipW;
				boolean chipHover = inRect(mouseX, mouseY, chipX, y + 4, chipW + 6, ROW_HEIGHT - 12);
				g.fill(chipX - 3, y + 4, chipX + chipW + 3, y + ROW_HEIGHT - 8,
						chipHover ? 0x66FFFFFF : 0x33FFFFFF);
				g.drawString(font, chip, chipX, y + 7, rgb(time, 200));
			}

			y += ROW_HEIGHT;
		}

		// Footer hint
		g.drawString(font, "tap row = toggle  •  tap <> = style  •  drag top = move",
				px + 10, y + 6, 0xFF9A9A9A);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		int px = (int) panelX;
		int y = (int) panelY + HEADER_HEIGHT + 14;
		for (Module module : ModuleManager.modules()) {
			if (module instanceof SwingModule swing) {
				String chip = "< " + swing.getStyle().getLabel() + " >";
				int chipW = font.width(chip);
				int chipX = px + PANEL_WIDTH - 48 - chipW;
				if (inRect(mx, my, chipX - 3, y + 4, chipW + 6, ROW_HEIGHT - 12)) {
					swing.cycleStyle();
					announce("Swing style: " + swing.getStyle().getLabel());
					return true;
				}
			}
			if (inRect(mx, my, px + 6, y, PANEL_WIDTH - 12, ROW_HEIGHT - 4)) {
				module.toggle();
				announce(module.getName() + (module.isEnabled() ? " ON" : " OFF"));
				return true;
			}
			y += ROW_HEIGHT;
		}
		// Header drag
		if (inRect(mx, my, px, panelY, PANEL_WIDTH, HEADER_HEIGHT)) {
			dragging = true;
			dragOffX = mx - panelX;
			dragOffY = my - panelY;
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging) {
			panelX = mx - dragOffX;
			panelY = my - dragOffY;
			panelX = Math.max(0, Math.min(panelX, width - PANEL_WIDTH));
			panelY = Math.max(0, Math.min(panelY, height - HEADER_HEIGHT));
			return true;
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		dragging = false;
		return super.mouseReleased(mx, my, button);
	}

	private void announce(String message) {
		if (minecraft != null && minecraft.player != null) {
			minecraft.player.displayClientMessage(
					Component.literal("\u00a7a\u00a7lSpyderClient \u00a77\u00bb \u00a7f" + message), false);
		}
	}

	private boolean inRect(double mx, double my, double x, double y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	/** Animated rainbow color: hue cycles over ~4 seconds. */
	private int rgb(long time, int offset) {
		float hue = (((time + offset * 8L) % 4000L) / 4000.0F);
		float sat = 0.8F;
		float val = 1.0F;
		int h = (int) (hue * 6.0F);
		float f = hue * 6.0F - h;
		float p = val * (1.0F - sat);
		float q = val * (1.0F - sat * f);
		float t = val * (1.0F - sat * (1.0F - f));
		float r;
		float gr;
		float b;
		switch (h % 6) {
			case 0 -> { r = val; gr = t; b = p; }
			case 1 -> { r = q; gr = val; b = p; }
			case 2 -> { r = p; gr = val; b = t; }
			case 3 -> { r = p; gr = q; b = val; }
			case 4 -> { r = t; gr = p; b = val; }
			default -> { r = val; gr = p; b = q; }
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
