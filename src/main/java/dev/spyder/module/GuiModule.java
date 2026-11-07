package dev.spyder.module;

import dev.spyder.core.GuiTheme;
import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;

/**
 * GUI Editor - customize the look of the client GUI itself:
 * client name, background style (incl. cosmic), glass-window
 * transparency, and star density. Color combos live in the GUI's
 * SETTINGS section as preset cards (7 palettes, one tap to apply).
 */
public class GuiModule extends Module {
	public final CycleSetting clientName;
	public final CycleSetting background;
	public final RangeSetting opacity;
	public final RangeSetting cosmicIntensity;
	public final ToggleSetting showStars;

	public GuiModule() {
		super("GUI Editor", "Customize the client GUI look");
		this.clientName = cycle("name", "Client name", new String[] {
				"SPYDER", "SILICON"
		}, 0);
		this.background = cycle("background", "Background", new String[] {
				"None", "Dark", "Blur", "Cosmic"
		}, 3);
		this.opacity = range("opacity", "Glass opacity", 0.4f, 1.0f, 0.05f, 0.95f);
		this.cosmicIntensity = range("cosmic", "Cosmic intensity", 0f, 1f, 0.1f, 1.0f);
		this.showStars = toggleSetting("stars", "Show stars", true);
		syncToTheme();
	}

	@Override
	protected void onToggle() {
	}

	@Override
	public void onTick() {
		// Live-sync while enabled: the panel turns frosted/solid the instant
		// the slider moves, no extra clicks needed.
		if (isEnabled()) {
			syncToTheme();
		}
	}

	/**
	 * Push the module settings into the static theme engine so the ClickGUI
	 * and HUD pick them up instantly, with zero per-frame lookups.
	 * Accent colors are NOT set here - they come from the SETTINGS section
	 * preset cards so the two never fight.
	 */
	public void syncToTheme() {
		String name = clientName.value();
		GuiTheme.clientName = name.equals("SILICON") ? "SILICON" : "SPYDER";
		switch (background.value()) {
			case "None" -> GuiTheme.background = GuiTheme.Background.NONE;
			case "Dark" -> GuiTheme.background = GuiTheme.Background.DARK;
			case "Blur" -> GuiTheme.background = GuiTheme.Background.BLUR;
			default -> GuiTheme.background = GuiTheme.Background.COSMIC;
		}
		GuiTheme.panelOpacity = opacity.value();
		GuiTheme.cosmicIntensity = cosmicIntensity.value();
		GuiTheme.showStars = showStars.value();
	}
}
