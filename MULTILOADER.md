# Pyre Multiloader Notes

Pyre is being refactored toward a real multiloader and multiversion layout. This file records
the audited target groups so future build modules and hook shims can stay honest.

## Shared logic

The following code is intended to stay shared across loaders wherever the underlying Minecraft
runtime shape still matches:

- `pyre.common.config`
- `pyre.common.compat`
- `pyre.common.debug`
- `pyre.common.manager`
- `pyre.client.manager`
- `pyre.client.debug`

These packages implement Pyre's actual optimization behavior. They do not own loader bootstrap,
event bus wiring, or metadata format.

## Loader-specific code

Current Fabric-only wiring now lives under:

- `pyre.fabric`
- `pyre.fabric.client`
- `pyre.fabric.lifecycle`
- `pyre.fabric.platform`

This split exists so Quilt and NeoForge can supply their own bootstrap and lifecycle adapters
without changing the shared explosion managers.

## Version families

The audited `1.21` Fabric line splits into four hook families that should not be conflated:

1. `1.21` through `1.21.1`
   - Uses the older `net.minecraft.world.explosion.Explosion` pipeline
   - Server hook lives on `Explosion.collectBlocksAndDamageEntities()`
   - Client packet handling constructs an `Explosion` and then calls `Explosion.affectWorld(true)`

2. `1.21.2` through `1.21.4`
   - Uses `net.minecraft.world.explosion.ExplosionImpl`
   - `ExplosionImpl.explode()` returns `void`
   - Client packet handling calls `ClientWorld.playSound(...)` and `ClientWorld.addParticle(...)`

3. `1.21.5` through `1.21.8`
   - Uses `net.minecraft.world.explosion.ExplosionImpl`
   - `ExplosionImpl.explode()` still returns `void`
   - Client packet handling moved to `playSoundClient(...)` and `addParticleClient(...)`

4. `1.21.9` through `1.21.11`
   - Uses `net.minecraft.world.explosion.ExplosionImpl`
   - `ExplosionImpl.explode()` returns `int`
   - Client packet handling adds `ClientWorld.addBlockParticleEffects(...)`

5. `26.1`, `26.1.1`, `26.1.2`
   - Real Minecraft release targets, not loader versions
   - Official Fabric game metadata and Fabric API artifacts exist for these versions
   - Official Yarn artifacts were not available during this audit, so these versions need either
     an official-mappings path or a separate mapping strategy before support can be claimed

## NeoForge target prefixes

NeoForge artifacts use their own version line. The official NeoForge maven metadata resolves the
requested targets to these prefixes:

- `20.4` for Minecraft `1.20.4`
- `20.5` for Minecraft `1.20.5`
- `20.6` for Minecraft `1.20.6`
- `21.1` through `21.11` for Minecraft `1.21.1` through `1.21.11`
- `26.1`, `26.1.1`, `26.1.2` for the `26.1.x` release line

## Honesty rules

Pyre should only claim support for a loader/version output when all of the following are true:

- the module compiles against that loader and target version
- the loader metadata matches the produced artifact
- the mixin targets and injection points were validated for that exact target family
- client-only classes stay isolated from dedicated-server classloading
- fallback behavior remains conservative when compatibility is uncertain
