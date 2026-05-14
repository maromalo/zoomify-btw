package dev.isxander.zoomify.mixin;

import dev.isxander.zoomify.Zoomify;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "runTick", at = @At("TAIL"))
    private void zoomify$tick(CallbackInfo ci) {
        Zoomify.tick((Minecraft) (Object) this);
    }

    @Redirect(
            method = "runTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/src/InventoryPlayer;changeCurrentItem(I)V")
    )
    private void zoomify$consumeScrollZoom(InventoryPlayer inventory, int mouseDelta) {
        if (!Zoomify.consumeMouseWheel(mouseDelta)) {
            inventory.changeCurrentItem(mouseDelta);
        }
    }
}
