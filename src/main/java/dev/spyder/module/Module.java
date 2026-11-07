package dev.spyder.module;

/**
 * Base class for all SpyderClient modules.
 * Extendable: future modules (Ambient, Skybox, HandShader, Cosmetics, ...)
 * just extend this and register themselves in ModuleManager.
 */
public class Module {
	private final String name;
	private final String description;
	private boolean enabled = false;

	public Module(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		if (enabled == value) return;
		enabled = value;
		onToggle();
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	/** Called when the module is turned on or off. Override in subclasses. */
	protected void onToggle() {
	}

	/** Called once per client tick while enabled. Override in subclasses. */
	public void onTick() {
	}
}
