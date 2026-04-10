package pyre;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import pyre.compat.CompatDetector;
import pyre.compat.CompatFlags;
import pyre.config.PyreConfig;
import pyre.debug.PyreDebugHooks;
import pyre.manager.PyreExplosionManager;

public final class Pyre implements ModInitializer {
    @Override
    public void onInitialize() {
        PyreConfig config = PyreConfig.load();
        CompatFlags compatFlags = CompatDetector.detect();

        PyreExplosionManager.INSTANCE.configure(config, compatFlags);
        PyreDebugHooks.logCompatSummary(config, compatFlags);

        ServerTickEvents.START_WORLD_TICK.register((world) -> PyreExplosionManager.INSTANCE.onWorldTickStart(world));
        ServerTickEvents.END_WORLD_TICK.register((world) -> PyreExplosionManager.INSTANCE.onWorldTickEnd(world));
        ServerWorldEvents.UNLOAD.register((server, world) -> PyreExplosionManager.INSTANCE.onWorldUnload(world));
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> PyreExplosionManager.INSTANCE.onServerStopping());
    }
}
