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

## Standalone sandstorms

**Added in 2.3.0**, adapted with original dust sprites and nine wind
sounds from [Scorchful](../developers/Licensing.md#scorchful-sandstorm-adaptation).
Requires only IW's existing Fabric API/Moonlight dependencies, not Scorchful,
Thermoo, CCA or another library. The [ST01-ST08 checklist](Feature-testing.md#standalone-sandstorms)
has **Not run** manual verdicts; existing snow/fire acceptance does not verify it.

Ordinary vanilla rain (`/weather rain`) becomes a sandstorm in tagged
**precipitation-disabled** desert/badlands biomes. Thunder, daytime and a separate
storm timer are not required. `/weather clear` ends it; `doWeatherCycle` controls
vanilla weather as usual. Regular sand wins when both subtype tags match.
Rainy biomes (including warm ocean) do not gain this new storm behavior.

Exposed living entities with a movement-speed attribute lose **30% total speed**
by default. Camels and entities in `immersive_weathering:sandstorm_slow_immune`
(including optional `#c:bosses`) are exempt. Roofs must block sky visibility or
the MOTION_BLOCKING heightmap to shelter entities. Clearing weather, entering
shelter/another biome or dimension, changing immunity or disabling slowing
removes IW's temporary modifier on the next server entity tick.

Each ticking chunk samples **one column**, then has a **1/16** deposition trial.
Existing IW `sand_layer_block`/`red_sand_layer_block` are placed on dry, sturdy
support, not in fluids, unsupported plants, other occupied cells or cauldrons.
Version 2.4.0 also covers the supported hosts described below.
Fresh configs now default to **eight layers per pile**, configurable from one;
existing 2.3.0 configs keep their previous cap of one. Existing taller piles never shrink
and full piles do not receive another storm pile above them. No source sand is
consumed. Existing pile loot and recipes remain.
This hook is independent of `randomTickSpeed` and `general.block_growths`.
Legacy sandy-stone growth still uses its own rain tick, chance, config and old
`has_sandstorm` biome tag, including warm ocean. Its sandy-block conversion
remains separate; its pile output now uses the same guarded weather cap.

Client dust, fog, cloud hiding and intermittent wind have separate controls.
Fog blends with vanilla weather and biome boundaries, lighting and view distance;
underwater/lava/powder-snow, blindness and darkness retain vanilla fog. Visual
biome fog/cloud hiding can remain **indoors**; shelter protects gameplay, not
every visual. Wind samples nearby storm surfaces within ten vertical blocks,
uses the Weather slider and **Wind blows** subtitles, and is not a continuous
loop. Vanilla Minimal particles disables storm dust; Decreased reduces trials.

If Scorchful is actually installed, **all new IW storm mechanics/effects yield
to it** and IW removes its own slowdown modifier. Scorchful owns its settings,
piles and behavior in that combination; IW's legacy growth still runs.
No new damage, wind force, storm-driven erosion, thirst, armor, temperature, world generation
or cauldron mechanics are included. See [settings](Configuration.md#standalone-sandstorms).

## Sand piles

**Added in 2.4.0; not present in the published 2.3.0 jar.**
Regular and red sand piles use one to eight layers; a full pile stays a layer
block, not a destructive world conversion. The configurable **weather-only**
cap never limits manual stacking or falling-pile merging, and lowering it does
not remove existing layers.

With `sand.host_mixing=true`, sand shares a dry block cell with any of the
**12 vanilla fences or a dead bush**, in either placement order. Sandstorms and
falling sand can also cover those hosts. Fence connections, collision and
interactions stay native. Waterlogged fences, walls, cactus, other plants and
modded hosts are not included; a datapack alone cannot add a new host schema.
Regular and red sand never merge with one another.

Ordinary block items replace **uncovered piles at every depth, including eight
layers**. Supported host items keep the sand around their block instead.
A covered fence is still a fence: another block cannot erase it just by
placement. A covered dead bush remains natively replaceable. Right-click a
cover with a shovel to remove its sand while leaving the host; Survival uses
one durability and the existing pile loot rules. Breaking the entire cover
uses the native host and sand loot. Recipes and eight-layer sand-block drops
are unchanged.

Unsupported sand falls without moving a fence or plant. Falling sand fills a
matching pile/eligible host to eight; excess becomes items, not an automatic
second pile. It does not freeze or replace water.

**Actual rain** gradually removes one layer per successful random tick:
default chance **1/8**, not one layer per game tick. At zero, a covered host
returns to its raw native state. There are no erosion drops. Roofs, clear
weather and snowfall preserve sand. Biomes in either
`immersive_weathering:regular_sandstorms` or `immersive_weathering:red_sandstorms`
are exempt, even if a datapack makes one rainy; temperature alone grants no
exemption. Erosion depends on `randomTickSpeed`; storm deposition does not.
See [sand controls](Configuration.md#sand-piles) and
[SP01-SP08](Feature-testing.md#sand-and-thunder). On 2026-11-02 the tester
reported **SP01-SP07 Pass; SP08 Not applicable**, at test-ID level without
itemized variant notes. This does not establish a multiplayer/coexistence pass.

## Standalone fire

Added in the **2.2.0** release line. The [manual run checklist](Feature-testing.md#standalone-fire)
is still **Not run**; this behavior contract is not a recorded gameplay pass.

Burning entities can ignite **nearby flammable blocks**, not other entities
directly. The default is one **2% trial every 20 ticks**, staggered per entity,
and at most one new fire cell per trial. Burning projectiles have **one sixteenth**
of that chance. Only loaded, dry air beside flammable blocks is eligible;
occupied cells are never replaced. Exposed rain, wet carriers and powder snow
prevent spreading. Entity-created fire does not create Nether portals.

Sunlight-only burning does not spread. Fire contact, lava, lightning, successful
Fire Aspect attacks (player or mob), burning-arrow/small-fireball hits, and torch
attacks can authorize spreading, including during an existing sunlight burn.
Vanilla unarmed burning-zombie melee retains its own ignition behavior; its
victim may spread only when the zombie's burn had a known non-sun source.
Unattributed burning from older saves or unsupported mod ignition stays
ineligible until one of these known events occurs. Extinguishing clears that
permission; known provenance survives save/reload.

Successful **direct melee** with a main-hand Torch or Soul Torch sets a living
target on ordinary vanilla fire for four seconds (subject to vanilla fire
protection), without shortening a longer burn. Survival consumes one torch;
Creative does not. Failed/blocked attacks, dead, fire-immune, fire-resistant,
wet/powder-snow targets and unsupported nonliving targets do not consume it.
Both torches use ordinary fire: no blue renderer or separate soul damage/state.

**Grass Blocks** are fuel and lose their grass surface to become vanilla
**Dirt at the same position**, without drops. Podzol, mycelium and other soils
are not converted. Fire still follows vanilla burn trials, not instant ground
conversion by a burning entity.

`doFireTick=false` stops new entity spread and grass consumption, but not the
direct torch combat action. `mobGriefing=false` stops non-player carriers,
including ownerless/mob-owned projectiles; players and player-owned projectiles
are exempt. Fire, soul fire, magma blocks and lava provide a bounded local heat
bonus; **torches and campfires do not**. Hotter biome base temperatures increase
the chance above 0.8, clamped at 2; no season integration is included. This
adjusts entity spread only, not vanilla fire's global probability.
The separate **2.4.0 thunder rule** does affect exposed ordinary fire:
while thunder is active, sky-facing sources have **90% fewer neighboring burn
and spread attempts**, and **25% chance to go out on each normal scheduled fire
tick** (roughly every 1.5-2 seconds). This includes deserts/badlands without
actual rainfall and fires on eternal-fire supports. Roofs protect the source.
Exposed burning-entity environmental spread trials also receive the 0.1
multiplier, after other chance adjustments; their burning is not extinguished
by this rule. Fire aging/scheduling and normal rain behavior remain.
Soul fire, torches and campfires are unchanged. `doFireTick=false` disables
both new thunder effects. All three thunder controls are configurable.
See [fire settings](Configuration.md#standalone-fire) and the
[manual fire checklist](Feature-testing.md#standalone-fire).
These mechanics are independent of `general.block_growths` and do not replace
existing wood charring, soot, smoldering, snow or datapack mechanics.

## Standalone snow

Beta 2.1.0-beta.1 adds four related mechanics without a separate snow mod:
snow layers can share a cell with the 12 vanilla fences, 22 vanilla walls,
grass, fern, tall grass, large fern, sunflower, lilac, rose bush, and peony.
Place snow into a dry supported host, or place a supported host into partial
snow (layers 1-7). Host properties and plant halves are retained. Gates, slabs,
stairs, waterlogged hosts, block entities, and arbitrary modded blocks are not
snow containers. Existing IW snowy masonry remains a separate, unchanged system.

The **2.1.0-beta.2** follow-up expands the explicit list to **72 hosts**:
all vanilla small flowers, including torchflowers and pink petals; the seven
tree saplings and dry mangrove propagules; red/brown mushrooms; dead bushes;
and IW weeds, dune grass, frosty grass and frosty fern. Other crops, aquatic
plants, vines and arbitrary modded plants remain excluded. Plant-first and
snow-first placement keep the existing layer count. Snow-covered short grass
uses the existing frosty-grass appearance only while covered; removing the
snow restores normal grass rather than permanently converting the plant.
Stable **2.1.0** applies the same rule to short ferns: covered ferns
use the existing frosty-fern model/tint and become normal ferns again when the
cover is removed or melts. This does not change large ferns or permanently
convert plants to IW frosty blocks.

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
[G07 retest](Feature-testing.md#beta3-paired-growth-retest) has a later broad
N11 Pass report on 2.1.0-beta.2; separate transition outcomes were not itemized.

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
