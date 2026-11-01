# Development and validation

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Environment

Use Java 17 and the repository Gradle wrapper, not a guessed system Gradle.
The single root Fabric Loom 1.7.4 project uses official Mojang mappings.
Gradle 8.8 is pinned by the wrapper. Versions,
runtime dependencies, and the fixed Moonlight artifact are
recorded in the [generated reference](Reference.md).

`moonlight_file_id=5619072` pins Moonlight **1.20-2.12.11** on Fabric.
`moonlight_min_version` declares the runtime minimum. Update both deliberately
when changing the library and verify the artifact's own metadata and license.
Fabric API is **0.92.12+1.20.1**, Loader **0.16.14**, and the optional development
mods are EMI **1.1.24+1.20.1**, Mod Menu **7.2.2**, Cloth Config **11.1.136**.

## Windows commands

From the repository root:

```powershell
java -version
.\gradlew.bat build
python tools\fabric.py --check --jar build\libs\immersive_weathering-1.20.1-2.0.6-beta.1.jar --sources build\libs\immersive_weathering-1.20.1-2.0.6-beta.1-sources.jar
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Loom supplies development launch tasks. The dedicated server may need its usual
EULA setup before starting. Dev launches are for local test instances, not your
only production world. The Fabric remapped artifact is produced under
`build\libs\immersive_weathering-1.20.1-2.0.6-beta.1.jar`; install that remapped
jar, not `build\devlibs` output or the matching `-sources.jar`.
Both distribution jars include the unchanged `LICENSE`, `COPYING`, and
`ATTRIBUTIONS`. Dependencies are separate, never bundled or shaded.

For a narrower Java change, start with `.\gradlew.bat compileJava`.
Resource/packaging changes need the root build. A successful compile is not a
gameplay reproduction or dedicated-server class-loading check.

The [build workflow](../../../.github/workflows/build.yml) builds on maintained
Linux/Windows runners using Java 17, checks source/artifact Fabric-only contracts,
and uploads only the root remapped jar and sources jar.

Development client and server instances use `run\client` and `run\server`.
Review Mojang's EULA before accepting it in the disposable server instance.
Never copy development dependencies or launch into an external profile as part
of a build. Client rendering/gameplay still needs a real interactive check;
headless or EULA-blocked attempts must be reported as such.

With Moonlight **1.20-2.12.11** and EMI **1.1.24**, the development-only Moonlight
mixin audit can force-load EMI's optional JEMI implementation and fail on absent
JEI `IRecipeExtrasBuilder`. A failing `runClient` audit is not by itself a
production crash reproduction. Use an isolated production instance with the
remapped IW jar to distinguish this case; JEI is not an IW runtime requirement.
The beta's production smoke loaded client/server generated packs, **78** growth
definitions and **14** fluid generators, joined a disposable integrated world,
synced the common config, and completed the IW EMI plugin reload. Dedicated
server initialization was checked only up to the unaccepted EULA gate. Gameplay
interactions, existing-world upgrades, and modded tree/resource-pack combinations
still require user testing.

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
