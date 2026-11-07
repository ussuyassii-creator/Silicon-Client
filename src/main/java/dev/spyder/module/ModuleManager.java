package dev.spyder.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of all SpyderClient modules.
 * Add new modules by registering them in init().
 */
public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager() {
	}

	public static void init() {
		register(new SwingModule());
		register(new AmbientModule());
		register(new SkyboxModule());
		register(new HandShaderModule());
		register(new CosmeticsModule());
		register(new GuiModule());
	}

	public static void register(Module module) {
		MODULES.add(module);
	}

	public static List<Module> modules() {
		return Collections.unmodifiableList(MODULES);
	}

	public static Module byName(String name) {
		for (Module module : MODULES) {
			if (module.getName().equalsIgnoreCase(name)) {
				return module;
			}
		}
		return null;
	}
}
