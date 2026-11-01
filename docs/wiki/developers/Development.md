# Development and validation

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Environment

Use Java 17 and the repository Gradle wrapper, not a guessed system Gradle.
Minecraft mappings combine official Mojang mappings with Parchment. Versions,
project inclusion, runtime dependencies, and the fixed Moonlight artifact are
recorded in the [generated reference](Reference.md).

The `moonlight_version` property is not necessarily the resolved build dependency:
common/Fabric currently declare a fixed Curse Maven artifact, while Fabric
metadata declares its own minimum. Update the appropriate surfaces together when
changing dependencies; do not assume editing one property changes all of them.

## Windows commands

From the repository root:

```powershell
java -version
.\gradlew.bat :fabric:build
.\gradlew.bat :fabric:runClient
.\gradlew.bat :fabric:runServer
```

Loom supplies development launch tasks. The dedicated server may need its usual
EULA setup before starting. Dev launches are for local test instances, not your
only production world. The Fabric remapped artifact is produced under
`fabric\build\libs`; use the remapped Fabric jar rather than a dev/common jar.

For a narrower Java change, start with `.\gradlew.bat :fabric:compileJava`.
Resource/packaging changes need the Fabric build. A successful compile is not a
gameplay reproduction or dedicated-server class-loading check.

The inherited [build workflow](../../../.github/workflows/build.yml) still runs
the root `build` task. This wiki does not claim that CI or project inclusion has
already been converted to Fabric-only.

## Verification by change type

| Change | Minimum relevant checks |
| --- | --- |
| Java behavior | Targeted compile/build plus a Fabric reproduction and unchanged comparison behavior |
| Mixins/registration | Client launch and dedicated-server launch; verify injection/registry errors and actual registered manifest entries |
| Datapacks | Fabric launch, `/reload`, parse logs, target behavior, and override/blacklist behavior |
| Generated models/textures | Pack reload, vanilla and modded types, alternate resource pack, useful errors, and affected particle rendering |
| Networking/config | Matching client/server setup, enabled/disabled settings, restart/reload, delivery and side safety |
| Documentation | Reference freshness, local links/navigation, tool tests, and manual prose/example review |

## Wiki checks

Python 3.10+ and its standard library are sufficient:

```powershell
python tools\wiki.py --write
python tools\wiki.py --check
python -m unittest discover -s tools -p "test_*.py"
```

The [documentation workflow](../../../.github/workflows/docs.yml) runs the checks
on pushes and pull requests independently of the Minecraft build. It does not
launch Minecraft, validate external URLs, or prove narrative/gameplay accuracy.
