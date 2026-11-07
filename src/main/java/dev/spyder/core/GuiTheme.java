package dev.spyder.core;

/**
 * GUI theme engine. All colors + the animated cosmic background live here.
 * The GUI module mutates these values; the ClickGUI and HUD read them every
 * frame straight from statics - no allocation, no lookups, no lag.
 */
public final class GuiTheme {
	public enum Background {
		NONE, DARK, BLUR, COSMIC
	}

	public static int accentR = 0x2E; // #2EE86C green
	public static int accentG = 0xE8;
	public static int accentB = 0x6C;
	public static Background background = Background.COSMIC;
	public static float cosmicIntensity = 1.0f;
	public static boolean showStars = true;
	public static String clientName = "SPYDER";
	public static String version = "0.2.0";
	/** Glass-window panel transparency: 0.4 (frosted) .. 1.0 (solid). */
	public static float panelOpacity = 0.95f;

	private GuiTheme() {
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
