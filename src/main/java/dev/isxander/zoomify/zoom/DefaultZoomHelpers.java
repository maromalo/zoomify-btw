package dev.isxander.zoomify.zoom;

import dev.isxander.zoomify.Zoomify;
import dev.isxander.zoomify.config.ZoomifySettings;

public final class DefaultZoomHelpers {
    private DefaultZoomHelpers() {
    }

    public static ZoomHelper regular(ZoomifySettings settings) {
        return new ZoomHelper(
                new TransitionInterpolator(() -> settings.zoomInTransition, () -> settings.zoomOutTransition, () -> settings.zoomInTime, () -> settings.zoomOutTime),
                new SmoothInterpolator(() -> Zoomify.lerp(settings.scrollZoomSmoothness / 100.0D, 1.0D, 0.1D)),
                () -> settings.initialZoom,
                () -> settings.zoomPerStep,
                () -> settings.scrollStepCount
        );
    }

    public static ZoomHelper secondary(ZoomifySettings settings) {
        return new ZoomHelper(
                new TimedInterpolator(() -> settings.secondaryZoomInTime, () -> settings.secondaryZoomOutTime),
                InstantInterpolator.INSTANCE,
                () -> settings.secondaryZoomAmount,
                () -> 100,
                () -> 0
        );
    }
}
