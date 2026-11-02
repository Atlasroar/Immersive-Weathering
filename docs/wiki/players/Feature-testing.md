# In-game feature test sheet

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

**Target:** Immersive Weathering **2.2.0**, Minecraft **1.20.1**, Fabric.
The latest recorded snow round below is **2.1.0-beta.2**, not a new stable-version pass.
This is a source-backed manual acceptance checklist with a dated user-reported
results ledger below. The detailed tables remain a **reusable blank checklist**:
their `Not run` cells are template defaults, not the current test-round verdicts.
Use the dated ledger for recorded outcomes.
Datapacks, configuration, other mods, and later releases can change expectations.

## Standalone fire

The tester reported on November 2, 2026 that the deployed 2.2.0 features
"work as expected". This is a **user-reported broad fire-feature pass**, not
an independently reproduced or itemized F01-F06 result. Exact variants, counts,
negative controls, configuration and multiplayer coverage were not specified.
The reusable F01-F06 rows below retain **Not run** template cells until an
itemized round is recorded; they do not negate that broad reported pass.

These are runnable acceptance instructions, not a claim that the immutable
2.1.0 contains these features or that stable versioning establishes gameplay
acceptance. Automated fixtures and hook checks
are described in the [fire harness](../developers/Development.md#fire-mechanics-harness).
Use a new disposable Creative flat world with cheats, the remapped 2.2.0 jar and
matching server/client dependencies. Back up the instance configuration first.
Record the release/source SHA, jar SHA-256, server config, gamerules, difficulty,
weather, biome, trial count and singleplayer/dedicated-client setup.
Probabilistic controls need counted trials, not a short wait or accelerated
`randomTickSpeed`; the entity interval uses ordinary entity ticks.

| Case | Setup and controls | Required result | Manual verdict |
| --- | --- | --- | --- |
| F01 | Sun-burning zombie/skeleton/phantom beside dry wood/grass; then independently ignite the same burn with fire, lava, lightning, player/mob Fire Aspect, burning arrow or small fireball | Sun-only never spreads; each supported later source can authorize; owner-attributed projectiles never consume held torches | Not run |
| F02 | Successful, blocked, failed and repeated direct torch/soul-torch melee; Survival/Creative, offhand, long existing burn, wet/dead/fire-immune/fire-resistant targets | Four seconds ordinary burning subject to vanilla protection, no shortening; exactly one Survival item only on supported successful ignition; no soul state/renderer | Not run |
| F03 | Same fixtures in temperate/hot biomes, zero/eight/excess heat cells; FIRE/SOUL_FIRE/MAGMA_BLOCK/LAVA versus torches/campfires; ordinary carrier versus projectile | Measured low per-interval chance, bounded heat/temperature and one-sixteenth projectile reduction; at most one cell per trial | Not run |
| F04 | Rain/exposed neighbors/roof, water/waterlogged/occupied cells, chunk edge, build limits, border, obsidian portal frame; change doFireTick/mobGriefing and all fire controls | Dry supported air and loaded neighbors only; no overwrite, chunk forcing or portal creation; documented gamerule/player exemptions and live settings | Not run |
| F05 | Burn exact grass versus dirt/podzol/mycelium; toggle grass/firetick; add external flammability; preserve adjacent snow-covered plants and native wood/soot behavior | Successful vanilla grass consumption leaves dirt at same position, no drops; unrelated soils/config/host flammability and charring/soot remain unchanged | Not run |
| F06 | Save/reopen while authorized or sun-only burning; extinguish by expiry/water/rain/powder snow then sun-ignite again; vanilla zombie melee from sun-only versus known fire | Provenance persists only for active known burns; extinguishing clears it; natural zombie melee cannot launder sunlight into spread permission; dedicated clients agree | Not run |

### Shared fixture and diagnostic settings

These commands **overwrite the small area around 0,64,0**. Run them only in the
disposable test world, not a build or existing production world. Keep other
flammable builds and burning entities away. Start without fuel; ignition sources
must be removed before adding fuel so vanilla source fire cannot fake a carrier
spread result.

```text
/gamemode creative
/difficulty normal
/gamerule doMobSpawning false
/gamerule doDaylightCycle false
/gamerule doWeatherCycle false
/gamerule doFireTick true
/gamerule mobGriefing true
/weather clear
/time set noon
/fill -4 63 -4 4 63 4 minecraft:stone
/fill -4 64 -4 4 67 4 minecraft:air
/tp @s 0.5 64 -3.5
/summon minecraft:pig 0.5 64 0.5 {Tags:["iw_fire"],NoAI:1b,PersistenceRequired:1b,Silent:1b,Attributes:[{Name:"minecraft:generic.max_health",Base:200.0d}],Health:200.0f}
```

In the **server/common `fire` section**, temporarily use `spread_interval=1`,
`spread_chance=1`, `spread_chance_cap=1`, `projectile_multiplier=1`,
`heat_bonus=0`, `temperature_bonus=0`, with the three feature toggles enabled and
`spread_radius=1`. Save/restart as required by the active config integration,
then verify the effective server values. These **non-default diagnostic settings**
make positive placement deterministic once an eligible carrier/cell exists.
Restore ordinary probabilities for F03; never report diagnostic rates as defaults.

For real fire-contact ignition, place `/setblock 0 64 0 minecraft:fire`, wait
two seconds, then `/setblock 0 64 0 minecraft:air`. Query the tagged carrier:

```text
/data get entity @e[tag=iw_fire,limit=1] Fire
/data get entity @e[tag=iw_fire,limit=1] "immersive_weathering:non_sun_fire"
```

Expect positive `Fire` and provenance `1b` after real contact. **Do not write the
provenance key yourself**; that would manufacture the condition under test.
Only now add `/setblock 2 64 0 minecraft:oak_planks`: the one eligible air cell
inside radius one is `1,64,0`. An authorized carrier should place fire there.
Remove that fire immediately before a repeat; never count later vanilla spreading.
For long fixtures, after real authorization use
`/data merge entity @e[tag=iw_fire,limit=1] {Fire:32000s}` to extend the existing
burn without inventing its origin. Living carriers can receive
`/effect give @e[tag=iw_fire,limit=1] minecraft:fire_resistance 1200 0 true`
**after** ignition to survive long counts; that effect does not extinguish them.
It intentionally makes them unsuitable for the positive torch-consumption case.

### F01: sunlight and real ignition sources

Remove the pig (`/kill @e[tag=iw_fire]`) and fuel. Summon a zombie using the same
NBT fixture above but entity ID `minecraft:zombie`; it must have no helmet and
remain exposed at noon. Wait until `Fire` is positive. Its provenance must be
`0b`, and diagnostic fuel must remain unlit. Repeat separately with
`minecraft:skeleton` and `minecraft:phantom` (use `NoGravity:1b` for the phantom).
Keep each run separate; clear fuel/fire and kill the previous tagged carrier.

For each real source below, use a fresh sun-burning zombie or fresh pig, inspect
provenance before/after, remove the source, then add diagnostic fuel:

- **Fire:** use the two-second contact sequence above, including during an
  already-active sunlight burn.
- **Lava:** replace the carrier's cell with `minecraft:lava` until burning, then
  remove the lava and any flowed lava before installing fuel.
- **Lightning:** `/summon minecraft:lightning_bolt 0.5 64 0.5`; remove all resulting
  fire before installing fuel.
- **Player Fire Aspect:** give
  `/give @s minecraft:wooden_sword{Enchantments:[{id:"minecraft:fire_aspect",lvl:1s}]} 1`
  and land one successful melee hit.
- **Mob Fire Aspect:** at midnight, summon an active zombie beside a NoAI tagged
  villager. Give the zombie a wooden sword in `HandItems[0]` with
  `tag:{Enchantments:[{id:"minecraft:fire_aspect",lvl:1s}]}`, and let it land a
  hit on the villager. Inspect the victim, not just the attacker.
- **Burning arrow:** give
  `/give @s minecraft:bow{Enchantments:[{id:"minecraft:flame",lvl:1s}]} 1` and
  `/give @s minecraft:arrow 64`, then shoot the fixture.
- **Small fireball:** with the pig at the shared coordinates, run
  `/summon minecraft:small_fireball -3 64.5 0.5 {power:[0.1d,0.0d,0.0d],Motion:[0.5d,0.0d,0.0d]}`.
- **Torch and Soul Torch:** use the successful-hit procedure in F02.

**Expected:** successful supported ignition authorizes the target (`1b`), even
without extending a longer existing burn. Repeat arrow/fireball shots against
an invulnerable target (`/data merge entity @e[tag=iw_fire,limit=1] {Invulnerable:1b}`)
that started with unknown/sun provenance: failed hits must not authorize it.
Holding torches while shooting must never consume the held torches.

### F02: torch consumption and ordinary burning

Use a fresh non-resistant pig, no source fire/fuel, Survival, and a main-hand
stack from `/give @s minecraft:torch 16`. Record count and target `Fire`, land
one hit, then recheck: one torch is used and the new burn is at most 80 ticks
(four seconds, minus elapsed ticks/vanilla fire protection). Repeat with
`minecraft:soul_torch`: ordinary fire and damage, no blue state/renderer.

Repeat with Creative (count unchanged), offhand torch/main-hand stick (no
ignition), an invulnerable target (failed hit, no consumption), and rapid hits
during vanilla hurt immunity (only actual successful hits consume). With a
second player, repeat against a raised shield from the front. Remove
invulnerability before positive controls. Repeat with water/rain/powder snow,
Fire Resistance, a NoAI blaze, and a dead/dying target: no torch consumption.
For the longer-burn control, give the pig `Fire:400s` before the hit; torch use
must not reduce the remaining duration to 80. Toggle `torch_attacks=false`,
save/restart, and repeat: no IW ignition or consumption.

### F03: counted probabilities, heat and biome temperature

Use `spread_interval=1` for faster counted trials but restore **all other fire
probability/heat settings to their defaults**. Record this interval deviation.
Use only stone flooring, fuel at `2,64,0`, and one authorized stationary carrier
at the shared origin. Plains base temperature is 0.8; set the fixture biome with
`/fillbiome -4 63 -4 4 67 4 minecraft:plains`.

Create objective `/scoreboard objectives add iw_fire_counts dummy`, then set
`ticks` and `fires` to zero in that objective. Outside the fixture, place one
always-active repeating command block and two always-active **unconditional**
chain blocks in order, containing:

```text
scoreboard players add ticks iw_fire_counts 1
execute if score ticks iw_fire_counts matches ..10000 if block 1 64 0 minecraft:fire run scoreboard players add fires iw_fire_counts 1
setblock 1 64 0 minecraft:air
```

The count freezes after 10,000 game ticks (about 8 minutes 20 seconds at 20 TPS);
read `/scoreboard players get fires iw_fire_counts`, disable the controller,
and record ticks/count/config. Clearing every tick prevents vanilla fire
propagation or a previous placed fire adding heat. At interval one this is
approximately 10,000 trials (at most one boundary-tick discrepancy).
Default no-heat plains expectations are about **200 normal** versus **12.5
projectile** ignitions, not exact quotas or a guaranteed short-run 16:1 ratio.
Repeat noisy results; automated fixtures establish the exact probability math.

For a stationary projectile, remove the pig and summon
`/summon minecraft:arrow 0.5 64.4 0.5 {Tags:["iw_fire"],NoGravity:1b,pickup:0b,Motion:[0.0d,0.0d,0.0d]}`.
Use **real fire contact** and verify authorization before extending `Fire`;
setting a raw NBT burn alone is not valid provenance. Keep the tester clear of
its collision box and verify it remains stationary throughout the run.

Use eight magma floor cells for uncontaminated heat-rate measurements:
`/fill -2 63 -2 -2 63 2 minecraft:magma_block`, plus magma at
`-1,63,-2`, `-1,63,2` and `0,63,-2`. They are inside heat radius two and do not
replace the carrier's support or fuel. Add a ninth at `0,63,2`: the capped result
must not increase. Repeat in `minecraft:desert` using the same `/fillbiome`
box: eight heat cells and temperature 2 give about **640 normal** versus
**40 projectile** successes per 10,000 default-probability trials.
Test FIRE/SOUL_FIRE/LAVA inventory separately in contained fixtures; direct
vanilla fire/lava spread must not be counted as carrier events. Torches and
campfires add no heat. Zero base chance, zero cap, or zero projectile multiplier
must give zero events in their respective control.

### F04: gates, placement and configuration

Restore deterministic diagnostic settings and a fresh known carrier. With
`/gamerule doFireTick false`, the eligible cell stays empty; successful direct
torch combat still works. Restore true, then set `mobGriefing=false`: pig,
ownerless arrow and mob-owned projectile spreading stops. A burning Survival
player and a Flame arrow actually shot by a player remain exempt. Do not assign
an NBT owner to an ownerless arrow and call it a player-shot reproduction.

Restore `mobGriefing=true` and repeat with a torch, water, waterlogged block
or other occupied block at `1,64,0`: no replacement. Set plains biome,
`/weather rain`, and expose the carrier/cell or a horizontal cell neighbor;
compare a fully roofed, dry control. Also test powder-snow carriers and a cell
with no fuel/support. Use fresh fixtures at build-height/world-border edges and
at the simulation-distance boundary; inspect server chunk/profiler evidence
for loading rather than assuming a client render boundary means unloaded.
The automated fixture separately checks the exact unloaded-neighbor rejection.

Put the carrier and nearby flammable support inside an empty obsidian frame:
entity placement may make supported fire, never portal blocks. Ordinary flint
and steel must still form a valid portal as a separate positive control.
Open Common -> Standalone fire, inspect every name/tooltip, change/save/reopen
and restart, then compare effective server behavior with `entity_spread=false`,
other toggles, interval/radii/cap changes and a connected client. Restore defaults.

### F05: grass ground and existing systems

Disable `entity_spread` to isolate vanilla fire trials. In separate stone-edged
samples place grass, dirt, podzol and mycelium, then actual fire beside each.
With `doFireTick=true` and `flammable_grass=true`, wait for a successful grass
burn: record coordinates/state before/after, verify **DIRT at the same position**,
no air/fire hole and no grass-consumption drops. Other soils must not convert.
Repeat `flammable_grass=false` and `doFireTick=false`; IW must not consume grass.
External mods may retain their own flammability when IW's addition is disabled.

Compare supported wood charring, soot formation/config, and fire beside
snow-covered plants against 2.1.0 in a separate disposable baseline instance.
Use matched settings/dependencies and fresh fixtures; do not overwrite immutable
release files or infer that the open soot-toggle report is fixed. Repeat the
existing snow N10/N11 and growth comparison controls; record any integration
mod's ID/version and flammability override.

### F06: persistence, reset and zombie melee

Save/quit/reopen while one fixture has real authorized fire and another has only
sunlight fire, using different tags. Query both `Fire` and provenance after
reload: only the still-active known burn retains `1b`. Extinguish the known
zombie separately by water, rain, powder snow, and natural expiry. For expiry,
set midnight first so sunlight cannot continually refresh it; a short positive
`Fire` duration may shorten an already-known burn for this test.
After each extinction verify nonpositive `Fire`/false provenance, then clear
weather/set noon: a new sun-only burn must remain false until real ignition.

At night, let an **unarmed active zombie** with a recorded sun-only burn hit a
NoAI villager at close range. Repeat until vanilla's random melee ignition
actually occurs; not every successful attack ignites. Victim fire from that
source remains unauthorized. Repeat with the attacker independently ignited by
real fire before attacking: an actual vanilla melee ignition may propagate
authorization. Inspect source and victim separately and repeat save/reload with
a dedicated client connected. Damage/fire rendering remain vanilla.

### Cleanup and result recording

Disable/remove the three controller blocks, `/kill @e[tag=iw_fire]`, remove
fixture fire/lava/temporary mobs, and restore the backed-up common config and
recorded gamerules. Remove the test scoreboard only if this run created it.
Do not delete or alter another world's data. Record F01-F06 individually with
evidence and subcase limits; this document starts all six as **Not run**.

## Standalone snow candidate retest

Use a new backed-up/disposable test area. Automated production-code fixtures and client/server launch evidence
are described in [development validation](../developers/Development.md#snow-mechanics-harness).
Keep the historical 2.0.6-beta.2 ledger below unchanged.

**Reported snow results: November 1, 2026, following the 2.1.0-beta.2 test-profile deployment**

| Cases | Reported manual verdict | Coverage |
| --- | --- | --- |
| N01-N11 | **Pass** | The tester explicitly reported all eleven cases passing. |
| N12-N14 | **Not reported** | The expanded dry-plant, native-growth and common-config cases remain unreported, not failed. |

These are user-reported in-game results, not an independent agent reproduction.
No new per-control inventory, exact settings, logs, screenshots or complete
mod/resource-pack versions were supplied. Preserve the reported passes without
assuming every variant within a combined row was individually exercised.
N11 includes the paired-growth regression controls, but the report does not
itemize separate grass/fern/seagrass transitions or replace the historical G07
failure with a separate G07 pass.
Stable 2.1.0 additionally makes covered short ferns look frosty; retest that appearance
and normal-fern restoration in N02/N12. The beta.2 passes do not certify it.

**November 1 follow-up on 2.1.0-beta.1:** the tester reported that the jar seems
to work overall, then reported flowers replacing snow and requested shared weeds,
more dry plants, common-config controls and frosty-looking covered grass.
That earlier report was not a per-case N01-N11 pass; the later beta.2 report
above explicitly supplies those passes. N12-N14 still need individual results.

**Reusable snow checklist:** the `Not run` cells below are blank template
defaults; the dated results above are the current recorded verdicts.

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
| N12 | Each small flower, pink petals, sapling/dry propagule, mushroom, dead bush, IW weed/dune grass/frosty plant; snow-first and plant-first; layers 1-8; shovel/melt and both config toggles | Supported dry plants retain layer counts and native state; normal short grass and (2.1.0) short ferns use matching frosty appearances only while covered, then restore their original normal plants; wet hosts and full-layer replacement stay excluded | Not run |
| N13 | Grow/bonemeal covered weeds and saplings; repeat failed/successful 2x2 tree growth with four different snow counts; mushroom density; stack petals and snow; plant contact effects | Ages, stages, petal amounts/direction and each layer count survive; failed trees restore all covers, successful trees remain trees; snow-item placement actually increments the cover | Not run |
| N14 | Open Mod Menu -> IW Configs -> Common -> Snow; hover all six controls; change/save/reopen, restart and compare effective server config | Every snow toggle and the 1-8 cap is named/described and persisted in common settings, not client settings; previous values/defaults remain intact | Not run |

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
now retain both halves. That implementation evidence alone was **not** a new
in-game pass for G07 and did not change its recorded beta.2 failure.
The later 2.1.0-beta.2 snow round reports **N11 Pass**, including this broad
regression row, without itemizing separate grass/fern/seagrass outcomes.
Keep that follow-up distinct from the original failure and from an independent
agent reproduction.

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
