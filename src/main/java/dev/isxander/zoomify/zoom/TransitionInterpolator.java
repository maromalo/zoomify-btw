package dev.isxander.zoomify.zoom;

import dev.isxander.zoomify.config.TransitionType;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class TransitionInterpolator extends TimedInterpolator {
    private final Supplier<TransitionType> transitionIn;
    private final Supplier<TransitionType> transitionOutBase;
    private TransitionType activeTransition;
    private TransitionType inactiveTransition;
    private double prevTargetInterpolation;
    private boolean justSwappedTransition;

    public TransitionInterpolator(Supplier<TransitionType> transitionIn, Supplier<TransitionType> transitionOut, DoubleSupplier timeIn, DoubleSupplier timeOut) {
        super(timeIn, timeOut);
        this.transitionIn = transitionIn;
        this.transitionOutBase = () -> transitionOut.get().opposite();
        this.activeTransition = transitionIn.get();
        this.inactiveTransition = transitionOutBase.get();
    }

    @Override
    public double tickInterpolation(double targetInterpolation, double currentInterpolation, double tickDelta) {
        double currentInterpolationMod = currentInterpolation;

        if (targetInterpolation > currentInterpolation) {
            activeTransition = transitionIn.get();
            inactiveTransition = transitionOutBase.get();
            if (prevTargetInterpolation < targetInterpolation) {
                justSwappedTransition = true;
                currentInterpolationMod = activeTransition.inverse(inactiveTransition.apply(currentInterpolationMod));
            }
        } else if (targetInterpolation < currentInterpolation) {
            activeTransition = transitionOutBase.get();
            inactiveTransition = transitionIn.get();
            if (prevTargetInterpolation > targetInterpolation) {
                justSwappedTransition = true;
                currentInterpolationMod = activeTransition.inverse(inactiveTransition.apply(currentInterpolationMod));
            }
        }

        prevTargetInterpolation = targetInterpolation;
        if (activeTransition == TransitionType.INSTANT) {
            return targetInterpolation;
        }
        return super.tickInterpolation(targetInterpolation, currentInterpolationMod, tickDelta);
    }

    @Override
    public double modifyInterpolation(double interpolation) {
        return activeTransition.apply(interpolation);
    }

    @Override
    public double modifyPrevInterpolation(double interpolation) {
        if (justSwappedTransition) {
            justSwappedTransition = false;
            return activeTransition.inverse(inactiveTransition.apply(interpolation));
        }
        return interpolation;
    }

    @Override
    public boolean isSmooth() {
        return !justSwappedTransition && super.isSmooth() && activeTransition != TransitionType.INSTANT;
    }
}
