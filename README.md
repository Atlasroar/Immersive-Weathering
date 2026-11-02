# Immersive Weathering

A Minecraft mod adding natural weathering, leaf piles, bark, soil variants,
frost, icicles, and interactions that make the world change over time.

This fork targets **Minecraft 1.20.1 on Fabric only**. Version
**2.3.0** is the stable release target, with a single root
Fabric Loom project and no IW-owned loader bridges. Moonlight Lib remains a
required dependency for registration, configuration, generated packs, and events.

The default development branch is `1.20.1-fabric`. Historical
branches were retired after preserving their tips as `archive/2026-11-01/*` tags;
fully merged feature branches are removed after landing. See the
[branch policy](docs/wiki/developers/Architecture.md).

## Wiki

| Players: features and instructions | Developers: architecture and contribution |
| --- | --- |
| [Open the player guide](docs/wiki/players/README.md) | [Open the developer guide](docs/wiki/developers/README.md) |
| Installation, gameplay, settings, and troubleshooting | Code map, Moonlight dependencies, build setup, and maintenance |

The [wiki home](docs/wiki/README.md) connects both sections. The wiki lives in this
repository so code and documentation can be reviewed and versioned together.
It is also published to the [GitHub Wiki](https://github.com/Atlasroar/Immersive-Weathering/wiki);
see the [publication process](docs/wiki/developers/Maintenance.md) to refresh that mirror.

## Development

Use Java 17 and the checked-in Gradle wrapper. See the
[development guide](docs/wiki/developers/Development.md) and
[contribution instructions](CONTRIBUTING.md).

Build with `.\gradlew.bat build`. The installable jar is
`build\libs\immersive_weathering-2.3.0.jar`; the matching
`-sources.jar` is for source distribution, not installation.

## Installation

For published downloads, use the repository's
[Releases](https://github.com/Atlasroar/Immersive-Weathering/releases).
The source version alone does not establish that its release has been published.
Use a backed-up or disposable Minecraft **1.20.1** instance with Java 17,
Fabric Loader **0.16.14+**, Fabric API **0.92.12+1.20.1**, and the Fabric
Moonlight Lib **1.20-2.12.11** build (CurseForge file **5619072**).
Install only the remapped IW jar, replacing any older IW jar rather than keeping
both. Dependencies are not bundled. Optional client integrations are EMI,
Mod Menu, and Cloth Config; see [installation](docs/wiki/players/Getting-started.md).
This release includes standalone snow mixing, falling, storm accumulation, and
partial-layer replacement without SnowRealMagic or Kiwi. See the
[snow controls](docs/wiki/players/Configuration.md#standalone-snow).
Snow sharing supports small flowers, saplings, mushrooms,
dead bushes and IW weeds/dune grass/frosty plants. Covered short grass and ferns
look frosty while retaining their normal underlying states. All six common snow
settings have names and explanatory tooltips.
Removing the cover restores the original normal grass or fern.
This release also includes the paired plant placement correction from
2.0.6-beta.3 (grass, fern, seagrass and double flowers), without changing growth
chances. N01-N11 were reported Pass on 2.1.0-beta.2; N12-N14 and the final fern
appearance remain unreported. Stable release status does not imply that every
catalogued gameplay report is fixed; see the [test ledger](docs/wiki/players/Feature-testing.md).

**New in 2.3.0:** standalone sandstorms with imported Scorchful dust sprites and
nine wind sounds, guarded weather/biome-blended fog, exposed-entity slowdown
and existing IW sand/red-sand layer deposition. No new dependencies, thirst,
temperature or cauldron changes. Default slowdown is 30%; default pile cap is
one layer, configurable to eight without trimming existing piles. Client effects
have independent controls, and new IW storms yield to installed Scorchful.
See [sandstorm settings](docs/wiki/players/Configuration.md#standalone-sandstorms),
[asset licensing/provenance](docs/wiki/developers/Licensing.md#scorchful-sandstorm-adaptation)
and the [ST01-ST08 runnable checklist](docs/wiki/players/Feature-testing.md#standalone-sandstorms).
All manual storm cases remain **Not run**; builds and isolated resource loading
are not in-world visual/audio or multiplayer acceptance.

**Introduced in 2.2.0:** standalone fire mechanics without another fire mod or library.
Known non-sun burning entities can ignite nearby flammable blocks with a small
configurable chance; projectiles default to one sixteenth the ordinary rate.
Bounded fire/soul-fire/magma/lava heat and hot-biome bonuses increase that chance.
Successful direct main-hand Torch or Soul Torch attacks use ordinary four-second
burning and consume one Survival item. Grass Blocks burn into Dirt without
removing the ground. Environmental changes obey `doFireTick`, and non-player
carriers respect `mobGriefing` with a player-projectile exemption.
See [fire controls](docs/wiki/players/Configuration.md#standalone-fire) and the
[F01-F06 runnable checklist](docs/wiki/players/Feature-testing.md#standalone-fire).
The tester reported the deployed 2.2.0 features work as expected; individual
F01-F06 variants remain unreported. Automated checks are not gameplay acceptance.
The immutable 2.1.0 release and historical results are unchanged.

## Versioning

Mod releases follow [Semantic Versioning 2.0.0](https://semver.org/):
`MAJOR.MINOR.PATCH`, with prereleases such as `2.1.0-beta.1`. Minecraft
compatibility is declared separately in Fabric metadata and release notes.
Published versions and assets are immutable; the historical
`1.20.1-2.0.6-beta.1` release retains its original name and contents.
See the [versioned compatibility contract](docs/wiki/developers/Development.md#release-versioning).

Documentation checks require Python 3.10 or newer, with no third-party packages:

```powershell
python tools\wiki.py --write
python tools\wiki.py --check
python -m unittest discover -s tools -p "test_*.py"
```

## Origin and license

Based on [Silversmith-Mods/Immersive-Weathering](https://github.com/Silversmith-Mods/Immersive-Weathering),
originally maintained by the authors credited in the mod metadata.

The upstream **GNU LGPLv3** license is retained verbatim in [LICENSE](LICENSE).
Its incorporated GNU GPLv3 terms are provided in [COPYING](COPYING).
Third-party sound attribution remains in [ATTRIBUTIONS](ATTRIBUTIONS).
See [licensing and attribution](docs/wiki/developers/Licensing.md).
