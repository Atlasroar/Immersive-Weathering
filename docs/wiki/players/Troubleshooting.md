# Troubleshooting

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

## Reported Fabric 1.20.1 problems

The following is the initial catalog established on November 1, 2026, not a
live issue-status table. Reports were imported from upstream and have not all
been reproduced on this fork's current build. Follow the linked issues for
current status and any verified fixes.

| Report | Safe next step |
| --- | --- |
| [Loamy farmland rejects crops](https://github.com/Atlasroar/Immersive-Weathering/issues/1) | Compare with ordinary farmland; include lighting, plant ID, and mod list in a reproduction. |
| [Falling-layer errors and resource usage](https://github.com/Atlasroar/Immersive-Weathering/issues/2) | Preserve the full log and profile a disposable reproduction; do not merely hide log messages. |
| [EMI plugin loading exception](https://github.com/Atlasroar/Immersive-Weathering/issues/3) | Record EMI/version information and the entire plugin exception. |
| [Wilder Wild piles crash over water](https://github.com/Atlasroar/Immersive-Weathering/issues/4) | Upstream reports minimal Minecraft particles or blacklisting the original leaves as workarounds; these are not verified fixes. Back up before revisiting affected chunks. |
| [Floating ivy after tree felling](https://github.com/Atlasroar/Immersive-Weathering/issues/5) | Record Panda's Falling Trees version and the supporting-block layout. |
| [Leaf piles block sapling growth](https://github.com/Atlasroar/Immersive-Weathering/issues/6) | Remove nearby piles in a test area and compare the growth result. |
| [Soot ignores its toggle](https://github.com/Atlasroar/Immersive-Weathering/issues/7) | Confirm server/common settings; include the effective file and restart/reload steps. |
| [Diagonal Walls compatibility](https://github.com/Atlasroar/Immersive-Weathering/issues/9) | Compare affected and unaffected walls in the same layout. |
| [Water Erosion property conflict](https://github.com/Atlasroar/Immersive-Weathering/issues/8) | Test the mods separately in copies of the world and capture the full combined stack trace. |

[Browse current Fabric issues](https://github.com/Atlasroar/Immersive-Weathering/issues?q=is%3Aissue+is%3Aopen+label%3Afabric+label%3Aminecraft-1.20.1).
Do not file Forge/NeoForge support requests against this fork.

## Before reporting

1. Back up the affected world and reproduce in a disposable copy.
2. Record Minecraft, Fabric Loader, Fabric API, IW, Moonlight, and relevant
   compatibility-mod versions. Note singleplayer versus dedicated server.
3. Save the matching `latest.log`, crash report if present, configuration changes,
   datapacks, and a minimal numbered reproduction. Use a profiler for lag.
4. Reduce the mod set without modifying your only world copy.
5. Check existing issues and add evidence to a matching report rather than opening
   a duplicate. Remove credentials, server addresses, and other private data
   from logs before sharing.

Missing generated textures are not automatically Moonlight bugs. IW owns its
generation rules and may encounter unusual mod models or resource-pack layouts.
For technical diagnosis, see the [developer architecture map](../developers/Architecture.md).
