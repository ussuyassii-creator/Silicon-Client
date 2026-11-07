package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;

/**
 * Swing - custom hand swing animations.
 * 32 styles selectable from the settings screen, plus speed and
 * smoothness controls. All animation math lives in AnimationStyles.
 */
public class SwingModule extends Module {
	public final CycleSetting style;
	public final RangeSetting speed;
	public final ToggleSetting smooth;

	public SwingModule() {
		super("Swing", "Custom hand swing animations");
		this.style = cycle("style", "Style", new String[] {
			"Classic", "Chop", "Slash", "Spin", "Wave", "Flick", "Snappy",
			"Twist", "Helix", "Ninja", "Smooth", "Slow-Mo", "Fortnite", "Cursed",
			"Overhead", "Underhand", "Frenzy", "Wobble", "Punch", "Boxing",
			"Stab", "Swipe", "Whirl", "Bounce", "Elastic", "Noodle", "Hammer",
			"Swordmaster", "Flail", "Glitch", "Vortex", "Magnet"
		}, 12);
		this.speed = range("speed", "Speed", 0.25f, 2.0f, 0.25f, 1.0f);
		this.smooth = toggleSetting("smooth", "Smooth", true);
	}
}
