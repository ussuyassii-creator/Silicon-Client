package dev.spyder.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple, allocation-free settings framework shared by all modules.
 * Settings are declared once per module and rendered by the ClickGUI.
 * No objects are created while the game runs - only when GUI is open.
 */
public final class SpyderSettings {
	public interface Setting {
		String name();

		String display();
	}

	public static final class CycleSetting implements Setting {
		private final String name;
		private final String display;
		private final String[] options;
		private int index;

		public CycleSetting(String name, String display, String[] options, int defaultIndex) {
			this.name = name;
			this.display = display;
			this.options = options;
			this.index = defaultIndex;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String display() {
			return display;
		}

		public String[] options() {
			return options;
		}

		public int index() {
			return index;
		}

		public String value() {
			return options[index];
		}

		public void next() {
			index = (index + 1) % options.length;
		}

		public void setIndex(int newIndex) {
			if (newIndex >= 0 && newIndex < options.length) {
				index = newIndex;
			}
		}
	}

	public static final class ToggleSetting implements Setting {
		private final String name;
		private final String display;
		private boolean value;

		public ToggleSetting(String name, String display, boolean defaultValue) {
			this.name = name;
			this.display = display;
			this.value = defaultValue;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String display() {
			return display;
		}

		public boolean value() {
			return value;
		}

		public void toggle() {
			value = !value;
		}
	}

	public static final class RangeSetting implements Setting {
		private final String name;
		private final String display;
		private final float min;
		private final float max;
		private final float step;
		private float value;

		public RangeSetting(String name, String display, float min, float max, float step, float defaultValue) {
			this.name = name;
			this.display = display;
			this.min = min;
			this.max = max;
			this.step = step;
			this.value = defaultValue;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String display() {
			return display;
		}

		public float min() {
			return min;
		}

		public float max() {
			return max;
		}

		public float step() {
			return step;
		}

		public float value() {
			return value;
		}

		public void increment() {
			value = Math.min(max, value + step);
		}

		public void decrement() {
			value = Math.max(min, value - step);
		}
	}
}
