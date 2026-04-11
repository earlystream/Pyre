package pyre.client.debug;

import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pyre.client.manager.ClientExplosionEffectManager;

public final class PyreClientDebugHooks {
    private static final Logger LOGGER = LoggerFactory.getLogger("Pyre/Client");

    private PyreClientDebugHooks() {
    }

    public static void logTickSummary(ClientWorld world, ClientExplosionEffectManager.ClientStatsSnapshot snapshot) {
        LOGGER.info(
                "Pyre client tick [{} @ {}]: packets={}, clustered={}, soundSuppressed={}, primaryParticlesSuppressed={}, blockEffectsSuppressed={}, pyreNs={}, vanillaNs={}",
                world.getRegistryKey().getValue(),
                snapshot.tick(),
                snapshot.explosionPackets(),
                snapshot.clusteredPackets(),
                snapshot.suppressedSounds(),
                snapshot.suppressedPrimaryParticles(),
                snapshot.suppressedBlockEffectBursts(),
                snapshot.pyreNanos(),
                snapshot.vanillaNanos()
        );
    }
}
