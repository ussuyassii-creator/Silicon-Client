package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;

/**
 * Skybox - full control over the sky: sun, moon, stars, meteors.
 * Presets cover the requested looks; custom colors follow in the editor.
 */
public class SkyboxModule extends Module {
	public final CycleSetting skyStyle;
	public final ToggleSetting customSun;
	public final ToggleSetting shootingStars;
	public final RangeSetting meteorRate;

	public SkyboxModule() {
		super("Skybox", "Custom sky, sun, moon, stars, meteors");
		this.skyStyle = cycle("sky", "Sky style", new String[] {
				"Vanilla", "Realistic", "Animated", "Doodle", "Winter", "Nebula"
		}, 0);
		this.customSun = toggleSetting("customsun", "Custom sun & moon", true);
		this.shootingStars = toggleSetting("shooting", "Shooting stars", true);
		this.meteorRate = range("meteors", "Meteor rate", 0f, 1f, 0.1f, 0.3f);
	}
}
