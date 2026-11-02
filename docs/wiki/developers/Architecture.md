# Architecture map

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

Java source links below use the root package
[`com.ordana.immersive_weathering`](../../../src/main/java/com/ordana/immersive_weathering).

## Startup and ownership

| Location | Responsibility |
| --- | --- |
| [`src/main/java`](../../../src/main/java) | Gameplay, registries, data codecs, generated packs, client setup, configuration, networking, and mixins |
| [`fabric` package](../../../src/main/java/com/ordana/immersive_weathering/fabric) | Fabric entrypoint, interaction callbacks, wax pairs, and ceiling/wall item placement |
| [`src/main/resources`](../../../src/main/resources) | Fabric metadata, two registered mixin manifests, access widener, assets, data, and optional resource packs |
| [`build.gradle`](../../../build.gradle), [`gradle.properties`](../../../gradle.properties), [`settings.gradle`](../../../settings.gradle) | One root Fabric Loom build, Java 17, versions, and external dependencies; no subprojects |

Fabric metadata loads
[`ImmersiveWeatheringFabric`](../../../src/main/java/com/ordana/immersive_weathering/fabric/ImmersiveWeatheringFabric.java).
Its `onInitialize` calls
[`ImmersiveWeathering.commonInit`](../../../src/main/java/com/ordana/immersive_weathering/ImmersiveWeathering.java),
then registers Fabric interaction callbacks and waxable pairs.
Common initialization constructs config specs, initializes client setup only on
the physical client, registers the generated server pack and reload listeners,
hooks fire/lightning/loot events, and initializes registry classes.

Client initialization is reached through common initialization and Moonlight's
side/lifecycle helpers. There is deliberately no separate Fabric `client`
entrypoint: adding one would run setup twice. Dedicated servers must never load
the client initialization branch.

`ModFeatures` calls Fabric `BiomeModifications` with the existing biome tags,
generation steps, and placed feature IDs. `LiquidBlockMixin` reads its shadowed
`FlowingFluid` field directly. Empty extra moss/cracking hooks are removed without
changing the vanilla/IW transition pairs. Moonlight's flammability registration
remains in leaf-pile/weed constructors; dead loader-specific fire methods are not
needed on Fabric.

IW-owned platform bridge classes/annotations, Forge resources and overrides,
Quark-only optional pairs, and the unused/unimplemented Serene Seasons bridge
are removed. Fabric never adjusted temperature through that seasonal stub.
Unregistered historical config placeholder methods, their unused enum/field,
and misleading tail comments are removed; registered config keys/defaults stay
unchanged.
EMI and Mod Menu entrypoints remain optional; Moonlight continues to supply the
config screen, lifecycle, registries, events, networking, and generated packs.

## System map

| System | Start reading here | Coupled surfaces |
| --- | --- | --- |
| Registration | [`reg`](../../../src/main/java/com/ordana/immersive_weathering/reg) | Items/blocks, tags, creative visibility, recipes, loot, dynamic wood/leaf registration |
| Moss and cracking | [`Weatherable`](../../../src/main/java/com/ordana/immersive_weathering/util/Weatherable.java), [`PatchSpreader`](../../../src/main/java/com/ordana/immersive_weathering/util/PatchSpreader.java), [`blocks/mossy`](../../../src/main/java/com/ordana/immersive_weathering/blocks/mossy), [`blocks/cracked`](../../../src/main/java/com/ordana/immersive_weathering/blocks/cracked) | Placement, neighbor updates, scheduled/random ticks, config influences, state transitions |
| Rust | [`blocks/rusty`](../../../src/main/java/com/ordana/immersive_weathering/blocks/rusty), [`ModWaxables`](../../../src/main/java/com/ordana/immersive_weathering/reg/ModWaxables.java) | Weathering stages, wax pairs, door halves, scraping/sponging, recipes and tags |
| Growths | [`BlockGrowthHandler`](../../../src/main/java/com/ordana/immersive_weathering/data/block_growths/BlockGrowthHandler.java), [`ConfigurableBlockGrowth`](../../../src/main/java/com/ordana/immersive_weathering/data/block_growths/growths/ConfigurableBlockGrowth.java) | Tick sources, owners, predicates, area conditions, placement/survival, datapack reload |
| Fluid generators | [`FluidGeneratorsHandler`](../../../src/main/java/com/ordana/immersive_weathering/data/fluid_generators/FluidGeneratorsHandler.java), [`LiquidBlockMixin`](../../../src/main/java/com/ordana/immersive_weathering/mixins/LiquidBlockMixin.java) | Still/flowing fluid, prioritized generator lists, neighbors, generated state |
| Leaves and bark | [`WeatheringHelper`](../../../src/main/java/com/ordana/immersive_weathering/util/WeatheringHelper.java), [`LeafPileBlock`](../../../src/main/java/com/ordana/immersive_weathering/blocks/LeafPileBlock.java) | Moonlight type relationships, decay, piles, rebarking, generated resources, colors |
| User interactions | [`ModEvents`](../../../src/main/java/com/ordana/immersive_weathering/events/ModEvents.java) | Ordered handlers; Fabric `UseBlockCallback`; durability, drops, stats, client effects |
| Soils and plants | [`blocks/soil_types`](../../../src/main/java/com/ordana/immersive_weathering/blocks/soil_types), [`CropBlockMixin`](../../../src/main/java/com/ordana/immersive_weathering/mixins/CropBlockMixin.java) | Fertile tags, placement/survival, plant-specific mixins, growth speed |
| Cold and fire | [`blocks/frosted`](../../../src/main/java/com/ordana/immersive_weathering/blocks/frosted), [`blocks/charred`](../../../src/main/java/com/ordana/immersive_weathering/blocks/charred), [`TemperatureManager`](../../../src/main/java/com/ordana/immersive_weathering/util/TemperatureManager.java) | Biome/weather predicates, thin ice, icicles, freezing, smoldering, fire/lightning events |
| Standalone snow | [`snow`](../../../src/main/java/com/ordana/immersive_weathering/snow) | Explicit vanilla host schemas, placement hooks, scheduled falling, one sampled weather hook, static multipart models |
| Standalone fire | [`fire`](../../../src/main/java/com/ordana/immersive_weathering/fire) | Per-entity ignition provenance, bounded server trials, successful direct torch combat, dynamic grass odds/ground preservation |
| Standalone sandstorms | [`sandstorm`](../../../src/main/java/com/ordana/immersive_weathering/sandstorm), [`client/SandstormEffects`](../../../src/main/java/com/ordana/immersive_weathering/client/SandstormEffects.java) | Vanilla weather, dry subtype tags, exposed temporary slowdown, independent sampled deposition, guarded fog/dust/wind; no new dependencies |
| Entities and visuals | [`entities`](../../../src/main/java/com/ordana/immersive_weathering/entities), [`ImmersiveWeatheringClient`](../../../src/main/java/com/ordana/immersive_weathering/ImmersiveWeatheringClient.java), [`network`](../../../src/main/java/com/ordana/immersive_weathering/network) | Physics/persistence, renderers, particles, color blending, server-to-client decay notifications |
| Compatibility | [`integrations`](../../../src/main/java/com/ordana/immersive_weathering/integrations) | Fabric EMI and Mod Menu entrypoints; external integrations must be verified individually |

## Important execution paths

### Standalone sandstorms

`Sandstorms.biomeType` is the union of dry regular/red subtype tags; regular wins
overlap. There is intentionally no separate master tag that can drift from the
subtypes. `exposedStorm` adds vanilla rain, sky visibility and MOTION_BLOCKING
height <= query Y; client visuals use biome/weather without the roof gate.
The existing `TemperatureManager.hasSandstorm` and its `has_sandstorm` tag are
**unchanged** and remain owned by legacy `SandGrowth`, including warm ocean.

`ServerLevelMixin.tickChunk` TAIL calls `SandAccumulation` once, independent of
growth dispatch and the randomTickSpeed parameter. One sampled XZ column and
1/16 trial, no scan/retry, no generic precipitation/cauldron callback. The
heightmap cell is adjusted down only for a matching IW pile. Dry loaded in-height
air/matching piles require dry sturdy support and survival; different piles,
full/at-cap piles and occupied hosts get no write. Entity push-up precedes the
ordinary block update. Vanilla block/tag/attribute synchronization and save
formats suffice; no CCA, custom packets or per-world storm state.

`SandstormLivingMixin.tick` TAIL reconciles the actual MOVEMENT_SPEED modifier
each server tick, not an edge-triggered boolean. IW owns UUID
`ddcbef92-e913-4994-9524-72e97d4eb92c`, uses transient MULTIPLY_TOTAL, replaces
changed amount/operation, and removes only that UUID after shelter, clear weather,
immunity/tag reload, config disable or dimension/biome changes. Camels plus the
optional `#c:bosses` immunity tag are defaults. If Scorchful is loaded, all new
IW storm behavior yields and the IW modifier is removed next tick. Legacy
growth is not suppressed. Scorchful need not be present to link/load any class.

Client initialization registers END_WORLD_TICK dust/wind and disconnect timer
cleanup through existing Fabric API. A simple serialized particle type is enough:
color is fixed `0xD9AA84`, upstream requested scale 10 actually clamps to 4 in
vanilla `DustParticleOptions`, velocity (-1,0,0), gravity 0.1. The default 40x40
scan is capped at 64x64 and never loads chunks; Y is camera +/- half-range,
clamped above the surface. Both sand subtypes use the same sprites/color.
Minimal skips all dust work; Decreased doubles rarity. The ordinary particle
engine still owns its limits. WindTimer resets on clear/disable/world replacement
or disconnect and emits one-shot WEATHER 0.1/0.5 events, not persistent loops.

Client-only fog/cloud mixins do not load on the server. Fog blends the dust
target's dimension/time lighting with native color using cubic sampled biome
weight and rain gradient. Distance applies only to terrain, skips thick fog and
fluid/blindness/darkness cameras, clamps to stronger native fog/view distance,
and changes shape only when actually reducing distance above 0.75 gradient.
Cloud hiding has its **own** toggle, unlike implicitly tying it to fog.
See [licensing](Licensing.md#scorchful-sandstorm-adaptation) and
[fixture limits](Development.md#sandstorm-mechanics-harness).

`ServerLevelMixin` injects growth dispatch into chunk random ticking and adds
sky-access growth ticks. `ModEvents.onLightningHit` dispatches lightning growths.
`BlockGrowthHandler` selects universal and block-specific growth sets by source.
The detailed dispatch/placement rules remain IW code, not Moonlight code.

Dispatch checks the growth config and conditional block hook before checking
whether either registration group has callbacks. Empty dispatches do not create
a biome supplier. Active dispatches retain one invocation-local memoized supplier,
universal-before-specific iteration, and the original state/position arguments.
When universal callbacks exist, the specific group is looked up after them,
preserving callback-triggered registration reload behavior without a redundant
preflight lookup. The public optional lookup API is unchanged.

`AreaCheck` uses
[`RandomizedBlockPositions`](../../../src/main/java/com/ordana/immersive_weathering/util/RandomizedBlockPositions.java)
to shuffle invocation-local integer indices over immutable offset templates.
Templates use vanilla `BlockPos.withinManhattanStream` shell order and a value
key of three radii; backwards Fisher-Yates uses the same `java.util.Random`
seed/calls as the existing shuffled-list helper. Absolute immutable positions are
created only when visited. At most 32 templates and 4096 total positions are
retained, with synchronized admission/lookup and no eviction. Negative,
excessive, overflow-prone, or over-budget radii use the original list helper
without changing its behavior. Only geometry survives calls, reloads, dimension
changes, and world unloads: no world, biome, block state, predicate result, or
mutable scratch position is cached. Nested traversals have separate permutations.
`WeatheringHelper.grabBlocksAroundRandomly` and its mutable-list contract,
`hasEnoughBlocksAround`, fluid generators, and `PatchSpreader` are unchanged.

The public [Immersive Weathering Tweaks repository](https://github.com/Admany/Immersive-Weathering-Tweaks)
is research context only. The implementation was independently authored from
IW's own LGPL source; no add-on source was copied, ported, or reused, and no
dependency, library, third-party implementation, or license notice was imported.
This is not a claim of whole-game TPS/MSPT improvement; see the
[isolated validation procedure](Development.md#growth-performance-harness).

`BlocksMixin` replaces several vanilla block implementations. Compatibility
failures may therefore occur on vanilla IDs, not just IW namespaced blocks.
Check both gameplay and Fabric mixin manifests: a source file existing does
not mean its mixin is actually registered. Registered mixins appear in the
[generated reference](Reference.md).

Do not conflate logical server authority with physical client initialization.
Verify side-safe class loading, authoritative world mutation, packet handling,
and dedicated-server behavior when touching shared code.

## Standalone fire ownership

The 2.2.0 `fire` package is independently authored using vanilla/IW
facilities; no external fire mod code/assets, library, component API or newer
Minecraft API is imported. The immutable published 2.1.0 artifacts are unchanged.
The [player contract](../players/Features.md#standalone-fire)
and [common controls](../players/Configuration.md#standalone-fire)
are authoritative; season integration is deferred.

`EntityFireMixin` owns one `IgnitionHistory` per entity. Server-side authorization
requires positive vanilla fire ticks and a non-immune entity. Living
`setSecondsOnFire` calls are **not** blanket-authorized: sunlight from zombie,
skeleton and phantom ticks is naturally unknown. Explicit hooks authorize fire
contact (`BaseFireBlock.entityInside`), lava and lightning; successful
player/mob Fire Aspect, burning-arrow and small-fireball damage; and successful
torch combat. Nonliving projectiles' `setSecondsOnFire` calls are known non-sun
ignition, covering Flame bows, enchanted mob arrows and burning fireballs.
Arrow/fireball authorization follows successful `hurt`, not the provisional
pre-hit ignition, so a failed hit that restores old fire ticks cannot authorize
a sun burn. Enderman arrow ignition remains excluded. Zombie's vanilla melee
ignition propagates permission only from an already-authorized zombie; it
does not remove vanilla burning damage from a sunlight-only attack.

`setRemainingFireTicks <= 0` resets provenance immediately, including natural
expiry and vanilla water/rain/powder-snow extinguishing. The namespaced boolean
NBT key `immersive_weathering:non_sun_fire` is saved/loaded with vanilla `Fire`;
missing/false keys and nonpositive loaded burns are ineligible. Dimension copies
use vanilla save/load. A later real source authorizes an existing sun burn even
when the vanilla duration does not increase. Unsupported mod ignition APIs do
not automatically authorize living entities.

`EntityFire.tick` runs at `Entity.baseTick` tail and exits before scanning unless
the carrier is alive, burning with known provenance, non-immune, dry, outside
current/previous powder snow, enabled, and permitted by gamerules. Trials are
staggered by entity ID modulo the current interval. Non-player carriers respect
`mobGriefing`; players and player-owned projectiles are exempt. Each due trial
reads current settings, counts at most the configured source cap in a bounded
heat cube, and uses the biome's vanilla **base temperature**, not daylight,
species, altitude or a season adapter. Probability is capped before projectile
reduction. One successful roll uses reservoir selection of at most 343 candidate
cells, not a chance per cell. Heat radius is at most four (729 cells); no world
state/cache, entity-list scan, chunk forcing or world-wide scan is retained.

Candidate and all six neighboring cells must be in build height/world border
and already loaded before block queries. Air/fluid, exposed rain including
horizontal neighbors, original `FireBlock.getIgniteOdds(LevelReader, BlockPos)`
(including IW's existing `FIRE_REPLACEABLE` query), and actual vanilla fire-state
survival must all pass. Placement uses `BaseFireBlock.getState` and normal block
updates. An invocation-scoped thread-local flag suppresses only the portal-shape
lookup during that placement; `finally` removes/restores it even on failure.
No portal scan or portal creation occurs from entity spread; ordinary ignition's
portal behavior and survival checks remain untouched.

`PlayerFireMixin` wraps only the first direct `Entity.hurt` invocation in
`Player.attack`, not sweep damage or owner-attributed projectile damage.
`afterMeleeHit` handles only successful server hits. Torch targets are living,
alive, non-immune, non-fire-resistant and dry. Main-hand TORCH/SOUL_TORCH uses
vanilla `setSecondsOnFire(4)` (fire protection still applies), preserves a longer
duration, and consumes one item unless `instabuild`. This direct combat action
does not depend on environmental gamerules. There is no separate soul burn.

`FireMixin` adds exact GRASS_BLOCK odds at return (5 ignition/20 burn minimum)
with priority 900, after Fabric's priority-1000 registry overrides. External
larger values survive; disabling IW grass stops supplementing native/other-mod
values without mutating a global registry on config reload. At the second
`Level.getBlockState` in `checkBurnOut`, vanilla has already won its random burn
trial. `GrassFire.consume` replaces only that grass surface with DIRT and cancels
the fire/air replacement, preserving ground and emitting no drops. It also
guards `doFireTick`. Other soils, snow-host fire registration, existing charring
events, `tick` soot removeBlock ordinal 3 and the FIRE_REPLACEABLE query are
unchanged. No loot, datapack or growth schema is added.

## Standalone snow ownership

`SnowHosts` explicitly registers 72 `snow_covered_<host_path>` blocks: 12 fences,
22 walls and 38 dry plants (34 vanilla, four IW). This does not replace existing `snowy_*` masonry IDs,
`SnowGrowth`, their datapacks/configs, or leaf/sand mechanics. No automatic
modded-host discovery or dependency is added. `register` is an explicit startup
integration point: register a block/supplier before registry freeze, preserve
the host schema and supply matching resources/semantics. Block entities,
wet hosts, nested wrappers and mappings that lose host properties are rejected.
IW suppliers are resolved inside their deferred block registration callbacks,
not during `commonInit`, before Moonlight has instantiated the original plants.

The evaluated inheritance approach cannot safely pass a `LAYERS`-extended state
to vanilla `WallBlock`'s exact-state shape maps. Instead, explicit schema classes
inside `SnowCoveredBlock` share a deterministic delegating implementation:
vanilla methods receive a reconstructed **raw host state**, never a composite.
Every property except `WATERLOGGED` is stored with its original property object;
only dry hosts are eligible, so raw restoration always retains false waterlogging.
Saplings retain `stage`; propagules also retain `age`/`hanging`; petals retain
`facing`/`flower_amount`; weeds retain `age`; frosty plants retain `natural`.
There is no per-cell block entity, NBT payload, packet, render attachment, mutable
world cache or global scratch position. Ordinary block-state persistence and
vanilla block updates provide save/client synchronization.

Shapes union raw host and vanilla snow outline/collision/support shapes.
Vanilla block-state caches retain these position-independent shapes. Raw wall
post detection additionally recognizes wrapped upper walls through the narrow
`SnowWallPostMixin`; fence/wall/mining tags and copied Fabric fire entries preserve
their relevant semantics. Rotation/mirroring, cloning, bonemeal and interaction
delegate through raw states. Wrapped plants omit vanilla's visual XZ jitter so
their snow plane stays aligned with the cell; biome tint is delegated to the raw
plant, while snow quads are untinted. Covered normal short grass and short fern
delegate their models and both tint channels to the matching IW frosty plant;
their stored raw identities and restoration remain normal grass/fern.
The fern appearance ships in stable 2.1.0; tall grass and large ferns keep
their original two-part models. The actual client color-state selector is
exercised directly without initializing a renderer.

`tools/snow_resources.py` emits reviewable, checked-in multipart blockstates,
eight ordinary snow models and IW semantic tags. Host parts reference existing
vanilla/IW models, including bamboo's four special side models, weed age models,
propagule variants and petal segments. Snow references the vanilla
sprite; a 1/1024-block depth bias separates coplanar post/arm surfaces from snow.
There is no FRAPI custom model, Kiwi/Indium requirement, copied SnowRealMagic
model/texture, or client import in shared snow code. Actual alternate-pack/Sodium
visual acceptance remains required.

`SnowHostReplaceMixin`, `SnowLayerBlockMixin` and `SnowHostPlacementMixin` cooperate
with normal `BlockPlaceContext`/`BlockItem` placement rather than reimplementing
permissions, collision tests, callbacks, advancements, sounds or stack consumption.
Partial covered plants permit ordinary block-item replacement only when
`replace_partial_layers` is enabled and their reconstructed raw host accepts
the item. Full eight-layer covers, fences/walls and non-block
items are not widened. Native same-item petal stacking is preserved with the
original facing/count; other same-host plant items remain rejected.
A supported dry replacement keeps the old layer count
through the same placement injection while mixing is enabled; unsupported
solids or mixing-disabled replacements consume the cover normally. Snow items
remain a separate mixing/add-layer path and bypass the replacement preservation
hook, so adding snow cannot reset the newly incremented layer count.
The normal double-plant placement callback applies the same eligibility check
to a covered upper cell, preserving its independent layer count too.
`SnowDoublePlantMixin` recognizes raw/wrapped partners and preserves an existing
partial-snow upper cell. The location-predicate fallback is restricted to explicit
vanilla double-plant block-ID predicates against wrapped counterparts, with their
state/tag checks and no NBT predicate. This retains paired grass/fern loot checks
without replacing vanilla loot tables or ignoring datapack overrides.

Shovel **use** restores the raw host and evaluates vanilla snow loot with the
player/tool. Breaking the composite is host destruction and uses host mining
requirements plus host/snow loot; double plants take their pre-removal loot
snapshot once. Creative upper destruction suppresses partner drops. An invalid
host remains until its two-tick scheduled resolution, then host loot is evaluated
and snow becomes a plain layer or falls. Melt restores the host without loot.
Disabling mixing does not unregister existing states.

Native random growth, bonemeal, interaction, animation and entity-contact
callbacks receive raw plant states. Eligible state changes are rewrapped with
the original layer count; successful tree/mushroom growth into solid structures
is not overwritten. Narrow identity hooks in crop growth-speed checks, mushroom
density checks and mega-sapling recognition compare raw hosts. A stage-1 sapling
snapshots only covered matching neighbors in its 3x3 cell neighborhood, restoring
independent covers if a failed native 2x2 tree attempt restores raw saplings.
This is a bounded operation-local snapshot, not a world cache or extra tick.

`SnowPhysics` detaches only the snow state into the existing falling-layer entity
type; the leaf/sand branch is unchanged. The snow branch advances its own time,
ray-checks source water on every move, and lands without overwriting blocked/wet
cells. Deposits return an explicit remainder; at most eight layers enter one cell,
and overflow becomes snow-layer items under the drop gamerules. Source WATER is
the deliberate material-conversion sink; flowing/waterlogged blocks do not freeze.

`ServerLevelMixin` redirects only `Biome.shouldSnow` in vanilla's existing
`tickChunk` sample. `SnowWeather` is the sole extra-accumulation authority:
current rain/thunder, biome snow temperature, heightmap identity, loaded position,
block light <= 11, snow support, config and one-cell cap are checked. Ordinary
initial air-cell snow still uses vanilla `Biome.shouldSnow`. No RNG draw, extra
tick source, scan, asynchronous world access or `BlockGrowthHandler` cadence
change is introduced. Gamerule 0 disables deposition; 1 is the user-selected
default-compatibility sentinel; other positives intersect the bounded IW cap.
No getter or unrelated gamerule is changed.

Research context:
[Snownee/SnowRealMagic at ef40d3319a6f9a98673816e0781cfdbb8e8caaea](https://github.com/Snownee/SnowRealMagic/tree/ef40d3319a6f9a98673816e0781cfdbb8e8caaea).
Source was inspected during research; this is not a clean-room/source-unread
claim. The implementation and static resources here are independently authored
against IW/Mojang behavior; no SnowRealMagic source or assets were ported.

English `block.immersive_weathering.snow_covered_*` display names cover all 72
registered hosts; the resource tests enforce exact inventory coverage without
rewriting existing translations or other locales.

The six snow options already belong to the synchronized common spec and the
existing Mod Menu/Moonlight config screen. Builder comments opt each control into
Moonlight's description-key support; matching English tooltips explain scope,
defaults and the gamerule sentinel without introducing a second config system.

## Branch policy and historical reference

The GitHub default development branch is `atlasroar-fabric-1-20-1-bugs`.
Despite its task-oriented name, it is now the active Fabric 1.20.1 line.
Use the repository's default branch as the base for new contributions; do not
assume a branch literally named `main` exists.

On November 1, 2026, the other 11 remote branches were retired after preserving
their exact tips as lightweight tags named `archive/2026-11-01/BRANCH_NAME`.
The archives preserve unique historical commits, not an ongoing support promise.

| Archived branch | Historical role |
| --- | --- |
| `1.18.2-Fabric` | Earlier Fabric implementation |
| `1.18.2-Multiloader` | Earlier multiloader implementation |
| `1.19.2-multiloader` | Earlier Minecraft version |
| `1.20.0-multiloader` | Baseline inherited by the active branch |
| `1.21.1` | Later-version experiment, outside current scope |
| `forge` | Legacy Forge implementation |
| `main` | Older upstream default line, not the active 1.20.1 code |
| `Inferno` | Historical April Fools variant |
| `weatherite` | Historical variant |
| `pages` | Historical Pages branch; GitHub Pages was disabled at retirement |
| `revert-402-1.20.0-multiloader` | Historical revert already included in the active history |

Browse the [archive tags](https://github.com/Atlasroar/Immersive-Weathering/tags)
to inspect retired tips. Do not remove them or recreate retired maintenance
branches without an explicit decision. Existing local checkouts can still hold
old local branch names; deleting remote branches does not switch those checkouts.

Branch cleanup preserved history independently of the later Fabric-only beta
migration. The migration removes obsolete source modules, transformer output,
and bundled historical jars from the active tree; archive tags are unchanged.
The GitHub Wiki has its own `.wiki.git` repository and is unaffected by
source-repository branch deletion.
