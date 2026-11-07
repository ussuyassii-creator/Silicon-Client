package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;

/**
 * Ambient - custom fog, time of day, and star density.
 * Time control is client-side only: it changes what YOU see, not the server.
 */
public class AmbientModule extends Module {
	public final CycleSetting timeOfDay;
	public final RangeSetting starDensity;
	public final RangeSetting fogStrength;
	public final ToggleSetting customFog;

	public AmbientModule() {
		super("Ambient", "Custom fog, time of day, and stars");
		this.timeOfDay = cycle("time", "Time", new String[] {
				"Vanilla", "Sunrise", "Noon", "Sunset", "Midnight", "Custom"
		}, 0);
		this.starDensity = range("stars", "Star density", 0f, 1f, 0.1f, 0.5f);
		this.fogStrength = range("fog", "Fog strength", 0f, 1f, 0.1f, 0.3f);
		this.customFog = toggleSetting("customfog", "Custom fog", false);
	}
}
