package dev.isxander.zoomify.zoom;

import java.util.function.DoubleSupplier;

public class SmoothInterpolator extends LinearInterpolator {
    private final DoubleSupplier smoothness;

    public SmoothInterpolator(DoubleSupplier smoothness) {
        this.smoothness = smoothness;
    }

    @Override
    protected double getTimeIncrement(boolean zoomingOut, double tickDelta, double targetInterpolation, double currentInterpolation) {
        double diff = zoomingOut ? currentInterpolation - targetInterpolation : targetInterpolation - currentInterpolation;
        return diff * smoothness.getAsDouble() / 0.05D * tickDelta;
    }

    @Override
    public double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta) {
        if (!isSmooth()) {
            return targetInterpolation;
        }
        return super.tickInterpolation(targetInterpolation, currentInterpolation, tickDelta);
    }

    @Override
    public boolean isSmooth() {
        return smoothness.getAsDouble() != 1.0D;
    }
}
