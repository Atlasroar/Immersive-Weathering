# Troubleshooting

[Wiki home](../README.md) | [Players](README.md) | [Developers](../developers/README.md)

For **2.5.0 freezing wind / thunder snowstorms**, the published 2.4.0 jar
does not contain this work. Record the release/source commit and actual binary
hash. Natural ambient gusts require night, natural skylit dimensions, exposed
loaded space, nonzero chance/budget/cap; cold thunder can generate them in daytime.
Do not raise randomTickSpeed to accelerate wind. Below Y100/hot biomes are
intentionally rare. Use the [exact formula](Configuration.md#freezing-wind-and-thunder-snowstorms).
No flame/ice/icicle changes with mobGriefing=false is expected, not a bug;
disable_icicles/light/occupied-cell guards still apply. Unlit torches retain
their normal/soul identity: relight, do not replace them to recover illumination.
F3 distinguishes the two unlit types despite their shared frozen artwork.

Snowstorm requires **thunder**, unlike sandstorm. Snow still needs its global
accumulation/cap/gamerule/support rules; bonus0/disabled storm preserves ordinary
snow behavior rather than disabling snowfall. Check client snowstorm toggles,
vanilla particle/Weather settings and actually installed Immersive Storms
(which owns presentation only). No new upstream wind recordings were imported.
See the [results ledger](../developers/Test-results.md) rather than inferring compatibility or rendering acceptance.

For the **2.3.0 standalone sandstorms**, check vanilla rain first, then the
new dry-biome subtype tags and common/client `sandstorms` settings. Thunder is
unnecessary; wet-biome `has_sandstorm` legacy growth does not imply new dust/fog
or layer deposition. A roof shelters slowdown/deposition, not biome fog/clouds.
Check the Weather slider, subtitles and particle setting for missing wind/dust.
IW deliberately yields its new storm features when Scorchful is installed.
The [ST01-ST08 instructions](../developers/Feature-testing.md#standalone-sandstorms)
are replication steps, not verified graphics, audio or multiplayer results.

For **Unreleased shared mineral covers**, confirm the exact source build:
published 2.5.0 does not contain the expanded shared inventory. Check the
appropriate `snow.host_mixing` or `sand.host_mixing` setting, dry plant state,
native plant support and a floor for the pile. Waterlogged plants and aquatic
plants are intentionally excluded; a hanging plant alone cannot hold sand/snow
in the air. Plant interactions retain their own light, support and growth rules.
Leaf/flower litter is not part of this system. Use
[MP01-MP06](../developers/Feature-testing.md#shared-mineral-pile-plants).

For **2.4.0 sand/thunder changes**, first distinguish that version
from published 2.3.0. Saved `sandstorms.layer_cap=1` stays one until
explicitly changed; manual stacking still reaches eight. Mixing covers only
dry vanilla fences and dead bushes in the published 2.4.0/2.5.0 jars. The
Unreleased inventory is described above. A covered
fence cannot be replaced like bare sand. Rain erosion needs actual rainfall,
positive randomTickSpeed and absence from **both** sandstorm biome tags;
temperature alone is irrelevant. Thunder fire suppression uses sky exposure,
not actual rainfall, and requires `doFireTick`. Covered/indoor fire, soul fire,
torches and campfires are not extinguished by it. See
[SP01-SP08](../developers/Feature-testing.md#sand-and-thunder) and its
[dated results](../developers/Test-results.md). Automated coverage does not
itself establish gameplay or compatibility results.

## Reported Fabric 1.20.1 problems

The following is the initial catalog established on November 1, 2026, not a
live issue-status table. Reports were imported from upstream and have not all
been reproduced on this fork's current build. Follow the linked issues for
current status and any verified fixes.

| Report | Safe next step |
| --- | --- |
| [Loamy farmland rejects crops](https://github.com/Atlasroar/Immersive-Weathering/issues/1) | Compare with ordinary farmland; include lighting, plant ID, and mod list in a reproduction. |
| [Falling-layer errors and resource usage](https://github.com/Atlasroar/Immersive-Weathering/issues/2) | Preserve the full log and profile a disposable reproduction; do not merely hide log messages. |
| [EMI plugin loading exception](https://github.com/Atlasroar/Immersive-Weathering/issues/3) | Record EMI/version information and the entire plugin exception. |
| [Wilder Wild piles crash over water](https://github.com/Atlasroar/Immersive-Weathering/issues/4) | Upstream reports minimal Minecraft particles or blacklisting the original leaves as workarounds; these are not verified fixes. Back up before revisiting affected chunks. |
| [Floating ivy after tree felling](https://github.com/Atlasroar/Immersive-Weathering/issues/5) | Record Panda's Falling Trees version and the supporting-block layout. |
| [Leaf piles block sapling growth](https://github.com/Atlasroar/Immersive-Weathering/issues/6) | Remove nearby piles in a test area and compare the growth result. |
| [Soot ignores its toggle](https://github.com/Atlasroar/Immersive-Weathering/issues/7) | Confirm server/common settings; include the effective file and restart/reload steps. |
| [Diagonal Walls compatibility](https://github.com/Atlasroar/Immersive-Weathering/issues/9) | Compare affected and unaffected walls in the same layout. |
| [Water Erosion property conflict](https://github.com/Atlasroar/Immersive-Weathering/issues/8) | Test the mods separately in copies of the world and capture the full combined stack trace. |

The current-fork
[stone-brick growth/spread report](https://github.com/Atlasroar/Immersive-Weathering/issues/11)
was closed at the tester's request on November 1, 2026 after W01-W06 and
SB01-SB06 passed in the beta.2 test round. This is user-reported confirmation,
not a new code fix or measured equality of rates. The
[results ledger](../developers/Test-results.md) preserves the results and limitations.
The same round reports double-tall plant growth failing (G07), and icicle
support-loss falling passing but vibration-triggered falling failing (C08).
Beta.3 corrects a deterministically reproduced paired-placement failure behind
G07. The later 2.1.0-beta.2 snow round reports N11's regression checks passing;
separate transition outcomes in the [manual retest](../developers/Feature-testing.md#paired-plant-growth)
were not itemized. C08 vibration is not fixed by that change. Steel-wool availability/use
also remains unresolved despite the reported axe-interaction passes.

[Browse current Fabric issues](https://github.com/Atlasroar/Immersive-Weathering/issues?q=is%3Aissue+is%3Aopen+label%3Afabric+label%3Aminecraft-1.20.1).
Do not file Forge/NeoForge support requests against this fork.

## Standalone fire controls

The new [fire behavior](Features.md#standalone-fire) belongs to the **2.2.0**
release line, not the immutable 2.1.0 jar. If a carrier does not spread, check
server/common `fire.entity_spread`, `doFireTick`, and `mobGriefing` before
increasing probabilities. Sunlight-only or unknown/legacy saved burns are
intentionally excluded; extinguish and use a supported independent ignition.
Rain, water, powder snow, occupied air cells, absent fuel and unloaded neighbors
also prevent placement. Default trials are small; raising `randomTickSpeed`
does not accelerate them. Campfires/torches do not provide heat bonuses.

Torch consumption requires a successful direct main-hand hit on a supported
living target; projectiles, blocked hits and offhand torches do not qualify.
Soul torches deliberately use ordinary fire. Grass turning into dirt is
intentional surface consumption, not a restoration bug. Review
[settings and reload rules](Configuration.md#standalone-fire)
and record counted trials before reporting a probability defect.

## Before reporting

For this beta, a missing Moonlight error requires the Fabric
**1.20-2.12.11** library (or a deliberately verified compatible newer version),
not a second IW jar. Fabric API **0.92.12+1.20.1** also needs a recent Loader;
IW declares **0.16.14+**. Check the complete dependency message before assuming
the migration broke gameplay. Forge-only Quark integrations and the unfinished
season-temperature bridge are not offered by this Fabric build; existing
Fabric behavior never implemented that seasonal adjustment.

The **2.0.6-beta.1** isolated production smoke successfully loaded the IW EMI plugin with
EMI **1.1.24+1.20.1**. That is limited startup/reload evidence, not a verified fix
for every reproduction of [the EMI report](https://github.com/Atlasroar/Immersive-Weathering/issues/3).
It is not blanket gameplay verification for **2.1.0-beta.1**; record current-version
observations with the feature test sheet.
Developer `runClient` audits have a separate optional-JEI class-loading caveat;
see [development validation](../developers/Development.md).

1. Back up the affected world and reproduce in a disposable copy.
2. Record Minecraft, Fabric Loader, Fabric API, IW, Moonlight, and relevant
   compatibility-mod versions. Note singleplayer versus dedicated server.
3. Save the matching `latest.log`, crash report if present, configuration changes,
   datapacks, and a minimal numbered reproduction. Use a profiler for lag.
4. Reduce the mod set without modifying your only world copy.
5. Check existing issues and add evidence to a matching report rather than opening
   a duplicate. Remove credentials, server addresses, and other private data
   from logs before sharing.

Missing generated textures are not automatically Moonlight bugs. IW owns its
generation rules and may encounter unusual mod models or resource-pack layouts.
For technical diagnosis, see the [developer architecture map](../developers/Architecture.md).

## Standalone snow checks

No extra layers in ordinary snowfall is the default, not a failure. Compare
`/weather thunder` in a cold biome with a warm-rain control, then inspect the
[snow config and gamerule sentinel](Configuration.md#standalone-snow).
Check for roofs, block light above 11, and the column cap before increasing tick
speed. A modded fence, gate, slab, or waterlogged block is deliberately not a host.
Right-click a covered fence/wall with a shovel to retain it; mining the composite
breaks the host instead. Full plain snow (layer 8) is not replaced in place by the
partial-layer override. Source-water ice conversion requires falling snow, not
stationary layers adjacent to water.

Rendering uses ordinary multipart models with vanilla host textures and the
vanilla snow sprite, with no Indium/Kiwi requirement. Alternate resource packs
and Sodium are included in N10's combined row, reported Pass in the
[2.1.0-beta.2 snow results](../developers/Test-results.md).
The report did not itemize tested renderer/resource-pack combinations, so do not
assume arbitrary combinations are verified. A model bake or fixture pass alone
is not visual acceptance. N12-N14 remain unreported.
