package dev.isxander.zoomify.mixin;

import dev.isxander.zoomify.Zoomify;
import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameSettings.class)
public class GameSettingsMixin {
    @Shadow public KeyBinding[] keyBindings;

    @Inject(method = "<init>(Lnet/minecraft/src/Minecraft;Ljava/io/File;)V", at = @At("RETURN"))
    private void zoomify$registerKeybindings(CallbackInfo ci) {
        KeyBinding[] zoomifyKeys = Zoomify.keyBindings();
        KeyBinding[] combined = new KeyBinding[this.keyBindings.length + zoomifyKeys.length];
        System.arraycopy(this.keyBindings, 0, combined, 0, this.keyBindings.length);
        System.arraycopy(zoomifyKeys, 0, combined, this.keyBindings.length, zoomifyKeys.length);
        this.keyBindings = combined;
        KeyBinding.resetKeyBindingArrayAndHash();
    }
}
