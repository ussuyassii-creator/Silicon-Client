package dev.spyder.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
 * Replaces vanilla's first-person attack transform with SpyderClient animations.
 * Only active while the Swing module is enabled - otherwise vanilla is untouched.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true)
	private void spyder$customSwing(PoseStack poseStack, HumanoidArm humanoidArm, float f, CallbackInfo ci) {
		SwingModule swing = ModuleManager.get(SwingModule.class);
		if (swing == null || !swing.isEnabled()) {
			return;
		}

		float side = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		// Guard against degenerate input values (NaN or >1) that can appear
		// right after world/entity transitions.
		if (f <= 0.0F || f > 1.0F || !Float.isFinite(f)) {
			return; // vanilla handles it
		}

		AnimationStyles.apply(swing.getStyle(), f, side, poseStack);
		ci.cancel();
	}
}
