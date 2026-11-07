package dev.spyder.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
	}

	public static final class ToggleSetting implements Setting {
		private final String name;
		private per final String display;
		private boolean value;
	}
}
