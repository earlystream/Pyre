package pyre.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import pyre.client.manager.ClientExplosionEffectManager;
import pyre.common.compat.CompatDetector;
import pyre.common.compat.CompatFlags;
import pyre.common.config.PyreConfig;
import pyre.common.platform.PyrePlatformHolder;
import pyre.fabric.lifecycle.FabricClientLifecycle;
import pyre.fabric.platform.FabricPlatform;

public final class PyreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PyrePlatformHolder.setPlatform(FabricPlatform.INSTANCE);

        PyreConfig config = PyreConfig.load();
        CompatFlags compatFlags = CompatDetector.detect();

        ClientExplosionEffectManager.INSTANCE.configure(config.client, compatFlags);
        FabricClientLifecycle.register();
    }
}
