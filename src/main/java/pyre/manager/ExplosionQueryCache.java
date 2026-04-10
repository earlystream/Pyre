package pyre.manager;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ExplosionQueryCache {
    private long currentTick = Long.MIN_VALUE;
    private final Map<QueryKey, List<Entity>> cachedQueries = new HashMap<>();

    // Cache lifetime is deliberately one world tick at most. If the tick changes, Pyre discards
    // everything instead of assuming cross-tick world state is still valid.
    public void beginTick(long tick) {
        if (this.currentTick == tick) {
            return;
        }

        this.currentTick = tick;
        this.cachedQueries.clear();
    }

    public Optional<List<Entity>> get(long tick, @Nullable Entity except, Box box) {
        this.beginTick(tick);
        return Optional.ofNullable(this.cachedQueries.get(QueryKey.from(tick, except, box)));
    }

    public void put(long tick, @Nullable Entity except, Box box, List<Entity> entities) {
        this.beginTick(tick);
        this.cachedQueries.put(QueryKey.from(tick, except, box), List.copyOf(entities));
    }

    public void clear() {
        this.cachedQueries.clear();
        this.currentTick = Long.MIN_VALUE;
    }

    private record QueryKey(long tick, int excludedEntityId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        private static QueryKey from(long tick, @Nullable Entity except, Box box) {
            int excludedEntityId = except == null ? -1 : except.getId();
            return new QueryKey(
                    tick,
                    excludedEntityId,
                    floor(box.minX),
                    floor(box.minY),
                    floor(box.minZ),
                    ceil(box.maxX),
                    ceil(box.maxY),
                    ceil(box.maxZ)
            );
        }

        private static int floor(double value) {
            return (int) Math.floor(value);
        }

        private static int ceil(double value) {
            return (int) Math.ceil(value);
        }
    }
}
