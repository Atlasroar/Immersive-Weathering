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

The maintenance target is **Minecraft 1.20.1, Fabric only**. The current beta,
**2.1.0-beta.1** (unreleased candidate), includes standalone snow features,
the Fabric-only migration, growth optimizations, and a paired-plant placement correction.
Moonlight remains required; no snow-mod dependency is added.
The [G07 retest](players/Feature-testing.md#beta3-paired-growth-retest) is still
needed; this is not a claim that all independent gameplay reports are fixed.

The [generated reference](developers/Reference.md) records source-derived
versions, settings, entrypoints, mixins, Moonlight imports, and datapack IDs.
Gameplay descriptions are grounded in source but are not a claim of runtime
verification. Check the live issue tracker for reported problems.

Documentation must change with implementation. See
[wiki maintenance](developers/Maintenance.md).
