# Adding features and fixing bugs

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Establish evidence

Use the [live Fabric tracker](https://github.com/Atlasroar/Immersive-Weathering/issues?q=is%3Aissue+is%3Aopen+label%3Afabric+label%3Aminecraft-1.20.1)
and [player troubleshooting](../players/Troubleshooting.md) to find existing
reports. Record exact versions and minimize the Fabric reproduction. Distinguish
observed behavior, intended design, hypotheses, and measured results.

Base new work on the GitHub default branch, currently
`1.20.1-fabric`. The old version/support branches are archived tags,
not active contribution targets; see the [branch policy](Architecture.md).

Do not import Forge-only reports as confirmed Fabric bugs. Shared code may
justify investigation, but it does not replace a Fabric reproduction.

## Wire all relevant surfaces

| Work | Check alongside the implementation |
| --- | --- |
| New block/item | Registry ID, item form, models/blockstates/textures, loot/recipes/tags, creative visibility, interaction durability/drops, translations |
| Weathering transition | Source/destination properties, stair/slab/wall shape, waterlogging, door halves, wax/rust mappings, neighbor/scheduled/random ticks |
| Plant/soil support | Placement and survival, fertile tags, crop/stem/special plant mixins actually registered on Fabric, growth speed |
| Generated leaf/bark type | Family discovery, dynamic registration timing, stable IDs/children, client and server packs, colors, particles, unusual mod models |
| Growth/generator | Codec fields, source dispatch, owners/priority, predicates, loaded-area checks, reload/blacklist, logs and invalid inputs |
| Config/packet | Existing keys/defaults, synchronization, thread/side safety, persistence and protocol, docs and migration |

Preserve intended user behavior and world compatibility. Gate intentional changes,
document them for players, and do not replace failures with silent success-shaped
fallbacks.

## Finish together

Update player instructions and developer contracts in the same pull request.
Regenerate [Reference.md](Reference.md), run
[validation](Development.md), and state what was not verified.

Maintain upstream attribution and license notices. See
[licensing](Licensing.md) and the root [contribution policy](../../../CONTRIBUTING.md).
