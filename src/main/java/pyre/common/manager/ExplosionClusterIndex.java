package pyre.common.manager;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class ExplosionClusterIndex {
    private long currentTick = Long.MIN_VALUE;
    private int nextSequence = 1;
    private final List<TrackedExplosion> trackedExplosions = new ArrayList<>();

    public void beginTick(long tick) {
        if (this.currentTick == tick) {
            return;
        }

        this.currentTick = tick;
        this.nextSequence = 1;
        this.trackedExplosions.clear();
    }

    public ClusterMembership register(long tick, Vec3d center, float power, double extraRadius) {
        this.beginTick(tick);

        long overlapTimer = System.nanoTime();
        int overlapCount = 0;
        double nearestDistanceSquared = Double.POSITIVE_INFINITY;
        double effectiveRadius = (power * 2.0D) + 1.0D;

        // This is metadata only. Pyre never merges explosions or alters the order vanilla uses.
        for (TrackedExplosion trackedExplosion : this.trackedExplosions) {
            double allowedDistance = trackedExplosion.effectiveRadius() + effectiveRadius + extraRadius;
            double distanceSquared = trackedExplosion.center().squaredDistanceTo(center);
            if (distanceSquared <= (allowedDistance * allowedDistance)) {
                overlapCount++;
                nearestDistanceSquared = Math.min(nearestDistanceSquared, distanceSquared);
            }
        }

        ClusterMembership membership = new ClusterMembership(
                tick,
                this.nextSequence++,
                overlapCount,
                overlapCount > 0,
                overlapCount == 0 ? -1.0D : Math.sqrt(nearestDistanceSquared)
        );

        this.trackedExplosions.add(new TrackedExplosion(center, effectiveRadius));
        membership.attachTiming(System.nanoTime() - overlapTimer);
        return membership;
    }

    public void clear() {
        this.trackedExplosions.clear();
        this.currentTick = Long.MIN_VALUE;
        this.nextSequence = 1;
    }

    public static ClusterMembership isolated(long tick) {
        return new ClusterMembership(tick, 0, 0, false, -1.0D);
    }

    private record TrackedExplosion(Vec3d center, double effectiveRadius) {
    }

    public static final class ClusterMembership {
        private final long tick;
        private final int sequence;
        private final int overlapCount;
        private final boolean overlapsExistingCluster;
        private final double nearestDistance;
        private long indexCostNanos;

        private ClusterMembership(long tick, int sequence, int overlapCount, boolean overlapsExistingCluster, double nearestDistance) {
            this.tick = tick;
            this.sequence = sequence;
            this.overlapCount = overlapCount;
            this.overlapsExistingCluster = overlapsExistingCluster;
            this.nearestDistance = nearestDistance;
        }

        private void attachTiming(long nanos) {
            this.indexCostNanos = nanos;
        }

        public boolean overlapsExistingCluster() {
            return this.overlapsExistingCluster;
        }

        public long indexCostNanos() {
            return this.indexCostNanos;
        }
    }
}
