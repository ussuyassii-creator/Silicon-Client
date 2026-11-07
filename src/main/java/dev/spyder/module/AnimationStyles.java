package dev.spyder.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * The swing animation library - all 32 styles from the Swing module.
 * Every style is pure math on the PoseStack: no allocations, no state,
 * runs identically at 20 or 240 FPS.
 *
 * v2 - AMPLITUDE BOOST: every rotation and translation roughly doubled
 * so each style is clearly noticeable at a glance (user request).
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

	/** Vanilla-style fast arc downward - amplified. */
	private static void chop(float t, float side, PoseStack pose) {
		float g = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.YP.rotationDegrees(side * (45.0F + g * -40.0F)));
		float h = Mth.sin(Mth.sqrt(t) * (float) Math.PI);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -40.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -160.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * -45.0F));
	}

	/** Wide diagonal cut across the screen - amplified. */
	private static void slash(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -100.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -80.0F));
		pose.translate(side * -0.4F * h, -0.2F * h, -0.4F * h);
	}

	/** Full 360 degree blade spin - faster whip. */
	private static void spin(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * t * 360.0F));
		pose.mulPose(Axis.XP.rotationDegrees(h * -60.0F));
	}

	/** Gentle sine-wave sweep - wider. */
	private static void wave(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 2.0F);
		pose.translate(side * 0.3F * s, 0.0F, -0.5F * arc(t));
		pose.mulPose(Axis.XP.rotationDegrees(-50.0F * arc(t)));
	}

	/** Fast, tight snap - bigger flick. */
	private static void flick(float t, float side, PoseStack pose) {
		float h = Mth.sin(t * t * (float) Math.PI);
		pose.mulPose(Axis.XP.rotationDegrees(h * -220.0F));
		pose.translate(0.0F, 0.0F, -0.3F * h);
	}

	/** Rising spiral, like drawing a sword - amplified. */
	private static void helix(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(side * t * 360.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-t * 90.0F));
		pose.translate(0.0F, 0.2F * t, 0.0F);
	}

	/** Quick low-profile slash - anime style, amplified. */
	private static void ninja(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.YP.rotationDegrees(side * h * 60.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 180.0F));
		pose.translate(side * -0.2F * h, -0.1F * h, -0.7F * h);
	}

	/** Ultra smooth eased swing - amplified. */
	private static void smoothSwing(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(side * 120.0F * t));
		pose.mulPose(Axis.XP.rotationDegrees(-100.0F * t));
		pose.translate(0.0F, 0.0F, -0.2F * t);
	}

	/** Wind up back, then sharp forward whip with roll - amplified. */
	private static void fortnite(float t, float side, PoseStack pose) {
		float wind = t < 0.3F ? (t / 0.3F) : 1.0F - ((t - 0.3F) / 0.7F);
		float whip = t < 0.3F ? 0.0F : ease((t - 0.3F) / 0.7F);
		pose.translate(-side * 0.5F * wind, 0.16F * wind, 0.5F * wind);
		pose.mulPose(Axis.YP.rotationDegrees(side * 70.0F * wind));
		pose.mulPose(Axis.XP.rotationDegrees(-140.0F * whip));
		pose.mulPose(Axis.ZP.rotationDegrees(side * 70.0F * whip));
		pose.translate(0.0F, 0.0F, -0.6F * whip);
	}

	/** Big overhead smash from above - amplified. */
	private static void overhead(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(-h * 260.0F));
		pose.translate(0.0F, -0.3F * h, -0.5F * h);
	}

	/** Underhand uppercut-style swing (upward) - amplified. */
	private static void underhand(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(h * 200.0F));
		pose.translate(0.0F, 0.2F * h, -0.4F * h);
	}

	/** Shaky wobbly frenzy - amplified. */
	private static void wobble(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 4.0F) * 0.5F;
		pose.mulPose(Axis.ZP.rotationDegrees(side * s * 50.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-arc(t) * 140.0F));
		pose.translate(side * 0.16F * s, 0.0F, -0.2F * arc(t));
	}

	/** Boxing straight punch with recoil - amplified. */
	private static void punch(float t, float side, PoseStack pose) {
		float p = ease(Math.min(t / 0.5F, 1.0F));
		float recoil = t > 0.5F ? Mth.sin((t - 0.5F) * (float) Math.PI) * 0.3F : 0.0F;
		pose.translate(-side * (0.8F * p - recoil), -0.1F * p, -0.9F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-20.0F * p));
	}

	/** Forward knife stab - amplified. */
	private static void stab(float t, float side, PoseStack pose) {
		float p = ease(Math.min(t / 0.4F, 1.0F));
		pose.translate(0.0F, 0.0F, -1.0F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-30.0F * p));
	}

	/** Horizontal swipe across - amplified. */
	private static void swipe(float t, float side, PoseStack pose) {
		float s = Mth.sin(t * (float) Math.PI * 2.0F);
		pose.translate(side * 0.4F * s, 0.0F, -0.6F * arc(t));
		pose.mulPose(Axis.YP.rotationDegrees(side * 80.0F * s));
		pose.mulPose(Axis.XP.rotationDegrees(-70.0F * arc(t)));
	}

	/** Double 360 whirl - amplified. */
	private static void whirl(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(t * 720.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * arc(t) * 40.0F));
	}

	/** Springy overshoot with settling - amplified. */
	private static void elastic(float t, float side, PoseStack pose) {
		float bounce = Mth.sin(t * (float) Math.PI * 3.0F) * (1.0F - t);
		pose.mulPose(Axis.XP.rotationDegrees(-140.0F * t + bounce * 60.0F));
		pose.translate(0.0F, -0.1F * bounce, -0.4F * t);
	}

	/** Floppy multi-jointed look - amplified. */
	private static void noodle(float t, float side, PoseStack pose) {
		float s1 = Mth.sin(t * (float) Math.PI * 2.0F);
		float s2 = Mth.sin(t * (float) Math.PI * 4.0F);
		pose.translate(side * 0.24F * s1, 0.16F * s2, -0.4F * arc(t));
		pose.mulPose(Axis.ZP.rotationDegrees(side * s1 * 60.0F));
		pose.mulPose(Axis.XP.rotationDegrees(s2 * 40.0F - 80.0F * t));
	}

	/** Heavy hammer slam - amplified. */
	private static void hammer(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.XP.rotationDegrees(-h * 300.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * 30.0F));
		pose.translate(0.0F, -0.4F * h, -0.6F * h);
	}

	/** Elegant slash + recovery, master swordsman style - amplified. */
	private static void swordmaster(float t, float side, PoseStack pose) {
		float h = arc(t);
		pose.mulPose(Axis.ZP.rotationDegrees(side * h * -120.0F));
		pose.mulPose(Axis.YP.rotationDegrees(side * t * 50.0F));
		pose.mulPose(Axis.XP.rotationDegrees(-h * 70.0F));
		pose.translate(side * -0.3F * h, -0.2F * h, -0.5F * h);
	}

	/** Wild uncontrolled flailing - amplified. */
	private static void flail(float t, float side, PoseStack pose) {
		float s1 = Mth.sin(t * (float) Math.PI * 5.0F);
		float s2 = Mth.cos(t * (float) Math.PI * 3.0F);
		pose.mulPose(Axis.ZP.rotationDegrees(side * s1 * 70.0F));
		pose.mulPose(Axis.XP.rotationDegrees(s2 * 90.0F - 120.0F * t));
		pose.translate(side * 0.2F * s2, 0.1F * s1, -0.3F * t);
	}

	/** Glitchy stutter-stepped movement - amplified. */
	private static void glitch(float t, float side, PoseStack pose) {
		float stepped = (float) Math.floor(t * 5.0F) / 5.0F;
		pose.translate(side * (stepped % 2.0F) * 0.3F, (stepped % 3.0F) * -0.1F, -0.6F * stepped);
		pose.mulPose(Axis.XP.rotationDegrees(-160.0F * stepped));
	}

	/** Triple-spin vortex - amplified. */
	private static void vortex(float t, float side, PoseStack pose) {
		pose.mulPose(Axis.YP.rotationDegrees(t * 1080.0F));
		pose.translate(0.0F, 0.3F * arc(t), 0.0F);
	}

	/** Pull toward screen center as if magnetized - amplified. */
	private static void magnet(float t, float side, PoseStack pose) {
		float p = ease(t);
		pose.translate(side * -0.6F * p, 0.2F * p, -0.4F * p);
		pose.mulPose(Axis.XP.rotationDegrees(-50.0F * p));
	}
}
