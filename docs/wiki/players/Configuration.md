# Configuration

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

## Which settings matter?

Use the IW configuration screen through Mod Menu where available, or inspect the
files created by Moonlight in the instance's configuration directory. This guide
does not prescribe a universal filename: confirm the active instance and file
before editing. Back up files, stop the game/server before manual edits, then
restart and verify the effective settings.

Common settings control simulation and interactions; client settings control
visuals and sounds. The common spec is marked for synchronization in source.
On a dedicated server, configure simulation on the server.

The exact keys, defaults, bounds, and source fields are listed in the
[generated settings reference](../developers/Reference.md). The reference keeps
source expressions rather than guessing what a setting's name means.

| Goal | Setting or approach |
| --- | --- |
| Disable the block-growth dispatcher | Common `general.block_growths`; this is broad and does not disable every independent weathering/block behavior |
| Disable specific growths | Common `general.block_growth_blacklist`, or a datapack override |
| Control moss/crack spread | Common `mossy_blocks` and `cracked_blocks` sections |
| Control natural rust | Common `rusting` section |
| Control leaf piles | Common `leaf_piles` section |
| Control standalone snow | Common `snow` section; independent of `general.block_growths` |
| Control standalone fire | Common `fire` section; independent of `general.block_growths` and existing `charring` settings |
| Control standalone sandstorms | Common and client `sandstorms` sections; independent of legacy sandy-block growth |
| Reduce leaf visuals/sounds | Client `general` section: `falling_leaf_particles`, `leaves_decay_particles`, `decay_sound`, and rate keys |
| Inspect generated assets/data | Common `general.debug_save_dynamic_pack`; use a disposable development instance because it writes debug resources |

## Standalone sandstorms

Unreleased source feature. In Mod Menu open **Immersive Weathering -> Configs ->
Common/Client -> Standalone sandstorms**. The common/server spec is synchronized
by the existing Moonlight config system. No new library is required.
Stop/restart for manual file edits, then verify the effective server config.
Gameplay suppliers are read on every relevant tick; disabling deposition does
not remove already-deposited sand. `/reload` applies datapack biome/immunity
tag changes; it is not a substitute for reloading config files.

| Spec / key in `sandstorms` | Default (bounds) | Meaning |
| --- | --- | --- |
| Common `deposition` | `true` | Add existing IW sand layers on exposed, dry, sturdy surfaces; no cauldrons. |
| Common `layer_cap` | `1` (1-8) | Maximum deposited layers per pile; never trims taller piles or creates a new storm pile above a full one. |
| Common `slowing` | `true` | Temporary total-speed reduction for exposed nonimmune living entities. Disabling removes IW's modifier next tick. |
| Common `slow_amount` | `0.3` (0-1) | Fraction removed: 0.3 is 30%. Zero removes IW's modifier; one stops attribute-based movement. |
| Client `particles` | `true` | Windblown dust; vanilla Minimal suppresses it, Decreased halves column trial probability. |
| Client `particle_range` | `20` (0-32) | Scans `(2 * range)^2` loaded columns/tick, default 1600, maximum 4096; zero disables. |
| Client `particle_rarity` | `60` (1-1200) | One particle trial in this many columns. Higher is sparser. |
| Client `fog` | `true` | Weather/biome-blended sand-colored terrain fog, including indoors. |
| Client `fog_start` | `16` (0-256) | Full-storm fog start, clamped to end and stronger vanilla fog. |
| Client `fog_end` | `64` (1-512) | Full-storm fog end, never extending vanilla view distance. |
| Client `hide_clouds` | `true` | Hide clouds above 0.75 rain strength in storm biomes; deliberately independent of fog. |
| Client `sounds` | `true` | Intermittent wind near storm surfaces; Weather volume and subtitles still apply. |

These settings do **not** disable/retune the older `weather_sandstorm` sandy-block
growth or its `has_sandstorm` tag. Use growth settings/datapacks for that.
New storm subtype tags are `regular_sandstorms` and `red_sandstorms`; both also
require precipitation-disabled biomes. Entity exemptions use
`sandstorm_slow_immune`. When Scorchful is installed, new IW storm settings have
no simulation/presentation effect because Scorchful owns that behavior.

## Standalone fire

Open **Immersive Weathering -> Configs -> Common -> Standalone fire** using the
existing optional configuration-screen integration. The common/server spec is
synced by Moonlight; no extra library is required. Stop/restart after manual
file edits and verify the effective server settings. The implementation reads
current suppliers on every action/trial rather than caching config or rewriting
global flammability tables. Disabling features stops new actions, not existing
fires or prior dirt conversion. Config changes do not replace `/reload`'s
datapack behavior or alter the existing `charring` controls.

| Key in `fire` | Default (bounds) | Meaning |
| --- | --- | --- |
| `entity_spread` | `true` | Known non-sun burning entities may ignite nearby blocks, subject to gamerules. |
| `torch_attacks` | `true` | Successful direct main-hand torch/soul-torch melee uses ordinary four-second burning; one item outside Creative. |
| `flammable_grass` | `true` | Adds grass ignition/burn odds 5/20; successful consumption leaves dirt at the same position, without drops. |
| `spread_interval` | `20` (1-1200) | Ticks between staggered trials per entity. At normal 20 TPS, default is once per second. |
| `spread_radius` | `1` (1-3) | Cube radius around the carrier for candidate air cells. |
| `heat_radius` | `2` (0-4) | Cube radius for counting FIRE, SOUL_FIRE, MAGMA_BLOCK and LAVA block cells, including flowing lava. Radius zero checks the carrier cell. |
| `heat_source_cap` | `8` (1-64) | Stops the heat count early; no unlimited bonus. |
| `spread_chance` | `0.02` (0-1) | Base probability per trial, not per air cell. Zero disables successful trials. |
| `projectile_multiplier` | `0.0625` (0-1) | Applied **after** the cap; default is one sixteenth. Zero disables projectile spreading. |
| `heat_bonus` | `0.125` (0-1) | Adds this amount per counted source to a heat multiplier starting at one. |
| `temperature_bonus` | `0.5` (0-4) | Adds this amount per degree of biome base temperature above 0.8, up to temperature 2. |
| `spread_chance_cap` | `0.25` (0-1) | Maximum trial probability **before** projectile reduction. |

Chance is the smaller of the cap and
`base chance * (1 + counted sources * heat bonus) * (1 + max(0, min(2, temperature) - 0.8) * temperature bonus)`,
then multiplied by the projectile multiplier for projectiles. With all defaults,
eight heat cells and temperature 2 give about **6.4% per trial**, or **0.4%**
for a projectile, not guaranteed spreading near heat. Default cold/temperate
trials with no heat remain 2%, or 0.125% for projectiles. One successful trial
chooses uniformly among eligible cells and attempts at most one placement.

`doFireTick` is a hard environmental gate, including grass burning; direct torch
combat is deliberately independent. Non-player carriers obey `mobGriefing`;
players and their projectiles do not. Existing external flammability values are
retained (larger grass values are not reduced); disabling IW grass restores
native/other-mod behavior rather than removing another mod's registration.
Grass consumed by IW remains dirt; lowering a setting does not restore grass.
See [feature rules](Features.md#standalone-fire) for supported
ignition sources, wetness and save/reload behavior.

## Leaf blacklist

`leaf_piles_blacklist` compares **original leaf block IDs**, including the
namespace. Do not enter the generated IW pile ID. For example, a modded leaf
ID has the form `mod_namespace:cypress_leaves`; obtain the actual ID from your
installed mod rather than assuming this example exists.

Blacklisting future pile creation does not remove piles already in the world.
Do not assume a particles toggle controls pile entities or ambient vanilla
falling-block particles.

## Standalone snow

The common `snow` section is server-authoritative and synchronized by Moonlight.
Restart after file edits, and test fresh fixtures with the effective server config.
In Mod Menu, open **Immersive Weathering -> Configs -> Common -> Snow**.
The existing optional Cloth Config (or Moonlight-supported YACL) integration
provides the settings screen; snow does not add another config dependency.
All six existing keys remain in the common spec, with named controls and
explanatory hover text in the beta.2 source. The client spec does not own them.
If these controls are absent, confirm the active instance/jar and optional screen
integration before editing another profile's files.

| Key | Default | Effect |
| --- | --- | --- |
| `host_mixing` | `true` | Allows new snow/host combinations and snow-item additions. Existing covered states remain registered/removable when disabled. |
| `falling` | `true` | Detaches unsupported snow into falling entities. When disabled, newly unsupported covered hosts lose the cover rather than the host; plain snow follows vanilla neighbor removal. |
| `replace_partial_layers` | `true` | Widens ordinary block-item replacement of plain snow to layers 1-7 and allows raw-plant-eligible replacement of partial covered plants. Native petal stacking is retained; ordinary same-plant replacement, layer 8 and covered nonreplaceable hosts remain protected. |
| `accumulation` | `true` | Enables extra weather layers and weather mixing. Disabling it retains vanilla initial snow, not extra layering. |
| `accumulate_without_thunder` | `false` | Opts into extra layers during ordinary snowfall; clear weather and warm rain still cannot accumulate snow. |
| `column_cap` | `8` (1-8) | Maximum weather-deposited layers, in one cell. Lowering it does not delete existing snow. |

`snowAccumulationHeight = 0` disables weather snow. The vanilla default value
**1 is a compatibility sentinel**: IW uses `snow.column_cap` so default storms
can reach eight layers without a gamerule command. Other positive gamerule values
are hard limits intersected with the IW cap (for example 2 caps at two; 24 still
caps at eight). An explicitly set value of 1 is indistinguishable from the vanilla
default; set **`snow.column_cap = 1`** for a one-layer maximum. This does not
change the gamerule itself or unrelated gamerules.

Manual placement is not weather deposition. Falling piles also never spill
overflow into a new cell; recover their excess as snow-layer items. Weather uses
vanilla's existing chunk sample, not the block-growth dispatcher. Changing
`randomTickSpeed` or disabling IW block growths is not a snow-weather off switch;
use the snow config and gamerule. Cold temperature, actual weather, heightmap
exposure, block light at most 11, and normal ticking-chunk limits still matter.

Replacing a partial covered plant with another supported dry host retains its
layer count only while `host_mixing` is enabled. Ordinary solid replacement
removes the old plant and snow together through normal item placement. Turning
off `replace_partial_layers` blocks these covered-plant replacements; snow-item
addition remains controlled independently by `host_mixing`.

## Growth blacklist and datapacks

The growth blacklist compares the JSON resource's path, without `.json` or the
namespace. For example, `weeds` and `weeds_spread` are separate shipped growths.
For a resource in a subfolder, retain that path. Apply Minecraft's `/reload`
after changing datapack-driven behavior and check the log for parse failures.
Operator permission is needed on a server. Restart if changing configuration
files directly; do not rely on a file edit being noticed automatically.

For precise control, override a shipped growth at the same namespace/path.
The [developer data guide](../developers/Data-and-resources.md) includes a
copy-based example. A pack may override your desired behavior, so confirm the
active datapack stack.

## Known limitations

The soot toggle has an open Fabric report and should not be presented as a
verified complete disable switch. Read [troubleshooting](Troubleshooting.md).
A workaround is not proof that the underlying bug is fixed.

The [feature test sheet](Feature-testing.md) records additional source caveats
for advertised settings and icicle vibration behavior. A registered setting is
not, by itself, proof that every relevant handler consults it; verify effective
behavior with fresh fixtures and record the exact configuration.
