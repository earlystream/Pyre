# Pyre Testing

All validation should be performed against Minecraft 1.21.11 with the Yarn `1.21.11+build.4` mappings target.

## Matrix

- Pyre alone
- Pyre + Sodium
- Pyre + Lithium
- Pyre + Krypton
- Pyre + BadOptimizations
- Pyre + Sodium + Lithium + FerriteCore + ImmediatelyFast
- Pyre in a medium-sized Fabric optimization stack

## Scenario coverage

- Small TNT burst in a flat test world
- Large TNT wall detonation
- Chain explosions across clustered TNT lines
- TNT plus entities near the blast edge
- Technical redstone TNT launchers and duper-style timing sanity checks
- World reload and dimension change after TNT use

## Manual validation checklist

1. Compare TNT fuse timing against vanilla with the same setup. Fuse duration and detonation tick must match.
2. Compare destroyed blocks against vanilla for the same TNT arrangement. The broken block set must match.
3. Compare entity damage and knockback against vanilla for players, mobs, and item entities at multiple distances.
4. Enable `strictCompatibilityMode` and verify Pyre still runs without crashes while keeping all explosions vanilla.
5. Disable strict mode, keep `enableExplosionQueryCache=true`, and verify the manager reports safe fallback behavior when reuse is uncertain.
6. Install known optimization mods with `autoDisableRiskyPathsWithKnownMods=true` and verify risky paths are disabled without errors.
7. Reload the world, change dimension, and repeat TNT tests. Pyre transient state must reset cleanly.
8. If `debugLogging` or `profilerMarkers` is enabled, inspect the logs for cache hits, misses, cluster reuse opportunities, explosion counts, and Pyre-vs-vanilla timing.

## Focused correctness checks

- Cache invalidation: confirm no cached support data survives a tick boundary or world unload
- Compatibility mode: confirm only safe bookkeeping remains active in strict mode
- Known-mod fallback: confirm risky paths disable automatically when configured
- Crash safety: confirm Pyre does nothing destructive when a hook cannot prove correctness
