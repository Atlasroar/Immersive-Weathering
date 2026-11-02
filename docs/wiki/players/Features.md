# Features and interactions

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

These descriptions cover systems and interactions represented in the current
source. They are not an exhaustive recipe book or a claim that every modded
variant works. Settings and datapacks can change availability and probabilities.

For reproducible in-game checks, use the
[feature test sheet](Feature-testing.md). It separates random growth from tool
actions, physics, weather, fluid generators, and world generation, and includes
a dedicated stone-brick comparison matrix.

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

## Standalone snow

Beta 2.1.0-beta.1 adds four related mechanics without a separate snow mod:
snow layers can share a cell with the 12 vanilla fences, 22 vanilla walls,
grass, fern, tall grass, large fern, sunflower, lilac, rose bush, and peony.
Place snow into a dry supported host, or place a supported host into partial
snow (layers 1-7). Host properties and plant halves are retained. Gates, slabs,
stairs, waterlogged hosts, block entities, and arbitrary modded blocks are not
snow containers. Existing IW snowy masonry remains a separate, unchanged system.

The unreleased **2.1.0-beta.2** follow-up expands the explicit list to **72 hosts**:
all vanilla small flowers, including torchflowers and pink petals; the seven
tree saplings and dry mangrove propagules; red/brown mushrooms; dead bushes;
and IW weeds, dune grass, frosty grass and frosty fern. Other crops, aquatic
plants, vines and arbitrary modded plants remain excluded. Plant-first and
snow-first placement keep the existing layer count. Snow-covered short grass
uses the existing frosty-grass appearance only while covered; removing the
snow restores normal grass rather than permanently converting the plant.

Right-click a covered host with a shovel to remove its snow and retain the host.
Survival uses one durability and vanilla snow loot, including Silk Touch;
Creative produces no cover loot. Bright block light or rain melts the cover
without deleting the host. Breaking the composite is different: it breaks the
host, using its mining requirements and loot plus snow loot. Pick-block selects
the original host item, not an obtainable container item. Invalid plant support
or a missing partner is resolved on a scheduled tick, preserving snow as a
plain layer or falling snow rather than moving the plant.

Unsupported snow falls on scheduled ticks. It merges up to eight layers in the
landing cell; undeposited layers become snow-layer items rather than overwriting
hosts or spilling into an unlimited stack. Falling snow contacting a **water
source block** turns that block into ice and is consumed. Flowing water and
waterlogged solids do not freeze through this path. Ordinary ice formation
remains separate. Entity/block drop gamerules still apply.

Extra snowfall accumulation defaults to **thunderstorms only**, in cold, exposed,
dim-enough, ticking columns, capped at eight layers in one cell. Ordinary snowfall
still creates vanilla's initial snow; extra ordinary-weather layers are opt-in.
The [snow configuration](Configuration.md#standalone-snow) explains the gamerule
compatibility sentinel and independent toggles. Any block item can replace plain
vanilla partial snow layers 1-7 in place; this override does not replace layer 8
or a nonreplaceable snow-covered fence/wall.
At layers 1-7, covered plants also retain their raw plant's replacement rules
when partial-layer replacement is enabled: ordinary same-plant replacement stays
rejected, but native pink-petal stacking retains its count and orientation.
Replacing one with another supported dry host retains its snow when mixing is
enabled; an ordinary solid block replaces the plant and cover together.
Disabling mixing stops that preservation but does not prevent an otherwise
eligible ordinary replacement. Full eight-layer covers cannot be replaced this
way, and snow items still require mixing and room for another layer.
Weed aging/bonemeal, sapling stages, mushroom density checks and native plant
contact effects are retained. Failed 2x2 tree growth preserves each sapling's
independent snow count; successful tree growth replaces the affected plants
with the tree, not a snow-covered trunk.

The [snow acceptance matrix](Feature-testing.md#standalone-snow-candidate-retest)
separates automated fixture evidence from outstanding in-game checks.

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

Beta.3 corrects placement of paired tall grass, large ferns, tall seagrass, and
double flowers. Plants still need valid support and an eligible upper position;
seagrass requires water, and solid or waterlogged obstructions are not replaced.
Existing chance, biome, and density rules still apply. The
[G07 retest](Feature-testing.md#beta3-paired-growth-retest) remains pending in-game.

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
