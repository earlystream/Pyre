# Pyre

Pyre is a vanilla-faithful Fabric mod focused on reducing lag from TNT-heavy gameplay and large explosion chains without changing vanilla outcomes.

Pyre keeps a split, side-aware architecture inside a single mod:

- a server or integrated-server path for real explosion workload optimization
- a client-side path for local explosion sound and particle smoothing

## Supported versions

Pyre currently targets Fabric on exact Minecraft versions from `1.21` through `1.21.11`.

This repository builds separate jars for each supported Minecraft version rather than pretending one universal jar safely covers every internal variation.

## What Pyre does

Pyre is built to reduce the cost of explosion-heavy scenarios while preserving vanilla behavior.

Pyre is designed to preserve:

- TNT fuse timing
- explosion radius and power
- block destruction results
- entity damage and knockback
- redstone behavior and technical gameplay expectations

## Core principle

> Optimize the workload, not the outcome.

That means Pyre does not replace vanilla explosion logic. It only reorganizes or reuses narrow support work when doing so is safe.

## How it works

### Server or integrated-server side

This is the real optimization path.

It is responsible for:

- explosion lifecycle coordination
- conservative same-tick query caching
- cluster-aware overlap tracking
- strict compatibility fallbacks
- lightweight profiling and debug hooks

This is the side that can reduce the real CPU cost of TNT and explosion simulation in:

- singleplayer
- LAN host worlds
- dedicated servers running Pyre

### Client side

This side is cosmetic and local only.

It is responsible for:

- particle budgeting during explosion spam
- repeated explosion sound coalescing
- local packet clustering for effect smoothing
- cleanup on world unload and disconnect

This path does not change gameplay, server authority, or packet semantics.

## Behavior by environment

### Singleplayer

Both sides can run:

- the integrated server handles the real TNT and explosion optimization
- the client smooths local explosion effects

### Multiplayer without Pyre on the server

Only the client-side smoothing is active:

- local sound and particle spam can be reduced
- server TPS and remote explosion simulation are unchanged

### Multiplayer with Pyre on the server

Both sides can help independently:

- the server optimizes TNT and explosion workload
- the client smooths local explosion effects

## Compatibility

Pyre is designed to coexist with common optimization stacks, including:

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

Pyre stays deliberately narrow in scope. It does not try to replace rendering systems, networking behavior, or broad engine internals that other optimization mods already touch.

## What Pyre does not do

Pyre does not:

- replace vanilla explosion logic
- change TNT behavior or fuse timing
- use fake or simplified blast physics
- combine multiple explosions into a custom composite explosion system
- change client-side gameplay outcomes
- modify packet behavior or networking semantics
- replace Sodium-managed rendering paths
- affect chunk meshing, lighting, or unrelated rendering systems
- add unrelated general-purpose optimization features

## Installation

1. Install Fabric Loader for your exact supported Minecraft version.
2. Install Fabric API.
3. Use the Pyre jar built for that exact Minecraft version.
4. Place it in your `mods` folder.

## Project status

Pyre currently focuses on a compatibility-first foundation for Fabric `1.21` through `1.21.11`, with:

- server-side explosion workload optimization
- conservative nearby-entity query reuse
- cluster-aware overlap tracking
- client-side explosion effect smoothing
- strict compatibility safeguards
- profiling and debug hooks

## Development notes

Pyre favors correctness, side safety, and compatibility over aggressive optimization.

If an optimization risks vanilla accuracy or mod compatibility, Pyre falls back to the safer path.

## License

This project is licensed under the Mozilla Public License 2.0 (`MPL-2.0`).

