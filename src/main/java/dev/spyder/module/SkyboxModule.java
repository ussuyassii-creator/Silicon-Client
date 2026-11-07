package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Skybox ENGINE v2 - real, visible, version-safe.
 * getSkyColor is gone in 1.21.11, so this engine delivers:
 * - Shooting stars: bright streaks racing across the night sky
 * - Meteors: long multi-particle comets, style-colored
 * - Sky style changes the meteor particle + behavior
 * All via the stable addParticle API - zero mixin risk.
 */
public class SkyboxModule extends Module {
	public final CycleSetting skyStyle;
	public final ToggleSetting shootingStars;
	public final RangeSetting meteorRate;
	public final RangeSetting meteorLength;

	public SkyboxModule() {
		super("Skybox", "Shooting stars & meteors in your sky");
		this.skyStyle = cycle("sky", "Sky style", new String[] {
				"Vanilla", "Realistic", "Animated", "Doodle", "Winter", "Nebula"
		}, 0);
		this.shootingStars = toggleSetting("shooting", "Shooting stars", true);
		this.meteorRate = range("meteors", "Meteor rate", 0f, 1f, 0.1f, 0.4f);
		this.meteorLength = range("streak", "Streak length", 0f, 1f, 0.1f, 0.5f);

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		if (!isEnabled() || client.level == null || client.player == null) {
			return;
		}
		if (!shootingStars.value()) {
			return;
		}
		// Spawn chance scales with the rate slider (per tick, 20 ticks/sec)
		if (client.level.random.nextFloat() < meteorRate.value() * 0.15f) {
			spawnMeteor(client);
		}
	}

	private void spawnMeteor(Minecraft client) {
		double px = client.player.getX();
		double pz = client.player.getZ();

		// Start high up, random horizontal offset around the player
		double x = px + (client.level.random.nextDouble() - 0.5) * 80.0;
		double z = pz + (client.level.random.nextDouble() - 0.5) * 80.0;
		double y = client.player.getY() + 30.0 + client.level.random.nextDouble() * 15.0;

		// Diagonal downward velocity
		double dx = (client.level.random.nextDouble() - 0.5) * 1.2;
		double dz = (client.level.random.nextDouble() - 0.5) * 1.2;
		double dy = -0.35;

		int streak = Math.max(2, (int) (meteorLength.value() * 14.0f));

		for (int i = 0; i < streak; i++) {
			double sx = x - dx * i * 1.4;
			double sy = y - dy * i * 1.4;
			double sz = z - dz * i * 1.4;
			client.level.addParticle(particleFor(skyStyle.value(), i),
					sx, sy, sz, dx, dy, dz);
		}
	}

	private net.minecraft.core.particles.ParticleOptions particleFor(String style, int i) {
		return switch (style) {
			case "Realistic" -> ParticleTypes.END_ROD;
			case "Animated" -> ParticleTypes.FIREWORK;
			case "Doodle" -> ParticleTypes.HAPPY_VILLAGER;
			case "Winter" -> ParticleTypes.SNOWFLAKE;
			case "Nebula" -> i % 3 == 0 ? ParticleTypes.PORTAL : ParticleTypes.END_ROD;
			default -> i % 4 == 0 ? ParticleTypes.FIREWORK : ParticleTypes.END_ROD;
		};
	}
}
