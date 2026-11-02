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
and checksums pinned to the verified source commit. Stable tags omit the prerelease
flag and can be marked Latest; stable promotion does not manufacture missing
manual results. Replace only the previous IW
jar when deploying; keep a verified backup outside the profile's `mods` directory.

## Windows commands

From the repository root:

```powershell
java -version
.\gradlew.bat build
python tools\fabric.py --check --jar build\libs\immersive_weathering-2.2.0.jar --sources build\libs\immersive_weathering-2.2.0-sources.jar
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Loom supplies development launch tasks. The dedicated server may need its usual
EULA setup before starting. Dev launches are for local test instances, not your
only production world. The Fabric remapped artifact is produced under
`build\libs\immersive_weathering-2.2.0.jar`; install that remapped
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

## Fire mechanics harness

Run `.\gradlew.bat fireMechanicsTest` for dependency-free, production-executing
fire rules with Mojang 1.20.1 block/entity states and explicit bounded server
storage fixtures. It covers small default thresholds, source/temperature/cap
math, projectile ordering, interval staggering, loaded/border/height/rain/air/
fuel/survival gates, gamerules, known/unknown ignition NBT/reset, successful/
failed torch actions and item counts, grass odds/config updates and dirt
preservation. Structural ASM checks verify the exact vanilla descriptors and
successful-burn/direct-melee ordinals, including the retained soot hook.
It is wired into `check` and the root build alongside snow/growth checks.

The harness does **not** weave Mixins or emulate a running chunk engine,
networking, damage protection from every mod, rendering or client/server
gameplay. Its test-only bridge supplies access to private vanilla fire queries
and applies production grass rules; it is not shipped in the jar. Development
server `runServer --args="--initSettings nogui"` can check initialization and
Mixin loading without accepting the EULA or opening a world. Neither check
replaces the [manual F01-F06 run checklist](../players/Feature-testing.md#standalone-fire);
the tester's broad 2.2.0 pass does not itemize its variants.
No external profile/world or published 2.1.0 artifact is changed by these tasks.

## Snow mechanics harness

```powershell
python tools\snow_resources.py
python tools\snow_resources.py --check
.\gradlew.bat snowMechanicsTest --console=plain
```

`build`/`check` includes this separate Java 17 source set, without adding a test
framework or packaged dependency. Production `SnowHosts`, `SnowCoveredBlock`,
`SnowPhysics`, `SnowWeather`, falling-entity save/tick methods, placement injection
bodies and Mojang block states/shapes/loot code are exercised. The source set
substitutes common-config initialization, the falling entity's registration
supplier, and the platform registration boundary. Test `ModBlocks` constructs
actual production weeds/dune grass/frosty plants, queues snow registration, and
rejects early supplier resolution. A test Moonlight `RegHelper` captures and
asserts weed fire registration instead of requiring Fabric fire mixins.
Reflection stages test registry registration before freeze, rather than
unfreezing a live game registry. Raw and covered test blocks initialize native
state caches; these fixture classes are not distributed.

Coverage includes every dry state/layer of all 72 hosts, exact NBT round trips,
native collision unions, rotations, vanilla-model reference resolution, raw fence
connectivity, wet/unsupported host rejection, placement-policy 1-8 boundaries and
toggles, mixed double halves, scheduled orphan resolution for both halves at
every layer count, unsupported-host detachment, falling NBT, all
incoming/target layer counts, overflow item counts, actual source/flow/waterlogged
ray contacts, vanilla host/snow loot composition, melt/clone/piston-reaction
contracts, and cold/warm/light/heightmap/load/config/weather/cap controls.
Weather fixtures use Mojang's actual heightmap opacity predicate and verify all
72 hosts reach eight without a second cell. Gamerules 0,1,2,8,24 and IW caps 1,3,8
are crossed with clear/rain/thunder and ordinary-weather opt-in.
The covered-plant replacement matrix invokes actual wrapper replaceability and
the placement injection for every plant state, layers 1-8, both independent
settings, same/different supported hosts, solid/chest replacements, snow items,
non-block items, wet/failed placements, negative fence/wall controls, and
upper-half preservation through the normal double-plant placement callback.
Snow-item additions invoke both placement hooks in their real order to catch
layer-count resets. New controls cover native weed aging/bonemeal, sapling stage
progression, petal stacking, raw animation/contact forwarding, identity-hook
bodies and failed/successful multi-sapling restoration probes with independent
cover counts. Direct calls into the actual client color-state selector cover
grass/fern frost matching and unchanged hosts at all eight layer counts; the
existing melt checks retain the original normal plants. These are selection/state
checks, not a GPU render or full tint-provider invocation.
The current harness run passed **176,665 cases**.

The explicit fixture uses `Unsafe` to bypass server/level constructors, with
in-memory storage, a supplied vanilla loot-table registry, seeded RNG, native
heightmap predicates, and captured entity/item additions. Falling-contact probes
move by an explicit fixture vector/on-ground flag, not the chunk collision engine.
Injection bodies are invoked directly where needed: these checks are not a
Mixin-loader, networking, chunk-save, piston-engine, explosion-engine, permissions,
or real rendering test. Block-state and falling-entity NBT round trips are not a
whole-world save/reload claim. The test-only entity type can log its missing data
fixer; it is not shipped.

For the beta.2 plant follow-up, a real Fabric dedicated launch completed mod
initialization/mixin loading and stopped at the **unaccepted EULA gate**.
The generated common config contained all six snow keys with unchanged defaults.
This launch first exposed early IW supplier resolution, which was corrected and
added to the deferred-registration fixture before rerunning successfully.
An isolated client then completed resource/model baking without snow-model or
injection errors, using a session-only Gradle init script to exclude optional
EMI from `modLocalRuntime` for the documented Moonlight/JEMI issue. Repository
dependencies and external profiles were not changed. The client was stopped
after the resource-load check; no agent-controlled gameplay, visual/tint,
multiplayer or whole-world reload acceptance is claimed.
The client bake preceded the final mixing-disabled petal-stack refinement;
the final build, replacement matrix and dedicated EULA-gate launch were rerun.

Python checks include the existing 26 wiki/Fabric-tool tests and nine snow-resource
tests, including English display-name coverage for all registered snow hosts.
The artifact guard additionally verifies every generated composite
blockstate/model/tag is packaged byte-for-byte, retaining all prior license,
metadata, side/mixin/refmap and dependency checks.

For the original **beta.1** feature candidate, an isolated development server initialized Fabric and all snow
mixins/registrations up to the **unaccepted EULA gate**; no dedicated world was
started. Default `runClient` reproduced the documented Moonlight/optional-EMI
JEMI audit failure before window creation. A session-only Gradle init script
excluding EMI from `modLocalRuntime` (not compile-time API or repository
dependencies) allowed a real client resource bake and integrated-world startup,
with config sync and 78 growth/14 fluid-generator definitions. Model baking
caught bamboo's directional side-model naming, subsequently corrected in the
generator. Observed player actions during the development launches are not an agent-controlled
acceptance reproduction. On November 1, after the 2.1.0-beta.2 deployment, the
tester reported **N01-N11 Pass**, including the combined visual/synchronization
row. These are user-reported results without a new per-control or mod/resource-pack
inventory, not independent engine acceptance. **N12-N14 remain unreported** in the
[manual sheet](../players/Feature-testing.md#standalone-snow-candidate-retest).
Do not modify an external profile or accept an EULA to make a smoke command pass.

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
