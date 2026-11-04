## 2.6.0

Stable release target for Minecraft **1.20.1**, **Fabric** and **Java 17**.
Install only `immersive_weathering-2.6.0.jar`, replacing the previous IW jar
after backing up worlds/configuration. Fabric Loader **0.16.14+**, Fabric API
**0.92.12+1.20.1** and Fabric Moonlight **1.20-2.12.11** remain required.
No new dependency, texture or recording is added. MP01-MP06 remain **Not run**;
automated coverage and stable versioning do not establish manual acceptance.

- Snow, regular sand and red sand now share one vegetation inventory and cover
  implementation: 75 vanilla vegetation blocks, all seven IW plants, and the
  existing 12 vanilla fences/22 walls. This includes crops, cactus, cane, bamboo,
  vines, berries, Nether plants, dripleaf and chorus; dry states only.
- Covers retain native properties, growth/bonemeal, head/body transitions,
  interactions, shapes and loot. Farmland and soul sand support mineral piles;
  ceiling/side attachment alone cannot suspend a pile. Shoveling keeps the host.
- Aquatic/waterlogged expansion, arbitrary third-party plants, leaf piles and
  azalea-flower litter remain outside this change. Existing block IDs, saved
  configuration paths, layer limits and distinct snow/sand weather rules remain.
- Expanded automated regressions and version-neutral MP01-MP06 manual procedures;
  manual outcomes are recorded separately and do not inherit earlier passes.
  Published 2.5.0 artifacts are unchanged.

## 2.5.0

Stable release target for Minecraft **1.20.1**, **Fabric** and **Java 17**.
Install only `immersive_weathering-2.5.0.jar` when published, replacing the
previous IW jar after backing up worlds/configuration. Fabric Loader
**0.16.14+**, Fabric API **0.92.12+1.20.1** and Fabric Moonlight
**1.20-2.12.11** remain required. No runtime dependency is added. Source version
and build output do not establish publication, deployment or manual acceptance.

- Standalone traveling freezing wind adapted from pinned Frostiful source and
  six original particle frames, without Frostiful/Thermoo or player temperature,
  freeze ticks, damage, wetness, armor or heart systems. Ambient wind is
  night-only in all biomes; independent cold/altitude bonuses favor cold mountains.
- Gusts push and apply configurable vanilla Slowness, extinguish open flames
  (including normal/soul torches left unlit in place and relightable), freeze
  eligible source water into existing thin ice, and grow native-survival
  underside icicles without restricting support materials. Block changes require
  mobGriefing; existing thin-ice light rules and disable_icicles remain.
- Relighting and wind extinguishing honor rejected world writes: failure leaves
  state/inventory unchanged, without success sounds/events/particles or campfire
  bookkeeping. Successful candles retain native wick offsets and campfires their
  native dowse effects; normal/soul/wall orientation remains unchanged.
- Cold vanilla thunder snowstorms run day or night, add a capped bonus through
  the existing single sampled snow deposition authority, and generate more wind.
  Guarded white fog, shared upstream grain artwork, cloud hiding and intermittent
  existing IW wind audio are independently configurable. No new audio recordings
  or modern Immersive Storms APIs/dependencies imported.
- Pinned LGPLv3 provenance, packaged-source/resource guards and a separate
  FW01-FW08 acceptance checklist accompany the work. All new manual scenarios
  remain **Not run**; previous 2.4.0 SP01-SP07 Pass/SP08 N/A and ST01-ST08
  Not run records are unchanged. Published 2.4.0 and earlier artifacts remain
  immutable and do not contain this feature.

## 2.4.0

Stable release for Minecraft **1.20.1**, **Fabric** and **Java 17**.
Install only `immersive_weathering-2.4.0.jar`, replacing the previous IW jar
after backing up worlds/configuration. Fabric Loader **0.16.14+**, Fabric API
**0.92.12+1.20.1** and Fabric Moonlight **1.20-2.12.11** remain required.
Sources are distributed separately; no new runtime dependency is added.

- Regular/red sand piles share dry cells with the 12 vanilla fences or dead
  bushes, in either placement order. Native host states/connections, collision,
  interactions and drops are retained. Right-click a cover with a shovel to
  remove its sand without removing the host. Other plants/modded hosts are not
  enabled by a datapack; explicit registered state schemas are still required.
- Ordinary block items can replace uncovered sand at every depth, including
  eight layers. Supported host items retain the sand instead of replacing it.
  Covered fences retain native protection against replacement; covered dead
  bushes retain native replaceability.
- Weather deposition defaults to eight layers for newly generated configs,
  configurable from one to eight through the existing `sandstorms.layer_cap`.
  Existing configs keep their value. Manual stacking and falling piles still
  reach eight; lowering the weather cap never trims a pile. Legacy sandy
  growth's pile output uses the same guarded deposition, without overwriting
  occupied blocks; its variant conversion and old biome tag are unchanged.
- Sand falls independently of its host, merges without mixing colors, and
  drops excess layers rather than creating an automatic overflow column.
  Water is neither frozen nor overwritten.
- Actual rain erodes exposed sand by one layer per successful random tick
  (default chance 0.125), without drops, restoring a host when the last layer
  disappears. Either sandstorm biome tag exempts a biome, regardless of its
  temperature. Roofs, clear weather and snowfall preserve sand.
- Thunder suppresses sky-exposed ordinary fire in all biomes, including dry
  biomes and eternal-fire supports: default 90% fewer neighboring burn/spread
  attempts and 25% chance of extinguishing on each normal scheduled fire tick.
  Exposed burning-entity environmental trials receive the same multiplier.
  Fire aging/scheduling and ordinary rain rules remain; sheltered fire,
  entity burning, soul fire, torches and campfires are not extinguished by this
  rule. All new environmental fire effects respect `doFireTick`.
- Common `sand` and `fire` controls, generated native-model cover resources,
  production/native regression coverage, and a separate SP01-SP08 manual
  checklist accompany the changes. At publication these were **Not run**;
  on 2026-11-02 the tester reported **SP01-SP07 Pass; SP08 Not applicable**.
  The report is at test-ID level, without itemized variant evidence or an
  SP08 multiplayer/coexistence pass.
  Published 2.3.0 and earlier assets remain immutable. A release/deployment
  does not count as a manual gameplay pass.

## 2.3.0

Stable release target for Minecraft **1.20.1**, **Fabric** and **Java 17**.
Requires Fabric Loader **0.16.14+**, Fabric API **0.92.12+1.20.1** and
Fabric Moonlight **1.20-2.12.11** (CurseForge file **5619072**).
Install only `immersive_weathering-2.3.0.jar`, replacing the previous IW jar
after backing up worlds/configuration. Sources are distributed separately.
Published 2.2.0 and older releases, assets, tags and historical results remain
unchanged. This heading records release contents, not proof of publication.

### Standalone sandstorms

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
- Adaptation pins Scorchful **d74712e9b5fc4348aa8ad9c417ce938daf8bc0a1** by
  TheDeathlyCow under the intended root LGPLv3 license; the stale ARR manifest
  clarification and per-wind third-party provenance limits are recorded in
  `sandstorm-NOTICE.txt` and appended `ATTRIBUTIONS`. Three original dust sprites
  and nine original wind sounds are included without inventing CC0 attribution.
- Common/server defaults: `sandstorms.deposition=true`, `layer_cap=1` (1-8),
  `slowing=true`, `slow_amount=0.3` (0-1). Deposition samples one column per
  ticking chunk with a 1/16 trial, independent of randomTickSpeed and block
  growth settings. Camels and optional `#c:bosses` are
  immune; shelter/clear/config/immunity/biome/dimension changes reconcile the
  temporary IW modifier every server entity tick.
- Client defaults: particles/fog/hide_clouds/sounds enabled, particle scan radius
  20 (40x40 loaded columns), rarity 60, fog start/end 16/64. Cloud hiding above
  0.75 rain strength is independently configurable; wind uses Weather volume,
  pitch 0.5 and the Wind blows subtitle. Dust uses the original color/gravity;
  the upstream requested scale 10 actually clamps to 4 in vanilla.

### Run checklist and validation limits

- Automated storm/native fixtures and resource guards are not a visual/gameplay
  pass. New [ST01-ST08 manual cases](docs/wiki/players/Feature-testing.md#standalone-sandstorms)
  are **Not run**. Run the linked disposable-world fixture and its positive and
  negative controls for weather/subtypes, exposure/slowdown, cap/occupied support,
  live config/tag cleanup, client fog/dust/wind/reload and multiplayer/coexistence.
- The prior broad user-reported 2.2.0 fire pass and snow historical outcomes are
  retained, not relabeled as sandstorm or complete 2.3.0 acceptance. Real wind
  playback/decoding, in-world visuals/rates, resource reload and multiplayer
  remain unverified. Stable versioning does not manufacture those results.

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