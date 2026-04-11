package pyre.common.debug;

public final class PyreProfilerMarkers {
    private PyreProfilerMarkers() {
    }

    public static long begin(boolean enabled) {
        return enabled ? System.nanoTime() : 0L;
    }

    public static long finish(long startNanos) {
        if (startNanos == 0L) {
            return 0L;
        }

        return Math.max(0L, System.nanoTime() - startNanos);
    }
}
