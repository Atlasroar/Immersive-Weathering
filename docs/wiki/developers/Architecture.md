# Architecture map

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

Java source links below use the common package root
[`com.ordana.immersive_weathering`](../../../common/src/main/java/com/ordana/immersive_weathering).

## Startup and ownership

| Location | Responsibility |
| --- | --- |
| [`common`](../../../common) | Gameplay, registries, data codecs, generated packs, client setup, configuration, and shared mixins |
| [`fabric`](../../../fabric) | Loader entrypoint, Fabric events/registries, platform implementations, and Fabric-specific mixins |
| [`forge`](../../../forge) | Inherited legacy module; not a maintenance target |
| [`gradle.properties`](../../../gradle.properties), [`settings.gradle`](../../../settings.gradle) | Versions and project inclusion; still declare the inherited layout |

Fabric metadata loads
[`ImmersiveWeatheringFabric`](../../../fabric/src/main/java/com/ordana/immersive_weathering/fabric/ImmersiveWeatheringFabric.java).
Its `onInitialize` calls
[`ImmersiveWeathering.commonInit`](../../../common/src/main/java/com/ordana/immersive_weathering/ImmersiveWeathering.java),
then registers Fabric interaction callbacks and waxable pairs.
Common initialization constructs config specs, initializes client setup only on
the physical client, registers the generated server pack and reload listeners,
hooks fire/lightning/loot events, and initializes registry classes.

Client initialization is reached through common initialization and Moonlight's
side/lifecycle helpers; the empty Fabric `client` entrypoint list does not mean
there is no client setup.

## System map

| System | Start reading here | Coupled surfaces |
| --- | --- | --- |
| Registration | [`reg`](../../../common/src/main/java/com/ordana/immersive_weathering/reg) | Items/blocks, tags, creative visibility, recipes, loot, dynamic wood/leaf registration |
| Moss and cracking | [`Weatherable`](../../../common/src/main/java/com/ordana/immersive_weathering/util/Weatherable.java), [`PatchSpreader`](../../../common/src/main/java/com/ordana/immersive_weathering/util/PatchSpreader.java), [`blocks/mossy`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/mossy), [`blocks/cracked`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/cracked) | Placement, neighbor updates, scheduled/random ticks, config influences, state transitions |
| Rust | [`blocks/rusty`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/rusty), [`ModWaxables`](../../../common/src/main/java/com/ordana/immersive_weathering/reg/ModWaxables.java) | Weathering stages, wax pairs, door halves, scraping/sponging, recipes and tags |
| Growths | [`BlockGrowthHandler`](../../../common/src/main/java/com/ordana/immersive_weathering/data/block_growths/BlockGrowthHandler.java), [`ConfigurableBlockGrowth`](../../../common/src/main/java/com/ordana/immersive_weathering/data/block_growths/growths/ConfigurableBlockGrowth.java) | Tick sources, owners, predicates, area conditions, placement/survival, datapack reload |
| Fluid generators | [`FluidGeneratorsHandler`](../../../common/src/main/java/com/ordana/immersive_weathering/data/fluid_generators/FluidGeneratorsHandler.java), [`LiquidBlockMixin`](../../../common/src/main/java/com/ordana/immersive_weathering/mixins/LiquidBlockMixin.java) | Still/flowing fluid, prioritized generator lists, neighbors, generated state |
| Leaves and bark | [`WeatheringHelper`](../../../common/src/main/java/com/ordana/immersive_weathering/util/WeatheringHelper.java), [`LeafPileBlock`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/LeafPileBlock.java) | Moonlight type relationships, decay, piles, rebarking, generated resources, colors |
| User interactions | [`ModEvents`](../../../common/src/main/java/com/ordana/immersive_weathering/events/ModEvents.java) | Ordered handlers; Fabric `UseBlockCallback`; durability, drops, stats, client effects |
| Soils and plants | [`blocks/soil_types`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/soil_types), [`CropBlockMixin`](../../../common/src/main/java/com/ordana/immersive_weathering/mixins/CropBlockMixin.java) | Fertile tags, placement/survival, plant-specific mixins, growth speed |
| Cold and fire | [`blocks/frosted`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/frosted), [`blocks/charred`](../../../common/src/main/java/com/ordana/immersive_weathering/blocks/charred), [`TemperatureManager`](../../../common/src/main/java/com/ordana/immersive_weathering/util/TemperatureManager.java) | Biome/weather predicates, thin ice, icicles, freezing, smoldering, fire/lightning events |
| Entities and visuals | [`entities`](../../../common/src/main/java/com/ordana/immersive_weathering/entities), [`ImmersiveWeatheringClient`](../../../common/src/main/java/com/ordana/immersive_weathering/ImmersiveWeatheringClient.java), [`network`](../../../common/src/main/java/com/ordana/immersive_weathering/network) | Physics/persistence, renderers, particles, color blending, server-to-client decay notifications |
| Compatibility | [`integrations`](../../../common/src/main/java/com/ordana/immersive_weathering/integrations) | EMI, optional-mod checks, inherited integrations; availability on Fabric must be checked individually |

## Important execution paths

`ServerLevelMixin` injects growth dispatch into chunk random ticking and adds
sky-access growth ticks. `ModEvents.onLightningHit` dispatches lightning growths.
`BlockGrowthHandler` selects universal and block-specific growth sets by source.
The detailed dispatch/placement rules remain IW code, not Moonlight code.

`BlocksMixin` replaces several vanilla block implementations. Compatibility
failures may therefore occur on vanilla IDs, not just IW namespaced blocks.
Check both the common and Fabric mixin manifests: a source file existing does
not mean its mixin is actually registered. Registered mixins appear in the
[generated reference](Reference.md).

Do not conflate logical server authority with physical client initialization.
Verify side-safe class loading, authoritative world mutation, packet handling,
and dedicated-server behavior when touching shared code.
