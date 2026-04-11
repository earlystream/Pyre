package pyre.client.manager;

public final class ClientExplosionParticleBudgeter {
    private long currentTick = Long.MIN_VALUE;
    private int usedPrimaryParticles;

    public boolean tryAcquire(long tick, boolean exactMode) {
        if (this.currentTick != tick) {
            this.currentTick = tick;
            this.usedPrimaryParticles = 0;
        }

        int budget = exactMode ? 32 : 16;
        if (this.usedPrimaryParticles >= budget) {
            return false;
        }

        this.usedPrimaryParticles++;
        return true;
    }

    public void clear() {
        this.currentTick = Long.MIN_VALUE;
        this.usedPrimaryParticles = 0;
    }
}
