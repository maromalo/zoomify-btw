package dev.isxander.zoomify.zoom;

public final class InstantInterpolator implements Interpolator {
    public static final InstantInterpolator INSTANCE = new InstantInterpolator();

    private InstantInterpolator() {
    }

    @Override
    public double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta) {
        return targetInterpolation;
    }

    @Override
    public boolean isSmooth() {
        return false;
    }
}
