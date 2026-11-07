package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * Ambient - custom fog, time of day, and star density.
 * Time control is client-side only: it changes what YOU see, not the server.
 * The time setting is REAL: toggling this module on with a time mode
 * selected instantly freezes the sky at that time of day.
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

		ClientTickEvents.END_CLIENT_TICK.register(this::applyTime);
	}

	/** Real client-side time freeze. Same API used by working 1.21.11 clients. */
	private void applyTime(Minecraft client) {
		if (!isEnabled() || client.level == null) {
			return;
		}
		long target = switch (timeOfDay.value()) {
			case "Sunrise" -> 23000L;
			case "Noon" -> 6000L;
			case "Sunset" -> 12500L;
			case "Midnight" -> 18000L;
			case "Custom" -> (long) (starDensity.value() * 24000.0f);
			default -> -1L;
		};
		if (target < 0L) {
			return; // Vanilla mode: let the server rule the clock
		}
		if (client.level.getDayTime() % 24000L != target % 24000L) {
			client.level.getLevelData().setDayTime(target);
			client.level.environmentAttributes().invalidateTickCache();
		}
	}
}
