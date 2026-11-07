package dev.spyder.module;

import dev.spyder.core.SpyderSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all SpyderClient modules.
 * Modules declare settings in their constructor; the ClickGUI renders
 * them automatically - new modules get their settings UI for free.
 */
public class Module {
	private final String name;
	private final String description;
	private final List<SpyderSettings.Setting> settings = new ArrayList<>();
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

	public void addSetting(SpyderSettings.Setting setting) {
		settings.add(setting);
	}

	public List<SpyderSettings.Setting> settings() {
		return Collections.unmodifiableList(settings);
	}

	protected SpyderSettings.CycleSetting cycle(String name, String display, String[] options, int defaultIndex) {
		SpyderSettings.CycleSetting setting = new SpyderSettings.CycleSetting(name, display, options, defaultIndex);
		addSetting(setting);
		return setting;
	}

	protected SpyderSettings.ToggleSetting toggleSetting(String name, String display, boolean defaultValue) {
		SpyderSettings.ToggleSetting setting = new SpyderSettings.ToggleSetting(name, display, defaultValue);
		addSetting(setting);
		return setting;
	}

	protected SpyderSettings.RangeSetting range(String name, String display, float min, float max, float step, float defaultValue) {
		SpyderSettings.RangeSetting setting = new SpyderSettings.RangeSetting(name, display, min, max, step, defaultValue);
		addSetting(setting);
		return setting;
	}

	/** Called when the module is turned on or off. Override in subclasses. */
	protected void onToggle() {
	}

	/** Called once per client tick while enabled. Override in subclasses. */
	public void onTick() {
	}
}
