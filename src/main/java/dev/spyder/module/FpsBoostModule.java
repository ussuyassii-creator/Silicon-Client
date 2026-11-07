package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.GraphicsStatus;

/**
 * FPS Boost ENGINE - REAL, REVERSIBLE performance optimizations.
 * Every setting here changes an actual vanilla option via the verified
 * 1.21.11 Options API. Your original values are saved on enable and
 * fully restored on disable - nothing is lost, nothing is faked.
 *
 * Profiles:
 * - Mobile:    render 4, sim 5, entities 0.5, fast graphics, no clouds
 * - Balanced:  render 6, sim 7, entities 0.75, fast graphics, no clouds
 * - Max FPS:   render 4, sim 4, entities 0.25, fast graphics, no clouds
 * - Custom:    use the sliders below
 */
public class FpsBoostModule extends Module {
	public final CycleSetting profile;
	public final RangeSetting renderDist;
	public final RangeSetting simDist;
	public final RangeSetting entityScale;

	private int savedRender = -1;
	private int savedSim = -1;
	private double savedEntity = -1;
	private int savedClouds = -1;
	private int savedGraphics = -1;
	private boolean savedVsync = false;
	private boolean savedPause = false;
	private boolean applied = false;

	public FpsBoostModule() {
		super("FPS Boost", "Real performance boost (saves & restores settings)");
		this.profile = cycle("profile", "Profile", new String[] {
				"Mobile", "Balanced", "Max FPS", "Custom"
		}, 0);
		this.renderDist = range("render", "Render distance", 2, 12, 1, 4);
		this.simDist = range("sim", "Simulation distance", 2, 12, 1, 5);
		this.entityScale = range("entities", "Entity distance", 0.25f, 1.0f, 0.05f, 0.5f);

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	@Override
	protected void onToggle() {
		if (isEnabled()) {
			applyBoost();
		} else {
			restore();
		}
	}

	private void tick(Minecraft client) {
		if (!isEnabled() || !applied) {
			return;
		}
		// Keep pauseOnLostFocus false while boosted (tab-out = no background render)
		if (client.options.pauseOnLostFocus) {
			client.options.pauseOnLostFocus = false;
		}
		// Custom profile: live-apply slider values
		if ("Custom".equals(profile.value())) {
			client.options.renderDistance().set((int) renderDist.value());
			client.options.simulationDistance().set((int) simDist.value());
			client.options.entityDistanceScaling().set(entityScale.value());
		}
	}

	private void applyBoost() {
		Minecraft client = Minecraft.getInstance();
		if (client.options == null) {
			return;
		}
		if (!applied) {
			// Save originals (once)
			savedRender = client.options.renderDistance().get();
			savedSim = client.options.simulationDistance().get();
			savedEntity = client.options.entityDistanceScaling().get();
			savedClouds = client.options.cloudStatus().get().ordinal();
			savedGraphics = client.options.graphicsMode().get().ordinal();
			savedVsync = client.options.enableVsync().get();
			savedPause = client.options.pauseOnLostFocus;
			applied = true;
		}

		String p = profile.value();
		int render;
		int sim;
		double entity;
		switch (p) {
			case "Balanced" -> {
				render = 6;
				sim = 7;
				entity = 0.75;
			}
			case "Max FPS" -> {
				render = 4;
				sim = 4;
				entity = 0.25;
			}
			case "Custom" -> {
				render = (int) renderDist.value();
				sim = (int) simDist.value();
				entity = entityScale.value();
			}
			default -> { // Mobile
				render = 4;
				sim = 5;
				entity = 0.5;
			}
		}
		client.options.renderDistance().set(render);
		client.options.simulationDistance().set(sim);
		client.options.entityDistanceScaling().set(entity);
		client.options.cloudStatus().set(CloudStatus.OFF);
		client.options.graphicsMode().set(GraphicsStatus.FAST);
		client.options.enableVsync().set(false);
		client.options.pauseOnLostFocus = false;
	}

	private void restore() {
		Minecraft client = Minecraft.getInstance();
		if (client.options == null || !applied) {
			return;
		}
		client.options.renderDistance().set(savedRender);
		client.options.simulationDistance().set(savedSim);
		client.options.entityDistanceScaling().set(savedEntity);
		client.options.cloudStatus().set(CloudStatus.values()[savedClouds]);
		client.options.graphicsMode().set(GraphicsStatus.values()[savedGraphics]);
		client.options.enableVsync().set(savedVsync);
		client.options.pauseOnLostFocus = savedPause;
		applied = false;
	}
}
