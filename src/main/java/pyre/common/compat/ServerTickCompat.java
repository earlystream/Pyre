package pyre.common.compat;

import net.minecraft.server.world.ServerWorld;

public final class ServerTickCompat {
    private ServerTickCompat() {
    }

    /**
     * Pyre only needs a conservative same-tick key for transient cache and cluster lifetime.
     * Using the server's global tick counter avoids per-version ServerWorld time access seams
     * while preserving the intended "same server tick" validity window.
     */
    public static long currentTick(ServerWorld world) {
        return world.getServer().getTicks();
    }
}
