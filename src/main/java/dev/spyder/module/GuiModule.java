package dev.spyder.module;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;

/**
 * GUI Editor - customize the look of the client GUI itself:
 * client name, accent color, background style (incl. cosmic), and animations.
 * Changing "Client name" rebrands the GUI header AND the HUD watermark live.
 */
public class GuiModule extends Module {
	public final CycleSetting clientName;
	public final CycleSetting accentColor;
	public final CycleSetting background;
	public final RangeSetting cosmicIntensity;
	public final ToggleSetting showStars;

	public GuiModule() {
		super("GUI Editor", "Customize the client GUI look");
		this.clientName = cycle("name", "Client name", new String[] {
				"SPYDER", "SILICON"
		}, 0);
		this.accentColor = cycle("accent", "Accent color", new String[] {
				"Green", "Red", "Blue", "Purple", "Gold", "Cyan", "Orange", "White"
		}, 0);
		this.background = cycle("background", "Background", new String[] {
				"None", "Dark", "Blur", "Cosmic"
		}, 3);
		this.cosmicIntensity = range("cosmic", "Cosmic intensity", 0f, 1f, 0.1f, 1.0f);
		this.showStars = toggleSetting("stars", "Show stars", true);
		syncToTheme();
	}

	@Override
	protected void onToggle() {
	}

	/**
	 * Push the module settings into the static theme engine so the ClickGUI
	 * and HUD pick them up instantly, with zero per-frame lookups.
	 */
	public void syncToTheme() {
		String name = clientName.value();
		GuiTheme.clientName = name.equals("SILICON") ? "SILICON" : "SPYDER";
		switch (accentColor.value()) {
			case "Red" -> { GuiTheme.accentR = 0xFF; GuiTheme.accentG = 0x4C; GuiTheme.accentB = 0x4C; }
			case "Blue" -> { GuiTheme.accentR = 0x4C; GuiTheme.accentG = 0x9A; GuiTheme.accentB = 0xFF; }
			case "Purple" -> { GuiTheme.accentR = 0xB4; GuiTheme.accentG = 0x6B; GuiTheme.accentB = 0xFF; }
			case "Gold" -> { GuiTheme.accentR = 0xFF; GuiTheme.accentG = 0xD7; GuiTheme.accentB = 0x3D; }
			case "Cyan" -> { GuiTheme.accentR = 0x3D; GuiTheme.accentG = 0xE1; GuiTheme.accentB = 0xFF; }
			case "Orange" -> { GuiTheme.accentR = 0xFF; GuiTheme.accentG = 0x8C; GuiTheme.accentB = 0x3D; }
			case "White" -> { GuiTheme.accentR = 0xFF; GuiTheme.accentG = 0xFF; GuiTheme.accentB = 0xFF; }
			default -> { GuiTheme.accentR = 0x2E; GuiTheme.accentG = 0xE8; GuiTheme.accentB = 0x6C; }
		}
		switch (background.value()) {
			case "None" -> GuiTheme.background = GuiTheme.Background.NONE;
			case "Dark" -> GuiTheme.background = GuiTheme.Background.DARK;
			case "Blur" -> GuiTheme.background = GuiTheme.Background.BLUR;
			default -> GuiTheme.background = GuiTheme.Background.COSMIC;
		}
		GuiTheme.cosmicIntensity = cosmicIntensity.value();
		GuiTheme.showStars = showStars.value();
	}
}
