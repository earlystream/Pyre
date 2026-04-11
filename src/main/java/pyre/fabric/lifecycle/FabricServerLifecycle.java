package pyre.fabric.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import pyre.common.manager.PyreExplosionManager;

/**
 * Fabric lifecycle adapter for the shared explosion manager. Quilt and NeoForge need their own
 * adapter classes because loader event buses differ even when the manager logic does not.
 */
public final class FabricServerLifecycle {
    private FabricServerLifecycle() {
    }

    public static void register() {
        ServerTickEvents.START_WORLD_TICK.register(PyreExplosionManager.INSTANCE::onWorldTickStart);
        ServerTickEvents.END_WORLD_TICK.register(PyreExplosionManager.INSTANCE::onWorldTickEnd);
        ServerWorldEvents.UNLOAD.register((server, world) -> PyreExplosionManager.INSTANCE.onWorldUnload(world));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PyreExplosionManager.INSTANCE.onServerStopping());
    }
}
