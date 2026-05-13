package dev.isxander.zoomify;

import net.fabricmc.api.ModInitializer;

public class ZoomifyMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Zoomify.init();
    }
}
