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
		// Register modules here. Swing first; Ambient, Skybox, HandShader,
		// GUI extras and Cosmetics will follow in later milestones.
		register(new SwingModule());
	}

	public static void register(Module module) {
		MODULES.add(module);
	}

	/** Lookup a registered module by class. Returns null if not registered. */
	public static <T extends Module> T get(Class<T> type) {
		for (Module module : MODULES) {
			if (type.isInstance(module)) {
				return type.cast(module);
			}
		}
		return null;
	}

	public static List<Module> modules() {
		return Collections.unmodifiableList(MODULES);
	}
}
