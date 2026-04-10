# Pyre

Pyre is a vanilla-faithful Fabric mod for **Minecraft 1.21.11** focused on reducing lag from **TNT-heavy situations** and **explosion chains** without changing vanilla behavior.

Pyre now uses a **split side-aware architecture inside one mod jar**:
- a **server or integrated-server path** for real TNT and explosion optimization
- a **client-side path** for local explosion visual and audio smoothing

## What Pyre does

Pyre is designed to make explosion-heavy gameplay smoother by optimizing how explosion-related work is handled while keeping vanilla results intact.

Pyre aims to preserve:

- TNT fuse timing
- explosion power and radius
- block destruction behavior
- entity damage and knockback
- redstone and technical gameplay expectations

## Design goals

Pyre follows a simple rule:

> Optimize when work happens, not what the result is.

The mod is built around:

- vanilla-faithful explosion handling
- conservative, safety-first optimization paths
- strong compatibility with common Fabric optimization mods
- side-safe architecture for singleplayer, multiplayer, and modpack use

## How Pyre works

Pyre keeps both halves inside a single Fabric mod:

### Server or integrated-server side
This is the real gameplay-safe optimizer.

It handles:
- explosion workload scheduling
- conservative explosion query caching
- cluster-aware support reuse
- strict compatibility fallbacks
- profiler and debug hooks

This is the path that improves actual TNT and explosion simulation cost in:
- singleplayer
- LAN host worlds
- dedicated servers with Pyre installed

### Client side
This path only smooths local explosion-related effects after the authoritative result is already known.

It handles:
- client-side explosion effect management
- particle budgeting during explosion spam
- repeated explosion sound coalescing
- local explosion clustering for effect smoothing
- client lifecycle cleanup on world unload and disconnect

This path does **not** change gameplay.

## Behavior by environment

### Singleplayer
Both sides can run:
- the integrated server gets the real TNT and explosion optimization
- the client gets local explosion effect smoothing

### Multiplayer without Pyre on the server
Only the client-side smoothing applies:
- local particle and sound spam can be reduced
- remote server TPS and explosion simulation are unchanged

### Multiplayer with Pyre on the server
Both sides help independently:
- the server can optimize TNT and explosion workload
- the client can smooth local explosion effects

## Compatibility

Pyre is built to coexist cleanly with common optimization mods, including:

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

Pyre does **not** try to replace rendering, networking, or broad game systems. It stays scoped to TNT and explosion workload handling on the simulation side, plus local explosion effect smoothing on the client side.

## What Pyre does not do

Pyre does **not**:

- replace vanilla explosion logic
- change TNT behavior or fuse countdown
- use fake or simplified blast physics
- merge multiple explosions into a custom composite explosion
- change explosion damage, knockback, or block results on the client
- modify packet semantics or networking behavior
- replace Sodium-managed rendering paths
- touch chunk meshing, lighting, or unrelated rendering systems
- add unrelated general-purpose performance features

## Current status

Pyre is currently focused on a compatibility-first foundation for **Minecraft 1.21.11**.

Current development includes:

- server-side or integrated-server explosion workload optimization
- conservative query caching
- cluster-aware support reuse
- client-side explosion particle and sound smoothing
- strict compatibility safeguards
- debug and profiling hooks

## Installation

1. Install **Fabric Loader** for **Minecraft 1.21.11**
2. Install **Fabric API** if required by the build
3. Place Pyre in your `mods` folder
4. Launch the game

## Development notes

Pyre is intentionally developed with a narrow scope and a strong focus on correctness, reviewability, side safety, and mod compatibility.

If a possible optimization risks vanilla accuracy or compatibility, the safer path wins.

## License

This project is licensed under the **Mozilla Public License 2.0 (MPL-2.0)**.  
See the `LICENSE` file for details.

---
Vanilla-faithful TNT and explosion lag optimization for Fabric 1.21.11.
