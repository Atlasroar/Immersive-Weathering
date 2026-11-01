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
| Entities and visuals | [`entities`](../../../src/main/java/com/ordana/immersive_weathering/entities), [`ImmersiveWeatheringClient`](../../../src/main/java/com/ordana/immersive_weathering/ImmersiveWeatheringClient.java), [`network`](../../../src/main/java/com/ordana/immersive_weathering/network) | Physics/persistence, renderers, particles, color blending, server-to-client decay notifications |
| Compatibility | [`integrations`](../../../src/main/java/com/ordana/immersive_weathering/integrations) | Fabric EMI and Mod Menu entrypoints; external integrations must be verified individually |

## Important execution paths

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
