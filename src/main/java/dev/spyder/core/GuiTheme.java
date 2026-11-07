package dev.spyder.core;

/**
 * GUI theme engine v3 "Orchard" - violet premium palette.
 * Light purple + deep violet by default; seven presets for the SETTINGS
 * section picker. Glass-window transparency preserved.
 * The GUI reads these statics every frame - no allocation, no lag.
 */
public final class GuiTheme {
	public enum Background {
		NONE, DARK, BLUR, COSMIC
	}

	/** Built-in preset names, shown in the GUI's SETTINGS section. */
	public static final String[] PRESET_NAMES = {
			"Orchard", "Amethyst", "Nebula", "Aurora", "Blossom", "Frost", "Ember"
	};

	/** Per preset: accent r, g, b + panel body argb. */
	private static final int[][] PRESETS = {
			{0xC4, 0x9B, 0xFF, 0xF215141E}, // Orchard - light purple on deep violet
			{0xA7, 0x8B, 0xFA, 0xFF141020}, // Amethyst - soft violet on plum
			{0x8E, 0x7B, 0xFF, 0xFF121226}, // Nebula - indigo on night blue
			{0x8F, 0xE1, 0xD4, 0xFF0E1418}, // Aurora - mint on dark teal
			{0xFF, 0x9E, 0xC7, 0xFF1A1016}, // Blossom - pink on rose black
			{0xB7, 0xD9, 0xFF, 0xFF0E1220}, // Frost - ice blue on deep blue
			{0xFF, 0x9E, 0x6C, 0xFF1A0F10}, // Ember - warm orange on ember black
	};

	public static int accentR = 0xC4;
	public static int accentG = 0x9B;
	public static int accentB = 0xFF;
	public static int panelBody = 0xF215141E;
	public static Background background = Background.COSMIC;
	public static float cosmicIntensity = 1.0f;
	public static boolean showStars = true;
	public static String clientName = "SPYDER";
	public static String version = "0.2.0";
	/** Glass-window panel transparency: 0.4 (frosted) .. 1.0 (solid). */
	public static float panelOpacity = 0.95f;

	private GuiTheme() {
	}

	/** Accent color (argb) of a preset by index, without applying it. */
	public static int presetAccent(int idx) {
		if (idx < 0 || idx >= PRESETS.length) {
			idx = 0;
		}
		int[] p = PRESETS[idx];
		return 0xFF000000 | (p[0] << 16) | (p[1] << 8) | p[2];
	}

	/** Apply a preset by index: accent + panel tint together. */
	public static void applyPreset(int idx) {
		if (idx < 0 || idx >= PRESETS.length) {
			idx = 0;
		}
		int[] p = PRESETS[idx];
		accentR = p[0];
		accentG = p[1];
		accentB = p[2];
		panelBody = p[3];
	}

	public static int accent() {
		return 0xFF000000 | (accentR << 16) | (accentG << 8) | accentB;
	}

	public static int accentDim() {
		return 0xFF000000 | ((accentR / 2) << 16) | ((accentG / 2) << 8) | (accentB / 2);
	}

	public static int onOffColor(boolean on) {
		return on ? accent() : 0xFF555555;
	}

	/** Scale an ARGB color's alpha by the glass opacity. */
	public static int glass(int argb) {
		int a = (argb >>> 24) & 0xFF;
		int ga = (int) (a * panelOpacity);
		return (ga << 24) | (argb & 0x00FFFFFF);
	}

	public static void cycleBackground() {
		Background[] values = Background.values();
		background = values[(background.ordinal() + 1) % values.length];
	}
}
