package dev.isxander.zoomify.zoom;

import java.util.function.DoubleSupplier;

public class TimedInterpolator extends LinearInterpolator {
    private final DoubleSupplier timeIn;
    private final DoubleSupplier timeOut;

    public TimedInterpolator(DoubleSupplier timeIn, DoubleSupplier timeOut) {
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    @Override
    protected double getTimeIncrement(boolean zoomingOut, double tickDelta, double targetInterpolation, double currentInterpolation) {
        double time = zoomingOut ? timeOut.getAsDouble() : timeIn.getAsDouble();
        return time <= 0.0D ? 1.0D : tickDelta / time;
    }

    @Override
    public boolean isSmooth() {
        return (goingIn && timeIn.getAsDouble() > 0.0D) || (!goingIn && timeOut.getAsDouble() > 0.0D);
    }
}
