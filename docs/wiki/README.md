# Immersive Weathering wiki

| Players: features and instructions | Developers: architecture and contribution |
| --- | --- |
| **[Enter the player guide](players/README.md)** | **[Enter the developer guide](developers/README.md)** |
| Learn what the mod adds and how to install, use, configure, and troubleshoot it. | Understand the source, Moonlight dependency, datapacks, build, and maintenance process. |

This is the repository's version-controlled wiki, also published to the
[GitHub Wiki tab](https://github.com/Atlasroar/Immersive-Weathering/wiki).
The repository copy is authoritative; the published Wiki is an exported mirror.
The two linked guide hubs serve as the player and developer navigation tabs.

## Scope and accuracy

The maintenance target is **Minecraft 1.20.1, Fabric only**. The current source
and stable release target is **2.5.0**, adding
[freezing wind and cold thunder snowstorms](players/Features.md#freezing-wind-and-thunder-snowstorms)
with pinned artwork, relightable unlit torches and no thermal-system dependency.
All [FW01-FW08](players/Feature-testing.md#freezing-wind-and-thunder-snowstorms)
manual scenarios remain **Not run**. Prior contents include
[standalone sandstorms](players/Features.md#standalone-sandstorms) with original
dust/wind assets, fog, slowdown and IW sand-layer deposition without new
dependencies or cauldron changes. The [ST01-ST08 checklist](players/Feature-testing.md#standalone-sandstorms)
remains **Not run**. Prior contents include
[standalone fire](players/Features.md#standalone-fire), standalone snow features,
the Fabric-only migration, growth optimizations, and a paired-plant placement correction.
Moonlight remains required; no snow-mod dependency is added.
Fire adds no dependency either. The tester reported a broad 2.2.0 fire-feature
pass; individual [F01-F06 variants](players/Feature-testing.md#standalone-fire)
remain unreported. Automated rule checks are not manual acceptance.
Consult [published releases](https://github.com/Atlasroar/Immersive-Weathering/releases)
for available binaries rather than inferring publication from source version.
Version 2.4.0 adds [sand-pile sharing and rain erosion](players/Features.md#sand-piles)
plus configurable [thunder fire suppression](players/Configuration.md#standalone-fire).
For the [SP01-SP08 checklist](players/Feature-testing.md#sand-and-thunder),
the tester reported **SP01-SP07 Pass; SP08 Not applicable** on 2026-11-02.
These are test-ID-level results, not a multiplayer/coexistence pass.
Published 2.3.0 remains immutable and does not include these additions.
It includes expanded dry-plant snow sharing and temporary frosty appearances
for covered short grass and ferns. Published prereleases remain unchanged;
see the [plant retests](players/Feature-testing.md#standalone-snow-candidate-retest).
The tester reported **N01-N11 Pass** after the beta.2 deployment; **N12-N14 remain
unreported**. The [G07 retest](players/Feature-testing.md#beta3-paired-growth-retest)
has a broad N11 follow-up, not separately itemized transitions. This is not a
claim that all independent gameplay reports are fixed.

The final fern appearance ships in stable 2.1.0 rather than a separate beta.3
release. The beta.2 passes do not verify that new appearance; retest N02/N12.
Stable release status does not change the scope or provenance of manual results.

The [generated reference](developers/Reference.md) records source-derived
versions, settings, entrypoints, mixins, Moonlight imports, and datapack IDs.
Gameplay descriptions are grounded in source but are not a claim of runtime
verification. Check the live issue tracker for reported problems.

Documentation must change with implementation. See
[wiki maintenance](developers/Maintenance.md).
