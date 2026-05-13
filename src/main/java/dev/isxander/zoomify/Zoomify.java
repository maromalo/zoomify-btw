package dev.isxander.zoomify;

public final class Zoomify {
    private static boolean initialized;

    private Zoomify() {
    }

    public static void init() {
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
