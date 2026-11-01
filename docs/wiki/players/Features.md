# Features and interactions

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

These descriptions cover systems and interactions represented in the current
source. They are not an exhaustive recipe book or a claim that every modded
variant works. Settings and datapacks can change availability and probabilities.

## Weathering and restoring builds

| System | Intended behavior and player interaction |
| --- | --- |
| Moss | Supported stone variants gain moss through nearby influences. Shears remove moss and can yield moss clumps; flint and steel removes moss and stabilizes the resulting block. |
| Cracks | Supported masonry has cracked variants. Right-click with a pickaxe to crack eligible blocks; use the corresponding repair item or mortar to repair supported cracked blocks. Pickaxe interaction can be configured to require sneaking. |
| Rust | Supported iron blocks have rust stages. Wet sponges advance eligible blocks by one stage. Axes scrape eligible non-waxed rust; fully rusted stages are not all axe-scrapable. Steel wool removes rust and can remove wax from supported waxed blocks. |
| Wax | Fabric's waxable-pair registration connects supported iron variants to their waxed counterparts. Waxing protects eligible blocks from ordinary rust progression. |
| Fire and soot | Fire can leave supported wood charred; charred variants can smolder. Fire/campfires can form soot on nearby surfaces. The soot-disable setting has an open report. |
| Snow and sand | Data-driven growths add snowy/sandy variants and layers in applicable environments. Conditions and probabilities differ by growth definition. |

Not every block has every variant. Tool actions can consume items or durability.
Do not infer that the mod weathers arbitrary blocks from other mods.

## Trees, bark, and leaves

Stripping eligible logs with an axe can drop their bark, or scales for wood types
Moonlight identifies as non-burning. Right-click the stripped block with its
matching bark to restore the original log/wood. Automatic detection supports
many modded trees but does not guarantee compatibility with all of them.

Leaf decay can produce falling layers and piles. Piles can accumulate, age,
decompose, and affect the ground below them. Deep piles can slow entities;
deep spruce piles can hurt unprotected moving entities. Leaf particles and
decay sounds have separate client settings.

Shears can remove flowers from supported flowering azalea variants. The mod also
provides azalea flowers and a wearable flower crown.

## Soil, plants, and cold environments

Soil content includes loam, silt, sandy dirt, earthen clay, permafrost, mulch,
nulch, rooted grass, and several farmland variants. Growths can add weeds,
vegetation, roots, or other changes under their configured conditions. Farmland
support depends on plant type, tags, and mod integration; loamy crop placement
has a [reported Fabric bug](Troubleshooting.md).

Cold systems include frost/frosted variants, thin ice, icicles, and freezing
effects. Icicles have configurable spawning and vibration-related behavior.
Lightning can create vitrified sand and fulgurite under applicable conditions.
These are separate systems; disabling one does not necessarily disable all
cold or lightning effects.

## Recipes and natural generators

For exact crafting recipes, consult the installed pack's recipe display and the
[shipped recipes](../../../src/main/resources/data/immersive_weathering/recipes).
Natural fluid generators have separate
[data definitions](../../../src/main/resources/data/immersive_weathering/fluid_generators);
their output depends on flow, neighboring blocks, and generator priority.
Do not rely on older upstream wiki layouts as current instructions.

See [configuration](Configuration.md) for controls and
[troubleshooting](Troubleshooting.md) for limitations.
