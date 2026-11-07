package dev.spyder.module;

/**
 * Swing - custom hand swing animations.
 * 24 styles replace the vanilla chop when enabled.
 */
public class SwingModule extends Module {
	public enum Style {
		CHOP("Chop"),
		FORTNITE("Fortnite"),
		SPIN_SLASH("Spin Slash"),
		HELIX("Helix"),
		WAVE("Wave"),
		HELICOPTER("Helicopter"),
		ONE_TWO("One-Two"),
		PUNCH_OUT("Punch Out"),
		HELIX_SLASH("Helix Slash"),
		WAVE_SLASH("Wave Slash"),
		REVERSE("Reverse"),
		SMOOTH("Smooth"),
		STAB("Stab"),
		KATANA("Katana"),
		UPPERCUT("Uppercut"),
		BACKHAND("Backhand"),
		HAMMER("Hammer"),
		RIPPLE("Ripple"),
		FLICK("Flick"),
		TORNADO("Tornado"),
		DRILL("Drill"),
		ZEN("Zen"),
		GRAVITY("Gravity"),
		RICOCHET("Ricochet");

		private final String label;

		Style(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public Style next() {
			Style[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}
	}

	private Style style = Style.FORTNITE;

	public SwingModule() {
		super("Swing", "Custom hand swing animations");
	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
	}

	public void cycleStyle() {
		style = style.next();
	}
}
