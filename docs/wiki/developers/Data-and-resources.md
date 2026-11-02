# Data and generated resources

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Shipped data

The main data root is
[`data/immersive_weathering`](../../../src/main/resources/data/immersive_weathering).
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

The `generate_if_not_too_many` area condition shifts its center by `yOffset`
before seeding both traversal and predicate randomness. Its optimized traversal
preserves the original shuffled Manhattan position order, possible-block/includes
short circuit, `must_have`/`else if must_not_have` predicate order, and early-exit
rules, including existing `requiredAmount` -1/0 edge cases. This allocation change
does not reinterpret those values, change JSON/config defaults or chances, skip
callbacks, throttle ticks, or alter sky-tick RNG/cadence. The geometry budget is
an implementation detail with baseline fallback, not a datapack radius limit.

## Paired output placement

[`BlockPair`](../../../src/main/java/com/ordana/immersive_weathering/data/block_growths/BlockPair.java)
encodes `block` at the selected target and optional `above_block` one block
**vertically above that target**, not along the growth face. `target_self`
selects the owner position; otherwise the face chooses the adjacent target.
The same production placement path handles G07's three plants, biome-specific
double flowers, and dark-forest fence/pumpkin pairs.

Before beta.3, the lower double-plant state was normalized using
`Block.updateFromNeighbourShapes` before its upper existed. Vanilla
`DoublePlantBlock.updateShape` consequently returned air. Independently, the upper
was normalized at the owner position rather than its actual destination, reading
the wrong supporting neighbor. Reintroducing either defect alone makes the
production-class regression fail with actual air writes and missing halves.

Beta.3 follows vanilla's lower-half placement approach:
`DoublePlantBlock.copyWaterloggedFrom` prepares paired double-plant lowers without
the premature shape normalization; normal upper preparation occurs at the real
upper position after the lower is placed. Other paired lowers and all single
outputs keep their existing shape/waterlogging helper. Writes remain sequential,
lower then upper, using normal update flags; this is **not an atomic world
transaction**. Both paired destinations must be inside build height, and the
existing upper replacement predicate and area condition must pass before any
write. Shipped seagrass requires a water **block** above, not merely fluid inside
a waterlogged obstruction. No bundled JSON, chance, weight, biome restriction,
density logic, or RNG call order changes.

The upstream investigation matched open
[Silversmith-Mods/Immersive-Weathering#458](https://github.com/Silversmith-Mods/Immersive-Weathering/issues/458).
The inspected upstream
[`ConfigurableBlockGrowth` at `1bcc03b6754d6840f3b3ab328744af16e06bdb9a`](https://github.com/Silversmith-Mods/Immersive-Weathering/blob/1bcc03b6754d6840f3b3ab328744af16e06bdb9a/common/src/main/java/com/ordana/immersive_weathering/data/block_growths/growths/ConfigurableBlockGrowth.java#L205-L243)
and tall-plant definitions retain the same problematic path. Bounded branch,
history, and PR searches found no ready upstream fix; that is not a guarantee
about all upstream work. This correction was independently authored in the
existing LGPL code, not copied from an external patch.

The [regression harness](Development.md#paired-growth-regression-harness) covers
every bundled paired entry's placement. It does not certify arbitrary modded
`above_block` combinations, custom neighbor callbacks, chunk writes, loot drops,
or real biome dispatch; retain normal custom-block survival requirements and
test datapack overrides in Fabric. The dated beta.2 G07 failure remains in the
player ledger; beta.3 requires a fresh manual retest.

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
[`weeds.json`](../../../src/main/resources/data/immersive_weathering/block_growths/weeds.json)
to `data\immersive_weathering\block_growths\weeds.json` in that pack and change
only `growth_chance` to `0`. Preserve the rest of the valid definition. Override
[`weeds_spread.json`](../../../src/main/resources/data/immersive_weathering/block_growths/weeds_spread.json)
separately if spreading should also stop. Install the pack in the world's
`datapacks` directory, use `/reload`, and inspect logs and effective behavior.

Alternatively, the common growth blacklist accepts `weeds` and `weeds_spread`.
The current handler compares the resource path without namespace or `.json`,
so equal paths in different namespaces are not distinguished by that setting.
This is a broad existing contract, not a recommendation for new API design.

## Snow host data and persistence

The [snow architecture](Architecture.md#standalone-snow-ownership) defines the
new deterministic `immersive_weathering:snow_covered_<host_path>` IDs. These
are ordinary block states with `layers=1..8` and the dry host's property values;
double plants retain `half`; added saplings, propagules, petals, weeds and frosty
plants retain their native stage/age/facing/amount/natural properties. The 72-host
list is explicit, not an arbitrary plant-class discovery rule.
They do not have block items or block entities.
Do not remap old `snowy_*` masonry IDs, change their growth JSON, or migrate world
data into these containers.
Exact block/state predicates still distinguish a covered ID from its raw host;
datapacks must explicitly include the covered ID and retained properties where
that distinction should be ignored. The narrow native plant-identity hooks are
not global RuleTest overrides. Shipped growth definitions and their existing
snow-target eligibility are unchanged by this placement/cohabitation follow-up.

`tools/snow_resources.py` derives the explicit host inventory from `SnowHosts`
and generates static multipart blockstates, eight snow models and IW semantic
tags. Run it after intentional host/resource changes, then `--check`. Vanilla
append-only tags reference these IW tags for fences, walls, mining, flowers and
saplings; keep existing tag values. Native weed age models and frosty-grass/fern
appearances are reused rather than copied. Covered normal grass and short ferns
retain their raw identities and loot despite their temporary frosty models/tints.
The fern model/tint mapping ships in stable 2.1.0.
The supported vanilla hosts do not require a raised
mining tier: do not infer `needs_stone_tool` from a blackstone/deepslate name.
Original host loot tables remain authoritative and can still be overridden by
datapacks. The snow half evaluates the vanilla snow table with the current
player/tool context; unsupported landing overflow instead returns layer items.

The falling entity stores a vanilla `minecraft:snow` block state and its count
in the existing `BlockState` NBT; it never stores or moves the hidden host.
Layer 8 remains `minecraft:snow[layers=8]`, not an automatic conversion to the
snow-block ID. Source-water contact is the one intentional ice-conversion sink.
No custom per-cell network payload or new migration/data-fixer schema is added.

Common `snow.*` settings are not datapack growth definitions. Extra accumulation
uses only vanilla's sampled server chunk hook, not a second block-growth entry.
The [configuration contract](../players/Configuration.md#standalone-snow) explains
the bounded column cap and gamerule-1 sentinel. Existing growths remain independent.

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
