# Immersive Weathering

A Minecraft mod adding natural weathering, leaf piles, bark, soil variants,
frost, icicles, and interactions that make the world change over time.

This fork's maintenance target is **Minecraft 1.20.1 on Fabric**. The inherited
`common`, `fabric`, and `forge` projects are still present; removing multiloader
support is planned, not completed. Moonlight Lib remains a required dependency.

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
