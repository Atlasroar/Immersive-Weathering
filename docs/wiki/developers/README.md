# Developers: architecture and contribution

[Wiki home](../README.md) | [Players](../players/README.md) | **Developers**

| Guide | Contents |
| --- | --- |
| [Architecture map](Architecture.md) | Startup, source ownership, dispatch paths, and client/server boundaries |
| [Moonlight dependency](Moonlight.md) | Required APIs, feature consumers, and Fabric-only migration boundaries |
| [Development](Development.md) | Build, runtime setup, and validation |
| [Data and generated resources](Data-and-resources.md) | Growth/fluid definitions, predicates, packs, and reloads |
| [Adding features and fixing bugs](Contributing.md) | Relevant surfaces, state safety, and reproduction discipline |
| [Generated reference](Reference.md) | Actual versions, entrypoints, mixins, settings, API imports, and data inventory |
| [Wiki maintenance](Maintenance.md) | Documentation ownership, regeneration, checks, and limits |
| [Licensing and attribution](Licensing.md) | Upstream LGPLv3, incorporated GPLv3, and third-party notices |

The inherited architecture is multiloader; maintenance is Fabric-only. Document
the code that exists rather than describing the planned migration as finished.
This wiki is a navigation map, not a replacement for reading the implementation
and reproducing behavior.
