package dev.isxander.zoomify.mixin;

import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    private void zoomify$modifyFov(float tickDelta, boolean worldFov, CallbackInfoReturnable<Float> cir) {
        if (worldFov || ZoomifySettings.INSTANCE.affectHandFov) {
            cir.setReturnValue(cir.getReturnValueF() / Zoomify.getZoomDivisor(tickDelta));
        }
    }

    @Redirect(
            method = {"updateCameraAndRender", "updateRenderer"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/src/GameSettings;smoothCamera:Z")
    )
    private boolean zoomify$smoothCameraIfZooming(GameSettings settings) {
        return Zoomify.shouldUseSmoothCamera(settings.smoothCamera);
    }

    @Redirect(
            method = "updateCameraAndRender",
            at = @At(value = "FIELD", target = "Lnet/minecraft/src/GameSettings;mouseSensitivity:F")
    )
    private float zoomify$relativeSensitivity(GameSettings settings) {
        return Zoomify.applyRelativeSensitivity(settings.mouseSensitivity);
    }

    @Redirect(
            method = "setupViewBobbing",
            at = @At(value = "FIELD", target = "Lnet/minecraft/src/EntityPlayer;cameraYaw:F")
    )
    private float zoomify$relativeCameraYaw(net.minecraft.src.EntityPlayer player) {
        return Zoomify.relativeBobbing(player.cameraYaw);
    }

    @Redirect(
            method = "setupViewBobbing",
            at = @At(value = "FIELD", target = "Lnet/minecraft/src/EntityPlayer;prevCameraYaw:F")
    )
    private float zoomify$relativePrevCameraYaw(net.minecraft.src.EntityPlayer player) {
        return Zoomify.relativeBobbing(player.prevCameraYaw);
    }

    @Redirect(
            method = "setupViewBobbing",
            at = @At(value = "FIELD", target = "Lnet/minecraft/src/EntityPlayer;cameraPitch:F")
    )
    private float zoomify$relativeCameraPitch(net.minecraft.src.EntityPlayer player) {
        return Zoomify.relativeBobbing(player.cameraPitch);
    }

    @Redirect(
            method = "setupViewBobbing",
            at = @At(value = "FIELD", target = "Lnet/minecraft/src/EntityPlayer;prevCameraPitch:F")
    )
    private float zoomify$relativePrevCameraPitch(net.minecraft.src.EntityPlayer player) {
        return Zoomify.relativeBobbing(player.prevCameraPitch);
    }

    @Redirect(
            method = "updateCameraAndRender",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiIngame;renderGameOverlay(FZII)V")
    )
    private void zoomify$hideHudForSecondaryZoom(GuiIngame gui, float tickDelta, boolean hasScreen, int mouseX, int mouseY) {
        if (!Zoomify.shouldHideHudForSecondaryZoom()) {
            gui.renderGameOverlay(tickDelta, hasScreen, mouseX, mouseY);
        }
    }

    @Redirect(
            method = "updateCameraAndRender",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/src/GuiIngame;renderGameOverlayWithGuiDisabled(FZII)V")
    )
    private void zoomify$hideDisabledHudForSecondaryZoom(GuiIngame gui, float tickDelta, boolean hasScreen, int mouseX, int mouseY) {
        if (!Zoomify.shouldHideHudForSecondaryZoom()) {
            gui.renderGameOverlayWithGuiDisabled(tickDelta, hasScreen, mouseX, mouseY);
        }
    }
}
