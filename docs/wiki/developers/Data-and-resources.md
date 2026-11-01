# Data and generated resources

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Shipped data

The main data root is
[`data/immersive_weathering`](../../../common/src/main/resources/data/immersive_weathering).
Recipes, loot, tags, worldgen definitions, block growths, and fluid generators
are separate resources. Data IDs are namespaced; file paths are part of their
contracts. Inventories and tick-source names are in the
[generated reference](Reference.md).

| Area | Implementation contract |
| --- | --- |
| `block_growths` | `BlockGrowthHandler` decodes configurable or builtin growths, applies the disabled-growth list, and builds source/block dispatch sets |
| Configurable growth | `ConfigurableBlockGrowth` defines chance, owners, replacing target, direction/weighted output, position predicates, area conditions, self targeting, and destruction |
| Builtin growth | `BuiltinGrowthsRegistry` maps names such as `campfire_soot` and `leaf_piles_from_leaves` to Java implementations |
| Position and replacement tests | `data/position_tests` tests locations/world conditions; the existing `data/rute_tests` spelling contains replacement rule tests |
| `fluid_generators` | Registry-aware codecs and prioritized still/flowing-fluid dispatch; first successful generator wins |

Growth tick sources are not synonymous with vanilla random ticks. See
`ServerLevelMixin`, the sky-access path, and lightning dispatch when choosing a
source. Plant growth must respect survival, both halves of double blocks,
neighbor-shape updates, and waterlogging. Read the placement code before copying
a definition to a new block type.

## Example: override weeds

For Minecraft 1.20.1, create a datapack with a root `pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "Disable Immersive Weathering weeds"
  }
}
```

Copy the shipped
[`weeds.json`](../../../common/src/main/resources/data/immersive_weathering/block_growths/weeds.json)
to `data\immersive_weathering\block_growths\weeds.json` in that pack and change
only `growth_chance` to `0`. Preserve the rest of the valid definition. Override
[`weeds_spread.json`](../../../common/src/main/resources/data/immersive_weathering/block_growths/weeds_spread.json)
separately if spreading should also stop. Install the pack in the world's
`datapacks` directory, use `/reload`, and inspect logs and effective behavior.

Alternatively, the common growth blacklist accepts `weeds` and `weeds_spread`.
The current handler compares the resource path without namespace or `.json`,
so equal paths in different namespaces are not distinguished by that setting.
This is a broad existing contract, not a recommendation for new API design.

## Generated resources

Moonlight discovers wood/leaf families before IW dynamically registers related
content. `ServerDynamicResourcesHandler` generates pile tags and modded pile
recipes/loot plus the bark item tag. `ClientDynamicResourcesHandler` generates
models, textures, and translations.

Both handlers declare dependence on loaded packs. Texture lookup and generated
assets therefore need verification after resource-pack changes, not only the
first launch. Generation templates and transformations are IW code.

Use `debug_save_dynamic_pack` in a disposable instance to inspect output. Do not
commit incidental generated debug files or override a player's pack silently.
Missing output must produce actionable errors identifying the type/resource.
