package pyre.client.manager;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import pyre.client.compat.ClientExplosionPacketCompat;
import pyre.client.debug.PyreClientDebugHooks;
import pyre.common.compat.CompatFlags;
import pyre.common.config.PyreClientConfig;
import pyre.common.debug.PyreProfilerMarkers;

import java.util.HashMap;
import java.util.Map;

public final class ClientExplosionEffectManager {
    public static final ClientExplosionEffectManager INSTANCE = new ClientExplosionEffectManager();

    private final ThreadLocal<ExplosionEffectPlan> currentPlan = new ThreadLocal<>();
    private final Map<RegistryKey<World>, ClientWorldState> worldStates = new HashMap<>();
    private PyreClientConfig config = new PyreClientConfig();
    private CompatFlags compatFlags = new CompatFlags(false, false, false, false, false, false, false, false, false, false);
    private RegistryKey<World> lastTickWorldKey;
    private long clientTickCounter;

    private ClientExplosionEffectManager() {
    }

    public void configure(PyreClientConfig config, CompatFlags compatFlags) {
        this.config = config;
        this.compatFlags = compatFlags;
    }

    public void onWorldTickStart(ClientWorld world) {
        if (!this.config.enabled) {
            return;
        }

        RegistryKey<World> worldKey = world.getRegistryKey();
        if (this.lastTickWorldKey != null && !this.lastTickWorldKey.equals(worldKey)) {
            this.onWorldUnload(this.lastTickWorldKey);
        }

        this.lastTickWorldKey = worldKey;
        this.clientTickCounter++;
        this.stateFor(world).beginTick(this.clientTickCounter);
    }

    public void onWorldTickEnd(ClientWorld world) {
        ClientWorldState state = this.worldStates.get(world.getRegistryKey());
        if (state == null) {
            return;
        }

        if (this.config.debugLogging && state.stats.explosionPackets > 0) {
            PyreClientDebugHooks.logTickSummary(world, state.snapshot());
        }
    }

    public void onWorldUnload(ClientWorld world) {
        this.onWorldUnload(world.getRegistryKey());
    }

    public void onWorldUnload(RegistryKey<World> worldKey) {
        ClientWorldState removed = this.worldStates.remove(worldKey);
        if (removed != null) {
            removed.clear();
        }
        if (worldKey.equals(this.lastTickWorldKey)) {
            this.lastTickWorldKey = null;
        }
        this.currentPlan.remove();
    }

    public void onDisconnect() {
        for (ClientWorldState state : this.worldStates.values()) {
            state.clear();
        }
        this.worldStates.clear();
        this.lastTickWorldKey = null;
        this.clientTickCounter = 0L;
        this.currentPlan.remove();
    }

    public void beginExplosionPacket(ClientWorld world, ExplosionS2CPacket packet) {
        if (!this.config.enabled) {
            this.currentPlan.set(ExplosionEffectPlan.passThrough());
            return;
        }

        ClientWorldState state = this.stateFor(world);
        long tick = this.clientTickCounter;
        state.beginTick(tick);
        state.stats.explosionPackets++;

        boolean exactMode = this.config.strictCompatibilityMode || this.compatFlags.hasKnownRenderOptimizationMod();
        long pyreStart = PyreProfilerMarkers.begin(this.config.profilerMarkers);
        ClientExplosionClusterIndex.ClusterMembership membership = state.clusterIndex.register(
                tick,
                ClientExplosionPacketCompat.center(packet),
                ClientExplosionPacketCompat.radius(packet),
                exactMode
        );
        state.stats.pyreNanos += PyreProfilerMarkers.finish(pyreStart);

        if (membership.overlapsExistingCluster()) {
            state.stats.clusteredPackets++;
        }

        boolean allowSound = true;
        if (this.config.enableSoundCoalescing) {
            allowSound = state.soundCoalescer.shouldPlay(tick, ClientExplosionPacketCompat.center(packet), exactMode);
            if (!allowSound) {
                state.stats.suppressedSounds++;
            }
        }

        boolean allowPrimaryParticle = true;
        if (this.config.enableParticleBudgeting) {
            allowPrimaryParticle = state.particleBudgeter.tryAcquire(tick, exactMode);
            if (!allowPrimaryParticle) {
                state.stats.suppressedPrimaryParticles++;
            }
        }

        boolean allowBlockEffectBurst = true;
        if (this.config.enableParticleClusterDedup && membership.overlapsExistingCluster()) {
            allowBlockEffectBurst = exactMode ? !membership.exactDuplicate() : false;
            if (!allowBlockEffectBurst) {
                state.stats.suppressedBlockEffectBursts++;
            }
        }

        // Client-side Pyre never changes gameplay or packet semantics. It only decides whether
        // the local sound and particle work should be emitted vanilla-style for this packet.
        this.currentPlan.set(new ExplosionEffectPlan(allowSound, allowPrimaryParticle, allowBlockEffectBurst));
    }

    public void endExplosionPacket() {
        this.currentPlan.remove();
    }

    public boolean shouldPlaySound() {
        ExplosionEffectPlan plan = this.currentPlan.get();
        return plan == null || plan.allowSound();
    }

    public boolean shouldSpawnPrimaryParticle() {
        ExplosionEffectPlan plan = this.currentPlan.get();
        return plan == null || plan.allowPrimaryParticle();
    }

    public boolean shouldTrackBlockEffects() {
        ExplosionEffectPlan plan = this.currentPlan.get();
        return plan == null || plan.allowBlockEffectBurst();
    }

    private ClientWorldState stateFor(ClientWorld world) {
        return this.worldStates.computeIfAbsent(world.getRegistryKey(), key -> new ClientWorldState());
    }

    public record ClientStatsSnapshot(
            long tick,
            int explosionPackets,
            int clusteredPackets,
            int suppressedSounds,
            int suppressedPrimaryParticles,
            int suppressedBlockEffectBursts,
            long pyreNanos,
            long vanillaNanos
    ) {
    }

    private record ExplosionEffectPlan(boolean allowSound, boolean allowPrimaryParticle, boolean allowBlockEffectBurst) {
        private static ExplosionEffectPlan passThrough() {
            return new ExplosionEffectPlan(true, true, true);
        }
    }

    private static final class ClientWorldState {
        private final ClientExplosionClusterIndex clusterIndex = new ClientExplosionClusterIndex();
        private final ClientExplosionParticleBudgeter particleBudgeter = new ClientExplosionParticleBudgeter();
        private final ClientExplosionSoundCoalescer soundCoalescer = new ClientExplosionSoundCoalescer();
        private final MutableStats stats = new MutableStats();
        private long tick = Long.MIN_VALUE;

        private void beginTick(long tick) {
            if (this.tick == tick) {
                return;
            }

            this.tick = tick;
            this.clusterIndex.beginTick(tick);
            this.stats.reset(tick);
        }

        private void clear() {
            this.clusterIndex.clear();
            this.particleBudgeter.clear();
            this.soundCoalescer.clear();
            this.stats.reset(Long.MIN_VALUE);
            this.tick = Long.MIN_VALUE;
        }

        private ClientStatsSnapshot snapshot() {
            return new ClientStatsSnapshot(
                    this.stats.tick,
                    this.stats.explosionPackets,
                    this.stats.clusteredPackets,
                    this.stats.suppressedSounds,
                    this.stats.suppressedPrimaryParticles,
                    this.stats.suppressedBlockEffectBursts,
                    this.stats.pyreNanos,
                    this.stats.vanillaNanos
            );
        }
    }

    private static final class MutableStats {
        private long tick;
        private int explosionPackets;
        private int clusteredPackets;
        private int suppressedSounds;
        private int suppressedPrimaryParticles;
        private int suppressedBlockEffectBursts;
        private long pyreNanos;
        private long vanillaNanos;

        private void reset(long tick) {
            this.tick = tick;
            this.explosionPackets = 0;
            this.clusteredPackets = 0;
            this.suppressedSounds = 0;
            this.suppressedPrimaryParticles = 0;
            this.suppressedBlockEffectBursts = 0;
            this.pyreNanos = 0L;
            this.vanillaNanos = 0L;
        }
    }
}
