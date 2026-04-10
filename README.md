# Pyre

Pyre is a vanilla-faithful Fabric mod for **Minecraft 1.21.11** focused on reducing lag from **TNT-heavy situations** and **explosion chains** without changing vanilla behavior.

## What Pyre does

Pyre is designed to make explosion-heavy gameplay smoother by optimizing how explosion-related work is handled internally while keeping vanilla results intact.

That means Pyre aims to preserve:

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
- singleplayer-first design, especially for integrated server TNT load

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

Pyre does **not** try to replace rendering, networking, or broad game systems. It stays scoped to TNT and explosion workload handling to reduce compatibility risk in larger modpacks.

## What Pyre does not do

Pyre does **not**:

- replace vanilla explosion logic
- change TNT behavior or fuse countdown
- use fake or simplified blast physics
- merge multiple explosions into a custom composite explosion
- add unrelated general-purpose performance features
- touch rendering pipelines, chunk meshing, lighting, or packet handling

## Current status

Pyre is focused on a minimal, compatibility-first foundation for **Minecraft 1.21.11**.

Current development is centered on:

- explosion workload scheduling
- conservative query caching
- cluster-aware support reuse
- strict compatibility fallbacks
- debug and profiling hooks

## Installation

1. Install **Fabric Loader** for **Minecraft 1.21.11**
2. Install **Fabric API** if required by the build
3. Place Pyre in your `mods` folder
4. Launch the game

## Development notes

Pyre is intentionally being developed with a narrow scope and a strong focus on correctness, reviewability, and mod compatibility.

If a possible optimization risks vanilla accuracy or mod compatibility, the safer path wins.

## License

This project is licensed under the **Mozilla Public License 2.0 (MPL-2.0)**.
See the `LICENSE` file for details.

---
Vanilla-faithful TNT and explosion lag optimization for Fabric 1.21.11.
