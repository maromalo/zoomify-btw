package dev.isxander.zoomify.zoom;

import dev.isxander.zoomify.Zoomify;

import java.util.function.IntSupplier;

public class ZoomHelper {
    private final Interpolator initialInterpolator;
    private final Interpolator scrollInterpolator;
    private final IntSupplier initialZoom;
    private final IntSupplier zoomPerStep;
    private final IntSupplier maxScrollTiers;
    private double prevInitialInterpolation;
    private double initialInterpolation;
    private boolean zoomingLastTick;
    private double prevScrollInterpolation;
    private double scrollInterpolation;
    private int lastScrollTier;
    private boolean resetting;
    private double resetMultiplier;

    public ZoomHelper(Interpolator initialInterpolator, Interpolator scrollInterpolator, IntSupplier initialZoom, IntSupplier zoomPerStep, IntSupplier maxScrollTiers) {
        this.initialInterpolator = initialInterpolator;
        this.scrollInterpolator = scrollInterpolator;
        this.initialZoom = initialZoom;
        this.zoomPerStep = zoomPerStep;
        this.maxScrollTiers = maxScrollTiers;
    }

    public void tick(boolean zooming, int scrollTiers) {
        tickInitial(zooming, 0.05D);
        tickScroll(scrollTiers, 0.05D);
    }

    private void tickInitial(boolean zooming, double lastFrameDuration) {
        if (zooming && !zoomingLastTick) {
            resetting = false;
        }

        double targetZoom = zooming ? 1.0D : 0.0D;
        prevInitialInterpolation = initialInterpolation;
        initialInterpolation = initialInterpolator.tickInterpolation(targetZoom, initialInterpolation, lastFrameDuration);
        prevInitialInterpolation = initialInterpolator.modifyPrevInterpolation(prevInitialInterpolation);
        if (!initialInterpolator.isSmooth()) {
            prevInitialInterpolation = initialInterpolation;
        }
        zoomingLastTick = zooming;
    }

    private void tickScroll(int scrollTiers, double lastFrameDuration) {
        if (scrollTiers > lastScrollTier) {
            resetting = false;
        }

        double targetZoom = maxScrollTiers.getAsInt() > 0 ? scrollTiers / (double) maxScrollTiers.getAsInt() : 0.0D;
        prevScrollInterpolation = scrollInterpolation;
        scrollInterpolation = scrollInterpolator.tickInterpolation(targetZoom, scrollInterpolation, lastFrameDuration);
        prevScrollInterpolation = scrollInterpolator.modifyPrevInterpolation(prevScrollInterpolation);
        if (!scrollInterpolator.isSmooth()) {
            prevScrollInterpolation = scrollInterpolation;
        }
        lastScrollTier = scrollTiers;
    }

    public double getZoomDivisor(float tickDelta) {
        double initialMultiplier = getInitialZoomMultiplier(tickDelta);
        double baseDivisor = 1.0D / initialMultiplier;
        double scrollT = resetting ? 0.0D : (scrollInterpolator.isSmooth()
                ? scrollInterpolator.modifyInterpolation(Zoomify.lerp(tickDelta, prevScrollInterpolation, scrollInterpolation))
                : scrollInterpolation);
        double stepMultiplier = zoomPerStep.getAsInt() / 100.0D;
        double currentStep = scrollT * maxScrollTiers.getAsInt();
        double rawDivisor = baseDivisor * Math.pow(stepMultiplier, currentStep);
        double finalDivisor = Math.max(0.5D, Math.min(500.0D, rawDivisor));

        if (initialInterpolation == 0.0D && scrollInterpolation == 0.0D) {
            resetting = false;
        }
        if (!resetting) {
            resetMultiplier = 1.0D / finalDivisor;
        }
        return finalDivisor;
    }

    private double getInitialZoomMultiplier(float tickDelta) {
        double interpolation = initialInterpolator.isSmooth()
                ? initialInterpolator.modifyInterpolation(Zoomify.lerp(tickDelta, prevInitialInterpolation, initialInterpolation))
                : initialInterpolation;
        return Zoomify.lerp(interpolation, 1.0D, !resetting ? 1.0D / initialZoom.getAsInt() : resetMultiplier);
    }

    public void reset() {
        if (!resetting && scrollInterpolation > 0.0D) {
            resetting = true;
            scrollInterpolation = 0.0D;
            prevScrollInterpolation = 0.0D;
        }
    }
}
