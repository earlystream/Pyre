# Pyre

Pyre is a vanilla-faithful Fabric mod for **Minecraft 1.21.11** built to reduce lag during **TNT-heavy gameplay** and **large explosion chains** while preserving vanilla behavior.

Pyre uses a **split, side-aware architecture inside a single mod jar**:

- a **server or integrated-server path** for real TNT and explosion optimization
- a **client-side path** for local explosion particle and sound smoothing

## What Pyre does

Pyre is made to improve performance in explosion-heavy scenarios by optimizing how explosion-related work is processed, while keeping vanilla results the same.

Pyre is designed to preserve:

- TNT fuse timing
- explosion radius and power
- block destruction results
- entity damage and knockback
- redstone behavior and technical gameplay expectations

## Design goals

Pyre follows one core principle:

> Optimize the workload, not the outcome.

The mod is built around:

- vanilla-faithful explosion behavior
- conservative, safety-first optimization
- strong compatibility with common Fabric optimization mods
- side-safe behavior across singleplayer, multiplayer, and modpacks

## How Pyre works

Pyre includes both sides inside one Fabric mod.

### Server or integrated-server side

This is the gameplay-safe optimization path.

It is responsible for:

- explosion workload scheduling
- conservative explosion query caching
- cluster-aware support reuse
- strict compatibility fallbacks
- profiling and debug hooks

This is the side that reduces the real cost of TNT and explosion simulation in:

- singleplayer
- LAN host worlds
- dedicated servers running Pyre

### Client side

This side only smooths local explosion-related effects after the server-authoritative result already exists.

It is responsible for:

- client-side explosion effect management
- particle budgeting during heavy explosion spam
- repeated explosion sound coalescing
- local explosion clustering for effect smoothing
- client lifecycle cleanup on world unload and disconnect

This path does **not** change gameplay.

## Behavior by environment

### Singleplayer

Both sides can run:

- the integrated server handles the real TNT and explosion optimization
- the client handles local explosion effect smoothing

### Multiplayer without Pyre on the server

Only the client-side smoothing is active:

- local particle and sound spam can be reduced
- server TPS and explosion simulation remain unchanged

### Multiplayer with Pyre on the server

Both sides can contribute independently:

- the server optimizes TNT and explosion workload
- the client smooths local explosion effects

## Compatibility

Pyre is designed to work cleanly alongside common optimization mods, including:

- Sodium
- Lithium
- Krypton
- FerriteCore
- ModernFix
- ImmediatelyFast
- BadOptimizations
- Noisium
- EntityCulling
- MoreCulling

Pyre does **not** try to replace rendering, networking, or wider game systems. Its scope stays focused on TNT and explosion workload handling on the simulation side, plus local explosion effect smoothing on the client.

## What Pyre does not do

Pyre does **not**:

- replace vanilla explosion logic
- change TNT behavior or fuse timing
- use fake or simplified blast physics
- combine multiple explosions into a custom composite system
- change explosion damage, knockback, or block results on the client
- modify networking or packet behavior
- replace Sodium-managed rendering paths
- affect chunk meshing, lighting, or unrelated rendering systems
- add unrelated general-purpose optimization features

## Current status

Pyre is currently centered on a compatibility-first foundation for **Minecraft 1.21.11**.

Current development includes:

- server-side or integrated-server explosion workload optimization
- conservative query caching
- cluster-aware support reuse
- client-side explosion particle and sound smoothing
- strict compatibility safeguards
- profiling and debug hooks

## Installation

1. Install **Fabric Loader** for **Minecraft 1.21.11**
2. Install **Fabric API** if the build requires it
3. Place Pyre in your `mods` folder
4. Launch the game

## Development notes

Pyre is intentionally narrow in scope and strongly focused on correctness, reviewability, side safety, and compatibility.

If an optimization risks vanilla accuracy or mod compatibility, Pyre favors the safer option.

## License

This project is licensed under the **Mozilla Public License 2.0 (MPL-2.0)**.  
See the `LICENSE` file for more information.

---

Vanilla-faithful TNT and explosion lag optimization for Fabric 1.21.11.
