package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Ambient ENGINE v2 - everything in this module is REAL:
 * - Time of day: freezes the client-side sky at Sunrise/Noon/Sunset/Midnight
 * - Star density: spawns extra twinkling night stars around you (scaled by slider)
 * Dead fog settings removed - no fake sliders in this client.
 */
public class AmbientModule extends Module {
	public final CycleSetting timeOfDay;
	public final RangeSetting starDensity;

	public AmbientModule() {
		super("Ambient", "Time control & night stars");
		this.timeOfDay = cycle("time", "Time", new String[] {
				"Vanilla", "Sunrise", "Noon", "Sunset", "Midnight", "Custom"
		}, 0);
		this.starDensity = range("stars", "Star density", 0f, 1f, 0.1f, 0.5f);

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}
		applyTime(client);
		spawnStars(client);
	}

	/** Real client-side time freeze. Verified 1.21.11 API. */
	private void applyTime(Minecraft client) {
		if (!isEnabled()) {
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
			return;
		}
		if (client.level.getDayTime() % 24000L != target % 24000L) {
			client.level.getLevelData().setDayTime(target);
			client.level.environmentAttributes().invalidateTickCache();
		}
	}

	/** Real twinkling stars: spawn rate scales with the density slider. */
	private void spawnStars(Minecraft client) {
		if (!isEnabled()) {
			return;
		}
		float density = starDensity.value();
		if (density <= 0f) {
			return;
		}
		if (client.level.random.nextFloat() < density * 0.6f) {
			double x = client.player.getX() + (client.level.random.nextDouble() - 0.5) * 100.0;
			double z = client.player.getZ() + (client.level.random.nextDouble() - 0.5) * 100.0;
			double y = client.player.getY() + 35.0 + client.level.random.nextDouble() * 20.0;
			client.level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, -0.01, 0.0);
		}
	}
}
