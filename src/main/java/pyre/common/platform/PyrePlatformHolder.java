package pyre.common.platform;

import java.nio.file.Path;

public final class PyrePlatformHolder {
    private static final PyrePlatform FALLBACK = new PyrePlatform() {
        @Override
        public Path getConfigDir() {
            return Path.of("config");
        }

        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public String getLoaderName() {
            return "unknown";
        }
    };

    private static volatile PyrePlatform platform = FALLBACK;

    private PyrePlatformHolder() {
    }

    public static void setPlatform(PyrePlatform platform) {
        PyrePlatformHolder.platform = platform == null ? FALLBACK : platform;
    }

    public static PyrePlatform getPlatform() {
        return platform;
    }
}
