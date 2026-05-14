package dev.isxander.zoomify.config;

public enum TransitionType {
    INSTANT,
    LINEAR,
    EASE_IN_SINE,
    EASE_OUT_SINE,
    EASE_IN_OUT_SINE,
    EASE_IN_QUAD,
    EASE_OUT_QUAD,
    EASE_IN_OUT_QUAD,
    EASE_IN_CUBIC,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    EASE_IN_EXP,
    EASE_OUT_EXP,
    EASE_IN_OUT_EXP;

    public double apply(double x) {
        switch (this) {
            case INSTANT:
                return x >= 1.0D ? 1.0D : 0.0D;
            case EASE_IN_SINE:
                return 1.0D - Math.cos((x * Math.PI) / 2.0D);
            case EASE_OUT_SINE:
                return Math.sin((x * Math.PI) / 2.0D);
            case EASE_IN_OUT_SINE:
                return -(Math.cos(Math.PI * x) - 1.0D) / 2.0D;
            case EASE_IN_QUAD:
                return x * x;
            case EASE_OUT_QUAD:
                return 1.0D - (1.0D - x) * (1.0D - x);
            case EASE_IN_OUT_QUAD:
                return x < 0.5D ? 2.0D * x * x : 1.0D - Math.pow(-2.0D * x + 2.0D, 2.0D) / 2.0D;
            case EASE_IN_CUBIC:
                return x * x * x;
            case EASE_OUT_CUBIC:
                return 1.0D - Math.pow(1.0D - x, 3.0D);
            case EASE_IN_OUT_CUBIC:
                return x < 0.5D ? 4.0D * x * x * x : 1.0D - Math.pow(-2.0D * x + 2.0D, 3.0D) / 2.0D;
            case EASE_IN_EXP:
                return x == 0.0D ? 0.0D : Math.pow(2.0D, 10.0D * x - 10.0D);
            case EASE_OUT_EXP:
                return x == 1.0D ? 1.0D : 1.0D - Math.pow(2.0D, -10.0D * x);
            case EASE_IN_OUT_EXP:
                if (x == 0.0D || x == 1.0D) {
                    return x;
                }
                return x < 0.5D ? Math.pow(2.0D, 20.0D * x - 10.0D) / 2.0D : (2.0D - Math.pow(2.0D, -20.0D * x + 10.0D)) / 2.0D;
            case LINEAR:
            default:
                return x;
        }
    }

    public TransitionType opposite() {
        switch (this) {
            case EASE_IN_SINE:
                return EASE_OUT_SINE;
            case EASE_OUT_SINE:
                return EASE_IN_SINE;
            case EASE_IN_QUAD:
                return EASE_OUT_QUAD;
            case EASE_OUT_QUAD:
                return EASE_IN_QUAD;
            case EASE_IN_CUBIC:
                return EASE_OUT_CUBIC;
            case EASE_OUT_CUBIC:
                return EASE_IN_CUBIC;
            case EASE_IN_EXP:
                return EASE_OUT_EXP;
            case EASE_OUT_EXP:
                return EASE_IN_EXP;
            default:
                return this;
        }
    }

    public double inverse(double x) {
        double low = 0.0D;
        double high = 1.0D;
        for (int i = 0; i < 16; i++) {
            double mid = (low + high) / 2.0D;
            if (apply(mid) < x) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) / 2.0D;
    }
}
