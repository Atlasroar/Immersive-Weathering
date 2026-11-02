# Immersive Weathering

A Minecraft mod adding natural weathering, leaf piles, bark, soil variants,
frost, icicles, and interactions that make the world change over time.

This fork targets **Minecraft 1.20.1 on Fabric only**. Version
**2.1.0-beta.2** is an unreleased snow/plant follow-up, with a single root
Fabric Loom project and no IW-owned loader bridges. Moonlight Lib remains a
required dependency for registration, configuration, generated packs, and events.

The default development branch is `atlasroar-fabric-1-20-1-bugs`. Historical
branches were retired after preserving their tips as `archive/2026-11-01/*` tags;
see the [branch policy](docs/wiki/developers/Architecture.md).

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
`build\libs\immersive_weathering-2.1.0-beta.2.jar`; the matching
`-sources.jar` is for source distribution, not installation.

## Beta installation

Use a backed-up or disposable Minecraft **1.20.1** instance with Java 17,
Fabric Loader **0.16.14+**, Fabric API **0.92.12+1.20.1**, and the Fabric
Moonlight Lib **1.20-2.12.11** build (CurseForge file **5619072**).
Install only the remapped IW jar, replacing any older IW jar rather than keeping
both. Dependencies are not bundled. Optional client integrations are EMI,
Mod Menu, and Cloth Config; see [installation](docs/wiki/players/Getting-started.md).
The published beta.1 adds standalone snow mixing, falling, storm accumulation, and
partial-layer replacement without SnowRealMagic or Kiwi. See the
[snow controls](docs/wiki/players/Configuration.md#standalone-snow).
The unreleased follow-up expands sharing to small flowers, saplings, mushrooms,
dead bushes and IW weeds/dune grass/frosty plants. Covered short grass looks
frosty while retaining its normal underlying state. All six common snow
settings have names and explanatory tooltips.
It also retains beta.3's paired plant placement correction (grass, fern, seagrass and double flowers)
without changing growth chances. A fresh in-game G07 retest is still required;
this is not a blanket fix for the existing gameplay issue catalog.

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
