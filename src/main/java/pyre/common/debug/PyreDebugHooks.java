package pyre.common.debug;

import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pyre.common.compat.CompatFlags;
import pyre.common.config.PyreConfig;
import pyre.common.manager.PyreExplosionManager;

public final class PyreDebugHooks {
    private static final Logger LOGGER = LoggerFactory.getLogger("Pyre");

    private PyreDebugHooks() {
    }

    public static void logCompatSummary(PyreConfig config, CompatFlags compatFlags) {
        if (!config.server.debugLogging && !config.client.debugLogging) {
            return;
        }

        LOGGER.info("Pyre compatibility summary: {}", compatFlags.summary());
    }

    public static void logRiskyPathDisabled(String pathName, CompatFlags compatFlags) {
        LOGGER.info("Pyre disabled {} because known optimization mods are present: {}", pathName, compatFlags.summary());
    }

    public static void logTickSummary(ServerWorld world, PyreExplosionManager.WorldStatsSnapshot snapshot) {
        LOGGER.info(
                "Pyre server tick [{} @ {}]: explosions={}, clustered={}, queries={}, cacheHits={}, cacheMisses={}, cacheBypasses={}, pyreNs={}, vanillaNs={}",
                world.getRegistryKey().getValue(),
                snapshot.tick(),
                snapshot.explosionsProcessed(),
                snapshot.clusteredExplosions(),
                snapshot.nearbyEntityQueries(),
                snapshot.cacheHits(),
                snapshot.cacheMisses(),
                snapshot.cacheBypasses(),
                snapshot.pyreNanos(),
                snapshot.vanillaNanos()
        );
    }
}
