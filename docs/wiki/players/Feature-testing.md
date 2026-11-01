# In-game feature test sheet

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

**Target:** Immersive Weathering **2.1.0-beta.1**, Minecraft **1.20.1**, Fabric.
This is a source-backed manual acceptance checklist with a dated user-reported
results ledger below. The detailed tables remain a **reusable blank checklist**:
their `Not run` cells are template defaults, not the current test-round verdicts.
Use the dated ledger for recorded outcomes.
Datapacks, configuration, other mods, and later releases can change expectations.

## Standalone snow candidate retest

Use a new backed-up/disposable test area. These are **not recorded in-game
passes**. Automated production-code fixtures and client/server launch evidence
are described in [development validation](../developers/Development.md#snow-mechanics-harness).
Keep the beta.2 ledger below unchanged.

| Case | Setup and controls | Required result | Manual verdict |
| --- | --- | --- | --- |
| N01 | Each of 12 vanilla fences and 22 walls; isolated, raw/wrapped neighbors, four rotations, all snow layers 1-8 | Snow shares the cell; raw connectivity, host textures, collision, pathfinding and pick-block remain correct | Not run |
| N02 | Grass, fern, tall grass, large fern, sunflower, lilac, rose bush, peony; snow-first and host-first, both halves | Correct host/state after placing, shoveling and melting; no transient paired disappearance; bonemeal preserves snow | Not run |
| N03 | Shovel use, ordinary break, correct/incorrect host tool, shears, Silk Touch, Creative; each half of a mixed pair | Shovel use removes only cover; breaking uses host mining/drop rules; no doubled host or snow drops; Creative no cover drops | Not run |
| N04 | Remove support under plain/covered snow at layers 1-8; remove plant soil/partner; unload/reload and save/reopen while falling | Only snow falls; valid hosts stay; invalid plants die; counts/state persist; no ghost entity/client disagreement | Not run |
| N05 | Land every incoming count 1-8 on partial piles, full piles, dry hosts, blocked/wet cells, moving pistons and build bounds | At most eight in target cell, exact remainder as items, no overwrites/spill/dupes; gamerule-disabled drops are intentional | Not run |
| N06 | Fall through source water, flowing levels, waterlogged fence/wall, and a high-speed source crossing; warm/cold controls | Only an actual source WATER block becomes ice; flowing/waterlogged solids stay intact; source consumes incoming snow | Not run |
| N07 | Cold exposed columns: clear, ordinary snowfall, thunder; warm rain, roof, block light 12, outside simulation distance | Default extra layers only in eligible thunder; vanilla first snow in ordinary snowfall; no roof/light/warm/unticked accumulation | Not run |
| N08 | Gamerule 0,1,2,8,24; IW cap 1,3,8; accumulation off; ordinary on/off; mixing/falling/replacement off; block-growth off | Sentinel 1 uses IW cap, 0 stops weather snow, custom positive limits intersect cap; independent toggles and single-column bound hold | Not run |
| N09 | Replace plain/plant-covered snow 1-8 with stone, chest, oriented fence, flowers, same-plant items and unsupported hosts; mixing/replacement toggles; permission/adventure/obstruction controls | Normal item placement/callbacks/consumption survive; covered plants obey raw replacement rules; supported replacements retain cover only with mixing; solids replace cover; layer 8/fences/walls remain protected | Not run |
| N10 | Piston push/destroy, explosion, fire, shearing; dedicated client sync; save/reload; vanilla renderer and existing Sodium setup; resource-pack reload | Original host/drop/state semantics; no missing models, plant-tint/snow-tint errors, flickering or rendering-library requirement | Not run |
| N11 | Existing IW snowy masonry IDs/config/data, old worlds, leaf/sand falling layers, beta.3 G07 cases | No destructive migration or changed independent mechanisms | Not run |

## Reported results: November 1, 2026

The tester supplied these verdicts for the current beta.2 test round. They are
**user-reported in-game observations**, not independent reproductions or proof
that every control/subcase in a combined row was exercised. The planned method
was accelerated random ticking; exact effective gamerules, durations, counts,
world seed/coordinates, configurations, logs, screenshots, and a fresh complete
mod/version inventory were not supplied with this report.

| Group | Reported Pass | Other outcomes / coverage limits |
| --- | --- | --- |
| Weathering | W01-W10 | W01 and W02 explicitly passed for all listed blocks. W09/W10 retain the reported pass, but steel-wool coverage is unresolved; see below. |
| Stone-brick subcases | SB01-SB06 | All passed. The tester explicitly authorized closure of #11. |
| Trees | T01-T11 | All reported Pass; no individual biome/modded-tree/control inventory supplied. |
| Soil and plants | G01-G06, G08-G09, G12-G13 | **G07 Fail:** double-tall blocks do not appear to work. G11 and G14 **Not observed**. **G10 not reported**. |
| Cold/weather | C01-C07, C09-C13 | **C08 mixed:** hanging-ice/support-loss drop Pass; **vibration does not work**. |
| Fire/lightning | H01-H05, H09-H10 | H06-H08 **Not observed**. |
| Fluid generators | None reported | **R01-R14 not reported**, not failed or passed by implication. |
| Other/regressions | Q01-Q06, Q08-Q09 | Q07 **Not applicable**. Q08's broad pass does not establish individual optional-mod combinations. |

Across the 81 main cases this records **58 reported passes** (including the
qualified W09/W10 reports), **one failure**, **one mixed result**, **five Not
observed**, **one Not applicable**, and **15 not reported**. SB01-SB06 are six
additional subcases, not additional main cases. Keep missing reports distinct
from unsuccessful observations.

**Stone-brick report closed:** [#11](https://github.com/Atlasroar/Immersive-Weathering/issues/11)
was closed as completed at the tester's explicit request, based on W01-W06 and
SB01-SB06 passing. No new code fix or measured equality of spread rates is
claimed.

**G07 follow-up:** the tester reports double-tall growth does not appear to
work. This combined case includes grass -> tall grass, fern -> large fern,
and seagrass -> tall seagrass; which individual transitions failed was not
specified. Preserve the reported failure without inventing a root cause or
claiming a fix.

**C08 follow-up:** falling after support loss passed, while vibration-triggered
falling failed according to the tester. This is consistent with the source
discrepancy documented in section C, but the report does not yet isolate the
event, distance, listener setting, or exact causal path.

**W09/W10 steel-wool discrepancy:** the tester could not find steel wool and
reported that axes replaced it in 1.20.1. Their axe observations and overall
pass verdicts are preserved. However, this fork still registers
`immersive_weathering:steel_wool` in
[ModItems](../../../src/main/java/com/ordana/immersive_weathering/reg/ModItems.java)
and retains its interaction path in
[ModEvents](../../../src/main/java/com/ordana/immersive_weathering/events/ModEvents.java).
The claim of removal/replacement is therefore **not confirmed for this build**.
Steel-wool acquisition/use and exact axe coverage remain unresolved; the report
does not validate absent steel-wool interactions.

## Beta.3 paired-growth retest

The beta.2 ledger above is historical and remains unchanged. Beta.3 corrects
paired placement in the implementation; deterministic production-code fixtures
now retain both halves. This is **not** a new in-game pass for G07, and does not
change its recorded beta.2 failure. No fresh client/server game-loop smoke is
claimed.

Retest grass -> tall grass, fern -> large fern, and seagrass -> tall seagrass
separately, on supported soil with loaded space above. For seagrass, use water
above the short plant, not air, lava, or a waterlogged solid block. Compare
blocked upper positions and unsupported plants; obstructions must not be
overwritten. Inspect `half=lower` and `half=upper` with F3, then update neighboring
blocks and confirm both halves persist without drops or holes.

Also repeat G04's double-flower outputs (lilac, rose bush, peony, sunflower) in
their configured biomes and the rare dark-forest fence/pumpkin variants when
observed. Keep rarity distinct from failure. Growth chances, density limits,
biome rules, and random-tick cadence have not been increased; accelerated ticking
does not guarantee a rare output in a short observation window. Record version,
fixtures, controls, counts, and logs using the preparation steps below.

## Prepare a disposable test world

Back up first. Use small, labeled plots in a disposable world, not your main
build. Keep plots loaded, inside simulation distance, and the game unpaused.
Separate fire, lightning, lava, and falling-icicle plots from everything else.
Use Creative to construct fixtures, then **Survival** for item consumption,
durability, drops, movement, and damage assertions. Interaction drops in Creative
are suppressed by default (`general.drop_in_creative=false`).

Record IW, Loader, Fabric API, Moonlight, other mods, datapacks/resource packs,
effective common config, world seed, dimension, coordinates, biome, light,
weather, time of day, and simulation distance. Use F3 to capture actual block
IDs and states rather than relying on similar-looking textures.

**`randomTickSpeed` is not the server's ticks-per-second setting.** Vanilla
1.20.1 supports this gamerule, not the later vanilla `/tick rate` command.
First query and record your existing value:

```mcfunction
/gamerule randomTickSpeed
/time query gametime
```

Do a short baseline observation at the vanilla default of **3**, then use your
proposed **1000** for the growth plots:

```mcfunction
/gamerule randomTickSpeed 3
/gamerule randomTickSpeed 1000
```

Run those changes at their respective stages, not together without observing.
Use bounded 15-30 second observation windows and query `gametime` before and
after each. At 20 server ticks/second, 30 seconds is about 600 game ticks, but
lag can make the actual number much lower. Pause acceleration if the world
becomes unresponsive or falling entities/drops accumulate. High values also
accelerate vanilla crops, saplings, decay, and eligible mod blocks.

**Return to 3 for interactions, fluids, movement, and physics tests.** These
systems are not uniformly accelerated by this gamerule. IW's sky-access
sampling does use it, but still needs the correct weather, biome, and exposure.
Use `/weather clear`, `/weather rain`, `/time set day`, and `/time set night`
only in the disposable world; record and restore any settings you change.
In cold biomes `/weather rain` produces snow. In a tagged desert it can drive
IW sandstorms despite the absence of vanilla rain particles.

At the end, restore the **original recorded** `randomTickSpeed`, not necessarily
3. Record `doFireTick`, `mobGriefing`, and `doWeatherCycle` if relevant; do not
silently change them and treat the resulting run as default behavior.

## How to judge a result

Use **Pass**, **Fail**, **Not observed**, or **Not applicable**, with notes.
Pass requires both the positive observation and the relevant control, not merely
that the game did not crash. Record variants separately, for example
`W03 / stone_brick_slab / top / waterlogged`.

**Not observed is not automatically Fail.** Default moss and crack immunity
are both **0.4**; position-seeded decisions intentionally leave resistant
positions. Waiting at the same position does not defeat that immunity.
Density checks, target eligibility, growth blacklists, weather, biome, and
plant survival also matter. Use several fresh fixtures at different coordinates
(at least 16 candidates for patch weathering) and keep adjacent air available.
Avoid mixing rust stages within their default four-block influence radius:
younger neighboring stages can inhibit older ones.

Use small empty planting spaces initially. Growths can stop when their local
density limit is reached; harvest/clear a plot before testing it again.
Natural weathering and a tool-forced conversion are **different tests**:
a successful tool action proves the variant mapping, not the spread rate.
Report diagnostic runs with altered immunity/chance/config separately from
default-config observations.

## W: masonry, moss, cracks, iron, and wax

| ID | Setup / trigger | Intended observation and control | Result |
| --- | --- | --- | --- |
| W01 | Put fresh stone, cobblestone, stone bricks, and bricks beside water and supported mossy neighbors, with exposed air. Use several positions. | Eligible blocks gain their mapped mossy variants in patches, not uniformly. Compare fresh specimens without strong sources, and a separate lava-adjacent plot; lava inhibits moss. Default `needs_air=true` matters. | Not run |
| W02 | Put fresh stone bricks and bricks beside cracked counterparts and a controlled fire/lava source. Keep this separate from W01. | Eligible masonry can crack. Compare source-free fixtures. Do not expect plain stone or cobblestone to gain nonexistent cracked variants. Fixed immunity means not every specimen converts. | Not run |
| W03 | In Survival, shear mossy blocks; then apply the recovered moss clump to their clean counterparts. | Shears remove moss, yield a clump, and use durability; a clump reapplies moss and is consumed. Repeat across the stone-brick matrix below. An unsupported block should not acquire an invented variant. | Not run |
| W04 | Use flint and steel on mossy masonry, then expose the cleaned result to W01 conditions. | Moss is removed and the resulting weatherable block is marked stable against ordinary re-weathering. Inspect the state. Compare a freshly placed, unstabilized counterpart; do not use the stabilized sample to measure natural spread. | Not run |
| W05 | Right-click eligible masonry with a pickaxe, then repair with its matching repair material or mortar. | Pickaxe cracks it and uses durability. Repair restores the uncracked counterpart, consumes an item in Survival, and marks it stable. Default pickaxe interaction does not require sneaking; default cracking does not drop a brick. | Not run |
| W06 | Repeat W01-W05 on the supported families listed below, including stairs, slabs, and walls. | Conversion preserves applicable shared properties such as facing, half, slab type, and waterlogging. Fixed neighboring layouts help distinguish conversion errors from legitimate connection recalculation. Report natural and manual results separately for #11. | Not run |
| W07 | Expose supported cut/plate iron and iron bars/doors/trapdoors to water, air, and rain. Start isolated groups at each stage. | Natural progression is clean -> exposed -> weathered -> rusted, under the rust settings and neighbor rules. Waxed controls should resist ordinary progression. Plain vanilla iron blocks are not interchangeable with cut/plate iron. | Not run |
| W08 | Apply a wet sponge to supported non-final rust stages. Test the final stage separately. | Each successful application advances one stage, not all stages. Default sponge drying is off. Do not assume a wet sponge is forbidden from transforming every waxed variant: this handler has no blanket wax exclusion. | Not run |
| W09 | Axe-scrape exposed/weathered non-waxed iron; then try fully rusted iron. Test steel wool separately. | Axe restores eligible intermediate rust by one stage, but not the fully rusted stage. Steel wool can restore fully rusted non-waxed iron by one stage. Sound/durability alone is not proof of restoration. The axe-scraping flag gates both paths. | Not run |
| W10 | Honeycomb-wax supported iron, inspect its ID, then use steel wool. Include a waxed copper control. | Waxing uses the registered waxed counterpart; steel wool removes supported wax. Additional uses can remove eligible rust/copper oxidation stages. Separate wax removal from rust removal and inspect each intermediate ID. | Not run |

### Stone-brick comparison matrix for issue #11

Keep equivalent water/air or heat exposure, timing, config, and plot size.
Use different coordinates and multiple samples; identical conversion counts are
**not** an intended requirement across different block families. Count
eligible starting specimens and converted specimens rather than comparing
visual coverage alone.

| Subcase | Starting block | Moss counterpart | Crack counterpart |
| --- | --- | --- | --- |
| SB01 | `minecraft:stone_bricks` | `minecraft:mossy_stone_bricks` | `minecraft:cracked_stone_bricks` |
| SB02 | `minecraft:stone_brick_stairs` | `minecraft:mossy_stone_brick_stairs` | `immersive_weathering:cracked_stone_brick_stairs` |
| SB03 | `minecraft:stone_brick_slab` | `minecraft:mossy_stone_brick_slab` | `immersive_weathering:cracked_stone_brick_slab` |
| SB04 | `minecraft:stone_brick_wall` | `minecraft:mossy_stone_brick_wall` | `immersive_weathering:cracked_stone_brick_wall` |
| SB05 | `minecraft:chiseled_stone_bricks` | `immersive_weathering:mossy_chiseled_stone_bricks` | No mapped cracked counterpart; not a crack failure |
| SB06 | Stone/cobblestone and their supported shapes; ordinary bricks and shapes | Matched moss variants, as W01/W03 controls | Bricks have crack variants; stone/cobblestone do not |

For SB02 vary facing and top/bottom half; for SB03 vary top/bottom/double slabs
and waterlogged single slabs. Check wall connections and waterlogging in SB04.
Additional **crack** families are nether bricks, polished blackstone bricks,
deepslate bricks, deepslate tiles, prismarine bricks, and end stone bricks,
including the mapped shapes. These are not all moss families.

Record the exact missing behavior in
[#11](https://github.com/Atlasroar/Immersive-Weathering/issues/11): moss,
cracking, tool conversion, or a different growth. That report was closed after
the November 1 tester confirmation above; the performance beta did not claim
a separate rate fix.
Authoritative variant maps:
[Mossable](../../../src/main/java/com/ordana/immersive_weathering/blocks/mossy/Mossable.java)
and [Crackable](../../../src/main/java/com/ordana/immersive_weathering/blocks/cracked/Crackable.java).

## T: trees, piles, ivy, and azalea

| ID | Setup / trigger | Intended observation and control | Result |
| --- | --- | --- | --- |
| T01 | Axe-strip an oak log and oak wood in Survival; repeat recognized vanilla/modded wood types. | Vanilla stripping proceeds and matching bark drops; recognized non-burning types can have scales instead. Creative drop suppression is a control. Discovery is not a guarantee for every modded tree or bamboo representation. | Not run |
| T02 | Apply the matching bark to stripped log/wood, then apply bark to the end-grain face of an unstripped log. | Stripped blocks regain their original barked counterparts and properties. Matching bark on an unstripped log's axis-facing end can make all-bark wood. Wrong bark/face should not perform that conversion. | Not run |
| T03 | Use naturally grown, non-persistent leaves less than 12 blocks above clear ground; compare player-placed persistent leaves. Keep the area loaded. | Natural leaf growth can deposit the matching pile. This placement path uses the default three-layer cap and local density/neighbor rules; it does not pile indefinitely. Persistent leaves are excluded from this path. | Not run |
| T04 | Remove a tree's supporting logs and let its natural leaves decay. Compare several trees, including oak and spruce. | Decay can create falling piles and client leaf effects, not on every decay (default chance 0.3). This separate decay path creates aged piles with 1-5 layers; the three-layer cap in T03 is not a universal cap on every source. | Not run |
| T05 | Observe an aged oak/spruce pile over dirt-base ground. Keep a fresh `age=0` manual pile as control. | Aged piles lose layers, age, and eventually disappear; leafy piles can turn suitable ground into loam, pine piles into podzol. Fresh piles do not enter that aging branch merely because time passes. Soil conversion is probabilistic. | Not run |
| T06 | Manually stack piles to eight layers; remove their support. Also place a thin pile in air directly above source water. | Manual stacking is distinct from natural deposition. Unsupported piles fall/settle; floating water-surface piles use `layers=0`, not ordinary underwater placement. No falling-layer error or crash should occur. Test both ground and water landings. | Not run |
| T07 | Walk through deep piles in Survival; compare 1-3 layers with 4+ layers. Repeat a 6+ layer spruce pile with and without leggings. | Deep piles slow ordinary living entities. Moving through sufficiently deep spruce piles can cause thorn damage without leggings. Depth Strider is a movement exemption; do not use it for the ordinary slowdown assertion. | Not run |
| T08 | Bonemeal a flowering-azalea pile with adjacent non-flowering azalea bush/leaves/pile targets. | The pile can flower eligible nearby azalea variants, probabilistically. An unrelated plant should not become azalea. Leave space and repeat rather than demanding every neighbor change on one use. | Not run |
| T09 | Shear flowering azalea bush, leaves, and pile; apply azalea flowers to the non-flowering versions. | Shearing yields flowers and removes flowering; flowers restore eligible flowering counterparts and are consumed in Survival. Repeat each of the three variant types. | Not run |
| T10 | Place young ivy on a supported wall, leave adjoining surfaces free, and observe growth; also try bonemeal and remove support. | Ivy can spread to eligible faces, with position/age restrictions. It need not cover every face forever. Unsupported remnants should be checked separately from spread; repeat with tree-felling mods only if installed. | Not run |
| T11 | Equip the flower crown in the head slot near non-hostile bees, then remove it. Try an anvil name such as `rainbow`. | Bees can follow a nearby crown wearer; the crown renders and emits its particles. Named supported variants change appearance. Wearing is the relevant trigger, not merely carrying it; competing bee tasks/pathfinding can delay following. | Not run |

For an isolated T05 fixture, prepare a solid dirt floor and use this placement
two blocks away from your standing position:

```mcfunction
/setblock ~2 ~ ~ immersive_weathering:oak_leaf_pile[age=1,layers=8]
```

This tests **decomposition**, not whether a naturally decaying tree creates the
right pile. Check the actual pile ID for other discovered leaf types.

## G: soil, crops, and living growth

| ID | Setup / trigger | Intended observation and control | Result |
| --- | --- | --- | --- |
| G01 | Hoe loam, silt, sandy dirt, and earthen clay. Plant wheat, carrots, potatoes, and beetroot beside matched vanilla-farmland controls at suitable light. | Supported soil makes its corresponding farmland. Test immediate seed acceptance and persistence at rate 3 before growth acceleration. Record plant/farmland IDs individually; loamy placement has report #1, not a confirmed universal pass. | Not run |
| G02 | Compare hydrated (`moisture=7`), well-lit farmland with dry and shaded controls. Inspect each soil's crop tags. | Matching crop types receive additional growth attempts on appropriate soils; moisture/light and tags matter. The implementation does not establish a guaranteed tenfold maturity speed. Water reach differs by soil, so use nearby water for the basic positive. | Not run |
| G03 | Put suitable dirt/soil beside grass and mycelium in lit plots; include covered controls. Test grass-over-mycelium and the reverse separately. | Supported grassy soils spread or lose grass under appropriate light/cover rules. The two grass/mycelium replacement flags govern different directions. Inspect actual soil variants, not just the top texture. | Not run |
| G04 | Clear air above small grassy-block plots in each listed biome below. Also leave a dense vegetation plot untouched. | Base grass and biome-specific vegetation can appear. Density-limited growth should slow/stop in crowded plots; rare outputs need separate Not observed results, not a forced failure. | Not run |
| G05 | Put rooted dirt/rooted grass into a bank of ordinary dirt and grass, with free underside and side spaces; try the biome-specific sapling plots below. | Eligible neighboring ground can become rooted dirt/rooted grass; exposed positions can gain hanging roots/wall roots. Rooted grass can spawn the configured biome's saplings. A solidly filled bank has no space for hanging roots. | Not run |
| G06 | Leave small fertile, sky-exposed plots during clear weather. Then add mature `age=7` weeds near replaceable plants and compare a weed-free plot. | Weeds can establish and mature; mature-weed spread can replace tagged targets, including susceptible plants, with density limits. Replacement is not the same bug as seeds failing immediate placement on bare farmland. | Not run |
| G07 | Grow grass, fern, and underwater seagrass with room for the second block; compare obstructed fixtures. | Eligible short plants become their tall versions. Upper space, correct water, and plant survival are required; record all three plants separately. | Not run |
| G08 | Leave open space above crimson/warped nylium. Place weeping-vine space below nether wart blocks and twisting-vine space above warped wart blocks. | Nylium produces its configured roots/fungi; supported wart-block growth produces the respective vines. Check orientation and survival rather than expecting either vine from any Nether block. | Not run |
| G09 | Test mycelium and podzol mushroom/fern plots; river-biome earthen clay with adjacent water for sugar cane; saplings in desert/Nether controls. | Their configured vegetation can grow. Saplings can become dead bushes in the relevant desert/Nether growths. Mushroom light and sugar-cane support/water still matter. Keep ordinary sapling-growth tests outside those drying conditions. | Not run |
| G10 | Underwater in `minecraft:warm_ocean`, test brain, bubble, fire, horn, and tube coral blocks separately with nearby water and low coral density. | Matching coral, fans, or wall fans can appear in eligible water. Wet sponge is a rare configured output. A plains water tank is not an equivalent biome test; retain all five coral results separately. | Not run |
| G11 | Complete a composter and collect its product with the dirt-output flag enabled; repeat disabled in a separate config run. | The advertised feature is dirt instead of vanilla bone meal, but no active IW Java consumer of `COMPOSTER_DIRT` was found. Treat this as a feature-gap check, not confirmed implemented behavior. Also test compostable IW materials separately. | Not run |
| G12 | Suspend plain permafrost over a landing area; compare a cold/dark fixture with adjacent water or bright block light. | Frozen permafrost can remain suspended. Adjacent water or sufficient light schedules a gravity check; an unsupported block can fall and may disappear on landing. Do not assume a universal conversion to dirt called "melting." | Not run |
| G13 | Place earthen clay and mulch in water and inspect `waterlogged`; compare dry placement. Land on mulch and stone from a matched safe test height. | Supported soil retains its waterlogged state/fluid behavior. Mulch reduces fall damage compared with stone; client drip particles are separate from fluid state. Repeat soil types individually. | Not run |
| G14 | In Survival, use a lava bucket on non-molten nulch, then an empty bucket on molten nulch. Compare stepping/landing on molten and cooled nulch, with plain boots and Frost Walker. | Buckets toggle `molten` and exchange lava correctly. Molten nulch hurts ordinary living entities stepping on it; Frost Walker exempts that damage. Nulch cushions falls. R13 separately tests cooling through water contact. | Not run |

### Biome subcases for G04/G05

Use F3 to confirm the biome **at the plot's elevation**, including underground
biomes. `/locate biome minecraft:...` can help find a site; no amount of waiting
in plains supplies a missing warm-ocean or lush-caves biome predicate.
These are illustrative outputs, not an exhaustive list of weighted outcomes.
For each row, record a vegetation result and, where present, a sapling result.

| Biome / rule family | Vegetation examples | Rooted-grass sapling examples |
| --- | --- | --- |
| Plains | Grass, flowers, weeds | No separate plains sapling rule |
| Sunflower plains | Grass and flowers, including sunflower outcomes | No separate rule |
| Forest / windswept forest | Grass and forest flowers | Oak/birch |
| Flower forest | Varied flowers and grass | Oak/birch |
| Birch / old-growth birch forest | Grass, flowers, mushrooms, weeds | Birch |
| Dark forest | Grass/mushrooms and rare pumpkin/fence formations | No separate rule |
| Taiga / snowy taiga | Grass, fern, sweet berry bush | Spruce |
| Old-growth spruce/pine taiga | Grass, fern, mushrooms | No separate old-growth sapling rule |
| Jungle / sparse jungle | Grass, fern, flowers, melon | Jungle/bamboo |
| Bamboo jungle | Grass, fern, bamboo, melon | No separate bamboo-jungle sapling rule |
| Swamp | Grass, blue orchid | No separate swamp sapling rule |
| Mangrove swamp | No separate mangrove vegetation rule beyond applicable base growth | Mangrove propagule |
| Lush caves | Grass, azalea, flowering azalea | No separate rule |
| Cherry grove | Grass, pink petals | Cherry |
| Desert / badlands / eroded badlands | Grass, dune grass, dead bush | No separate rule for these three |
| Wooded badlands / savanna / savanna plateau / windswept savanna | Grass, dune grass, dead bush | Acacia |

The [shipped growth definitions](../../../src/main/resources/data/immersive_weathering/block_growths)
are authoritative for exact owners, biome lists, replacement predicates, output
weights, density checks, and chances. The base and biome-specific rules can both
apply; disabling just one grass rule does not necessarily remove all grass.

## C: cold, weather coverings, and ice

| ID | Setup / trigger | Intended observation and control | Result |
| --- | --- | --- | --- |
| C01 | In an icy biome at night, place ordinary glass/panes, grass, and fern with free air above/nearby. Separately expose counterparts to snow. | Eligible frosty variants form through their night/block-tick or snow/sky rules; record their `natural` state. Compare warm clear-day specimens. A roof only controls the sky path, not the separate nighttime block-tick path. | Not run |
| C02 | Inspect frost in newly generated icy terrain; apply the frost item to glass/panes, grass, and fern in cold conditions. | The item converts mapped targets to frosty counterparts and is consumed in Survival; unsupported targets use ordinary frost placement instead. Shipped `frost_growth` and `frost_spreading` have chance **0**: lack of ambient growth from those two rules is expected, even at 1000. | Not run |
| C03 | Put ordinary ice beside source water in an icy biome on a clear night, then during snow. For solidification, use expanding thin ice with adjacent packed ice and water to its north. | Thin ice can form/spread. The solidification rule specifically needs packed ice nearby, `can_expand`, and its north-facing water target check. Ordinary ice alone is not a complete solidification fixture; warm daytime water is not an equivalent formation test. | Not run |
| C04 | At rate 3, walk/jump on thin ice in Survival without Feather Falling; repeat with enchanted boots. | Eligible heavy living entities can progressively crack thin ice or break it into water. Neighboring ice can crack too. Feather Falling is an explicit exemption, so use plain boots for the positive test. | Not run |
| C05 | Test thin ice near bright block light. Test ambient daytime/warm melting as a separate config run. | Bright-light melting is separate from the optional natural-melting branch. Natural thin-ice melting is **off by default**; absence of that daytime branch is not a failure. | Not run |
| C06 | In cold/icy conditions, hang ice above air; also test snowy exposed supports and non-persistent cold leaves. | Icicles can form through several paths. Spawning is probabilistic and some paths use position eligibility; one empty underside is insufficient evidence of failure. Keep melt conditions out of the positive fixture. | Not run |
| C07 | Place a downward icicle under ice with water above that ice, in an icy biome on a clear day. Use separate open-floor and cauldron fixtures. | A suitable tip can grow down or produce an upward counterpart; eligible drips can schedule cauldron filling. Keep vertical space clear. Cauldron completion uses scheduled ticks, so observe at rate 3 as well. | Not run |
| C08 | At rate 3, remove a hanging icicle's support with free space below. Separately open a nearby chest/door with support intact and vibration reaction enabled. | Support loss should produce falling icicles; falling/upward-tip hazards can damage entities. Vibration-only dislodging is an independent intended behavior with the source discrepancy noted below; do not count support loss as a vibration pass. | Not run |
| C09 | Move icicles into bright light, the Nether, or non-icy clear daytime conditions. Compare the cold supported control. | Icicles melt/disappear under their applicable random-tick conditions. Their behavior is not governed by the thin-ice natural-melting flag. | Not run |
| C10 | Eat icicles and the ice sickle at rate 3 with icicle food enabled; repeat disabled. | Eating adds freezing ticks and provides brief Fire Resistance without hunger nutrition. The effect is configured with hidden particles/icon; an absent HUD badge is not a failure. Food disabled should leave these non-edible uses/content available. | Not run |
| C11 | In snowfall, expose mapped stone to the sky; separately apply a snowball and shovel the cover away in Survival. | Snowy variants can form; snowball applies an eligible cover, shovel removes it, uses durability, and yields a snowball. Warmth/bright light can remove snow through separate logic. A roofed target controls only sky deposition. | Not run |
| C12 | Run `/weather rain` in tagged desert terrain with exposed sandable masonry. Separately apply a sand-layer item and shovel it off. | Sandstorm growth makes eligible sandy states/layers. Shoveling can remove a heavier sandiness stage first, then the remaining cover, yielding a sand layer. Ordinary rain in an untagged biome is not a sandstorm. | Not run |
| C13 | Put a multi-layer sand pile over a supported sandable block. Separately test ordinary/red sandstone ledges with air directly below and a landing floor underneath. | The ordinary sand-layer seeping rule can thin piles with more than one layer while making the block below sandy. Sandstone/red sandstone generate their matching layers below into eligible air. Do not assume every red layer has the same seeping rule. | Not run |

**Vibration discrepancy, not yet reproduced in game:** the inspected
[icicle listener](../../../src/main/java/com/ordana/immersive_weathering/entities/IcicleBlockEntity.java)
looks up volume using `context.affectedState()` in a map keyed by `GameEvent`,
rather than using the event. This suggests vibration-only falling may not
trigger. Ordinary footsteps are not in its explicit event-volume table either.
Test chest/door events and support removal separately, record the outcome, and
do not try to compensate with a larger random-tick speed. Recreate the icicle
after changing the reaction flag: listener radius is captured at construction.

**Food/config caveat:** the inspected
[icicle food definition](../../../src/main/java/com/ordana/immersive_weathering/reg/ModFoods.java)
unconditionally includes 80 game ticks of Fire Resistance. Do not assume the
separate `icicle_fire_resistance` flag is a verified working gate. Likewise,
the inspected brick-repair handler does not check the setting named
`item_interaction.brick_breaking`. These are source caveats, not claimed fixes
or completed gameplay reproductions.

## H: fire, soot, lava growth, and lightning

| ID | Setup / trigger | Intended observation and control | Result |
| --- | --- | --- | --- |
| H01 | With fire ticking permitted, let fire consume recognized burnable wood in an isolated plot. Use multiple logs/planks. | Some consumed wood is replaced by its charred variant (default chance 0.3), possibly smoldering. Total burning/destruction is also possible; a single missing charred block is inconclusive. | Not run |
| H02 | At rate 3, light charred blocks with flint and steel/fire charge, then right-click smoldering blocks with a shovel and other held items. | Lighting uses durability/consumes fire charges in Survival. The inspected interaction extinguishes an already-smoldering block on right-click without restricting the item to a shovel. Confirm state changes; the advertised shovel flag is not checked by this handler. | Not run |
| H03 | Put a lit campfire below a nonflammable ceiling/wall within eight blocks; repeat unlit, and test fire separately. | Soot can attach to nearby supported faces. A signal-fire campfire has a longer 23-block smoke search. Existing soot does not disappear just because a spawning setting is disabled. | Not run |
| H04 | In a separate restarted/configured run, disable soot spawning and place fresh lit campfire and fire fixtures. | Intended negative control is no newly generated soot from the disabled paths. This is specifically an open report (#7); test both paths and record any continuing growth rather than assuming the toggle works. | Not run |
| H05 | Expose cobweb to fire with its flammability setting enabled; compare disabled in a separate run. | Configured cobweb burning should differ from the disabled control. This is a fire interaction, not proof from passive growth alone. | Not run |
| H06 | Put magma beside lava without the extra fluid-generator ingredients below. | The magma-owned random growth can turn eligible nearby lava into magma, subject to its density/replacement rules. This is distinct from bubble-column fluid generation in R09. | Not run |
| H07 | Strike isolated stone-family, magma, and obsidian targets with real lightning above their surfaces. | Registered lightning growth can produce magma from overworld base stone, lava from magma, and crying obsidian from obsidian. Inspect which block actually receives the strike; this is not random-tick growth. | Not run |
| H08 | Strike sand/red-sand terrain in the separate lightning plot with vitrification enabled. Repeat strikes/fresh targets. | Applicable sand becomes vitrified; fulgurite can form, with default chance 0.4 rather than on every strike. Compare disabled vitrification in a separate run. Ordinary lightning/fire destruction remains a separate vanilla effect. | Not run |
| H09 | Put charred fixtures beside fire/magma sources, then compare rain/water exposure. Step on smoldering and extinguished fixtures with plain boots and Frost Walker. | Sufficient heat can relight charred blocks; wet/cooling conditions extinguish them. Smoldering surfaces cause hot-floor damage to eligible entities; Frost Walker is an exemption. Keep these environmental changes separate from H02's tool action. | Not run |
| H10 | Build a short charred-block overhang supported below or beside it, then remove supports at rate 3. Inspect `overhang`. | Supported blocks can remain; unsupported `overhang=2` blocks schedule falling. Gravity/collapse uses scheduled ticks and does not require randomTickSpeed 1000. Check the actual support layout and logs. | Not run |

## R: all 14 shipped fluid-generator definitions

Use **rate 3**, contained nonflammable rigs, and freshly placed fluid for each
trial. Most entries replace the **lava itself**; R11-R14 act on another target
block. All listed ingredient conditions are required, not alternatives:
an `any` list means **each** predicate must match some adjacent face.
For water-side requirements, use exposed horizontal contact in a possible-flow
direction, not just water above a sealed lava container. Vertical block-state
requirements include their specified axis.

Do not combine recipes accidentally. Lower numeric generator priorities are
checked first, so a more specific valid recipe can beat another valid recipe.
Remove one required ingredient for each negative control; it should stop that
specific recipe, but another custom or vanilla output may still occur. Also
retest unexpected results without accelerated magma growth.

| ID | Recipe / fresh-fluid setup | Expected output | Result |
| --- | --- | --- | --- |
| R01 | Lava, horizontal water contact, adjacent diorite; omit quartz. | Andesite replaces lava. | Not run |
| R02 | Lava, horizontal water contact, adjacent tagged quartz block; omit diorite. | Diorite replaces lava. | Not run |
| R03 | Lava, horizontal water contact, adjacent diorite **and** tagged quartz block. | Granite replaces lava; its priority wins over the simpler R01/R02 recipes. | Not run |
| R04 | **Flowing** lava directly above basalt with `axis=y`; omit other recipe ingredients. | Vertical basalt replaces flowing lava. A still-source-only fixture is not equivalent. | Not run |
| R05 | Lava adjacent to magma **and** blue ice; omit the R06 bone-block base. | Blackstone replaces lava. | Not run |
| R06 | R05 ingredients, plus a bone block directly below with `axis=y`. | Calcite replaces lava and takes priority over R05. | Not run |
| R07 | **Still/source** lava adjacent to soul fire **and** water. | Crying obsidian replaces the source lava. Flowing-only lava is the wrong control. | Not run |
| R08 | Lava with horizontal water contact, adjacent blue ice, and soul soil directly below. | Smooth basalt replaces lava; compare the vanilla generator without the water-side condition. | Not run |
| R09 | Lava directly above a rising bubble column (`drag=false`). | Magma replaces lava. This is distinct from H06's random-tick spread. | Not run |
| R10 | Lava directly above a downward bubble column (`drag=true`). | Tuff replaces lava. Verify the bubble-column state, not just the base block. | Not run |
| R11 | Bring lava into contact with supported mossy masonry. | The `burn_moss` generator removes eligible moss; inspect the masonry target, not just the lava. | Not run |
| R12 | Bring lava into contact with ordinary clay. | The clay target becomes terracotta. This is not the special earthen-clay soil block. | Not run |
| R13 | Bring water into contact with `nulch_block[molten=true]`. | Nulch becomes `molten=false`; water is the trigger, nulch the target. | Not run |
| R14 | Bring lava into contact with sand/red sand in the sand tag. | The contacted sand target becomes vitrified sand, without needing lightning. | Not run |

Exact [fluid-generator JSON](../../../src/main/resources/data/immersive_weathering/fluid_generators)
and [adjacency interpretation](../../../src/main/java/com/ordana/immersive_weathering/data/fluid_generators/SelfFluidGenerator.java)
are the reference if a rig gives an unexpected result.

## Q: crafting, presentation, configuration, and regressions

| ID | Setup / trigger | Intended observation and control | Result |
| --- | --- | --- | --- |
| Q01 | Craft representative content through the recipe book/EMI: bark-related content, repair materials, supported masonry shapes, crowns, and cold items where recipes exist. Use crafting/stonecutting/smelting as shown. | Displayed recipes produce their specified output/count; no invented legacy recipe is required. Check recipe IDs against shipped JSON. Creative availability alone is not a crafting pass. | Not run |
| Q02 | Eat ordinary/golden/enchanted golden moss clumps and azalea flowers at rate 3. | Ordinary moss provides small food value; golden moss provides regeneration; enchanted golden moss provides absorption, regeneration, and water breathing. Azalea flowers have zero nutrition. Acquisition/recipe availability is a separate result. | Not run |
| Q03 | Explore genuinely new chunks in eligible biomes, or a new disposable world. Record locations and generated feature IDs. | Applicable soil patches, icy frost/icicle features, dry lakebeds, ivy, dune grass, and moss patches can generate. Existing chunks do not retroactively gain worldgen because random ticks were raised. Rare absence in a short search is Not observed. | Not run |
| Q04 | Inspect vanilla-tree and recognized modded-tree piles/bark, generated models, inventory icons, sounds, and particles; compare a default resource-pack run. | Supported generated content renders without missing textures or generation errors. Client particle/sound settings should affect presentation, not prove server-side growth stopped. Unusual modded models/packs need individual results. | Not run |
| Q05 | With EMI installed, inspect IW interactions and fluid recipes; perform `/reload` in the disposable world and inspect the log. | The IW plugin loads/reloads and displays its supported entries without exceptions. Do not infer every gameplay feature works from a successful plugin load. Without EMI, mark this integration Not applicable. | Not run |
| Q06 | In a separate config run, disable `general.block_growths`; then test a targeted growth blacklist such as the `tall_seagrass` path, following Configuration. | Registered growth dispatch and the selected rule should stop, respectively. This is not a promise that the global growth flag stops independent rust, moss/crack patch logic, physics, or item actions. Preserve exact effective settings and restart/reload steps. | Not run |
| Q07 | Apply common-config changes using the documented procedure; if a server is available, join with a second client and perform W03/W05/R13 while both observe. | Server-effective settings and block/item updates agree between clients, with no ghost blocks or duplicate consumption. A successful singleplayer test is not dedicated-server coverage; mark unavailable setups Not applicable. | Not run |
| Q08 | Test normal-rate sapling growth with and without nearby piles; falling piles over water; ivy after support removal/tree felling; affected walls with optional compatibility mods. | These directly exercise reports #2/#4/#5/#6/#8/#9 where their mods/layouts apply. Record vanilla/minimal and combined-mod results separately. Do not count an absent optional mod as a compatibility pass. | Not run |
| Q09 | Observe the same bounded small growth plots at rate 3 and 1000; save, quit, reopen, leave/re-enter chunks, and change dimension. Inspect logs each time. | No new exceptions, lost blocks, stale growth behavior, or persistent falling-entity buildup. Acceleration may cause lag; this is stability evidence, not a TPS/MSPT benchmark or proof that rates match an older release. | Not run |

Worldgen eligibility is registered in
[ModFeatures](../../../src/main/java/com/ordana/immersive_weathering/reg/ModFeatures.java);
[biome tags](../../../src/main/resources/data/immersive_weathering/tags/worldgen/biome)
and configured/placed features determine actual sites. Use the
[configuration guide](Configuration.md) and
[generated reference](../developers/Reference.md) for settings/defaults,
and [troubleshooting](Troubleshooting.md) for existing reports.

## Return results by test ID

Copy this record for each case or variant. Keep separate observations when you
change biome, config, mod set, or random-tick speed. Leave genuinely untested
cases unconfirmed; report a crash immediately rather than continuing a lagged run.

```text
Case / variant:
Verdict: Pass / Fail / Not observed / Not applicable
IW / Minecraft / Loader / Fabric API / Moonlight:
Singleplayer or dedicated server; other mods:
World seed / dimension / coordinates / biome:
Starting block IDs and states:
Config / datapack / resource-pack differences:
Weather / day-night / block light / simulation distance:
randomTickSpeed; gametime before -> after; real observation time:
Setup and exact action:
Eligible specimens -> converted specimens (growth cases):
Observed block states / drops / durability / effects:
Negative control and its result:
Screenshot / relevant sanitized log:
```

For stone-brick spread, attach SB subcase results to
[#11](https://github.com/Atlasroar/Immersive-Weathering/issues/11).
For other failures, check the existing report before filing a duplicate.
Do not change the installed beta or overwrite release artifacts to record tests:
this checklist is documentation, not a new jar/release.
