# Getting started

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

## Install

1. Back up existing worlds and configuration.
2. Create a Minecraft 1.20.1 instance using Fabric Loader and Java 17.
3. Install the Fabric build of Immersive Weathering, Fabric API for 1.20.1, and
   Moonlight Lib for the same Minecraft version and loader. Do not substitute a
   Forge or NeoForge jar.
4. Launch and check for dependency or mixin errors before opening an important
   world. See the [current source versions](../developers/Reference.md); build
   versions and the declared minimum are not a guarantee that every later
   Moonlight release is compatible.

Moonlight is required for core registration, configuration, generated bark/leaf
content, and gameplay hooks. It is not an optional visual enhancement.

Mod Menu provides access to the mod's configuration screen. EMI is optional and
can display recipes/interactions, but has a [reported integration issue](Troubleshooting.md).
Consult each optional mod's own dependency requirements.

## Multiplayer

Install compatible Fabric versions of IW, Moonlight, and Fabric API on the server
and clients. Keep versions aligned while diagnosing problems. Client visual
preferences are separate from common simulation settings; change server
settings on the server rather than assuming a client's menu controls it.

## First steps

Start in a disposable world. Try collecting bark while stripping logs, rebarking
with the matching item, and observing leaf piles near decaying trees. Read
[features and interactions](Features.md) for other systems and
[configuration](Configuration.md) before changing natural world behavior.

Removing the mod can remove its blocks/items from a world. Back up first; this
guide does not promise that uninstalling or changing dependencies is lossless.
