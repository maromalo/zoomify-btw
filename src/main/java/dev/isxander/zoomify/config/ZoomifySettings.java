package dev.isxander.zoomify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class ZoomifySettings {
    public static final ZoomifySettings INSTANCE = new ZoomifySettings();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int initialZoom = 4;
    public double zoomInTime = 1.0D;
    public double zoomOutTime = 0.5D;
    public TransitionType zoomInTransition = TransitionType.EASE_OUT_EXP;
    public TransitionType zoomOutTransition = TransitionType.EASE_OUT_EXP;
    public boolean affectHandFov = true;
    public boolean retainZoomSteps = false;
    public boolean scrollZoom = true;
    public int scrollStepCount = 10;
    public int zoomPerStep = 150;
    public int scrollZoomSmoothness = 70;
    public ZoomKeyBehaviour zoomKeyBehaviour = ZoomKeyBehaviour.HOLD;
    public boolean keybindScrolling = false;
    public int relativeSensitivity = 100;
    public boolean relativeViewBobbing = true;
    public int cinematicCamera = 0;
    public int secondaryZoomAmount = 4;
    public double secondaryZoomInTime = 10.0D;
    public double secondaryZoomOutTime = 1.0D;
    public boolean secondaryHideHUDOnZoom = true;

    private ZoomifySettings() {
    }

    public void load() {
        File file = file();
        if (!file.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            ZoomifySettings loaded = GSON.fromJson(reader, ZoomifySettings.class);
            if (loaded != null) {
                copyFrom(loaded);
                clampValues();
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            save();
        }
    }

    public void save() {
        clampValues();
        File file = file();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFrom(ZoomifySettings loaded) {
        initialZoom = loaded.initialZoom;
        zoomInTime = loaded.zoomInTime;
        zoomOutTime = loaded.zoomOutTime;
        zoomInTransition = loaded.zoomInTransition == null ? TransitionType.EASE_OUT_EXP : loaded.zoomInTransition;
        zoomOutTransition = loaded.zoomOutTransition == null ? TransitionType.EASE_OUT_EXP : loaded.zoomOutTransition;
        affectHandFov = loaded.affectHandFov;
        retainZoomSteps = loaded.retainZoomSteps;
        scrollZoom = loaded.scrollZoom;
        scrollStepCount = loaded.scrollStepCount;
        zoomPerStep = loaded.zoomPerStep;
        scrollZoomSmoothness = loaded.scrollZoomSmoothness;
        zoomKeyBehaviour = loaded.zoomKeyBehaviour == null ? ZoomKeyBehaviour.HOLD : loaded.zoomKeyBehaviour;
        keybindScrolling = loaded.keybindScrolling;
        relativeSensitivity = loaded.relativeSensitivity;
        relativeViewBobbing = loaded.relativeViewBobbing;
        cinematicCamera = loaded.cinematicCamera;
        secondaryZoomAmount = loaded.secondaryZoomAmount;
        secondaryZoomInTime = loaded.secondaryZoomInTime;
        secondaryZoomOutTime = loaded.secondaryZoomOutTime;
        secondaryHideHUDOnZoom = loaded.secondaryHideHUDOnZoom;
    }

    private void clampValues() {
        initialZoom = clamp(initialZoom, 1, 500);
        zoomInTime = clamp(zoomInTime, 0.0D, 60.0D);
        zoomOutTime = clamp(zoomOutTime, 0.0D, 60.0D);
        scrollStepCount = clamp(scrollStepCount, 0, 100);
        zoomPerStep = clamp(zoomPerStep, 100, 1000);
        scrollZoomSmoothness = clamp(scrollZoomSmoothness, 0, 100);
        relativeSensitivity = clamp(relativeSensitivity, 0, 100);
        cinematicCamera = clamp(cinematicCamera, 0, 100);
        secondaryZoomAmount = clamp(secondaryZoomAmount, 1, 500);
        secondaryZoomInTime = clamp(secondaryZoomInTime, 0.0D, 60.0D);
        secondaryZoomOutTime = clamp(secondaryZoomOutTime, 0.0D, 60.0D);
    }

    private File file() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), "zoomify.json");
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
