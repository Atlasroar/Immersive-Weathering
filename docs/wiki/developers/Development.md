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

## Release versioning

Mod versions follow [Semantic Versioning 2.0.0](https://semver.org/). The versioned
compatibility contract covers the mod ID and registered content IDs, documented
config keys and datapack schemas, and documented external entrypoints/integration
interfaces. Undocumented implementation classes/helpers are internal, regardless
of Java visibility. Prereleases remain experimental and require backed-up worlds.

Use `MAJOR.MINOR.PATCH`: incompatible contract changes require a major increment,
backward-compatible additions a minor increment, and compatible corrections a
patch increment. Successive candidates for an unreleased version increment the
prerelease identifier: `2.0.6-beta.2` to `2.0.6-beta.3`. Minecraft compatibility
belongs in `fabric.mod.json` and release notes, not before the mod's version.
Historical Minecraft-prefixed releases/tags must not be renamed or overwritten.
Published artifacts are immutable; every new artifact needs a new version.

Update `mod_version`, changelog, current installation/build examples and generated
reference together. Fabric source/artifact checks enforce SemVer syntax, numeric
prerelease identifiers without leading zeroes, and separation from the Minecraft
prefix. Publish beta tags as GitHub prereleases, with matching binary/source jars
and checksums pinned to the verified source commit. Replace only the previous IW
jar when deploying; keep a verified backup outside the profile's `mods` directory.

## Windows commands

From the repository root:

```powershell
java -version
.\gradlew.bat build
python tools\fabric.py --check --jar build\libs\immersive_weathering-2.0.6-beta.3.jar --sources build\libs\immersive_weathering-2.0.6-beta.3-sources.jar
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Loom supplies development launch tasks. The dedicated server may need its usual
EULA setup before starting. Dev launches are for local test instances, not your
only production world. The Fabric remapped artifact is produced under
`build\libs\immersive_weathering-2.0.6-beta.3.jar`; install that remapped
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
The original `1.20.1-2.0.6-beta.1` production smoke loaded client/server generated packs, **78** growth
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

## Paired-growth regression harness

```powershell
.\gradlew.bat pairedGrowthTest --console=plain
```

`build`/`check` includes this dependency-free Java 17 task. Its separate
`src/growthTest` source set executes the actual production
`ConfigurableBlockGrowth`, `BlockPair` codecs, vanilla survival/shape methods,
weighted selection, seeded replacement tests, and Moonlight area-loaded helper.
It deliberately does not inherit the performance harness's configurable-growth
substitute. No test framework or runtime dependency is added or packaged.

The harness decodes all **18 shipped paired entries** from the eight JSON files:
the three short-to-tall plants, the double flowers, and dark-forest
fence/carved-pumpkin/jack-o-lantern variants (including duplicate entries).
Each selected output is isolated at weight 1 to test placement, with self/adjacent
targeting from its definition. Positive cases use its shipped growth chance and
replacement probability; the three self-growths also use their decoded real
area conditions. Biome/owner dispatch and the grass-block definitions' area
eligibility are intentionally outside these isolated placement fixtures.

The **322 cases** cover six face directions while keeping `above_block` vertical,
both halves after immediate neighboring shape propagation and repeated six-face
updates, no transient double-plant removal, lower-then-upper write order,
blocked/wrong-fluid uppers, unsupported plants, upper/lower build boundaries,
waterlogged obstruction rejection, paired waterlogging and position-dependent
fence shapes, area/probability/chance vetoes, and exact world-RNG calls.
They include **96 single-output differential cases** against the original control
flow (six directions, self/adjacent, wet/dry, allowed/blocked, destroy on/off),
comparing world state, placement/destruction trace, and RNG trace.

The explicit fixture binds only the vanilla dirt and water tags it needs, uses
JDK 17 `Unsafe` to bypass `ServerLevel` construction, and overrides world storage,
build height, loaded-area status, and scheduled ticks. A `RandomBlockMatchTest`
subclass implements the accessor that Fabric normally mixes in. The fixture
propagates actual vanilla `updateShape` calls to neighbors; it is not the chunk
engine, a real resource reload, a loot/drop simulation, or an in-game tick loop.
Neither it nor a clean build establishes Fabric lifecycle or a passed manual G07
retest. See the [placement contract](Data-and-resources.md#paired-output-placement)
and [manual retest](../players/Feature-testing.md#beta3-paired-growth-retest).

## Growth performance harness

Run the standard-library Java 17 harness using the existing Loom classpath:

```powershell
.\gradlew.bat growthPerformanceTest --console=plain
.\gradlew.bat growthPerformanceBenchmark --console=plain
.\gradlew.bat growthPerformanceBenchmark -PgrowthNoEscapeAnalysis --console=plain
```

`build`/`check` includes the differential task, not the optional benchmark.
No test framework or dependency is added. Tests execute the actual production
`AreaCheck`, `BlockGrowthHandler`, `RandomizedBlockPositions`, and
`WeatheringHelper` classes, using real Mojang position enumeration, block states,
holder membership, `RuleTest`, and `RandomSource`. Baseline control flow in the
harness comes from IW commit `56ed3501`, and the unchanged public list helper is
the position-order oracle.

The initial Windows/Temurin 17.0.20.1 run passed **33182 geometry**, **10230 area**,
and **10 dispatch** differential cases. Test output counts deterministic geometry
comparisons (3000 seeded centers per radius, including signed-coordinate
extremes), randomized and Cartesian area
cases, and dispatch cases. Coverage includes shipped/unequal/zero radii,
negative/overflow/large-radius fallback, both cache budgets, y offsets, stochastic
predicates and their exact RNG draws, includes short-circuit order, -1/0/positive
amounts, early exits, retained immutable positions, nested calls, changed world
instances, config/conditional gating, empty groups, callback order, shared biome
memoization, original arguments, and callback-triggered registration replacement.
The result and complete world-query/membership/predicate trace must match.

Three test-only classes substitute config initialization, configurable growth's
possible-block set, and Moonlight reload-listener startup. A constructor-free
`ServerLevel` fixture overrides only world state/biome queries; constructor-free
conditional block/state fixtures test the dispatch gate without mutating vanilla
registries. These fixtures use JDK 17 `Unsafe` only in test code and are never
packaged. They do **not** verify Fabric lifecycle, codecs/resource reload, chunk
loading, a real dimension transition, world unload, or in-game growth behavior.
Registration replacement and separate world instances exercise those isolation
contracts without pretending to be an actual `/reload` or gameplay reproduction.

The optional benchmark uses `ThreadMXBean` allocated bytes and `nanoTime`, fixed
512 MiB heap, at least three workload-sized warmup passes and one second of
warmup per scenario, and seven alternating
baseline/optimized rounds. It reports medians in ns/op and B/op. Empty dispatch
and active dispatch use 1000000 operations per round; empty dispatch asserts zero
optimized allocation. Each of
the eight shipped radii uses 10000 operations per round for full scan and
first-query exit, cycling 1024 preallocated centers. Allocation must decrease
for empty dispatch and every area scenario. Active universal-only, specific-only,
and combined dispatch (with/without biome reads) are reported separately.
CPU timings and active-path allocations are observations, not noisy CI thresholds.
The fixture world is deliberately cheap and tracing is disabled during timing.
Measure on a quiet host and repeat runs; these numbers are neither whole-game
TPS/MSPT evidence nor a substitute for controlled Fabric gameplay comparison.

Illustrative seven-round medians from that host (November 1, 2026):

| Scenario | Baseline ns/op | Optimized ns/op | Baseline B/op | Optimized B/op |
| --- | ---: | ---: | ---: | ---: |
| Empty dispatch | 41.9 | 22.3 | 40 | 0 |
| `(2,2,2)` full scan | 6390.2 | 3693.4 | 5776 | 3688 |
| `(2,2,2)` first-query exit | 4208.5 | 1383.9 | 5776 | 712 |

Across eight shipped radii, full-scan time ratios were 0.556-0.592 and early-exit
ratios 0.319-0.373 (optimized/baseline); full-scan allocation decreased 34-49%
and early-exit allocation 83-90%. Active dispatch ratios were 0.670-0.990 in
this run: effectively unchanged or improved, rather than a promised active-path
speedup. JIT escape analysis can change absolute allocation counts between JVM
runs, including small active-path differences (one repeat measured 40 versus
48 B/op for universal-only biome-reading dispatch despite similar CPU time).
The optional `-PgrowthNoEscapeAnalysis` run disables that compiler optimization
for an allocation diagnostic; report it separately, not as normal runtime timing.
That diagnostic measured 64 to 0 B/op for empty dispatch, 5848 to 3720/744 B/op
for `(2,2,2)` full/early scans, and lower allocations in all six active cases.
These are local harness observations with the startup/world fixtures above,
not vendor benchmarks or verified in-game gains.
