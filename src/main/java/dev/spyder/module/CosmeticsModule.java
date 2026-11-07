package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Cosmetics ENGINE v2 - real, visible particle cosmetics:
 * - Auras: fire ring orbit, rising spirit flames, serpentine orbit dragon
 * - Back: flapping particle wings (angel cloud / devil flame / broken)
 * - Cape: colored particle trail behind you while moving
 * - Fist FX: burst at your fist on attack press
 * Scale slider multiplies particle counts. All via stable addParticle API.
 */
public class CosmeticsModule extends Module {
	public final CycleSetting cape;
	public final CycleSetting back;
	public final CycleSetting aura;
	public final CycleSetting fistFx;
	public final RangeSetting scale;

	public CosmeticsModule() {
		super("Cosmetics", "Particle auras, wings, capes, fist FX");
		this.cape = cycle("cape", "Cape trail", new String[] {
				"None", "Fire", "Water", "Wind", "Void", "Galaxy", "Blood"
		}, 1);
		this.back = cycle("back", "Back item", new String[] {
				"None", "Angel wings", "Devil wings", "Broken devil wings"
		}, 1);
		this.aura = cycle("aura", "Aura", new String[] {
				"None", "Orbit dragon", "Fire ring", "Spirit"
		}, 2);
		this.fistFx = cycle("fistfx", "Fist FX", new String[] {
				"None", "Fire", "Blood drop", "Water drop", "Wind spin"
		}, 1);
		this.scale = range("scale", "Scale", 0.5f, 2.0f, 0.1f, 1.0f);

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		if (!isEnabled() || client.level == null || client.player == null) {
			return;
		}
		var player = client.player;
		float s = scale.value();
		int t = player.tickCount;
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();

		// ---- AURA ----
		switch (aura.value()) {
			case "Fire ring" -> {
				int n = (int) (10 * s);
				for (int i = 0; i < n; i++) {
					double ang = t * 0.25 + (Math.PI * 2.0 * i) / n;
					double r = 0.9;
					client.level.addParticle(ParticleTypes.FLAME,
							x + Math.cos(ang) * r, y + 1.0 + Math.sin(t * 0.1 + i) * 0.08, z + Math.sin(ang) * r,
							0.0, 0.01, 0.0);
				}
			}
			case "Spirit" -> {
				int n = (int) (2 * s);
				for (int i = 0; i < n; i++) {
					client.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
							x + (client.level.random.nextDouble() - 0.5),
							y + 0.2 + client.level.random.nextDouble() * 1.5,
							z + (client.level.random.nextDouble() - 0.5),
							0.0, 0.06, 0.0);
				}
			}
			case "Orbit dragon" -> {
				int n = (int) (14 * s);
				for (int i = 0; i < n; i++) {
					double ang = t * 0.18 - i * 0.45;
					double r = 1.1 + Math.sin(t * 0.08 + i * 0.3) * 0.25;
					double dy = Math.sin(t * 0.12 + i * 0.5) * 0.8;
					client.level.addParticle(ParticleTypes.END_ROD,
							x + Math.cos(ang) * r, y + 1.0 + dy, z + Math.sin(ang) * r,
							0.0, 0.0, 0.0);
				}
			}
			default -> {
			}
		}

		// ---- WINGS ----
		String wing = back.value();
		if (!wing.equals("None")) {
			boolean devil = wing.startsWith("Devil");
			boolean broken = wing.startsWith("Broken");
			ParticleOptions wp = devil ? ParticleTypes.FLAME : ParticleTypes.CLOUD;
			float flap = (float) Math.sin(t * 0.35) * 0.5f;
			int feathers = broken ? 4 : 8;
			for (int i = 0; i < feathers; i++) {
				float fi = (i + 1) / (float) feathers;
				for (int side = -1; side <= 1; side += 2) {
					double wx = x + side * (0.3 + fi * 0.9) * Math.cos(flap * 0.8);
					double wy = y + 1.2 + fi * 0.55 - flap * 0.5;
					double wz = z - 0.35 - Math.abs(side) * 0.1;
					client.level.addParticle(wp, wx, wy, wz, 0.0, 0.005, 0.0);
				}
			}
		}

		// ---- CAPE TRAIL (only while moving) ----
		var motion = player.getDeltaMovement();
		if (motion.horizontalDistanceSqr() > 0.02) {
			ParticleOptions trail = switch (cape.value()) {
				case "Fire" -> ParticleTypes.FLAME;
				case "Water" -> ParticleTypes.SPLASH;
				case "Wind" -> ParticleTypes.CLOUD;
				case "Void" -> ParticleTypes.PORTAL;
				case "Galaxy" -> ParticleTypes.FIREWORK;
				case "Blood" -> ParticleTypes.DAMAGE_INDICATOR;
				default -> null;
			};
			if (trail != null) {
				int n = (int) (3 * s);
				for (int i = 0; i < n; i++) {
					client.level.addParticle(trail,
							x - motion.x * (0.5 + i * 0.25) + (client.level.random.nextDouble() - 0.5) * 0.3,
							y + 0.8 + client.level.random.nextDouble() * 0.5,
							z - motion.z * (0.5 + i * 0.25) + (client.level.random.nextDouble() - 0.5) * 0.3,
							0.0, 0.01, 0.0);
				}
			}
		}

		// ---- FIST FX (on attack press) ----
		if (!fistFx.value().equals("None") && client.options.keyAttack.isDown()) {
			double fx = x + player.getLookAngle().x * 0.6;
			double fz = z + player.getLookAngle().z * 0.6;
			double fy = y + 1.3;
			switch (fistFx.value()) {
				case "Fire" -> {
					for (int i = 0; i < 4; i++) {
						client.level.addParticle(ParticleTypes.FLAME,
								fx + (client.level.random.nextDouble() - 0.5) * 0.4,
								fy + (client.level.random.nextDouble() - 0.5) * 0.4,
								fz + (client.level.random.nextDouble() - 0.5) * 0.4,
								0.0, 0.02, 0.0);
					}
				}
				case "Blood drop" -> client.level.addParticle(ParticleTypes.DAMAGE_INDICATOR, fx, fy, fz, 0.0, -0.02, 0.0);
				case "Water drop" -> client.level.addParticle(ParticleTypes.SPLASH, fx, fy, fz, 0.0, 0.0, 0.0);
				case "Wind spin" -> {
					for (int i = 0; i < 6; i++) {
						double ang = t * 0.5 + i;
						client.level.addParticle(ParticleTypes.CLOUD,
								fx + Math.cos(ang) * 0.4, fy, fz + Math.sin(ang) * 0.4,
								0.0, 0.0, 0.0);
					}
				}
				default -> {
				}
			}
		}
	}
}
