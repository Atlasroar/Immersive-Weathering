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
| Reduce leaf visuals/sounds | Client `general` section: `falling_leaf_particles`, `leaves_decay_particles`, `decay_sound`, and rate keys |
| Inspect generated assets/data | Common `general.debug_save_dynamic_pack`; use a disposable development instance because it writes debug resources |

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
