package dev.spyder.core;

/**
 * GUI theme engine v2 - preset palettes + live theming.
 * Seven built-in color combos; each preset sets the accent color AND a
 * matching tinted panel body so the whole GUI recolors coherently.
 * The GUI reads these statics every frame - no allocation, no lag.
 */
public final class GuiTheme {
	public enum Background {
		NONE, DARK, BLUR, COSMIC
	}

	/** Built-in preset names, shown in the GUI's SETTINGS section. */
	public static final String[] PRESET_NAMES = {
			"Midnight", "Vaporwave", "Sakura", "Oceanic", "Blood Moon", "Golden", "Mono"
	};

	/** Per preset: accent r, g, b + panel body argb. */
	private static final int[][] PRESETS = {
			{0x2E, 0xE8, 0x6C, 0xFF10151A}, // Midnight - green on deep slate
			{0xB4, 0x6B, 0xFF, 0xFF151021}, // Vaporwave - purple on plum black
			{0xFF, 0x77, 0xB9, 0xFF1A1016}, // Sakura - pink on rose black
			{0x3D, 0xC8, 0xFF, 0xFF0E141C}, // Oceanic - cyan on abyss blue
			{0xFF, 0x4C, 0x55, 0xFF1A0F10}, // Blood Moon - red on ember black
			{0xFF, 0xD7, 0x5A, 0xFF181410}, // Golden - gold on umber black
			{0xE8, 0xE8, 0xEC, 0xFF131317}, // Mono - white on graphite
	};

	public static int accentR = 0x2E;
	public static int accentG = 0xE8;
	public static int accentB = 0x6C;
	public static int panelBody = 0xFF10151A;
	public static Background background = Background.COSMIC;
	public static float cosmicIntensity = 1.0f;
	public static boolean showStars = true;
	public static String clientName = "SPYDER";
	public static String version = "0.2.0";

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

	public static void cycleBackground() {
		Background[] values = Background.values();
		background = values[(background.ordinal() + 1) % values.length];
	}
}
