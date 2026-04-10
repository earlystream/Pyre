# Pyre Compatibility

Pyre only optimizes explosion-domain support work for Minecraft 1.21.11 on Fabric. The vanilla explosion engine remains the source of truth for fuse timing, blast power, block destruction, damage, and knockback.

## What Pyre optimizes

- Tick-scoped explosion bookkeeping through a central manager
- Conservative same-tick explosion cluster tracking
- Tight-lifetime support-query caching for nearby entity scans, with vanilla fallback whenever reuse is uncertain
- Deferred cleanup of Pyre-owned transient state so bookkeeping does not amplify TNT chains

## What Pyre intentionally does not optimize

- Rendering, chunk meshing, particles, lighting, or GUI work
- Packet handling or connection behavior
- Vanilla blast ray marching, damage, knockback, or block destruction rules
- TNT fuse logic, redstone timing, or technical gameplay semantics
- Broad world, entity, or scheduling rewrites outside the explosion domain

## Strict compatibility mode

`strictCompatibilityMode=true` is the default. In this mode:

- Pyre keeps the manager, scheduler, and cluster bookkeeping active
- Riskier support-path reuse, especially cross-explosion nearby-entity query reuse, is disabled
- All explosion outcomes stay fully vanilla because Pyre only observes and coordinates

This mode is intended for large modpacks and singleplayer stacks where stability matters more than aggressive optimization.

## Behavior with common optimization mods

When mods such as Sodium, Lithium, Krypton, BadOptimizations, FerriteCore, ImmediatelyFast, ModernFix, Noisium, EntityCulling, or MoreCulling are present:

- Pyre does not touch rendering paths, so Sodium and client rendering mods are left alone
- Pyre does not patch networking, so Krypton is left alone
- Pyre does not replace vanilla explosion logic, which avoids the broad system overlap that often causes conflicts with Lithium- or BadOptimizations-style mods
- If `autoDisableRiskyPathsWithKnownMods=true`, Pyre automatically disables the risky nearby-entity query reuse path and keeps only the safest bookkeeping paths

If Pyre cannot prove that a reuse path is safe, it falls back to vanilla behavior immediately.
