package dev.spyder.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * The swing animation library. Every style is pure math on the PoseStack -
 * no allocations, no state, runs identically at 20 or 240 FPS.
 *
 * 'p' is the swing progress: 0.0 (swing starts) -> 1.0 (swing ends).
 * 'side' is +1 for right arm, -1 for left arm.
 */
public final class AnimationStyles {
	private AnimationStyles() {
	}

	public static void apply(SwingModule.Style style, float p, float side, PoseStack pose) {
		switch (style) {
			case CHOP -> applyChop(p, side, pose);
			case FORTNITE -> applyFortnite(p, side, pose);
			case SPIN_SLASH -> applySpinSlash(p, side, pose);
			case HELIX -> applyHelix(p, side, pose);
			case WAVE -> applyWave(p, side, pose);
			case HELICOPTER -> applyHelicopter(p, side, pose);
			case ONE_TWO -> applyOneTwo(p, side, pose);
			case PUNCH_OUT -> applyPunchOut(p, side, pose);
			case HELIX_SLASH -> applyHelixSlash(p, side, pose);
			case WAVE_SLASH -> applyWaveSlash(p, side, pose);
			case REVERSE -> applyReverse(p, side, pose);
			case SMOOTH -> applySmooth(p, side, pose);
		}
	}

	private static float easeOut(float t) {
		return 1.0F - (1.0F - t) * (1.0F - t);
	}

	/** Vanilla-style: fast arc downward, quick recovery. */
	private static void applyChop(float p, float side, PoseStack pose) {
		float g = Mth.sin(p * p * (float) Math.PI);
		pose.mulPose(Axis.YP.rotationDegrees(side * (45.0F + g * -20.0F)));
		float h = Mth.sin(Mth.sqrt(p) * (float) Math.PI);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -20.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -80.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * -45.0F));
	}

	/** Fortnite-style: wind up back, then a sharp forward whip with a slight roll. */
	private static void applyFortnite(float p, float side, PoseStack pose) {
		float wind = p < 0.3F ? (p / 0.3F) : 1.0F - ((p - 0.3F) / 0.7F);
		float whip = p < 0.3F ? 0.0F : easeOut((p - 0.3F) / 0.7F);
		pose.translate(-side * 0.25F * wind, 0.08F * wind, 0.25F * wind);
		pose.mulPose(Axis.YP.rotationDegrees(side * 35.0F * wind));
		pose.mulPose(Axis.XP.rotationDegrees(-70.0F * whip));
		pose.mulPose(Axis.ZP.rotationDegrees(side * 35.0F * whip));
		pose.translate(0.0F, 0.0F, -0.3F * whip);
	}

	/** Spins the hand around the Z axis, like a slashing anime cut. */
	private static void applySpinSlash(float p, float side, PoseStack pose) {
		float spin = easeOut(p) * 360.0F;
		pose.mulPose(Axis.ZP.rotationDegrees(side * spin));
		pose.mulPose(Axis.XP.rotationDegrees(-30.0F * Mth.sin(p * (float) Math.PI)));
	}

	/** Rising spiral motion, like drawing a sword upward. */
	private static void applyHelix(float p, float side, PoseStack pose) {
		float h = easeOut(p);
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 180.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 45.0F));
		pose.translate(0.0F, 0.1F * h, 0.0F);
	}

	/** Sine wave motion forward, gentle and stylish. */
	private static void applyWave(float p, float side, PoseStack pose) {
		float s = Mth.sin(p * (float) Math.PI * 2.0F);
		pose.translate(side * 0.15F * s, 0.0F, -0.25F * Mth.sin(p * (float) Math.PI));
		pose.mulPose(Axis.XP.rotationDegrees(-25.0F * Mth.sin(p * (float) Math.PI)));
	}

	/** Wild full spin above the head. */
	private static void applyHelicopter(float p, float side, PoseStack pose) {
		float spin = easeOut(p) * 720.0F;
		pose.mulPose(Axis.YP.rotationDegrees(spin));
		pose.translate(0.0F, 0.15F * Mth.sin(p * (float) Math.PI), 0.0F);
	}

	/** Double-tap jab combo: quick pull back, fast double jab forward. */
	private static void applyOneTwo(float p, float side, PoseStack pose) {
		float phase = p < 0.4F ? 0.0F : (p - 0.4F) / 0.6F;
		float jab = p < 0.4F ? -0.15F * (p / 0.4F) : Mth.sin(phase * (float) Math.PI * 2.0F) * 0.4F;
		pose.translate(-side * jab, 0.0F, -jab);
		pose.mulPose(Axis.XP.rotationDegrees(-15.0F * jab * side));
	}

	/** Sharp boxing-style straight punch with recoil. */
	private static void applyPunchOut(float p, float side, PoseStack pose) {
		float punch = easeOut(Math.min(p / 0.5F, 1.0F));
		float recoil = p > 0.5F ? Mth.sin((p - 0.5F) * (float) Math.PI) * 0.15F : 0.0F;
		pose.translate(-side * (0.4F * punch - recoil), -0.05F * punch, -0.45F * punch);
		pose.mulPose(Axis.XP.rotationDegrees(-10.0F * punch));
	}

	/** Helix + slash combined: rises while rotating and slicing forward. */
	private static void applyHelixSlash(float p, float side, PoseStack pose) {
		float h = easeOut(p);
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 180.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 30.0F));
		pose.translate(0.0F, 0.08F * h, -0.2F * h);
	}

	/** Wave + slash combined: horizontal sine sweep forward. */
	private static void applyWaveSlash(float p, float side, PoseStack pose) {
		float s = Mth.sin(p * (float) Math.PI * 2.0F);
		float fwd = Mth.sin(p * (float) Math.PI);
		pose.translate(side * 0.2F * s, 0.0F, -0.3F * fwd);
		pose.mulPose(Axis.YP.rotationDegrees(side * 40.0F * s));
		pose.mulPose(Axis.XP.rotationDegrees(-35.0F * fwd));
	}

	/** Vanilla backwards: the chop, but played in reverse. */
	private static void applyReverse(float p, float side, PoseStack pose) {
		applyChop(1.0F - p, side, pose);
	}

	/** Ultra-smooth eased swing: minimal, elegant, no snap. */
	private static void applySmooth(float p, float side, PoseStack pose) {
		float t = easeOut(p);
		pose.mulPose(Axis.YP.rotationDegrees(side * 60.0F * t));
		pose.mulPose(Axis.XP.rotationDegrees(-50.0F * t));
		pose.translate(0.0F, 0.0F, -0.1F * t);
	}
}
