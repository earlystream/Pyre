package pyre.fabric.platform;

import net.fabricmc.loader.api.FabricLoader;
import pyre.common.platform.PyrePlatform;

import java.nio.file.Path;

public enum FabricPlatform implements PyrePlatform {
    INSTANCE;

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String getLoaderName() {
        return "fabric";
    }
}
