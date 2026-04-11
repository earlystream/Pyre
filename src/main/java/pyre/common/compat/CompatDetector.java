package pyre.common.compat;

import pyre.common.platform.PyrePlatform;
import pyre.common.platform.PyrePlatformHolder;

public final class CompatDetector {
    private CompatDetector() {
    }

    public static CompatFlags detect() {
        PyrePlatform platform = PyrePlatformHolder.getPlatform();
        return new CompatFlags(
                platform.isModLoaded("sodium"),
                platform.isModLoaded("lithium"),
                platform.isModLoaded("krypton"),
                platform.isModLoaded("badoptimizations"),
                platform.isModLoaded("ferritecore"),
                platform.isModLoaded("immediatelyfast"),
                platform.isModLoaded("modernfix"),
                platform.isModLoaded("noisium"),
                platform.isModLoaded("entityculling"),
                platform.isModLoaded("moreculling")
        );
    }
}
