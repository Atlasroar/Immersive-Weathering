# Players: features and instructions

[Wiki home](../README.md) | **Players** | [Developers](../developers/README.md)

| Guide | Contents |
| --- | --- |
| [Getting started](Getting-started.md) | Fabric installation, required dependencies, multiplayer, and backups |
| [Features and interactions](Features.md) | Weathering, leaf piles, bark, soils, frost, sand piles, fire and practical controls |
| [In-game feature test sheet](Feature-testing.md) | Source-backed setups, expected results, controls, and a stone-brick comparison matrix |
| [Configuration](Configuration.md) | Client versus simulation settings, leaf blacklists, and datapacks |
| [Troubleshooting](Troubleshooting.md) | Known Fabric reports, safe workarounds, and useful bug-report information |

This fork targets Minecraft 1.20.1 on Fabric. Some systems have reported bugs;
read troubleshooting before depending on them in an existing world. Modpacks
can override settings, tags, and recipes, so the same mod may behave differently
in different packs.

Unreleased [freezing wind and thunder snowstorms](Features.md#freezing-wind-and-thunder-snowstorms)
have a separate [FW01-FW08 checklist](Feature-testing.md#freezing-wind-and-thunder-snowstorms),
all **Not run**. Version stays 2.4.0; do not infer these features are in its
already-published binary.

Version 2.4.0's [sand sharing/rain erosion](Features.md#sand-piles) and
[thunder fire suppression](Configuration.md#standalone-fire) are separate from
published 2.3.0; see [SP01-SP08](Feature-testing.md#sand-and-thunder).
The tester reported **SP01-SP07 Pass; SP08 Not applicable** on 2026-11-02.
See the dated ledger rather than the reusable checklist's blank template cells.
