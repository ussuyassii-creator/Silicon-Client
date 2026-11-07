package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;

/**
 * HandShader ENGINE v2 - real visible first-person hand effects,
 * applied every frame on top of the vanilla arm transform:
 * - Liquid: flowing sine bob + roll
 * - Holographic: scale pulse + tilt (hovering feel)
 * - Realistic: subtle breathing sway
 * - Prism: fast shimmer pulse + pull
 * - Wave: rolling side-to-side
 * Intensity slider scales all motion.
 */
public class HandShaderModule extends Module {
	public final CycleSetting effect;
	public final RangeSetting intensity;

	public HandShaderModule() {
		super("HandShader", "Animated first-person hand effects");
		this.effect = cycle("effect", "Effect", new String[] {
				"None", "Liquid", "Holographic", "Realistic", "Prism", "Wave"
		}, 1);
		this.intensity = range("intensity", "Intensity", 0.1f, 1.0f, 0.1f, 0.6f);
	}
}
