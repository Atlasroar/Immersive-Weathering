## Unreleased

- Standalone Fabric 1.20.1 sandstorms adapted from Scorchful's pinned LGPLv3
  1.20.1 source: imported dust sprites and nine wind sounds, weather/biome-blended
  fog, exposed-entity slowdown and existing IW sand/red-sand layer deposition.
- Vanilla rain drives dry desert/badlands storms; common/server and independent
  client controls, no new dependencies, no thirst/temperature or cauldron changes.
  Legacy sandy-block growth, including its warm-ocean tag, is unchanged.
- Fixed upstream at-cap pile reduction; bounded loaded/dry/supported deposition,
  live temporary-modifier cleanup and guarded vanilla fog. All new IW storm
  behavior yields to installed Scorchful to avoid duplicate mechanics/effects.
- Exact pinned asset hashes, source/modification notices and candid audio
  provenance are preserved in source and both distribution jars.
- Automated storm/native fixtures and resource guards are not a visual/gameplay
  pass. New [ST01-ST08 manual cases](docs/wiki/players/Feature-testing.md#standalone-sandstorms)
  are **Not run**. No new release, tag, deployment or version bump is implied.

## 2.2.0

Stable release target for Minecraft **1.20.1**, **Fabric** and **Java 17**.
Requires Fabric Loader **0.16.14+**, Fabric API **0.92.12+1.20.1** and
Fabric Moonlight **1.20-2.12.11** (CurseForge file **5619072**).
Install only `immersive_weathering-2.2.0.jar`, replacing the previous IW jar
after backing up worlds/configuration. Sources are distributed separately.
Published 2.1.0 and prerelease artifacts, tags and historical results remain
unchanged. This heading records the release contents, not proof of publication.

### New standalone fire features

- Independently authored standalone fire mechanics for Fabric 1.20.1: low-chance
  burning-entity block ignition, reduced projectile probability, bounded local
  heat and hot-biome bonuses, and server/common controls.
- Successful direct main-hand torch/soul-torch attacks use ordinary vanilla
  burning and consume one Survival item. Sunlight-only burns do not spread;
  legitimate later ignition can authorize the same burn.
- Exact grass blocks burn into vanilla dirt without removing the ground or
  dropping items. Existing charring, soot, snow and datapack mechanics remain.
- No new dependency, soul-burning renderer/state or season integration.
- Server/common `fire` controls default to 2% per 20-tick entity-staggered trial,
  at most one new cell, projectile multiplier 1/16, spread radius 1, heat radius 2,
  eight counted heat cells, heat bonus 0.125, temperature bonus 0.5 above biome
  base temperature 0.8 (clamped at 2), and pre-projectile probability cap 0.25.
  Maximum default heat/hot trials are about 6.4%, or 0.4% for projectiles.
- Only FIRE, SOUL_FIRE, MAGMA_BLOCK and LAVA count as heat. Dry, supported,
  already-loaded air/fuel checks prevent occupied/wet-cell replacement and
  chunk forcing. Entity fire does not create portals. `doFireTick=false`
  disables environmental spread and grass consumption; direct torch combat
  remains independent. Non-player carriers obey `mobGriefing`, except
  player-owned projectiles.

### Run checklist and validation limits

All **F01-F06 manual cases are Not run**. Do not treat stable versioning,
automated fixtures, CI or historical snow passes as a fire gameplay pass.
Use a disposable/backed-up world, the remapped 2.2.0 binary, aligned client/server
dependencies, and record source SHA, config, gamerules, biome and weather.
The [authoritative runnable instructions](docs/wiki/players/Feature-testing.md#standalone-fire)
include fixture commands, controlled rates, cleanup and expected outcomes.

| Case | Run steps | Expected result | Status |
| --- | --- | --- | --- |
| F01 | At noon, summon an unhelmeted zombie with tag `iw_fire`, inspect `Fire` and `immersive_weathering:non_sun_fire`, then apply each real ignition source separately; add fuel only after removing the source fire. Repeat skeleton/phantom sunlight controls and failed projectile hits. | Sun-only/failed-hit provenance remains false; successful independent fire/lava/lightning/torch/Fire Aspect/burning-arrow/small-fireball ignition authorizes the same active burn. | Not run |
| F02 | In Survival, hit the high-health fixture with a stack of 16 torches, then soul torches; repeat Creative, invulnerable/blocked hits, offhand, wet/fire-resistant/fire-immune/dead targets and an existing `Fire:400s` burn. | One item only for supported successful Survival ignition; ordinary four-second burning with no shortening or soul rendering; Creative and rejected hits keep items. | Not run |
| F03 | Count created fires in the single eligible cell and clear them every tick; compare normal/projectile carriers and zero/eight/excess magma heat in plains/desert, recording interval and trial count. Test other source types separately. | Small per-trial rates track the configured bounded formula; projectile probability is one sixteenth after the cap; torches/campfires add no heat; no exact short-run ratio is promised. | Not run |
| F04 | Use deterministic diagnostic settings to test doFireTick/mobGriefing, player/projectile exemptions, occupied/wet/rain-exposed cells, height/border/chunk edges and an obsidian frame; toggle/restart all controls. | Environmental gates and live settings hold; no overwrite, forced loading or entity-created portal; direct torch action remains independent of firetick. | Not run |
| F05 | Disable entity spread, place actual fire beside separate grass/dirt/podzol/mycelium samples, then repeat grass/firetick toggles and baseline wood/soot/snow fixtures. | Successful grass consumption leaves same-position vanilla dirt and no drops; other soils and existing charring/soot/covered-host behavior remain unchanged. | Not run |
| F06 | Save/reopen known and sun-only burns; extinguish by expiry/water/rain/powder snow and sun-ignite again; compare vanilla unarmed zombie melee from sun-only versus known ignition. | Only active known provenance survives reload; extinguishing clears it; natural zombie melee cannot turn sunlight into spread permission; clients agree. | Not run |

Production rule fixtures, snow/growth regressions, artifact contracts and wiki
checks cover automated behavior only. Interactive client rendering, multiplayer,
in-world rates, external flammability/config integrations and these manual cases
still need recorded results.

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