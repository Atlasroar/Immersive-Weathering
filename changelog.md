## 2.1.0-beta.3 (unreleased)

- Snow-covered short ferns now use the existing frosty-fern model and its matching
  tint provider, mirroring snow-covered short grass.
- This is temporary appearance only: the underlying block remains a normal fern,
  restored when the snow is removed or melts. Tall grass and large ferns are
  unchanged; no new content IDs, settings or dependencies are added.
- Added matching model and client color-state regression controls for all eight
  layer counts, including unchanged-host controls.

Published beta.2 and the installed test-profile jar remain unchanged. The new
fern appearance needs an in-game retest on this candidate.

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