# Immersive Weathering

A Minecraft mod that makes the world change over time through natural
weathering, changing ground cover, plant growth and weather interactions.
This fork maintains **Minecraft 1.20.1 on Fabric**, with Moonlight Lib required.

## Features

- Moss, cracks, rust, wax, charred wood and tools for restoring supported blocks.
- Bark, leaf piles, ivy, soil variants and biome-aware plant growth.
- Snow and sand layers, including supported fence and plant sharing.
- Frost, thin ice, icicles and configurable fire interactions.
- Sandstorms, freezing wind and cold-biome thunder snowstorms, without a player-temperature system.

[Download releases](https://github.com/Atlasroar/Immersive-Weathering/releases)
or explore the guides for behavior, dependencies and compatibility limits.

## Wiki

| Players | Developers |
| --- | --- |
| [Player guide](https://github.com/Atlasroar/Immersive-Weathering/wiki/Players) | [Developer guide](https://github.com/Atlasroar/Immersive-Weathering/wiki/Developers) |
| Installation, features, configuration and troubleshooting | Architecture, build setup, reproducible tests and recorded results |

Documentation is maintained in [docs/wiki](docs/wiki/README.md) and published
to the GitHub Wiki from matching source commits.

## Development environment

Use **Java 17**, the checked-in Gradle wrapper and the `1.20.1-fabric` branch.
Sources and resources live in the single root Fabric Loom project under `src/main`.

```powershell
.\gradlew.bat build
```

Install the remapped jar from `build\libs`, not the sources jar.
Documentation tooling requires **Python 3.10+**, without third-party packages:

```powershell
python tools\wiki.py --check
python -m unittest discover -s tools -p "test_*.py"
```

See the [development guide](docs/wiki/developers/Development.md) and
[CONTRIBUTING.md](CONTRIBUTING.md) for dependency versions and contribution checks.

Based on [Silversmith-Mods/Immersive-Weathering](https://github.com/Silversmith-Mods/Immersive-Weathering).
The upstream [LGPLv3 license](LICENSE), incorporated [GPLv3 terms](COPYING) and
[attributions](ATTRIBUTIONS) are preserved; see the [licensing guide](docs/wiki/developers/Licensing.md).
