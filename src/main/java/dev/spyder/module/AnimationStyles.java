package dev.spyder.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * The swing animation library - all 32 styles from the Swing module.
 * Every style is pure math on the PoseStack: no allocations, no state,
 * runs identically at 20 or 240 FPS.
 *
 * t = eased swing progress 0..1 (already includes speed + smooth settings)
 * side = +1 right arm, -1 left arm.
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
			case "Classic", "Chop" -> chop(t, side, pose);
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

	/** Vanilla-style fast arc downward. */
	private static void chop(float t, float side, PoseStack pose) {
		float g = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.YP.rotationDegrees(side * (45.0F + g * -20.0F)));
		float h = Mth.sin(Mth.sqrt(t) * (float) Math.PI);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -20.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -80.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * -45.0F));
	}

	/** Wide diagonal cut across the screen. */
	private static void slash(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -50.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -40.0F));
		pose.translate(side * -0.2F * h, -0.1F * h, -0.2F * h);
	}

	/** Full 360 degree blade spin. */
	private static void spin(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * t * 360.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -30.0F));
	}

	/** Gentle sine-wave sweep. */
	private static void wave(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 2.0F);
		pose.translate(side * 0.15F * s, 0.0F, -0.25F * arc(t));
		pose.mulPose(Axis.XP.rotationDegrees(-25.0F * arc(t)));
	}

	/** Fast, tight snap - minimal but snappy. */
	private static void flick(float t, float side, PoseStack pose) {
		float h = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.XP.rotationDegrees(h * -110.0F));
		pose.translate(0.0F, 0.0F, -0.15F * h);
	}

	/** Rising spiral, like drawing a sword. */
	private static void helix(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(side * t * 180.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-t * 45.0F));
		pose.translate(0.0F, 0.1F * t, 0.0F);
	}

	/** Quick low-profile slash - minimal motion, anime style. */
	private static void ninja(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 30.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 90.0F));
		pose.translate(side * -0.1F * h, -0.05F * h, -0.35F * h);
	}

	/** Ultra smooth eased swing. */
	private static void smoothSwing(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(side * 60.0F * t));
		pose.mulPose(Axis.XP.rotationDegrees(-50.0F * t));
		pose.translate(0.0F, 0.0F, -0.1F * t);
	}

	/** Wind up back, then sharp forward whip with roll. */
	private static void fortnite(float t, float side, PoseStack pose) {
		float wind = t < 0.3F ? (t / 0.3F) : 1.0F - ((t - 0.3F) / 0.7F);
		float whip = t < 0.3F ? 0.0F : ease((t - 0.3F) / 0.7F);
		pose.translate(-side * 0.25F * wind, 0.08F * wind, 0.25F * wind);
		pose.mulPose(Axis.YP.rotationDegrees(side * 35.0F * wind));
		pose.mulPose(Axis.XP.rotationDegrees(-70.0F * whip));
		pose.mulPose(Axis.ZP.rotationDegrees(side * 35.0F * whip));
		pose.translate(0.0F, 0.0F, -0.3F * whip);
	}

	/** Big overhead smash from above. */
	private static void overhead(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(-h * 130.0F));
		pose.translate(0.0F, -0.15F * h, -0.25F * h);
	}

	/** Underhand uppercut-style swing (upward). */
	private static void underhand(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(h * 100.0F));
		pose.translate(0.0F, 0.1F * h, -0.2F * h);
	}

	/** Shaky wobbly frenzy. */
	private static void wobble(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 4.0F) * 0.5F;
		pose.mulPose(Axis.ZP.rotationDegrees(side * s * 25.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-arc(t) * 70.0F));
		pose.translate(side * 0.08F * s, 0.0F, -0.1F * arc(t));
	}

	/** Boxing straight punch with recoil. */
	private static void punch(float t, float side, PoseStack pose) {
		float p = ease(Math.min(t / 0.5F, 1.0F));
		float recoil = t > 0.5F ? Mth.sin((t - 0.5F) * (float) Math.PI) * 0.15F : 0.0F;
		pose.translate(-side * (0.4F * p - recoil), -0.05F * p, -0.45F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-10.0F * p));
	}

	/** Forward knife stab. */
	private static void stab(float t, float side, PoseStack pose) {
		float p = ease(Math.min(t / 0.4F, 1.0F));
		pose.translate(0.0F, 0.0F, -0.5F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-15.0F * p));
	}

	/** Horizontal swipe across. */
	private static void swipe(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 2.0F);
		pose.translate(side * 0.2F * s, 0.0F, -0.3F * arc(t));
		pose.mulPose(Axis.YP.rotationDegrees(side * 40.0F * s));
		pose.mulPose(Axis.XP.rotationDegrees(-35.0F * arc(t)));
	}

	/** Double 360 whirl. */
	private static void whirl(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(t * 720.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * arc(t) * 20.0F));
	}

	/** Springy overshoot with settling. */
	private static void elastic(float t, float side, PoseStack pose) {
		float bounce = Mth.sin(t * (float) Math.PI * 3.0F) * (1.0F - t);
		pose.mulPose(Axis.XP.rotationDegrees(-70.0F * t + bounce * 30.0F));
		pose.translate(0.0F, -0.05F * bounce, -0.2F * t);
	}

	/** Floppy multi-jointed look. */
	private static void noodle(float t, float side, PoseStack pose) {
		float s1 = Mth.sin(t * (float) Math.PI * 2.0F);
		float s2 = Mth.sin(t * (float) Math.PI * 4.0F);
		pose.translate(side * 0.12F * s1, 0.08F * s2, -0.2F * arc(t));
		pose.mulPose(Axis.ZP.rotationDegrees(side * s1 * 30.0F));
		pose.mulPose(Axis.XP.rotationDegrees(s2 * 20.0F - 40.0F * t));
	}

	/** Heavy hammer slam. */
	private static void hammer(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(-h * 150.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * 15.0F));
		pose.translate(0.0F, -0.2F * h, -0.3F * h);
	}

	/** Elegant slash + recovery, master swordsman style. */
	private static void swordmaster(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -60.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * t * 25.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 35.0F));
		pose.translate(side * -0.15F * h, -0.1F * h, -0.25F * h);
	}

	/** Wild uncontrolled flailing. */
	private static void flail(float t, float side, PoseStack pose) {
		float s1 = Mth.sin(t * (float) Math.PI * 5.0F);
		float s2 = Mth.cos(t * (float) Math.PI * 3.0F);
		pose.mulPose(Axis.ZP.rotationDegrees(side * s1 * 35.0F));
		pose.mulPose(Axis.XP.rotationDegrees(s2 * 45.0F - 60.0F * t));
		pose.translate(side * 0.1F * s2, 0.05F * s1, -0.15F * t);
	}

	/** Glitchy stutter-stepped movement. */
	private static void glitch(float t, float side, PoseStack pose) {
		float stepped = (float) Math.floor(t * 5.0F) / 5.0F;
		pose.translate(side * (stepped % 2.0F) * 0.15F, (stepped % 3.0F) * -0.05F, -0.3F * stepped);
		pose.mulPose(Axis.XP.rotationDegrees(-80.0F * stepped));
	}

	/** Triple-spin vortex. */
	private static void vortex(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(t * 1080.0F));
		pose.translate(0.0F, 0.15F * arc(t), 0.0F);
	}

	/** Pull toward screen center as if magnetized. */
	private static void magnet(float t, float side, PoseStack pose) {
		float p = ease(t);
		pose.translate(side * -0.3F * p, 0.1F * p, -0.2F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-25.0F * p));
	}
}
