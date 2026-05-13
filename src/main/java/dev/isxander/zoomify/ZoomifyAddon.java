package dev.isxander.zoomify;

import api.AddonHandler;
import api.BTWAddon;

public class ZoomifyAddon extends BTWAddon {
    @Override
    public void initialize() {
        AddonHandler.logMessage(getName() + " Version " + getVersionString() + " Initializing...");
    }
}
