package pyre.client.manager;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class ClientExplosionClusterIndex {
    private long currentTick = Long.MIN_VALUE;
    private final List<TrackedExplosion> trackedExplosions = new ArrayList<>();

    public void beginTick(long tick) {
        if (this.currentTick == tick) {
            return;
        }

        this.currentTick = tick;
        this.trackedExplosions.clear();
    }

    public ClusterMembership register(long tick, Vec3d center, float radius, boolean exactMode) {
        this.beginTick(tick);

        int overlapCount = 0;
        boolean exactDuplicate = false;
        double nearestDistanceSquared = Double.POSITIVE_INFINITY;
        double overlapPadding = exactMode ? 0.75D : 1.5D;

        for (TrackedExplosion trackedExplosion : this.trackedExplosions) {
            double allowedDistance = trackedExplosion.radius() + radius + overlapPadding;
            double distanceSquared = trackedExplosion.center().squaredDistanceTo(center);
            if (distanceSquared <= allowedDistance * allowedDistance) {
                overlapCount++;
                nearestDistanceSquared = Math.min(nearestDistanceSquared, distanceSquared);
            }

            if (trackedExplosion.cellX() == cell(center.x) && trackedExplosion.cellY() == cell(center.y) && trackedExplosion.cellZ() == cell(center.z)) {
                exactDuplicate = true;
            }
        }

        this.trackedExplosions.add(new TrackedExplosion(center, radius, cell(center.x), cell(center.y), cell(center.z)));
        return new ClusterMembership(overlapCount, overlapCount > 0, exactDuplicate, overlapCount == 0 ? -1.0D : Math.sqrt(nearestDistanceSquared));
    }

    public void clear() {
        this.trackedExplosions.clear();
        this.currentTick = Long.MIN_VALUE;
    }

    private static int cell(double value) {
        return (int) Math.floor(value / 2.0D);
    }

    public record ClusterMembership(int overlapCount, boolean overlapsExistingCluster, boolean exactDuplicate, double nearestDistance) {
    }

    private record TrackedExplosion(Vec3d center, float radius, int cellX, int cellY, int cellZ) {
    }
}
