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
