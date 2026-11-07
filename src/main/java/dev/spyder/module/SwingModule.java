package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;

/**
 * Swing - dozens of hand swing animation styles.
 * Real implementation comes with the swing mixin (next push).
 */
public class SwingModule extends Module {
	public final CycleSetting style;
	public final RangeSetting speed;
	public final ToggleSetting smooth;

	public SwingModule() {
		super("Swing", "Custom hand swing animations");
		this.style = cycle("style", "Style", new String[] {
				"Classic", "Chop", "Slash", "Spin", "Wave", "Flick", "Twist", "Ninja",
				"Smooth", "Fortnite", "Cursed", "Overhead", "Underhand", "Frenzy",
				"Slow-Mo", "Snappy", "Wobble", "Helix", "Punch", "Stab", "Swipe",
				"Whirl", "Bounce", "Elastic", "Noodle", "Hammer", "Boxing", "Swordmaster",
				"Flail", "Glitch", "Vortex", "Magnet"
		}, 0);
		this.speed = range("speed", "Speed", 0.5f, 2.0f, 0.1f, 1.0f);
		this.smooth = toggleSetting("smooth", "Smooth", true);
	}

	@Override
	protected void onToggle() {
		// The swing mixin reads these settings every frame; nothing to do here.
	}
}
