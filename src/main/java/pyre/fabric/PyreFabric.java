package pyre.fabric;

import net.fabricmc.api.ModInitializer;
import pyre.common.compat.CompatDetector;
import pyre.common.compat.CompatFlags;
import pyre.common.config.PyreConfig;
import pyre.common.debug.PyreDebugHooks;
import pyre.common.manager.PyreExplosionManager;
import pyre.common.platform.PyrePlatformHolder;
import pyre.fabric.lifecycle.FabricServerLifecycle;
import pyre.fabric.platform.FabricPlatform;

public final class PyreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PyrePlatformHolder.setPlatform(FabricPlatform.INSTANCE);

        PyreConfig config = PyreConfig.load();
        CompatFlags compatFlags = CompatDetector.detect();

        PyreExplosionManager.INSTANCE.configure(config.server, compatFlags);
        PyreDebugHooks.logCompatSummary(config, compatFlags);
        FabricServerLifecycle.register();
    }
}
