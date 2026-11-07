package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;

/**
 * HandShader - effects applied to your first-person hand and held item.
 * Rendering implementation lands with the hand shader mixin.
 */
public class HandShaderModule extends Module {
	public final CycleSetting effect;
	public final RangeSetting intensity;
	public final ToggleSetting onlyItems;

	public HandShaderModule() {
		super("HandShader", "Visual effects for your hand and held items");
		this.effect = cycle("effect", "Effect", new String[] {
				"None", "Liquid", "Holographic", "Realistic", "Prism", "Clone"
		}, 0);
		this.intensity = range("intensity", "Intensity", 0.1f, 1.0f, 0.1f, 0.5f);
		this.onlyItems = toggleSetting("onlyitems", "Items only", false);
	}
}
