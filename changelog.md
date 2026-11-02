## Unreleased

- Independently authored standalone fire mechanics for Fabric 1.20.1: low-chance
  burning-entity block ignition, reduced projectile probability, bounded local
  heat and hot-biome bonuses, and server/common controls.
- Successful direct main-hand torch/soul-torch attacks use ordinary vanilla
  burning and consume one Survival item. Sunlight-only burns do not spread;
  legitimate later ignition can authorize the same burn.
- Exact grass blocks burn into vanilla dirt without removing the ground or
  dropping items. Existing charring, soot, snow and datapack mechanics remain.
- This is development source, not a second release of the immutable 2.1.0
  artifacts. No new dependency, soul-burning renderer/state or season integration.

## 2.1.0

Stable release consolidating the Fabric-only fork and all prerelease changes.
The final fern follow-up was prepared as 2.1.0-beta.3 but ships in 2.1.0 instead;
no beta.3 release of the 2.1.0 line was published.

### Fabric-only foundation and maintenance

- One root Fabric Loom project for Minecraft 1.20.1 and Java 17; removed IW-owned
  multiloader modules/bridges, Forge/Quark leftovers and the unfinished season bridge.
- Updated Fabric API/Loader and development integrations; retained the required
  Moonlight registration, configuration, networking, events and generated packs.
- Preserved content IDs and real registered config defaults, direct fluid-field
  access and Fabric biome registration.
- Added LGPLv3/GPLv3 companion texts and retained attribution notices in binary
  and sources jars, SemVer validation, Fabric-only artifact guards and Linux/Windows CI.
- Built and maintained player/developer wiki guides, source-generated references,
  publication tooling, upstream Fabric issue catalog and dated gameplay ledgers.

### Growth reliability and performance

- Avoided biome-supplier allocation for empty dispatch and added bounded immutable
  geometry caching with invocation-local permutations, without changing chances,
  RNG traversal, growth timing or datapack rules. Isolated benchmarks are not
  measured in-game TPS/MSPT claims.
- Fixed paired lower-half normalization, upper-half position normalization and
  vertical `above_block` placement with build-height preflight. Preserved fluid,
  obstruction, biome, weighted-output and single-output behavior.

### Snow and plants

- Added both-order snow sharing with 72 hosts: 12 fences, 22 walls and 38 plants.
  Added explicit native state schemas, English names, raw-host restoration,
  independent paired cover counts and host/snow loot handling.
- Added scheduled snow-only falling, bounded merging/remainder items, and source
  WATER-only ice conversion without moving hosts or freezing waterlogged solids.
- Added thunder-default accumulation, ordinary-snowfall opt-in, independent common
  controls/tooltips and a bounded 1-8-layer column cap through vanilla's sample.
  Gamerule 0 disables weather snow; 1 uses IW's cap; other positives intersect it.
- Allowed ordinary block-item replacement of partial snow/eligible covered plants
  through the native placement pipeline; full layer 8 and nonreplaceable hosts
  remain protected. Supported dry replacements preserve cover while mixing.
- Preserved weed ages, sapling stages, propagule state, petal amounts/orientation,
  native effects/bonemeal and growth identity checks; failed 2x2 sapling growth
  restores independent snow counts. Fixed snow stacking resets and retained
  native petal stacking with mixing disabled.
- Covered short grass and ferns use matching frosty appearances temporarily;
  melting or removing snow restores their normal raw plants.

### Final fern follow-up

- Snow-covered short ferns now use the existing frosty-fern model and its matching
  tint provider, mirroring snow-covered short grass.
- This is temporary appearance only: the underlying block remains a normal fern,
  restored when the snow is removed or melts. Tall grass and large ferns are
  unchanged; no new content IDs, settings or dependencies are added.
- Added matching model and client color-state regression controls for all eight
  layer counts, including unchanged-host controls.

The tester reported N01-N11 Pass on 2.1.0-beta.2. N12-N14 and the final fern
appearance remain unreported; these outcomes are not backfilled as stable-version
passes. Existing snowy masonry, leaf/sand mechanics and historical ledgers are
preserved. Earlier releases/tags/assets remain immutable; known unrelated issues
and unsupported plant/host types are not claimed fixed by stable promotion.

## 2.1.0-beta.2

- Added snow sharing for vanilla small flowers (including petals), saplings,
  mushrooms and dead bushes, plus IW weeds, dune grass and frosty grass/fern.
  Placement in either order retains snow instead of discarding it.
- Rendered snow-covered short grass with IW's frosty-grass model and tint.
  Shoveling or melting the cover restores the original normal grass.
- Exposed explanations for all six existing common `snow` controls without
  renaming keys, changing defaults, or requiring a new configuration dependency.
- Preserved weed ages, sapling stages, propagule state, petal amounts/orientation,
  native plant contact/animation effects, and delegated growth/bonemeal behavior.
  Growth identity hooks retain weed crowding, mushroom density and 2x2 sapling
  recognition; failed multi-sapling growth retains each original snow count.
- Fixed the replacement hook resetting a snow-item addition back to its old count.
  Petals still stack according to their native placement rules.

This is a SemVer feature prerelease. Published beta.1 and its assets remain
unchanged. Other crops, aquatic plants, vines, block
entities and arbitrary modded plants remain outside the supported dry-host list.
Manual validation of the expanded plants is still required.

## 2.1.0-beta.1

- Added snow cohabitation with explicitly supported vanilla fences, walls,
  grass/ferns and double plants, including both placement orders and host-state
  restoration on shovel use, melt and snow detachment.
- Added scheduled unsupported-snow falling, layer-conserving bounded landing,
  and ice conversion only on contact with a source WATER block.
- Added one vanilla-sampled accumulation path: thunderstorms by default,
  ordinary snowfall opt-in, maximum eight weather layers in one cell.
  Gamerule 0 disables deposition; default 1 uses IW's cap; other positive values
  intersect that cap. Vanilla initial snow remains in ordinary snowfall.
- Allowed ordinary block items to replace plain snow layers 1-7 in place,
  excluding layer 8 and nonreplaceable covered hosts. Partial covered plants
  retain raw-host replacement restrictions; supported replacements preserve snow
  while mixing is enabled, and ordinary solids replace plant and cover together.
- Added English display names for all 42 snow-covered host states.
- Kept existing snowy masonry, leaf/sand physics, growth cadence, Moonlight,
  Fabric API and all existing IDs; no snow-mod or renderer dependency.
- Added real-production transition/state/loot fixtures, static composite resources,
  artifact guards and a separate pending manual acceptance matrix.

This is a SemVer feature prerelease; previous releases remain unchanged.
Use backed-up/disposable worlds. Fixtures and launch logs are not complete
gameplay, dedicated-client synchronization or Sodium visual acceptance.

## 2.0.6-beta.3

- Corrected paired double-plant placement: do not normalize a lower half against
  its not-yet-placed upper half, and evaluate the upper half at its actual position.
  This addresses the deterministic placement failure behind G07's grass, fern,
  and seagrass transitions and the shared double-flower path.
- Kept `above_block` vertically above the target even for side/down growth faces,
  and reject paired placements crossing build-height bounds before writing.
- Preserved replacement/fluid rules, biome restrictions, area limits, weights,
  chances, random draws, single-block behavior, and dependencies.
- Added a separate real-production-class regression harness covering all 18
  bundled paired entries, neighbor-shape persistence, obstructions, water,
  build-height limits, direction semantics, and single-output differential checks.
- Investigated matching open
  [Silversmith-Mods/Immersive-Weathering#458](https://github.com/Silversmith-Mods/Immersive-Weathering/issues/458).
  No ready upstream patch was found in the bounded investigation; this correction
  was independently authored. See the wiki's technical contract for the inspected
  upstream revision.

Use backed-up or disposable worlds. Automated fixtures are not a fresh Fabric
game-loop smoke or a passed manual G07 retest. Preserve the dated beta.2 failure
report; test all three transitions, blocked/wrong-fluid controls, and G04's paired
biome outputs separately on beta.3. Icicle vibration and steel-wool reports are
outside this fix.

## 2.0.6-beta.2

- Included independently authored growth-dispatch and area-scan optimizations
  from [PR #12](https://github.com/Atlasroar/Immersive-Weathering/pull/12),
  without new dependencies or changes to growth timing, seeded traversal,
  configuration, content IDs, or datapack rules.
- Avoided biome-supplier allocation for empty growth dispatch and bounded
  immutable geometry caching for area scans with invocation-local permutations.
- Added real-code differential checks and an optional allocation benchmark;
  isolated measurements are not a claim of in-game TPS/MSPT gains.
- Adopted mod Semantic Versioning: Minecraft 1.20.1 is compatibility metadata,
  not the mod version prefix. The previous release remains unchanged.
- Fixed GitHub Wiki source links with code-formatted labels.

Use backed-up or disposable worlds. This beta does not claim to fix the
catalogued gameplay bugs, including the stone-brick growth/spread report.

## 1.20.1-2.0.6-beta.1

- Migrated to one root Fabric Loom project for Minecraft 1.20.1 and Java 17.
- Removed IW-owned Forge modules, platform annotations/bridges, Quark leftovers,
  unfinished seasonal bridge, transformer output, and historical binary copies.
- Removed unused throwing config placeholders and their unregistered enum/field;
  all real registered config keys and defaults remain unchanged.
- Kept Moonlight's required registration, config, event, networking, and dynamic
  resource behavior; retained existing Fabric flammability and stable content IDs.
- Read the actual liquid fluid field directly and register biome features through
  Fabric BiomeModifications without platform indirection.
- Updated Fabric API/Loader and optional development integrations; depend on
  separately installed Fabric Moonlight 1.20-2.12.11.
- Added deterministic Fabric source/artifact checks and embedded license and
  attribution notices in both remapped and sources jars.
- Updated the player/developer wiki and root build/CI instructions.

This is a migration prerelease after 2.0.5, not a blanket fix for the gameplay
issue catalog. Use backed-up or disposable worlds.

## 1.20.1-2.0.5

Added an area-loaded check to fluid generators to prevent an unknown blocking
issue when fluids are near unloaded chunks.