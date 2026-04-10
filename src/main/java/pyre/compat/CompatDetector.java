package pyre.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class CompatDetector {
    private CompatDetector() {
    }

    public static CompatFlags detect() {
        FabricLoader loader = FabricLoader.getInstance();
        return new CompatFlags(
                loader.isModLoaded("sodium"),
                loader.isModLoaded("lithium"),
                loader.isModLoaded("krypton"),
                loader.isModLoaded("badoptimizations"),
                loader.isModLoaded("ferritecore"),
                loader.isModLoaded("immediatelyfast"),
                loader.isModLoaded("modernfix"),
                loader.isModLoaded("noisium"),
                loader.isModLoaded("entityculling"),
                loader.isModLoaded("moreculling")
        );
    }
}
