package pyre.client.manager;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class ClientExplosionSoundCoalescer {
    private long currentTick = Long.MIN_VALUE;
    private final List<Vec3d> emittedSounds = new ArrayList<>();

    public boolean shouldPlay(long tick, Vec3d center, boolean exactMode) {
        if (this.currentTick != tick) {
            this.currentTick = tick;
            this.emittedSounds.clear();
        }

        double maxDistance = exactMode ? 1.0D : 3.0D;
        double maxDistanceSquared = maxDistance * maxDistance;
        for (Vec3d emitted : this.emittedSounds) {
            if (emitted.squaredDistanceTo(center) <= maxDistanceSquared) {
                return false;
            }
        }

        this.emittedSounds.add(center);
        return true;
    }

    public void clear() {
        this.currentTick = Long.MIN_VALUE;
        this.emittedSounds.clear();
    }
}
