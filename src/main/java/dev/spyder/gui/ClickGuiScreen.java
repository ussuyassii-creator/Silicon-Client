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
 * - Semi-transparent dark panel with border glow
 * - Draggable by the header (click-and-drag on desktop, or tap header on mobile)
 * - Tap a module row to toggle, tap the style chip to cycle swing styles
 */
public class ClickGuiScreen extends Screen {
	private static final int PANEL_WIDTH = 220;
	private static final int ROW_HEIGHT = 22;
	private static final int HEADER_HEIGHT = 30;

	private double panelX = 40;
	private double panelY = 40;

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
		int panelH = HEADER_HEIGHT + 14 + ModuleManager.modules().size() * ROW_HEIGHT + 36;

		// Dim the world behind the menu
		g.fill(0, 0, width, height, 0x66000000);

		int px = (int) panelX;
		int py = (int) panelY;

		// Panel background
		g.fill(px, py, px + PANEL_WIDTH, py + panelH, 0xE6120507);
		// Border
		int border = rainbow(time, 0).getRGB();
		g.fill(px, py, px + PANEL_WIDTH, py + 1, border);
		g.fill(px, py + panelH - 1, px + PANEL_WIDTH, py + panelH, border);
		g.fill(px, py, px + 1, py + panelH, border);
		g.fill(px + PANEL_WIDTH - 1, py, px + PANEL_WIDTH, py + panelH, border);

		// Header text
		g.drawString(font, "SpyderClient", px + 10, py + 11, 0xFFFFFFFF);
		// Animated rainbow accent under the title
		for (int i = 0; i < PANEL_WIDTH - 20; i++) {
			int col = rainbow(time, i).getRGB();
			g.fill(px + 10 + i, py + HEADER_HEIGHT - 6, px + 11 + i, py + HEADER_HEIGHT - 2, col);
		}

		// Module rows
		int y = py + HEADER_HEIGHT + 14;
		for (Module module : ModuleManager.modules()) {
			boolean hovered = isHovered(mouseX, mouseY, px, y);
			int rowBg = hovered ? 0x40FFFFFF : 0x14000000;
			g.fill(px + 6, y, px + PANEL_WIDTH - 6, y + ROW_HEIGHT - 4, rowBg);

			String state = module.isEnabled() ? "ON" : "OFF";
			int stateColor = module.isEnabled() ? 0xFF4BE04B : 0xFFD04B4B;
			g.drawString(font, module.getName(), px + 14, y + 7, 0xFFFFFFFF);
			g.drawString(font, state, px + PANEL_WIDTH - 38, y + 7, stateColor);

			// Swing style chip (drawn only for the Swing module)
			if (module instanceof SwingModule swing) {
				String chip = "‹ " + swing.getStyle().getLabel() + " ›";
				int chipW = font.width(chip);
				boolean chipHover = isHovered(mouseX, mouseY, px + PANEL_WIDTH - 60 - chipW, y, chipW, ROW_HEIGHT - 4);
				int chipBg = chipHover ? 0x66FFFFFF : 0x33FFFFFF;
				g.fill(px + PANEL_WIDTH - 46 - chipW, y + 4, px + PANEL_WIDTH - 42, y + ROW_HEIGHT - 8, chipBg);
				g.drawString(font, chip, px + PANEL_WIDTH - 44 - chipW, y + 7, rainbow(time, 200).getRGB());
			}

			y += ROW_HEIGHT;
		}

		// Footer hint
		String hint = "tap row = toggle • tap ‹› = style • drag header = move";
		g.drawString(font, hint, px + 10, y + 6, 0xFF9A9A9A);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		int px = (int) panelX;
		int y = (int) panelY + HEADER_HEIGHT + 14;
		for (Module module : ModuleManager.modules()) {
			if (module instanceof SwingModule swing) {
				String chip = "‹ " + swing.getStyle().getLabel() + " ›";
				int chipW = font.width(chip);
				// Style chip hitbox
				if (isHovered(mx, my, px + PANEL_WIDTH - 60 - chipW, y, chipW, ROW_HEIGHT - 4)) {
					swing.cycleStyle();
					announce("Swing style: " + swing.getStyle().getLabel());
					return true;
	(PLACEHOLDER_MARK2)
			}
			}
			// Row hitbox = toggle
			if (isHovered(mx, my, px + 6, y, PANEL_WIDTH - 12, ROW_HEIGHT - 4)) {
				module.toggle();
				announce(module.getName() + (module.isEnabled() ? " ON" : " OFF"));
				return true;
			}
			y += ROW_HEIGHT;
		}
		// Header drag
		if (isHovered(mx, my, px, panelY, PANEL_WIDTH, HEADER_HEIGHT)) {
			dragging = true;
			dragOffX = mx - panelX;
			dragOffY = my - panelY;
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	private boolean dragging = false;
	private double dragOffX, dragOffY;

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging) {
			panelX = mx - dragOffX;
			panelY = my - dragOffY;
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
					Component.literal("§a§lSpyderClient §7» §f" + message), false);
		}
	}

	private boolean isHovered(double mx, double my, double x, double y) {
		return mx >= x && mx < x + PANEL_WIDTH && my >= y && my < y + ROW_HEIGHT;
	}

	private boolean isHovered(double mx, double my, double x, double y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	/** Animated rainbow: hue cycles over ~4 seconds. */
	private java.awt.Color rainbow(long time, int offset) {
		float hue = ((time % 4000L) / 4000.0F + offset / 500.0F) % 1.0F;
		return java.awt.Color.getHSBColor(hue, 0.8F, 1.0F);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
