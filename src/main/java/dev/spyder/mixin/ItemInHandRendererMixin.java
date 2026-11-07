package dev.spyder.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.spyder.module.AnimationStyles;
import dev.spyder.module.HandShaderModule;
import dev.spyder.module.Module;
import dev.spyder.module.ModuleManager;
import dev.spyder.module.SwingModule;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Two hooks into the first-person hand renderer:
 * 1) HEAD-cancel of applyItemArmAttackTransform -> Swing module's dramatic styles
 * 2) TAIL-additive of applyItemArmTransform -> HandShader module's idle effects
 *    (TAIL = vanilla transform still runs, ours stack on top: nothing breaks)
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true)
	private void spyder$customSwing(PoseStack poseStack, HumanoidArm humanoidArm, float f, CallbackInfo ci) {
		Module module = ModuleManager.byName("Swing");
		if (!(module instanceof SwingModule swing) || !swing.isEnabled()) {
			return;
		}

		float side = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		if (f <= 0.0F || f > 1.0F || !Float.isFinite(f)) {
			return;
		}

		AnimationStyles.apply(swing.style.value(), swing.speed.value(), swing.smooth.value(), f, side, poseStack);
		ci.cancel();
	}

	@Inject(method = "applyItemArmTransform", at = @At("TAIL"))
	private void spyder$handFx(PoseStack poseStack, HumanoidArm humanoidArm, float f, CallbackInfo ci) {
		Module module = ModuleManager.byName("HandShader");
		if (!(module instanceof HandShaderModule fx) || !fx.isEnabled()) {
			return;
		}

		float i = fx.intensity.value();
		if (i <= 0f) {
			return;
		}
		long time = System.currentTimeMillis();
		float t = (time % 2000L) / 2000.0F;
		switch (fx.effect.value()) {
			case "Liquid" -> {
				float s = Mth.sin(t * (float) Math.PI * 2.0F);
				poseStack.translate(0.0F, s * 0.07F * i, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(s * 10.0F * i));
			}
			case "Holographic" -> {
				float p = 1.0F + Mth.sin(t * (float) Math.PI * 6.0F) * 0.07F * i;
				poseStack.mulPose(Axis.YP.rotationDegrees(4.0F * i));
				poseStack.scale(p, p, p);
			}
			case "Realistic" -> {
				float s = Mth.sin(t * (float) Math.PI * 2.0F);
				poseStack.translate(0.0F, s * 0.02F * i, 0.0F);
			}
			case "Prism" -> {
				float p = 1.0F + Mth.sin(t * (float) Math.PI * 8.0F) * 0.05F * i;
				poseStack.translate(0.0F, 0.0F, -0.06F * i);
				poseStack.scale(p, p, p);
			}
			case "Wave" -> {
				float s = Mth.sin(t * (float) Math.PI * 4.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(s * 14.0F * i));
				poseStack.mulPose(Axis.XP.rotationDegrees(3.0F * i));
			}
			default -> {
			}
		}
	}
}
