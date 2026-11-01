# Moonlight dependency map

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

Moonlight Lib, formerly Selene, is a hard runtime dependency. The Fabric manifest
requires it; the root build pins the historical `curse.maven:selene` artifact.
See the [generated reference](Reference.md) for current declarations and the
complete source-file/API import inventory.

## Functional dependencies

| Moonlight API | IW consumer and purpose | Replacement implications |
| --- | --- | --- |
| `BlockSetAPI`, `WoodType`, `LeavesType`, type registries | `ModBlocks`, `ModItems`, `WeatheringHelper`: discover families, register piles/bark/scales, associate original/stripped logs and generated children | Preserve registration timing, IDs, family relationships, and modded compatibility; not just a registry wrapper |
| Dynamic client/server resource generators and packs | `dynamicpack`: generated tags, recipes, loot, blockstates/models, and translations | Replace resource-pack registration/reload integration and preserve datapack/resource-pack overrides |
| `RPUtils`, `TextureImage`, `Palette`, `LangBuilder` | Find original textures, generate dense leaves/bark, name dynamic content | Preserve pack-dependent generation, image lifetime/animation handling, and useful diagnostics |
| `ConfigBuilder`, `ConfigSpec`, `FabricConfigListScreen` | Common/client defaults, synced common spec, Mod Menu screen | Preserve keys, defaults, bounds, persistence, synchronization, and migration of existing user settings |
| `RegHelper`, `PlatHelper`, `ClientHelper` | Registry/bootstrap, loot injection, composting/flammability, lifecycle, rendering/colors/particles, area-loaded checks | Fabric API replaces many surfaces, but callback ordering and client/server safety still matter |
| Fire-consumption and lightning events | `ImmersiveWeathering`, `ModEvents`: char burned blocks and invoke lightning growths | Preserve the exact event semantics, final-state replacement, and authoritative server execution |
| `RegistryAccessJsonReloadListener` | Growth and fluid handlers decode registry-aware JSON | Preserve registry-aware codecs, reload timing, errors, and rebuilt dispatch tables |
| `ChannelHandler`, `Message` | Decay particles/sounds delivered to nearby clients | Replace registration, encoding, delivery range, handling thread, and protocol behavior |
| Reusable blocks/items/entities, particles, colors | Stair helpers, fuel/wood-based items, falling propagule base, falling renderers, charred lightability, particles, aging-leaf color interpolation | Port the behavior intentionally; replacing a base class with vanilla can change physics/rendering |

## What it does not own

IW owns the weathering algorithms, growth definitions, generator codecs,
interaction rules, and dynamic resource templates. An error passing through
Moonlight is not enough to assign it blame. Establish the caller, input state,
library version, and actual incompatible contract first.

`FallingLayerEntity` and `FallingIcicleEntity` directly extend vanilla
`FallingBlockEntity`; `FallingPropaguleEntity` uses Moonlight's improved base.
All three use a Moonlight falling-block renderer on the client. Keep entity
physics, registration, rendering, and ambient leaf-pile particles separate
during crash diagnosis.

## Fabric-only direction

The Fabric-only migration retains CurseForge file **5619072**, whose actual
`fabric.mod.json` declares Moonlight **1.20-2.12.11**, Minecraft **1.20.1**,
and **gplv3**. `moonlight_file_id` controls the resolved build artifact;
`moonlight_min_version` controls IW's metadata requirement. The stale
`moonlight_version=1.20-2.7.0` property has been removed.

Moonlight is a separate runtime dependency, not shaded or included in either IW
artifact. Its own platform, registry, config, and resource APIs are retained.
Do not remove them or fork their implementations simply because the library
itself supports multiple loaders.

A complete removal would need an explicit compatibility policy plus replacements
for the functional APIs above. Preserve world IDs and generated item/block
relationships; document any intentional reduction of supported trees before
changing registration. Do not silently disable generated content to make the
mod launch without the library.
