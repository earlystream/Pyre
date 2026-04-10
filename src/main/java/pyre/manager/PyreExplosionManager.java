package pyre.manager;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import pyre.compat.CompatFlags;
import pyre.config.PyreConfig;
import pyre.debug.PyreDebugHooks;
import pyre.debug.PyreProfilerMarkers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class PyreExplosionManager {
    public static final PyreExplosionManager INSTANCE = new PyreExplosionManager();

    private PyreConfig config = new PyreConfig();
    private CompatFlags compatFlags = new CompatFlags(false, false, false, false, false, false, false, false, false, false);
    private final Map<RegistryKey<World>, WorldState> worldStates = new HashMap<>();
    private boolean loggedRiskyPathDisable;

    private PyreExplosionManager() {
    }

    public void configure(PyreConfig config, CompatFlags compatFlags) {
        this.config = config;
        this.compatFlags = compatFlags;
        this.loggedRiskyPathDisable = false;
    }

    public void onWorldTickStart(ServerWorld world) {
        if (!this.config.enabled) {
            return;
        }

        this.stateFor(world).beginTick(world.getTime());
    }

    public void onWorldTickEnd(ServerWorld world) {
        WorldState state = this.worldStates.get(world.getRegistryKey());
        if (state == null) {
            return;
        }

        state.finishTick(world, this.config);
    }

    public void onWorldUnload(ServerWorld world) {
        WorldState removed = this.worldStates.remove(world.getRegistryKey());
        if (removed != null) {
            removed.clear();
        }
    }

    public void onServerStopping() {
        for (WorldState state : this.worldStates.values()) {
            state.clear();
        }

        this.worldStates.clear();
    }

    public void onExplosionStart(Explosion explosion) {
        if (!this.config.enabled) {
            return;
        }

        ServerWorld world = explosion.getWorld();
        WorldState state = this.stateFor(world);
        state.beginTick(world.getTime());

        long managerStart = PyreProfilerMarkers.begin(this.config.profilerMarkers);
        ExplosionClusterIndex.ClusterMembership membership = this.config.enableClusterReuse
                ? state.clusterIndex.register(world.getTime(), explosion.getPosition(), explosion.getPower(), this.config.maxClusterRadius)
                : ExplosionClusterIndex.isolated(world.getTime());

        state.recordPyreTime(PyreProfilerMarkers.finish(managerStart));
        state.recordPyreTime(membership.indexCostNanos());
        state.activeExplosions.push(new ActiveExplosion(explosion, membership));
    }

    public void onExplosionEnd(Explosion explosion, int affectedBlocks) {
        if (!this.config.enabled) {
            return;
        }

        WorldState state = this.worldStates.get(explosion.getWorld().getRegistryKey());
        if (state == null) {
            return;
        }

        ActiveExplosion activeExplosion = state.removeActiveExplosion(explosion);
        if (activeExplosion == null) {
            return;
        }

        state.stats.explosionsProcessed++;
        if (activeExplosion.membership.overlapsExistingCluster()) {
            state.stats.clusteredExplosions++;
        }

        // Any completed vanilla explosion may have already changed entity or block state,
        // so Pyre stops considering cached support data for the rest of the tick.
        state.worldStateMutatedThisTick = true;
        state.scheduler.scheduleOnce("clear-query-cache", state.queryCache::clear);
    }

    public List<Entity> queryNearbyEntities(ServerWorld world, Explosion explosion, @Nullable Entity except, Box box, Supplier<List<Entity>> vanillaQuery) {
        if (!this.config.enabled) {
            return vanillaQuery.get();
        }

        WorldState state = this.stateFor(world);
        state.beginTick(world.getTime());
        state.stats.nearbyEntityQueries++;

        ActiveExplosion activeExplosion = state.findActiveExplosion(explosion);
        if (activeExplosion != null) {
            activeExplosion.usedEntityQuery = true;
        }

        if (!this.shouldUseQueryCache(state, activeExplosion)) {
            state.stats.cacheBypasses++;
            long vanillaStart = PyreProfilerMarkers.begin(this.config.profilerMarkers);
            List<Entity> result = vanillaQuery.get();
            state.recordVanillaTime(PyreProfilerMarkers.finish(vanillaStart));
            return result;
        }

        long pyreStart = PyreProfilerMarkers.begin(this.config.profilerMarkers);
        List<Entity> cached = state.queryCache.get(world.getTime(), except, box).orElse(null);
        state.recordPyreTime(PyreProfilerMarkers.finish(pyreStart));
        if (cached != null) {
            state.stats.cacheHits++;
            if (activeExplosion != null) {
                activeExplosion.reusedEntityQuery = true;
            }
            return new ArrayList<>(cached);
        }

        long vanillaStart = PyreProfilerMarkers.begin(this.config.profilerMarkers);
        List<Entity> result = vanillaQuery.get();
        state.recordVanillaTime(PyreProfilerMarkers.finish(vanillaStart));

        long storeStart = PyreProfilerMarkers.begin(this.config.profilerMarkers);
        state.queryCache.put(world.getTime(), except, box, result);
        state.recordPyreTime(PyreProfilerMarkers.finish(storeStart));
        state.stats.cacheMisses++;
        return result;
    }

    public WorldStatsSnapshot snapshot(ServerWorld world) {
        WorldState state = this.worldStates.get(world.getRegistryKey());
        if (state == null) {
            return WorldStatsSnapshot.empty(world.getTime());
        }

        return state.snapshot();
    }

    private boolean shouldUseQueryCache(WorldState state, @Nullable ActiveExplosion activeExplosion) {
        if (!this.config.enableExplosionQueryCache || this.config.strictCompatibilityMode) {
            return false;
        }

        if (this.config.autoDisableRiskyPathsWithKnownMods && this.compatFlags.hasAnyKnownOptimizationMod()) {
            if (!this.loggedRiskyPathDisable) {
                PyreDebugHooks.logRiskyPathDisabled("nearby-entity query reuse", this.compatFlags);
                this.loggedRiskyPathDisable = true;
            }
            return false;
        }

        if (state.worldStateMutatedThisTick) {
            return false;
        }

        // This cache is only considered for overlapping explosions before any completed explosion
        // has had a chance to mutate world state. If that assumption fails, Pyre uses vanilla.
        return activeExplosion != null && activeExplosion.membership.overlapsExistingCluster();
    }

    private WorldState stateFor(ServerWorld world) {
        return this.worldStates.computeIfAbsent(world.getRegistryKey(), key -> new WorldState());
    }

    public record WorldStatsSnapshot(
            long tick,
            int explosionsProcessed,
            int clusteredExplosions,
            int nearbyEntityQueries,
            int cacheHits,
            int cacheMisses,
            int cacheBypasses,
            long pyreNanos,
            long vanillaNanos
    ) {
        private static WorldStatsSnapshot empty(long tick) {
            return new WorldStatsSnapshot(tick, 0, 0, 0, 0, 0, 0, 0L, 0L);
        }
    }

    private static final class WorldState {
        private final ExplosionWorkScheduler scheduler = new ExplosionWorkScheduler();
        private final ExplosionClusterIndex clusterIndex = new ExplosionClusterIndex();
        private final ExplosionQueryCache queryCache = new ExplosionQueryCache();
        private final ArrayDeque<ActiveExplosion> activeExplosions = new ArrayDeque<>();
        private final MutableStats stats = new MutableStats();
        private long tick = Long.MIN_VALUE;
        private boolean worldStateMutatedThisTick;

        private void beginTick(long tick) {
            if (this.tick == tick) {
                return;
            }

            this.tick = tick;
            this.worldStateMutatedThisTick = false;
            this.activeExplosions.clear();
            this.stats.reset(tick);
            this.clusterIndex.beginTick(tick);
            this.queryCache.beginTick(tick);
            this.scheduler.clear();
        }

        private void finishTick(ServerWorld world, PyreConfig config) {
            this.scheduler.flushEndOfTick();
            if (config.debugLogging && this.stats.explosionsProcessed > 0) {
                PyreDebugHooks.logTickSummary(world, this.snapshot());
            }
        }

        private void clear() {
            this.scheduler.clear();
            this.clusterIndex.clear();
            this.queryCache.clear();
            this.activeExplosions.clear();
            this.stats.reset(Long.MIN_VALUE);
            this.tick = Long.MIN_VALUE;
            this.worldStateMutatedThisTick = false;
        }

        private ActiveExplosion removeActiveExplosion(Explosion explosion) {
            Iterator<ActiveExplosion> iterator = this.activeExplosions.iterator();
            while (iterator.hasNext()) {
                ActiveExplosion activeExplosion = iterator.next();
                if (activeExplosion.matches(explosion)) {
                    iterator.remove();
                    return activeExplosion;
                }
            }
            return null;
        }

        private ActiveExplosion findActiveExplosion(Explosion explosion) {
            for (ActiveExplosion activeExplosion : this.activeExplosions) {
                if (activeExplosion.matches(explosion)) {
                    return activeExplosion;
                }
            }
            return null;
        }

        private void recordPyreTime(long nanos) {
            if (nanos == 0L) {
                return;
            }

            this.stats.pyreNanos += nanos;
        }

        private void recordVanillaTime(long nanos) {
            if (nanos == 0L) {
                return;
            }

            this.stats.vanillaNanos += nanos;
        }

        private WorldStatsSnapshot snapshot() {
            return new WorldStatsSnapshot(
                    this.stats.tick,
                    this.stats.explosionsProcessed,
                    this.stats.clusteredExplosions,
                    this.stats.nearbyEntityQueries,
                    this.stats.cacheHits,
                    this.stats.cacheMisses,
                    this.stats.cacheBypasses,
                    this.stats.pyreNanos,
                    this.stats.vanillaNanos
            );
        }
    }

    private static final class ActiveExplosion {
        private final Explosion explosion;
        private final ExplosionClusterIndex.ClusterMembership membership;
        private boolean usedEntityQuery;
        private boolean reusedEntityQuery;

        private ActiveExplosion(Explosion explosion, ExplosionClusterIndex.ClusterMembership membership) {
            this.explosion = explosion;
            this.membership = membership;
        }

        private boolean matches(Explosion other) {
            return this.explosion == other;
        }
    }

    private static final class MutableStats {
        private long tick;
        private int explosionsProcessed;
        private int clusteredExplosions;
        private int nearbyEntityQueries;
        private int cacheHits;
        private int cacheMisses;
        private int cacheBypasses;
        private long pyreNanos;
        private long vanillaNanos;

        private void reset(long tick) {
            this.tick = tick;
            this.explosionsProcessed = 0;
            this.clusteredExplosions = 0;
            this.nearbyEntityQueries = 0;
            this.cacheHits = 0;
            this.cacheMisses = 0;
            this.cacheBypasses = 0;
            this.pyreNanos = 0L;
            this.vanillaNanos = 0L;
        }
    }
}
