# In-game feature test sheet

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

Reusable replication instructions for the Fabric maintenance target. Choose
the feature below, prepare its fixtures, perform the action and compare the
expected behavior with its negative controls. These instructions are not tied
to a mod release. Record the actual artifact, dependencies and settings for
each round using the [result record](#return-results-by-test-id).

Recorded outcomes and tested versions belong only in the
[test-results tables](Test-results.md), not in this sheet. Keep each test ID
when reporting a variant so later rounds can be compared without changing
the instructions.

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

## Freezing wind and thunder snowstorms

Use an intentionally disposable/backed-up cheats-enabled world. Record and
restore weather/daylight/mobGriefing/snowAccumulationHeight/randomTickSpeed and
configs; only alter a named small test area. To isolate block/contact behavior from probabilistic
spawning, `/summon immersive_weathering:freezing_wind ~3 ~ ~` creates a short-lived
gust east of the tester. Clear obstacles for its 2x2 volume and westward path;
use standing and wall fixtures on separate lanes. Natural spawning still needs
its own measured tests, not only `/summon`.
When isolating sand/thunder or fire scenarios, temporarily
disable `freezing_wind.enabled` to isolate the old thunder-only fire rule from
the new independent gust extinguishing; record and restore that control.

### FW01

**Replicate:** Compare exposed low hot/plains/cold biomes at midnight, and the same base-temperature biome below/at Y100. Record many per-chunk attempts and effective config; use base/bonuses=0 and then a deterministic 1 for isolated negative/positive controls, restoring defaults afterward. Compare clear noon/night and non-natural dimensions.

**Expected and controls:** Ambient gusts only at night, all biomes eligible, colder/altitude independently increase measured probability; no spawn through roofs/outside build limits or forced chunks. Two successes/dimension/20 ticks and 32-live cap are maxima, not guaranteed rates.

### FW02

**Replicate:** Summon into open lane, then behind a full wall and beside a step/eave. Observe five-second-scale lifetime and movement with block coordinates; test pig/player, spectator, iron golem, ravager, warden and elytra flight.

**Expected and controls:** 2x2 gust, west one block per ten ticks (not per tick), 80-140 tick lifespan, collision climbing; push plus 60-tick Slowness I every five ticks, no through-wall hit. Heavy tag/spectators excluded; stronger elytra push. No new temperature/freeze ticks/damage/hearts.

### FW03

**Replicate:** Ordinary/soul standing torches and each wall facing; fire/soul fire, every candle count/candle cake, lit ordinary/soul campfire including waterlogged native states. Include lit furnace, lantern, redstone torch/lamp controls. Relight with flint/charge/held torch in Survival and Creative, then break/pick unlit variants.

**Expected and controls:** Applicable flames go out; torches remain unlit/no light/no flame animation with exact soul/normal identity and wall facing, native survival/support updates. Relighting restores matching vanilla state; Survival one durability/charge, no held-torch consumption; correct vanilla torch loot/pick. Machines/lanterns/redstone unchanged.

### FW04

**Replicate:** Source pool, flowing water, waterlogged fence/slab, stacked water and nearby bright light; compare existing thin-ice cracking/melting. Put empty underside cells below stone, wood, glass, leaves, slab and invalid supports, with a chest/flower occupying other cells. Temporarily use icicle chance=1; restore.

**Expected and controls:** Only native-surviving surface sources freeze to existing thin ice. No flow/waterlogged replacement. Icicles form on any geometrically valid support; not occupied/unsupported cells or through walls. Existing ice/icicle physics remain.

### FW05

**Replicate:** Independently toggle wind enabled, extinguish, freeze_water, icicle chance0, disable_icicles, slow_ticks0 and mobGriefing=false. Set spawn_budget/live_cap0, low caps, then restore. Unload/reload a gust chunk, change dimension, save/reopen, restart server.

**Expected and controls:** Wind disable removes live gusts, zero caps stop natural spawns, zero effect controls respected. mobGriefing stops all gust block changes but not contact push/Slowness. No orphaned saved gusts/stale budget leak or loss of existing unlit/ice/icicle blocks.

### FW06

**Replicate:** Cold native snow-precipitation biome during daytime thunder; compare rain-only, clear, hot thunder, roof, night and biome/height boundaries. Compare bonus0/1/3, snow accumulation off, cap1/4/8 and snowAccumulationHeight0/1/3; supported dry hosts and wet/occupied cells.

**Expected and controls:** Storm only cold thunder; daytime gusts and additive bonus. Snow uses one existing sampled deposition authority, obeys light/support/host/cap/gamerule and never trims piles or adds a competing whole-chunk loop. Ordinary first snowfall/thunder behavior remains with storm disabled/bonus0.

### FW07

**Replicate:** Independently toggle snowstorm particles/fog/clouds/sounds, All/Decreased/Minimal particles, range0/default/max and rarity, Weather volume0/subtitles, underwater/lava/powder-snow, blindness/darkness/thick fog, short render distance, indoor shelter and biome edge. Disconnect, change worlds/dimensions, clear/restart storm and F3+T.

**Expected and controls:** Original six swirl frames/shared upstream white grains render without missing resources; guarded blended fog never extends stronger native fog/view distance. Cloud control independent; bounded loaded scan; native particle setting honored; intermittent existing IW audio, not new recordings/stale timers.

### FW08

**Replicate:** Matching dedicated server and two clients, save/reopen unlit wall torches and storm deposits, datapack wind_heavy reload, optional Immersive Storms coexistence in a separate disposable setup. Retest SP/ST and snow/fire controls without overwriting their historical verdicts.

**Expected and controls:** Server authority/client agreement; no missing entity factory/renderer/resources on dedicated server. Immersive Storms owns snowstorm presentation without disabling IW gameplay or duplicate IW storm fog/audio. Optional setup absent is N/A, not a compatibility pass.


Probabilistic observations need counted trials, not a promise of gusts in a
short wait. Restore all changed settings and delete only fixtures created for
this round. Record each variant using the [result record](#return-results-by-test-id).

## Sand and thunder

Use a disposable/backed-up cheats-enabled world and restore all settings
afterward. The commands below intentionally overwrite only this small test
area; they are manual instructions, not commands run by the build.

```text
/gamemode creative
/gamerule doWeatherCycle false
/gamerule randomTickSpeed 0
/weather clear
/fill -4 64 -4 4 70 4 minecraft:air
/fill -4 63 -4 4 63 4 minecraft:sand
/tp @s 0 64 0
/give @s immersive_weathering:sand_layer_block 64
/give @s immersive_weathering:red_sand_layer_block 64
```

Record the original gamerules, `sand`/`sandstorms`/`fire` configs and weather
first. Initial clear weather/randomTickSpeed zero isolates manual placement.
Use F3's targeted block ID/properties to distinguish native hosts from cover
states; graphics alone are insufficient.

### SP01

**Replicate:** For regular and red piles, place each of the 12 vanilla fences and a dead bush before sand and then into sand. Test depths 1, 4 and 8. Connect/disconnect neighboring fences and gates; attach a lead; rotate a saved structure. Compare waterlogged fence, cactus, wall and a modded plant.

**Expected and controls:** Both orders retain the native dry host, color and exact layer count, including eight. Connections/collision/leads remain native. Unsupported/wet hosts do not become covers. Regular/red layers do not merge. No datapack-only modded-host claim.

### SP02

**Replicate:** At depths 1-8 replace bare regular/red piles with stone, wood, glass, chest and torch. Place fence/dead-bush items instead. Try replacing covered fence and covered dead bush with stone. Repeat with host_mixing and replace_piles separately disabled after restart.

**Expected and controls:** Every bare depth, including eight, is replaceable by ordinary blocks. Supported hosts retain sand when mixing is on. Covered fences cannot be erased by placement; covered bushes retain native replacement rules. Matching sand stacks only to eight; toggles preserve existing saves/cover IDs.

### SP03

**Replicate:** Shovel-right-click regular/red covers in Survival and Creative; break whole covers with appropriate tools/shears at depths 1, 4 and 8. Remove support below a covered fence and dead bush; drop matching sand into raw/covered hosts and near full piles; repeat over water and at a blocked landing cell. Save/reopen while sand is falling.

**Expected and controls:** Shovel strips only sand; Survival durability/drop behavior matches existing pile economy. Whole-cover loot includes native host and sand (eight-layer loot remains one full sand/red-sand block). Only sand falls; no host movement, mixed-color merge, duplicated/lost remainder, automatic overflow column or frozen/replaced water. Falling state survives reload.

### SP04

**Replicate:** Set layer_cap=1, storm in desert/badlands at randomTickSpeed=0 over bare ground, dry fences and dead bushes. Manually stack to eight. Repeat cap=8; lower cap below a prepared six-layer cover. Compare original saved config=1 with fresh-config default=8, and disable mixing/deposition independently.

**Expected and controls:** Weather caps at the configured value around eligible hosts/bare ground; manual/falling sand still reaches eight. No trimming, color conversion, occupied-block overwrite or new storm pile above full piles. Saved values do not silently change.

### SP05

**Replicate:** In untagged plains, use `/weather rain`, restore positive randomTickSpeed and observe bare/covered regular/red piles. For a faster isolated trial, explicitly set erosion chance=1 and temporarily increase randomTickSpeed, recording/restoring both. Compare clear, roofed, snowfall and each storm-tag biome. Tag a rainy biome, `/reload`, then remove tag and reload; compare a hot but untagged rainy biome.

**Expected and controls:** Each successful eligible random tick removes exactly one layer, no items; last covered layer restores the host. Roof/clear/snow preserve piles; membership in either storm tag exempts, including rainy tagged biomes. Temperature alone does not. randomTickSpeed=0 and erosion=false/chance=0 stop erosion, not independent storms.

### SP06

**Replicate:** In a dry desert plot, light multiple separated netherrack fires with adjacent fuel. Compare clear/rain/thunder, exposed versus a broad solid roof covering source and neighbors. Record lightning separately. With extinguish chance=0 compare sustained spread/burn attempts; then restore 0.25.

**Expected and controls:** Thunder reduces exposed source attempts, including in dry biomes, and may extinguish ordinary eternal-support fires on scheduled ticks. Sheltered sources retain native behavior. Do not demand exactly 90%/25% in a small random sample; fire aging/scheduling and non-thunder rain behavior remain.

### SP07

**Replicate:** Repeat SP06 with doFireTick=false, thunder_dampening=false, spread multiplier=0/1 and extinguish chance=0/1. Compare ordinary fire, soul fire, torch and campfire. Separately compare authorized burning-entity environmental trials exposed/roofed in a dry biome.

**Expected and controls:** Gamerule/toggle disable new weather effects. Zero multiplier prevents attempts; one preserves attempts. Extinguish zero/one provides deterministic controls for eligible ordinary fire only. Entities are not extinguished by this rule; their environmental probability is reduced without removing projectile reduction. Soul fire/torches/campfires remain unchanged.

### SP08

**Replicate:** F3+T reload, save/reopen covers/falling sand, change dimensions, and repeat with a second client on a matching dedicated server. Retest snow covers/placement and existing fire/Scorchful coexistence controls.

**Expected and controls:** No missing models/textures, lost native properties, stale covers, duplicated items or client/server disagreement. Storm ownership still yields to actually installed Scorchful; native IW sand sharing/rain erosion are independent pile behavior. Earlier snow/fire ledger stays historical.


Restore the recorded configs, weather, biome changes and gamerules, and remove
only fixtures created for this round. Record each variant separately, using
the [result record](#return-results-by-test-id), rather than inferring a gameplay
pass from native fixtures, compilation or a title-screen initialization.

## Standalone sandstorms

Record source/jar SHA, dependencies, effective config, packs, biome, weather,
simulation distance, real time/game ticks and client/server setup. Use the
installable remapped jar; keep the tested artifact and original settings intact.
Automated [native fixtures](Development.md#sandstorm-mechanics-harness) do not
replace visual, audio, multiplayer or gameplay checks.

### Runnable storm fixture

Use an existing **disposable/backed-up** cheats-enabled world for manual testing.
The following commands overwrite the named small area. They are instructions
for an intentional test round, **not commands run by the build**. No EULA/world
setup is performed automatically. Keep other builds away and record/restore
original gamerules/weather/config after the round.

```text
/gamemode creative
/gamerule doMobSpawning false
/gamerule doWeatherCycle false
/gamerule doDaylightCycle false
/gamerule randomTickSpeed 0
/time set noon
/weather clear
/fill -16 64 -16 31 74 31 minecraft:air
/fill -16 63 -16 31 63 31 minecraft:stone
/fillbiome 0 48 0 31 79 31 minecraft:desert
/tp @s 8 64 8
/attribute @s minecraft:generic.movement_speed get
/weather rain 600
```

Current source default common `sandstorms`: deposition=true, layer_cap=8, slowing=true,
slow_amount=0.3. Default client: particles/fog/hide_clouds/sounds=true,
particle_range=20, particle_rarity=60, fog_start=16, fog_end=64.
Use vanilla All particles, nonzero Weather volume and subtitles, without a
shader/resource pack first. Allow the rain gradient to rise and remain near
the fixture for at least 60 seconds; absence in a short sample is **Not observed**,
not proof that deposition/sound is broken. `randomTickSpeed=0` intentionally
separates deposition from older sandy-block growth. Check block IDs/layers
with F3 targeted-block readout; screenshots of dust alone prove no server rule.

### ST01

**Replicate:** Run the fixture with ordinary rain (no thunder); `/weather clear`, then rain again. Use `/fillbiome 0 48 0 31 79 31 minecraft:badlands`, then warm_ocean and plains; return to desert. With weather cycling disabled, wait and explicitly change weather; repeat at night.

**Expected and controls:** Dry desert deposits regular IW layers, dry badlands red; warm ocean/plains have no new storm effects/deposition. Ordinary rain/night suffice; clear stops gameplay, gradients fade visuals. No independent storm timer. Warm-ocean legacy sandy-block growth is a separate control at a restored positive randomTickSpeed.

### ST02

**Replicate:** During rain compare `/attribute @s minecraft:generic.movement_speed get` with the clear value without sprint/effects. `/fill 4 67 4 12 67 12 minecraft:stone`, stand below it, then remove roof. Summon a pig and camel on the platform and inspect their movement-speed attributes too.

**Expected and controls:** Exposed eligible entities have a temporary 30% reduction; roof removes it on the next server tick, camel remains immune. No damage, armor/thirst/temperature state or wind force. Biome fog/cloud effects may remain indoors.

### ST03

**Replicate:** Clear the floor's air cells, leave randomTickSpeed=0 and explicitly set cap=1. Place `/setblock 8 64 8 immersive_weathering:sand_layer_block[layers=6]`, move aside and inspect it before/after. Restart with layer_cap=8, fresh piles; fill a pile to eight, inspect the cell above. Restore the original cap.

**Expected and controls:** Sampled supported cells accumulate to configured cap without resetting/reducing six/eight-layer piles or stacking a ninth layer in another cell. Each chunk performs one sampled 1/16 trial, not a randomTickSpeed multiplier. No source sand disappears; no claim of an exact short-run rate.

### ST04

**Replicate:** On separate recorded columns use water/lava, grass, a chest, bottom slab, leaves, empty/water/powder-snow cauldrons, red pile in desert and regular pile in badlands. Test pile support removal and a normal manual eight-layer pile/loot/recipe control; add SP01-SP04 host controls.

**Expected and controls:** No fluid/unsupported-host/different-pile replacement, unsupported deposition or cauldron conversion; sturdy dry free surfaces still work. Existing IW loot/recipes remain, not Scorchful's economy. Supported hosts gain sand without disappearing. Falling piles moving after support removal are not a deposition-cap violation.

### ST05

**Replicate:** Independently disable deposition and slowing, restart and verify server-effective values; set slow_amount=0 then 0.5. Add/remove pig immunity via a datapack's sandstorm_slow_immune tag and `/reload`; remove/re-add dry-biome subtype tags, including overlap and a wet biome.

**Expected and controls:** Existing modifier cleans up after disable/immunity/tag changes; strength changes without stacking. Regular wins overlap; wet biomes stay excluded. Old deposits remain. Disabling general.block_growths stops old growth, not new storm hooks. Config files require restart; `/reload` alone is for tags.

### ST06

**Replicate:** Watch dawn/noon/night and a desert/plains boundary. Change fog_start/end, deliberately invert them, lower render distance; compare fog off/cloud hiding on and vice versa. Submerge camera in water/lava/powder snow and apply blindness/darkness separately, then clear effects.

**Expected and controls:** Smooth weather/biome color/distance, sand tint lit by dimension/day; no extending stronger vanilla fog/view distance, no fluid/effect/sky-fog override. Clouds hide only above 0.75 gradient with own toggle. No missing sprites; dust goes west with gravity, both subtypes share color.

### ST07

**Replicate:** Compare All/Decreased/Minimal particles, range zero/default/32 and rarity 1/60. Toggle wind separately, set Weather slider to zero, enable subtitles, move >10 blocks vertically from surface, clear/restart weather, disconnect/change worlds/dimensions, and use F3+T resource reload.

**Expected and controls:** Bounded local dust (Minimal/range0 off), ordinary resource reload without missing atlas/sounds; nine event variants can load, wind intermittent at Weather 0.1/pitch0.5 near surfaces, no stale scheduling across world changes. No continuous loop or promise all random variants are heard in a short sample.

### ST08

**Replicate:** Save/reopen, change dimension/biome, join a dedicated server with a second client and matching dependencies. Repeat with actual Scorchful installed in a separate disposable setup; separately retest existing snow, fire and legacy sandy-stone growth fixtures.

**Expected and controls:** Server attributes/blocks agree; no stranded IW UUID. Installed Scorchful owns new storms, without duplicate IW effects/modifiers/piles; legacy growth remains. Coupled snow/fire/growth behavior and prior ledger stay unchanged. Without this optional setup record Not applicable, not a compatibility pass.


For ST06 effects, use `/effect give @s minecraft:blindness 30 0 true`,
then `/effect clear @s minecraft:blindness`, and the corresponding `darkness`
commands. Lava can damage Survival players; use Creative and an enclosed
disposable fixture. Clean up the exact fixture region/pigs/camels only if they
were created for this round; do not use global kill/clear commands.
Restore all changed configs/gamerules and remove temporary datapacks.
Report each variant using the [result record](#return-results-by-test-id);
include logs for resource reload and both clients where available.

## Standalone fire

Use a disposable Creative flat world with cheats, the installable remapped jar
and matching server/client dependencies. Back up the instance configuration.
Record artifact/source SHA, jar SHA-256, server config, gamerules, difficulty,
weather, biome, trial count and singleplayer/dedicated-client setup.
See the [fire harness](Development.md#fire-mechanics-harness) for automated checks.

Probabilistic controls need counted trials, not a short wait or accelerated
`randomTickSpeed`; the entity interval uses ordinary entity ticks.

### F01

**Replicate:** Sun-burning zombie/skeleton/phantom beside dry wood/grass; then independently ignite the same burn with fire, lava, lightning, player/mob Fire Aspect, burning arrow or small fireball

**Expected and controls:** Sun-only never spreads; each supported later source can authorize; owner-attributed projectiles never consume held torches

### F02

**Replicate:** Successful, blocked, failed and repeated direct torch/soul-torch melee; Survival/Creative, offhand, long existing burn, wet/dead/fire-immune/fire-resistant targets

**Expected and controls:** Four seconds ordinary burning subject to vanilla protection, no shortening; exactly one Survival item only on supported successful ignition; no soul state/renderer

### F03

**Replicate:** Same fixtures in temperate/hot biomes, zero/eight/excess heat cells; FIRE/SOUL_FIRE/MAGMA_BLOCK/LAVA versus torches/campfires; ordinary carrier versus projectile

**Expected and controls:** Measured low per-interval chance, bounded heat/temperature and one-sixteenth projectile reduction; at most one cell per trial

### F04

**Replicate:** Rain/exposed neighbors/roof, water/waterlogged/occupied cells, chunk edge, build limits, border, obsidian portal frame; change doFireTick/mobGriefing and all fire controls

**Expected and controls:** Dry supported air and loaded neighbors only; no overwrite, chunk forcing or portal creation; documented gamerule/player exemptions and live settings

### F05

**Replicate:** Burn exact grass versus dirt/podzol/mycelium; toggle grass/firetick; add external flammability; preserve adjacent snow-covered plants and native wood/soot behavior

**Expected and controls:** Successful vanilla grass consumption leaves dirt at same position, no drops; unrelated soils/config/host flammability and charring/soot remain unchanged

### F06

**Replicate:** Save/reopen while authorized or sun-only burning; extinguish by expiry/water/rain/powder snow then sun-ignite again; vanilla zombie melee from sun-only versus known fire

**Expected and controls:** Provenance persists only for active known burns; extinguishing clears it; natural zombie melee cannot launder sunlight into spread permission; dedicated clients agree


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
snow-covered plants against a recorded baseline in a separate disposable instance.
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
evidence and subcase limits in the results table.

## Standalone snow

Use a backed-up/disposable test area and repeat the controls for each supported
host, tool and layer count. Compare the
[snow mechanics harness](Development.md#snow-mechanics-harness) separately
from the in-game observations.

### N01

**Replicate:** Each of 12 vanilla fences and 22 walls; isolated, raw/wrapped neighbors, four rotations, all snow layers 1-8

**Expected and controls:** Snow shares the cell; raw connectivity, host textures, collision, pathfinding and pick-block remain correct

### N02

**Replicate:** Grass, fern, tall grass, large fern, sunflower, lilac, rose bush, peony; snow-first and host-first, both halves

**Expected and controls:** Correct host/state after placing, shoveling and melting; no transient paired disappearance; bonemeal preserves snow

### N03

**Replicate:** Shovel use, ordinary break, correct/incorrect host tool, shears, Silk Touch, Creative; each half of a mixed pair

**Expected and controls:** Shovel use removes only cover; breaking uses host mining/drop rules; no doubled host or snow drops; Creative no cover drops

### N04

**Replicate:** Remove support under plain/covered snow at layers 1-8; remove plant soil/partner; unload/reload and save/reopen while falling

**Expected and controls:** Only snow falls; valid hosts stay; invalid plants die; counts/state persist; no ghost entity/client disagreement

### N05

**Replicate:** Land every incoming count 1-8 on partial piles, full piles, dry hosts, blocked/wet cells, moving pistons and build bounds

**Expected and controls:** At most eight in target cell, exact remainder as items, no overwrites/spill/dupes; gamerule-disabled drops are intentional

### N06

**Replicate:** Fall through source water, flowing levels, waterlogged fence/wall, and a high-speed source crossing; warm/cold controls

**Expected and controls:** Only an actual source WATER block becomes ice; flowing/waterlogged solids stay intact; source consumes incoming snow

### N07

**Replicate:** Cold exposed columns: clear, ordinary snowfall, thunder; warm rain, roof, block light 12, outside simulation distance

**Expected and controls:** Default extra layers only in eligible thunder; vanilla first snow in ordinary snowfall; no roof/light/warm/unticked accumulation

### N08

**Replicate:** Gamerule 0,1,2,8,24; IW cap 1,3,8; accumulation off; ordinary on/off; mixing/falling/replacement off; block-growth off

**Expected and controls:** Sentinel 1 uses IW cap, 0 stops weather snow, custom positive limits intersect cap; independent toggles and single-column bound hold

### N09

**Replicate:** Replace plain/plant-covered snow 1-8 with stone, chest, oriented fence, flowers, same-plant items and unsupported hosts; mixing/replacement toggles; permission/adventure/obstruction controls

**Expected and controls:** Normal item placement/callbacks/consumption survive; covered plants obey raw replacement rules; supported replacements retain cover only with mixing; solids replace cover; layer 8/fences/walls remain protected

### N10

**Replicate:** Piston push/destroy, explosion, fire, shearing; dedicated client sync; save/reload; vanilla renderer and existing Sodium setup; resource-pack reload

**Expected and controls:** Original host/drop/state semantics; no missing models, plant-tint/snow-tint errors, flickering or rendering-library requirement

### N11

**Replicate:** Existing IW snowy masonry IDs/config/data, old worlds, leaf/sand falling layers and the paired-plant G07 controls

**Expected and controls:** No destructive migration or changed independent mechanisms

### N12

**Replicate:** Each small flower, pink petals, sapling/dry propagule, mushroom, dead bush, IW weed/dune grass/frosty plant; snow-first and plant-first; layers 1-8; shovel/melt and both config toggles

**Expected and controls:** Supported dry plants retain layer counts and native state; normal short grass and short ferns use matching frosty appearances only while covered, then restore their original normal plants; wet hosts and full-layer replacement stay excluded

### N13

**Replicate:** Grow/bonemeal covered weeds and saplings; repeat failed/successful 2x2 tree growth with four different snow counts; mushroom density; stack petals and snow; plant contact effects

**Expected and controls:** Ages, stages, petal amounts/direction and each layer count survive; failed trees restore all covers, successful trees remain trees; snow-item placement actually increments the cover

### N14

**Replicate:** Open Mod Menu -> IW Configs -> Common -> Snow; hover all six controls; change/save/reopen, restart and compare effective server config

**Expected and controls:** Every snow toggle and the 1-8 cap is named/described and persisted in common settings, not client settings; previous values/defaults remain intact


## Paired-plant growth

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

## W: masonry, moss, cracks, iron, and wax

### W01

**Replicate:** Put fresh stone, cobblestone, stone bricks, and bricks beside water and supported mossy neighbors, with exposed air. Use several positions.

**Expected and controls:** Eligible blocks gain their mapped mossy variants in patches, not uniformly. Compare fresh specimens without strong sources, and a separate lava-adjacent plot; lava inhibits moss. Default `needs_air=true` matters.

### W02

**Replicate:** Put fresh stone bricks and bricks beside cracked counterparts and a controlled fire/lava source. Keep this separate from W01.

**Expected and controls:** Eligible masonry can crack. Compare source-free fixtures. Do not expect plain stone or cobblestone to gain nonexistent cracked variants. Fixed immunity means not every specimen converts.

### W03

**Replicate:** In Survival, shear mossy blocks; then apply the recovered moss clump to their clean counterparts.

**Expected and controls:** Shears remove moss, yield a clump, and use durability; a clump reapplies moss and is consumed. Repeat across the stone-brick matrix below. An unsupported block should not acquire an invented variant.

### W04

**Replicate:** Use flint and steel on mossy masonry, then expose the cleaned result to W01 conditions.

**Expected and controls:** Moss is removed and the resulting weatherable block is marked stable against ordinary re-weathering. Inspect the state. Compare a freshly placed, unstabilized counterpart; do not use the stabilized sample to measure natural spread.

### W05

**Replicate:** Right-click eligible masonry with a pickaxe, then repair with its matching repair material or mortar.

**Expected and controls:** Pickaxe cracks it and uses durability. Repair restores the uncracked counterpart, consumes an item in Survival, and marks it stable. Default pickaxe interaction does not require sneaking; default cracking does not drop a brick.

### W06

**Replicate:** Repeat W01-W05 on the supported families listed below, including stairs, slabs, and walls.

**Expected and controls:** Conversion preserves applicable shared properties such as facing, half, slab type, and waterlogging. Fixed neighboring layouts help distinguish conversion errors from legitimate connection recalculation. Report natural and manual results separately for #11.

### W07

**Replicate:** Expose supported cut/plate iron and iron bars/doors/trapdoors to water, air, and rain. Start isolated groups at each stage.

**Expected and controls:** Natural progression is clean -> exposed -> weathered -> rusted, under the rust settings and neighbor rules. Waxed controls should resist ordinary progression. Plain vanilla iron blocks are not interchangeable with cut/plate iron.

### W08

**Replicate:** Apply a wet sponge to supported non-final rust stages. Test the final stage separately.

**Expected and controls:** Each successful application advances one stage, not all stages. Default sponge drying is off. Do not assume a wet sponge is forbidden from transforming every waxed variant: this handler has no blanket wax exclusion.

### W09

**Replicate:** Axe-scrape exposed/weathered non-waxed iron; then try fully rusted iron. Test steel wool separately.

**Expected and controls:** Axe restores eligible intermediate rust by one stage, but not the fully rusted stage. Steel wool can restore fully rusted non-waxed iron by one stage. Sound/durability alone is not proof of restoration. The axe-scraping flag gates both paths.

### W10

**Replicate:** Honeycomb-wax supported iron, inspect its ID, then use steel wool. Include a waxed copper control.

**Expected and controls:** Waxing uses the registered waxed counterpart; steel wool removes supported wax. Additional uses can remove eligible rust/copper oxidation stages. Separate wax removal from rust removal and inspect each intermediate ID.


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
cracking, tool conversion, or a different growth. Keep issue history separate
from a new replication; see the [results ledger](Test-results.md).
Authoritative variant maps:
[Mossable](../../../src/main/java/com/ordana/immersive_weathering/blocks/mossy/Mossable.java)
and [Crackable](../../../src/main/java/com/ordana/immersive_weathering/blocks/cracked/Crackable.java).

## T: trees, piles, ivy, and azalea

### T01

**Replicate:** Axe-strip an oak log and oak wood in Survival; repeat recognized vanilla/modded wood types.

**Expected and controls:** Vanilla stripping proceeds and matching bark drops; recognized non-burning types can have scales instead. Creative drop suppression is a control. Discovery is not a guarantee for every modded tree or bamboo representation.

### T02

**Replicate:** Apply the matching bark to stripped log/wood, then apply bark to the end-grain face of an unstripped log.

**Expected and controls:** Stripped blocks regain their original barked counterparts and properties. Matching bark on an unstripped log's axis-facing end can make all-bark wood. Wrong bark/face should not perform that conversion.

### T03

**Replicate:** Use naturally grown, non-persistent leaves less than 12 blocks above clear ground; compare player-placed persistent leaves. Keep the area loaded.

**Expected and controls:** Natural leaf growth can deposit the matching pile. This placement path uses the default three-layer cap and local density/neighbor rules; it does not pile indefinitely. Persistent leaves are excluded from this path.

### T04

**Replicate:** Remove a tree's supporting logs and let its natural leaves decay. Compare several trees, including oak and spruce.

**Expected and controls:** Decay can create falling piles and client leaf effects, not on every decay (default chance 0.3). This separate decay path creates aged piles with 1-5 layers; the three-layer cap in T03 is not a universal cap on every source.

### T05

**Replicate:** Observe an aged oak/spruce pile over dirt-base ground. Keep a fresh `age=0` manual pile as control.

**Expected and controls:** Aged piles lose layers, age, and eventually disappear; leafy piles can turn suitable ground into loam, pine piles into podzol. Fresh piles do not enter that aging branch merely because time passes. Soil conversion is probabilistic.

### T06

**Replicate:** Manually stack piles to eight layers; remove their support. Also place a thin pile in air directly above source water.

**Expected and controls:** Manual stacking is distinct from natural deposition. Unsupported piles fall/settle; floating water-surface piles use `layers=0`, not ordinary underwater placement. No falling-layer error or crash should occur. Test both ground and water landings.

### T07

**Replicate:** Walk through deep piles in Survival; compare 1-3 layers with 4+ layers. Repeat a 6+ layer spruce pile with and without leggings.

**Expected and controls:** Deep piles slow ordinary living entities. Moving through sufficiently deep spruce piles can cause thorn damage without leggings. Depth Strider is a movement exemption; do not use it for the ordinary slowdown assertion.

### T08

**Replicate:** Bonemeal a flowering-azalea pile with adjacent non-flowering azalea bush/leaves/pile targets.

**Expected and controls:** The pile can flower eligible nearby azalea variants, probabilistically. An unrelated plant should not become azalea. Leave space and repeat rather than demanding every neighbor change on one use.

### T09

**Replicate:** Shear flowering azalea bush, leaves, and pile; apply azalea flowers to the non-flowering versions.

**Expected and controls:** Shearing yields flowers and removes flowering; flowers restore eligible flowering counterparts and are consumed in Survival. Repeat each of the three variant types.

### T10

**Replicate:** Place young ivy on a supported wall, leave adjoining surfaces free, and observe growth; also try bonemeal and remove support.

**Expected and controls:** Ivy can spread to eligible faces, with position/age restrictions. It need not cover every face forever. Unsupported remnants should be checked separately from spread; repeat with tree-felling mods only if installed.

### T11

**Replicate:** Equip the flower crown in the head slot near non-hostile bees, then remove it. Try an anvil name such as `rainbow`.

**Expected and controls:** Bees can follow a nearby crown wearer; the crown renders and emits its particles. Named supported variants change appearance. Wearing is the relevant trigger, not merely carrying it; competing bee tasks/pathfinding can delay following.


For an isolated T05 fixture, prepare a solid dirt floor and use this placement
two blocks away from your standing position:

```mcfunction
/setblock ~2 ~ ~ immersive_weathering:oak_leaf_pile[age=1,layers=8]
```

This tests **decomposition**, not whether a naturally decaying tree creates the
right pile. Check the actual pile ID for other discovered leaf types.

## G: soil, crops, and living growth

### G01

**Replicate:** Hoe loam, silt, sandy dirt, and earthen clay. Plant wheat, carrots, potatoes, and beetroot beside matched vanilla-farmland controls at suitable light.

**Expected and controls:** Supported soil makes its corresponding farmland. Test immediate seed acceptance and persistence at rate 3 before growth acceleration. Record plant/farmland IDs individually; loamy placement has report #1, not a confirmed universal pass.

### G02

**Replicate:** Compare hydrated (`moisture=7`), well-lit farmland with dry and shaded controls. Inspect each soil's crop tags.

**Expected and controls:** Matching crop types receive additional growth attempts on appropriate soils; moisture/light and tags matter. The implementation does not establish a guaranteed tenfold maturity speed. Water reach differs by soil, so use nearby water for the basic positive.

### G03

**Replicate:** Put suitable dirt/soil beside grass and mycelium in lit plots; include covered controls. Test grass-over-mycelium and the reverse separately.

**Expected and controls:** Supported grassy soils spread or lose grass under appropriate light/cover rules. The two grass/mycelium replacement flags govern different directions. Inspect actual soil variants, not just the top texture.

### G04

**Replicate:** Clear air above small grassy-block plots in each listed biome below. Also leave a dense vegetation plot untouched.

**Expected and controls:** Base grass and biome-specific vegetation can appear. Density-limited growth should slow/stop in crowded plots; rare outputs need separate Not observed results, not a forced failure.

### G05

**Replicate:** Put rooted dirt/rooted grass into a bank of ordinary dirt and grass, with free underside and side spaces; try the biome-specific sapling plots below.

**Expected and controls:** Eligible neighboring ground can become rooted dirt/rooted grass; exposed positions can gain hanging roots/wall roots. Rooted grass can spawn the configured biome's saplings. A solidly filled bank has no space for hanging roots.

### G06

**Replicate:** Leave small fertile, sky-exposed plots during clear weather. Then add mature `age=7` weeds near replaceable plants and compare a weed-free plot.

**Expected and controls:** Weeds can establish and mature; mature-weed spread can replace tagged targets, including susceptible plants, with density limits. Replacement is not the same bug as seeds failing immediate placement on bare farmland.

### G07

**Replicate:** Grow grass, fern, and underwater seagrass with room for the second block; compare obstructed fixtures.

**Expected and controls:** Eligible short plants become their tall versions. Upper space, correct water, and plant survival are required; record all three plants separately.

### G08

**Replicate:** Leave open space above crimson/warped nylium. Place weeping-vine space below nether wart blocks and twisting-vine space above warped wart blocks.

**Expected and controls:** Nylium produces its configured roots/fungi; supported wart-block growth produces the respective vines. Check orientation and survival rather than expecting either vine from any Nether block.

### G09

**Replicate:** Test mycelium and podzol mushroom/fern plots; river-biome earthen clay with adjacent water for sugar cane; saplings in desert/Nether controls.

**Expected and controls:** Their configured vegetation can grow. Saplings can become dead bushes in the relevant desert/Nether growths. Mushroom light and sugar-cane support/water still matter. Keep ordinary sapling-growth tests outside those drying conditions.

### G10

**Replicate:** Underwater in `minecraft:warm_ocean`, test brain, bubble, fire, horn, and tube coral blocks separately with nearby water and low coral density.

**Expected and controls:** Matching coral, fans, or wall fans can appear in eligible water. Wet sponge is a rare configured output. A plains water tank is not an equivalent biome test; retain all five coral results separately.

### G11

**Replicate:** Complete a composter and collect its product with the dirt-output flag enabled; repeat disabled in a separate config run.

**Expected and controls:** The advertised feature is dirt instead of vanilla bone meal, but no active IW Java consumer of `COMPOSTER_DIRT` was found. Treat this as a feature-gap check, not confirmed implemented behavior. Also test compostable IW materials separately.

### G12

**Replicate:** Suspend plain permafrost over a landing area; compare a cold/dark fixture with adjacent water or bright block light.

**Expected and controls:** Frozen permafrost can remain suspended. Adjacent water or sufficient light schedules a gravity check; an unsupported block can fall and may disappear on landing. Do not assume a universal conversion to dirt called "melting."

### G13

**Replicate:** Place earthen clay and mulch in water and inspect `waterlogged`; compare dry placement. Land on mulch and stone from a matched safe test height.

**Expected and controls:** Supported soil retains its waterlogged state/fluid behavior. Mulch reduces fall damage compared with stone; client drip particles are separate from fluid state. Repeat soil types individually.

### G14

**Replicate:** In Survival, use a lava bucket on non-molten nulch, then an empty bucket on molten nulch. Compare stepping/landing on molten and cooled nulch, with plain boots and Frost Walker.

**Expected and controls:** Buckets toggle `molten` and exchange lava correctly. Molten nulch hurts ordinary living entities stepping on it; Frost Walker exempts that damage. Nulch cushions falls. R13 separately tests cooling through water contact.


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

### C01

**Replicate:** In an icy biome at night, place ordinary glass/panes, grass, and fern with free air above/nearby. Separately expose counterparts to snow.

**Expected and controls:** Eligible frosty variants form through their night/block-tick or snow/sky rules; record their `natural` state. Compare warm clear-day specimens. A roof only controls the sky path, not the separate nighttime block-tick path.

### C02

**Replicate:** Inspect frost in newly generated icy terrain; apply the frost item to glass/panes, grass, and fern in cold conditions.

**Expected and controls:** The item converts mapped targets to frosty counterparts and is consumed in Survival; unsupported targets use ordinary frost placement instead. Shipped `frost_growth` and `frost_spreading` have chance **0**: lack of ambient growth from those two rules is expected, even at 1000.

### C03

**Replicate:** Put ordinary ice beside source water in an icy biome on a clear night, then during snow. For solidification, use expanding thin ice with adjacent packed ice and water to its north.

**Expected and controls:** Thin ice can form/spread. The solidification rule specifically needs packed ice nearby, `can_expand`, and its north-facing water target check. Ordinary ice alone is not a complete solidification fixture; warm daytime water is not an equivalent formation test.

### C04

**Replicate:** At rate 3, walk/jump on thin ice in Survival without Feather Falling; repeat with enchanted boots.

**Expected and controls:** Eligible heavy living entities can progressively crack thin ice or break it into water. Neighboring ice can crack too. Feather Falling is an explicit exemption, so use plain boots for the positive test.

### C05

**Replicate:** Test thin ice near bright block light. Test ambient daytime/warm melting as a separate config run.

**Expected and controls:** Bright-light melting is separate from the optional natural-melting branch. Natural thin-ice melting is **off by default**; absence of that daytime branch is not a failure.

### C06

**Replicate:** In cold/icy conditions, hang ice above air; also test snowy exposed supports and non-persistent cold leaves.

**Expected and controls:** Icicles can form through several paths. Spawning is probabilistic and some paths use position eligibility; one empty underside is insufficient evidence of failure. Keep melt conditions out of the positive fixture.

### C07

**Replicate:** Place a downward icicle under ice with water above that ice, in an icy biome on a clear day. Use separate open-floor and cauldron fixtures.

**Expected and controls:** A suitable tip can grow down or produce an upward counterpart; eligible drips can schedule cauldron filling. Keep vertical space clear. Cauldron completion uses scheduled ticks, so observe at rate 3 as well.

### C08

**Replicate:** At rate 3, remove a hanging icicle's support with free space below. Separately open a nearby chest/door with support intact and vibration reaction enabled.

**Expected and controls:** Support loss should produce falling icicles; falling/upward-tip hazards can damage entities. Vibration-only dislodging is an independent intended behavior with the source discrepancy noted below; do not count support loss as a vibration pass.

### C09

**Replicate:** Move icicles into bright light, the Nether, or non-icy clear daytime conditions. Compare the cold supported control.

**Expected and controls:** Icicles melt/disappear under their applicable random-tick conditions. Their behavior is not governed by the thin-ice natural-melting flag.

### C10

**Replicate:** Eat icicles and the ice sickle at rate 3 with icicle food enabled; repeat disabled.

**Expected and controls:** Eating adds freezing ticks and provides brief Fire Resistance without hunger nutrition. The effect is configured with hidden particles/icon; an absent HUD badge is not a failure. Food disabled should leave these non-edible uses/content available.

### C11

**Replicate:** In snowfall, expose mapped stone to the sky; separately apply a snowball and shovel the cover away in Survival.

**Expected and controls:** Snowy variants can form; snowball applies an eligible cover, shovel removes it, uses durability, and yields a snowball. Warmth/bright light can remove snow through separate logic. A roofed target controls only sky deposition.

### C12

**Replicate:** Run `/weather rain` in tagged desert terrain with exposed sandable masonry. Separately apply a sand-layer item and shovel it off.

**Expected and controls:** Sandstorm growth makes eligible sandy states/layers. Shoveling can remove a heavier sandiness stage first, then the remaining cover, yielding a sand layer. Ordinary rain in an untagged biome is not a sandstorm.

### C13

**Replicate:** Put a multi-layer sand pile over a supported sandable block. Separately test ordinary/red sandstone ledges with air directly below and a landing floor underneath.

**Expected and controls:** The ordinary sand-layer seeping rule can thin piles with more than one layer while making the block below sandy. Sandstone/red sandstone generate their matching layers below into eligible air. Do not assume every red layer has the same seeping rule.


**Vibration diagnostic:** the inspected
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

### H01

**Replicate:** With fire ticking permitted, let fire consume recognized burnable wood in an isolated plot. Use multiple logs/planks.

**Expected and controls:** Some consumed wood is replaced by its charred variant (default chance 0.3), possibly smoldering. Total burning/destruction is also possible; a single missing charred block is inconclusive.

### H02

**Replicate:** At rate 3, light charred blocks with flint and steel/fire charge, then right-click smoldering blocks with a shovel and other held items.

**Expected and controls:** Lighting uses durability/consumes fire charges in Survival. The inspected interaction extinguishes an already-smoldering block on right-click without restricting the item to a shovel. Confirm state changes; the advertised shovel flag is not checked by this handler.

### H03

**Replicate:** Put a lit campfire below a nonflammable ceiling/wall within eight blocks; repeat unlit, and test fire separately.

**Expected and controls:** Soot can attach to nearby supported faces. A signal-fire campfire has a longer 23-block smoke search. Existing soot does not disappear just because a spawning setting is disabled.

### H04

**Replicate:** In a separate restarted/configured run, disable soot spawning and place fresh lit campfire and fire fixtures.

**Expected and controls:** Intended negative control is no newly generated soot from the disabled paths. This is specifically an open report (#7); test both paths and record any continuing growth rather than assuming the toggle works.

### H05

**Replicate:** Expose cobweb to fire with its flammability setting enabled; compare disabled in a separate run.

**Expected and controls:** Configured cobweb burning should differ from the disabled control. This is a fire interaction, not proof from passive growth alone.

### H06

**Replicate:** Put magma beside lava without the extra fluid-generator ingredients below.

**Expected and controls:** The magma-owned random growth can turn eligible nearby lava into magma, subject to its density/replacement rules. This is distinct from bubble-column fluid generation in R09.

### H07

**Replicate:** Strike isolated stone-family, magma, and obsidian targets with real lightning above their surfaces.

**Expected and controls:** Registered lightning growth can produce magma from overworld base stone, lava from magma, and crying obsidian from obsidian. Inspect which block actually receives the strike; this is not random-tick growth.

### H08

**Replicate:** Strike sand/red-sand terrain in the separate lightning plot with vitrification enabled. Repeat strikes/fresh targets.

**Expected and controls:** Applicable sand becomes vitrified; fulgurite can form, with default chance 0.4 rather than on every strike. Compare disabled vitrification in a separate run. Ordinary lightning/fire destruction remains a separate vanilla effect.

### H09

**Replicate:** Put charred fixtures beside fire/magma sources, then compare rain/water exposure. Step on smoldering and extinguished fixtures with plain boots and Frost Walker.

**Expected and controls:** Sufficient heat can relight charred blocks; wet/cooling conditions extinguish them. Smoldering surfaces cause hot-floor damage to eligible entities; Frost Walker is an exemption. Keep these environmental changes separate from H02's tool action.

### H10

**Replicate:** Build a short charred-block overhang supported below or beside it, then remove supports at rate 3. Inspect `overhang`.

**Expected and controls:** Supported blocks can remain; unsupported `overhang=2` blocks schedule falling. Gravity/collapse uses scheduled ticks and does not require randomTickSpeed 1000. Check the actual support layout and logs.


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

### R01

**Replicate:** Lava, horizontal water contact, adjacent diorite; omit quartz.

**Expected and controls:** Andesite replaces lava.

### R02

**Replicate:** Lava, horizontal water contact, adjacent tagged quartz block; omit diorite.

**Expected and controls:** Diorite replaces lava.

### R03

**Replicate:** Lava, horizontal water contact, adjacent diorite **and** tagged quartz block.

**Expected and controls:** Granite replaces lava; its priority wins over the simpler R01/R02 recipes.

### R04

**Replicate:** **Flowing** lava directly above basalt with `axis=y`; omit other recipe ingredients.

**Expected and controls:** Vertical basalt replaces flowing lava. A still-source-only fixture is not equivalent.

### R05

**Replicate:** Lava adjacent to magma **and** blue ice; omit the R06 bone-block base.

**Expected and controls:** Blackstone replaces lava.

### R06

**Replicate:** R05 ingredients, plus a bone block directly below with `axis=y`.

**Expected and controls:** Calcite replaces lava and takes priority over R05.

### R07

**Replicate:** **Still/source** lava adjacent to soul fire **and** water.

**Expected and controls:** Crying obsidian replaces the source lava. Flowing-only lava is the wrong control.

### R08

**Replicate:** Lava with horizontal water contact, adjacent blue ice, and soul soil directly below.

**Expected and controls:** Smooth basalt replaces lava; compare the vanilla generator without the water-side condition.

### R09

**Replicate:** Lava directly above a rising bubble column (`drag=false`).

**Expected and controls:** Magma replaces lava. This is distinct from H06's random-tick spread.

### R10

**Replicate:** Lava directly above a downward bubble column (`drag=true`).

**Expected and controls:** Tuff replaces lava. Verify the bubble-column state, not just the base block.

### R11

**Replicate:** Bring lava into contact with supported mossy masonry.

**Expected and controls:** The `burn_moss` generator removes eligible moss; inspect the masonry target, not just the lava.

### R12

**Replicate:** Bring lava into contact with ordinary clay.

**Expected and controls:** The clay target becomes terracotta. This is not the special earthen-clay soil block.

### R13

**Replicate:** Bring water into contact with `nulch_block[molten=true]`.

**Expected and controls:** Nulch becomes `molten=false`; water is the trigger, nulch the target.

### R14

**Replicate:** Bring lava into contact with sand/red sand in the sand tag.

**Expected and controls:** The contacted sand target becomes vitrified sand, without needing lightning.


Exact [fluid-generator JSON](../../../src/main/resources/data/immersive_weathering/fluid_generators)
and [adjacency interpretation](../../../src/main/java/com/ordana/immersive_weathering/data/fluid_generators/SelfFluidGenerator.java)
are the reference if a rig gives an unexpected result.

## Q: crafting, presentation, configuration, and regressions

### Q01

**Replicate:** Craft representative content through the recipe book/EMI: bark-related content, repair materials, supported masonry shapes, crowns, and cold items where recipes exist. Use crafting/stonecutting/smelting as shown.

**Expected and controls:** Displayed recipes produce their specified output/count; no invented legacy recipe is required. Check recipe IDs against shipped JSON. Creative availability alone is not a crafting pass.

### Q02

**Replicate:** Eat ordinary/golden/enchanted golden moss clumps and azalea flowers at rate 3.

**Expected and controls:** Ordinary moss provides small food value; golden moss provides regeneration; enchanted golden moss provides absorption, regeneration, and water breathing. Azalea flowers have zero nutrition. Acquisition/recipe availability is a separate result.

### Q03

**Replicate:** Explore genuinely new chunks in eligible biomes, or a new disposable world. Record locations and generated feature IDs.

**Expected and controls:** Applicable soil patches, icy frost/icicle features, dry lakebeds, ivy, dune grass, and moss patches can generate. Existing chunks do not retroactively gain worldgen because random ticks were raised. Rare absence in a short search is Not observed.

### Q04

**Replicate:** Inspect vanilla-tree and recognized modded-tree piles/bark, generated models, inventory icons, sounds, and particles; compare a default resource-pack run.

**Expected and controls:** Supported generated content renders without missing textures or generation errors. Client particle/sound settings should affect presentation, not prove server-side growth stopped. Unusual modded models/packs need individual results.

### Q05

**Replicate:** With EMI installed, inspect IW interactions and fluid recipes; perform `/reload` in the disposable world and inspect the log.

**Expected and controls:** The IW plugin loads/reloads and displays its supported entries without exceptions. Do not infer every gameplay feature works from a successful plugin load. Without EMI, mark this integration Not applicable.

### Q06

**Replicate:** In a separate config run, disable `general.block_growths`; then test a targeted growth blacklist such as the `tall_seagrass` path, following Configuration.

**Expected and controls:** Registered growth dispatch and the selected rule should stop, respectively. This is not a promise that the global growth flag stops independent rust, moss/crack patch logic, physics, or item actions. Preserve exact effective settings and restart/reload steps.

### Q07

**Replicate:** Apply common-config changes using the documented procedure; if a server is available, join with a second client and perform W03/W05/R13 while both observe.

**Expected and controls:** Server-effective settings and block/item updates agree between clients, with no ghost blocks or duplicate consumption. A successful singleplayer test is not dedicated-server coverage; mark unavailable setups Not applicable.

### Q08

**Replicate:** Test normal-rate sapling growth with and without nearby piles; falling piles over water; ivy after support removal/tree felling; affected walls with optional compatibility mods.

**Expected and controls:** These directly exercise reports #2/#4/#5/#6/#8/#9 where their mods/layouts apply. Record vanilla/minimal and combined-mod results separately. Do not count an absent optional mod as a compatibility pass.

### Q09

**Replicate:** Observe the same bounded small growth plots at rate 3 and 1000; save, quit, reopen, leave/re-enter chunks, and change dimension. Inspect logs each time.

**Expected and controls:** No new exceptions, lost blocks, stale growth behavior, or persistent falling-entity buildup. Acceleration may cause lag; this is stability evidence, not a TPS/MSPT benchmark or proof that rates match an older release.


Worldgen eligibility is registered in
[ModFeatures](../../../src/main/java/com/ordana/immersive_weathering/reg/ModFeatures.java);
[biome tags](../../../src/main/resources/data/immersive_weathering/tags/worldgen/biome)
and configured/placed features determine actual sites. Use the
[configuration guide](../players/Configuration.md) and
[generated reference](Reference.md) for settings/defaults,
and [troubleshooting](../players/Troubleshooting.md) for existing reports.

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
Keep the tested artifact intact; do not overwrite release assets to record results:
this checklist is documentation, not a new jar/release.
