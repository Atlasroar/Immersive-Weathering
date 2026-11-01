# Getting started

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

## Install

1. Back up existing worlds and configuration.
2. Create a Minecraft 1.20.1 instance using Fabric Loader **0.16.14 or newer**
   and Java 17.
3. Install the Fabric build of Immersive Weathering, Fabric API for 1.20.1, and
   Moonlight Lib for the same Minecraft version and loader. Do not substitute a
   Forge or NeoForge jar.
4. Launch and check for dependency or mixin errors before opening an important
   world. See the [current source versions](../developers/Reference.md); build
   versions and the declared minimum are not a guarantee that every later
   Moonlight release is compatible.

The current prerelease is **2.0.6-beta.2**. Its mod version follows Semantic
Versioning; Minecraft **1.20.1** is declared separately as compatibility.
Install `immersive_weathering-2.0.6-beta.2.jar`. The development dependency set
uses Fabric API **0.92.12+1.20.1** and Fabric Moonlight **1.20-2.12.11**
(CurseForge file **5619072**). Moonlight and Fabric API must be installed
separately; neither is bundled. Replace the previous IW jar, and never install
the `-sources.jar` or development jar. This beta includes the Fabric-only build
and allocation optimizations, not changed world IDs or a promise to fix every
catalogued gameplay report. Growth timing and seeded traversal are unchanged
in differential checks; in-game performance still needs testing.

Moonlight is required for core registration, configuration, generated bark/leaf
content, and gameplay hooks. It is not an optional visual enhancement.

Mod Menu **7.2.2** provides access to the mod's configuration screen. Cloth
Config **11.1.136** and EMI **1.1.24+1.20.1** are optional development companions.
EMI is optional and
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
