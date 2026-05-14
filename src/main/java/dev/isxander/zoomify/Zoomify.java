package dev.isxander.zoomify;

import dev.isxander.zoomify.config.ZoomKeyBehaviour;
import dev.isxander.zoomify.config.ZoomifySettings;
import dev.isxander.zoomify.zoom.DefaultZoomHelpers;
import dev.isxander.zoomify.zoom.ZoomHelper;
import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import org.lwjgl.input.Keyboard;

public final class Zoomify {
    private static boolean initialized;
    public static final KeyBinding ZOOM_KEY = new KeyBinding("zoomify.key.zoom", Keyboard.KEY_C);
    public static final KeyBinding SECONDARY_ZOOM_KEY = new KeyBinding("zoomify.key.zoom.secondary", Keyboard.KEY_F6);
    public static final KeyBinding SCROLL_ZOOM_IN_KEY = new KeyBinding("zoomify.key.zoom.in", Keyboard.KEY_NONE);
    public static final KeyBinding SCROLL_ZOOM_OUT_KEY = new KeyBinding("zoomify.key.zoom.out", Keyboard.KEY_NONE);

    private static final ZoomHelper ZOOM_HELPER = DefaultZoomHelpers.regular(ZoomifySettings.INSTANCE);
    private static final ZoomHelper SECONDARY_ZOOM_HELPER = DefaultZoomHelpers.secondary(ZoomifySettings.INSTANCE);

    private static boolean zooming;
    private static boolean secondaryZooming;
    private static double previousZoomDivisor = 1.0D;
    private static int scrollSteps;

    private Zoomify() {
    }

    public static void init() {
        ZoomifySettings.INSTANCE.load();
        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static KeyBinding[] keyBindings() {
        if (ZoomifySettings.INSTANCE.keybindScrolling) {
            return new KeyBinding[]{ZOOM_KEY, SECONDARY_ZOOM_KEY, SCROLL_ZOOM_IN_KEY, SCROLL_ZOOM_OUT_KEY};
        }
        return new KeyBinding[]{ZOOM_KEY, SECONDARY_ZOOM_KEY};
    }

    public static void tick(Minecraft minecraft) {
        if (!initialized) {
            init();
        }

        boolean previousZooming = zooming;
        if (ZoomifySettings.INSTANCE.zoomKeyBehaviour == ZoomKeyBehaviour.HOLD) {
            zooming = GameSettings.isKeyDown(ZOOM_KEY);
        } else {
            while (ZOOM_KEY.isPressed()) {
                zooming = !zooming;
            }
        }

        while (SECONDARY_ZOOM_KEY.isPressed()) {
            secondaryZooming = !secondaryZooming;
        }

        if (ZoomifySettings.INSTANCE.keybindScrolling) {
            while (SCROLL_ZOOM_IN_KEY.isPressed()) {
                mouseZoom(1);
            }
            while (SCROLL_ZOOM_OUT_KEY.isPressed()) {
                mouseZoom(-1);
            }
        }

        if (!zooming && previousZooming && !ZoomifySettings.INSTANCE.retainZoomSteps) {
            scrollSteps = 0;
        }

        ZOOM_HELPER.tick(zooming, scrollSteps);
        SECONDARY_ZOOM_HELPER.tick(secondaryZooming, 0);
    }

    public static float getZoomDivisor(float tickDelta) {
        if (!zooming) {
            if (!ZoomifySettings.INSTANCE.retainZoomSteps) {
                scrollSteps = 0;
            }
            ZOOM_HELPER.reset();
        }

        double divisor = ZOOM_HELPER.getZoomDivisor(tickDelta) * SECONDARY_ZOOM_HELPER.getZoomDivisor(tickDelta);
        previousZoomDivisor = divisor;
        return (float) divisor;
    }

    public static void mouseZoom(int mouseDelta) {
        if (mouseDelta > 0) {
            scrollSteps++;
        } else if (mouseDelta < 0) {
            scrollSteps--;
        }
        scrollSteps = clamp(scrollSteps, 0, ZoomifySettings.INSTANCE.scrollStepCount);
    }

    public static boolean consumeMouseWheel(int mouseDelta) {
        boolean activelyZooming = zooming || (ZoomifySettings.INSTANCE.zoomKeyBehaviour == ZoomKeyBehaviour.HOLD && GameSettings.isKeyDown(ZOOM_KEY));
        if (ZoomifySettings.INSTANCE.scrollZoom && activelyZooming && mouseDelta != 0 && !ZoomifySettings.INSTANCE.keybindScrolling) {
            mouseZoom(mouseDelta);
            return true;
        }
        return false;
    }

    public static boolean isZooming() {
        return zooming;
    }

    public static boolean isSecondaryZooming() {
        return secondaryZooming;
    }

    public static double getPreviousZoomDivisor() {
        return previousZoomDivisor;
    }

    public static boolean shouldHideHudForSecondaryZoom() {
        return secondaryZooming && ZoomifySettings.INSTANCE.secondaryHideHUDOnZoom;
    }

    public static float applyRelativeSensitivity(float mouseSensitivity) {
        if (!zooming && !secondaryZooming) {
            return mouseSensitivity;
        }

        double divisor = Math.max(1.0D, previousZoomDivisor);
        double factor = lerp(ZoomifySettings.INSTANCE.relativeSensitivity / 100.0D, 1.0D, divisor);
        if (factor <= 1.0D) {
            return mouseSensitivity;
        }

        double base = mouseSensitivity * 0.6D + 0.2D;
        double multiplier = base * base * base * 8.0D;
        double adjustedMultiplier = multiplier / factor;
        double adjustedBase = Math.cbrt(adjustedMultiplier / 8.0D);
        return (float) clamp((adjustedBase - 0.2D) / 0.6D, 0.0D, 1.0D);
    }

    public static boolean shouldUseSmoothCamera(boolean original) {
        return original || secondaryZooming || (zooming && ZoomifySettings.INSTANCE.cinematicCamera > 0);
    }

    public static float relativeBobbing(float value) {
        if (!ZoomifySettings.INSTANCE.relativeViewBobbing) {
            return value;
        }
        return (float) (value / lerp(0.2D, 1.0D, previousZoomDivisor));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
}
