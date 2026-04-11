package pyre.common.config;

public final class PyreServerConfig {
    public boolean enabled = true;
    public boolean strictCompatibilityMode = true;
    public boolean enableExplosionQueryCache = true;
    public boolean enableClusterReuse = true;
    public double maxClusterRadius = 4.0D;
    public boolean debugLogging = false;
    public boolean profilerMarkers = false;
    public boolean autoDisableRiskyPathsWithKnownMods = true;
}
