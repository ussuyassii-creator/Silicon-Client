package dev.spyder.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * Swing animation library v2 - DRAMATIC EDITION.
 * v1 was too subtle to notice; v2 uses 2-3x bigger rotations and
 * real translations so every style is unmistakable at first glance.
 * Pure math on the PoseStack: no allocations, no state.
 *
 * t = eased swing progress 0..1, side = +1 right arm, -1 left arm.
 */
public final class AnimationStyles {
	private AnimationStyles() {
	}

	public static void apply(String style, float speed, boolean smooth, float p, float side, PoseStack pose) {
		float t = Mth.clamp(p * speed, 0.0F, 1.0F);
		if (smooth) {
			t = ease(t);
		}
		switch (style) {
			case "Classic" -> classic(t, side, pose);
			case "Chop" -> chop(t, side, pose);
			case "Slash" -> slash(t, side, pose);
			case "Spin" -> spin(t, side, pose);
			case "Wave" -> wave(t, side, pose);
			case "Flick", "Snappy" -> flick(t, side, pose);
			case "Twist", "Helix" -> helix(t, side, pose);
			case "Ninja" -> ninja(t, side, pose);
			case "Smooth", "Slow-Mo" -> smoothSwing(t, side, pose);
			case "Fortnite" -> fortnite(t, side, pose);
			case "Cursed" -> chop(1.0F - t, side, pose);
			case "Overhead" -> overhead(t, side, pose);
			case "Underhand" -> underhand(t, side, pose);
			case "Frenzy", "Wobble" -> wobble(t, side, pose);
			case "Punch", "Boxing" -> punch(t, side, pose);
			case "Stab" -> stab(t, side, pose);
			case "Swipe" -> swipe(t, side, pose);
			case "Whirl" -> whirl(t, side, pose);
			case "Bounce", "Elastic" -> elastic(t, side, pose);
			case "Noodle" -> noodle(t, side, pose);
			case "Hammer" -> hammer(t, side, pose);
			case "Swordmaster" -> swordmaster(t, side, pose);
			case "Flail" -> flail(t, side, pose);
			case "Glitch" -> glitch(t, side, pose);
			case "Vortex" -> vortex(t, side, pose);
			case "Magnet" -> magnet(t, side, pose);
			default -> chop(t, side, pose);
		}
	}

	private static float ease(float t) {
		return 1.0F - (1.0F - t) * (1.0F - t);
	}

	private static float arc(float t) {
		return Mth.sin(t * (float) Math.PI);
	}

	/** Vanilla-feeling but 2x stronger. */
	private static void classic(float t, float side, PoseStack pose) {
		float g = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.YP.rotationDegrees(side * (50.0F + g * -40.0F)));
		float h = Mth.sin(Mth.sqrt(t) * (float) Math.PI);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -40.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -120.0F));
		pose.translate(0.0F, -0.15F * h, -0.2F * h);
		pose.mulPose(Axis.YP.rotationDegrees(side * -50.0F));
	}

	/** Massive overhead hack downward. */
	private static void chop(float t, float side, PoseStack pose) {
		float g = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.YP.rotationDegrees(side * (60.0F + g * -50.0F)));
		float h = Mth.sin(Mth.sqrt(t) * (float) Math.PI);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -55.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -180.0F));
		pose.translate(0.0F, -0.35F * h, -0.3F * h);
		pose.mulPose(Axis.YP.rotationDegrees(side * -60.0F));
	}

	/** Huge diagonal cut clear across the screen. */
	private static void slash(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -90.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -70.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 40.0F));
		pose.translate(side * -0.55F * h, -0.25F * h, -0.4F * h);
	}

	/** Double full spin. */
	private static void spin(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * t * 720.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -60.0F));
		pose.translate(0.0F, -0.1F * h, -0.3F * h);
	}

	/** Big ocean wave sweep. */
	private static void wave(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 2.0F);
		pose.translate(side * 0.4F * s, 0.12F * s, -0.45F * arc(t));
		pose.mulPose(Axis.XP.rotationDegrees(-70.0F * arc(t)));
		pose.mulPose(Axis.ZP.rotationDegrees(side * s * 35.0F));
	}

	/** Violent snap. */
	private static void flick(float t, float side, PoseStack pose) {
		float h = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.XP.rotationDegrees(h * -200.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 25.0F));
		pose.translate(0.0F, 0.0F, -0.4F * h);
	}

	/** Rising triple spiral. */
	private static void helix(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(side * t * 540.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-t * 110.0F));
		pose.translate(0.0F, 0.3F * t, -0.2F * t);
	}

	/** Anime instant-slash. */
	private static void ninja(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 60.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 170.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -40.0F));
		pose.translate(side * -0.35F * h, -0.15F * h, -0.7F * h);
	}

	/** Heavy slow powerful arc. */
	private static void smoothSwing(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(side * 120.0F * t));
		pose.mulPose(Axis.XP.rotationDegrees(-110.0F * t));
		pose.translate(0.0F, -0.1F * t, -0.35F * t);
	}

	/** Big windup then whip-crack. */
	private static void fortnite(float t, float side, PoseStack pose) {
		float wind = t < 0.35F ? (t / 0.35F) : 1.0F - ((t - 0.35F) / 0.65F);
		float whip = t < 0.35F ? 0.0F : ease((t - 0.35F) / 0.65F);
		pose.translate(-side * 0.6F * wind, 0.2F * wind, 0.4F * wind);
		pose.mulPose(Axis.YP.rotationDegrees(side * 70.0F * wind));
		pose.mulPose(Axis.XP.rotationDegrees(-150.0F * whip));
		pose.mulPose(Axis.ZP.rotationDegrees(side * 55.0F * whip));
		pose.translate(0.0F, 0.0F, -0.6F * whip);
	}

	/** Earth-shattering overhead slam. */
	private static void overhead(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(-h * 230.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * 30.0F));
		pose.translate(0.0F, -0.4F * h, -0.45F * h);
	}

	/** Rising uppercut. */
	private static void underhand(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(h * 190.0F));
		pose.translate(0.0F, 0.25F * h, -0.4F * h);
	}

	/** Shaky chaos. */
	private static void wobble(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 4.0F) * 0.5F;
		pose.mulPose(Axis.ZP.rotationDegrees(side * s * 55.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-arc(t) * 130.0F));
		pose.translate(side * 0.2F * s, 0.0F, -0.25F * arc(t));
	}

	/** Full boxing cross with recoil. */
	private static void punch(float t, float side, PoseStack pose) {
		float p = ease(Math.min(t / 0.5F, 1.0F));
		float recoil = t > 0.5F ? Mth.sin((t - 0.5F) * (float) Math.PI) * 0.3F : 0.0F;
		pose.translate(-side * (0.9F * p - recoil), -0.1F * p, -0.9F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-20.0F * p));
	}

	/** Deep forward knife stab. */
	private static void stab(float t, float side, PoseStack pose) {
		float p = ease(Math.min(t / 0.4F, 1.0F));
		pose.translate(0.0F, 0.0F, -1.1F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-30.0F * p));
		pose.mulPose(Axis.YP.rotationDegrees(side * 15.0F * p));
	}

	/** Wide horizontal sweep. */
	private static void swipe(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 2.0F);
		pose.translate(side * 0.55F * s, 0.0F, -0.6F * arc(t));
		pose.mulPose(Axis.YP.rotationDegrees(side * 80.0F * s));
		pose.mulPose(Axis.XP.rotationDegrees(-60.0F * arc(t)));
	}

	/** Quadruple whirl. */
	private static void whirl(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(t * 1440.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * arc(t) * 40.0F));
		pose.translate(0.0F, -0.1F * arc(t), 0.0F);
	}

	/** Springy overshoot. */
	private static void elastic(float t, float side, PoseStack pose) {
		float bounce = Mth.sin(t * (float) Math.PI * 3.0F) * (1.0F - t);
		pose.mulPose(Axis.XP.rotationDegrees(-140.0F * t + bounce * 60.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * bounce * 30.0F));
		pose.translate(0.0F, -0.1F * bounce, -0.35F * t);
	}

	/** Full floppy arm. */
	private static void noodle(float t, float side, PoseStack pose) {
		float s1 = Mth.sin(t * (float) Math.PI * 2.0F);
		float s2 = Mth.sin(t * (float) Math.PI * 4.0F);
		pose.translate(side * 0.3F * s1, 0.18F * s2, -0.35F * arc(t));
		pose.mulPose(Axis.ZP.rotationDegrees(side * s1 * 65.0F));
		pose.mulPose(Axis.XP.rotationDegrees(s2 * 45.0F - 90.0F * t));
	}

	/** Thunderous hammer slam. */
	private static void hammer(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(-h * 280.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * 35.0F));
		pose.translate(0.0F, -0.5F * h, -0.5F * h);
	}

	/** Elegant huge slash + recovery. */
	private static void swordmaster(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -110.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * t * 50.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 70.0F));
		pose.translate(side * -0.4F * h, -0.2F * h, -0.5F * h);
	}

	/** Total uncontrolled flail. */
	private static void flail(float t, float side, PoseStack pose) {
		float s1 = Mth.sin(t * (float) Math.PI * 5.0F);
		float s2 = Mth.cos(t * (float) Math.PI * 3.0F);
		pose.mulPose(Axis.ZP.rotationDegrees(side * s1 * 80.0F));
		pose.mulPose(Axis.XP.rotationDegrees(s2 * 90.0F - 130.0F * t));
		pose.translate(side * 0.25F * s2, 0.1F * s1, -0.35F * t);
	}

	/** Hard stutter-glitch. */
	private static void glitch(float t, float side, PoseStack pose) {
		float stepped = (float) Math.floor(t * 6.0F) / 6.0F;
		pose.translate(side * (stepped % 2.0F) * 0.45F, (stepped % 3.0F) * -0.15F, -0.6F * stepped);
		pose.mulPose(Axis.XP.rotationDegrees(-170.0F * stepped));
	}

	/** Six-spin vortex. */
	private static void vortex(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(t * 2160.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-arc(t) * 90.0F));
		pose.translate(0.0F, 0.3F * arc(t), -0.2F * t);
	}

	/** Yank to screen center. */
	private static void magnet(float t, float side, PoseStack pose) {
		float p = ease(t);
		pose.translate(side * -0.7F * p, 0.2F * p, -0.5F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-55.0F * p));
		pose.mulPose(Axis.YP.rotationDegrees(side * -35.0F * p));
	}
}
