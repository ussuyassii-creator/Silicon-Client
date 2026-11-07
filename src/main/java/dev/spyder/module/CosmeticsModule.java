package dev.spyder.module;

import dev.spyder.core.SpyderSettings.CycleSetting;
import dev.spyder.core.SpyderSettings.ToggleSetting;
import dev.spyder.core.SpyderSettings.RangeSetting;

/**
 * Cosmetics - client-side cosmetics: capes, wings, horns, orbit dragon, fist FX.
 * Everything renders only for you (client-side) until server sync is added.
 */
public class CosmeticsModule extends Module {
	public final CycleSetting cape;
	public final CycleSetting back;
	public final CycleSetting aura;
	public final CycleSetting fistFx;
	public final RangeSetting scale;

	public CosmeticsModule() {
		super("Cosmetics", "Capes, wings, auras, orbit dragon, fist FX");
		this.cape = cycle("cape", "Cape", new String[] {
				"None", "Fire", "Water", "Wind", "Void", "Galaxy", "Blood"
		}, 0);
		this.back = cycle("back", "Back item", new String[] {
				"None", "Angel wings", "Devil wings", "Broken devil wings"
		}, 0);
		this.aura = cycle("aura", "Aura", new String[] {
				"None", "Orbit dragon", "Fire ring", "Spirit"
		}, 0);
		this.fistFx = cycle("fistfx", "Fist FX", new String[] {
				"None", "Fire", "Blood drop", "Water drop", "Wind spin"
		}, 0);
		this.scale = range("scale", "Scale", 0.5f, 2.0f, 0.1f, 1.0f);
	}
}
