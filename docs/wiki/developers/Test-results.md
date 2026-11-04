# Test results

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

Use the [in-game test sheet](Feature-testing.md) for version-neutral replication
steps. This page records manual evidence, not automated build totals. Results
apply only to the listed round, artifact and reported scope; they do not
automatically transfer to a later release or every variant of a combined case.
Only the tables below record execution status and outcomes.

## Summary

Counts are per test ID, not per control, block variant or observed attempt.
The six SB subcases are counted separately from the 81 general feature cases.
Mixed is separate from both Pass and Fail; Not observed, Not reported and
Not run have different meanings. There is deliberately no aggregate pass
percentage that treats these different historical rounds as current acceptance.

| Result | Test IDs | Meaning |
| --- | ---: | --- |
| Pass | 82 | User explicitly reported the case or group as passing, subject to its coverage notes. |
| Fail | 1 | User reported a failing case. |
| Mixed | 1 | Different subcases have different outcomes. |
| Not observed | 5 | The expected event was not observed; this alone does not establish failure. |
| Not applicable | 2 | Tester marked the setup inapplicable; no pass is inferred. |
| Not reported | 18 | No individual verdict was supplied. |
| Not run | 28 | No manual execution result is recorded for these checklist cases. |
| **Total** | **137** | Historical per-ID records, not current-release certification. |

## Recorded rounds and evidence limits

| Round / subject | Recorded evidence and limits |
| --- | --- |
| 2026-11-01, 2.0.6-beta.2 general features | 81 main cases: 58 Pass, one Fail, one Mixed, five Not observed, one Not applicable, 15 Not reported. W01/W02 explicitly covered all listed blocks. Exact effective gamerules, durations, counts, seed, configs, logs, screenshots and a fresh full mod inventory were not supplied. Accelerated random ticking was the planned method, not measured rate evidence. |
| Stone-brick subcases, same round | SB01-SB06 Pass, six additional subcases. The user explicitly authorized closing [#11](https://github.com/Atlasroar/Immersive-Weathering/issues/11); no code fix or equality of spread rates is inferred. |
| W09/W10 source qualification | Tester could not find steel wool and attributed its behavior to axes. The reported outcomes are retained, but [ModItems](../../../src/main/java/com/ordana/immersive_weathering/reg/ModItems.java) still registers steel wool and [ModEvents](../../../src/main/java/com/ordana/immersive_weathering/events/ModEvents.java) retains its interaction path; removal/replacement is not established by that report. |
| 2026-11-01, 2.1.0-beta.1 follow-up | Broad jar-working report also identified flowers replacing snow and requested shared weeds, more dry plants, common config and frosty grass. It was not an itemized N-case pass. |
| 2026-11-01, 2.1.0-beta.2 snow | N01-N11 Pass; N12-N14 Not reported. No new complete per-control/config/mod inventory. Later short-fern appearance in stable 2.1.0 is not certified by the earlier report. |
| Paired-growth follow-up | Original G07 Fail remains historical. The implementation and deterministic fixture changes do not create an in-game verdict. Later N11 Pass is broad and does not itemize each grass/fern/seagrass transition. |
| 2026-11-02, 2.2.0 fire | User reported the deployed features "work as expected". This broad Pass is not six itemized F01-F06 results; variants, trial counts, negative controls and multiplayer were not specified. |
| Sandstorms, introduced in 2.3.0 | ST01-ST08 Not run; old snow/fire observations do not verify storms. |
| 2026-11-02, 2.4.0 sand/thunder | SP01-SP07 Pass; SP08 Not applicable. These are user-reported test-ID verdicts, not agent reproductions or an inventory of every host/configuration variant. No reason for SP08 was supplied. |
| Freezing wind/snowstorms, introduced in 2.5.0 | FW01-FW08 Not run. Release, deployment, builds, native fixtures and init-only startup do not establish in-world visuals, audio, gameplay, multiplayer or optional-mod coexistence. |
| Shared mineral-pile plants, introduced in 2.6.0 | MP01-MP06 Not run. Automated state/model/bonemeal/lifecycle assertions and init-only Mixin audits do not certify real rendering, AI, gameplay, multiplayer or optional-mod coexistence. Earlier SP/N passes cover only their original artifact/scope. |

### Documented sand/thunder round setup

| Setup field | 2.4.0 round record |
| --- | --- |
| Date and platform | 2026-11-02; Minecraft 1.20.1, Fabric only |
| Artifact | Release tag `2.4.0`, matching merged source and published `immersive_weathering-2.4.0.jar`; compare the release's `SHA256SUMS.txt` |
| Deployment target | Modrinth profile `Fabric Test Enviorment 1.20.1`; one active IW binary, never sources/dev jars |
| Documented dependencies | Fabric API 0.92.12+1.20.1, Moonlight 1.20-2.16.38B, Cloth Config 11.1.136, EMI 1.1.24+1.20.1, Mod Menu 7.2.2; actual Loader at launch was to be recorded |
| Upgrade controls | Worlds, other mods and configs retained; saved weather cap 1 was not silently migrated to the fresh default 8 |
| Evidence limit | Deployment/setup record, not a complete tester-supplied log or per-variant inventory |

## Per-test records

Find the matching test ID in the [replication sheet](Feature-testing.md).
An introduction/checklist version with no test date is not a tested artifact.

| Test | Result | Report date | Artifact / checklist | Coverage notes |
| --- | --- | --- | --- | --- |
| C01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C06 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C07 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C08 | Mixed | 2026-11-01 | 2.0.6-beta.2 | Support-loss falling passed; vibration-triggered falling failed. Event/distance/config cause not isolated. |
| C09 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C10 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C11 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C12 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| C13 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| F01 | Not run | - | 2.2.0 checklist | Broad fire-feature report is separate; no itemized F-case verdict. |
| F02 | Not run | - | 2.2.0 checklist | Broad fire-feature report is separate; no itemized F-case verdict. |
| F03 | Not run | - | 2.2.0 checklist | Broad fire-feature report is separate; no itemized F-case verdict. |
| F04 | Not run | - | 2.2.0 checklist | Broad fire-feature report is separate; no itemized F-case verdict. |
| F05 | Not run | - | 2.2.0 checklist | Broad fire-feature report is separate; no itemized F-case verdict. |
| F06 | Not run | - | 2.2.0 checklist | Broad fire-feature report is separate; no itemized F-case verdict. |
| FW01 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW02 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW03 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW04 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW05 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW06 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW07 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| FW08 | Not run | - | 2.5.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| G01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G06 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G07 | Fail | 2026-11-01 | 2.0.6-beta.2 | Original double-tall growth failure; no transition-by-transition breakdown. Later N11 is a broad follow-up, not a replacement verdict. |
| G08 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G09 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G10 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| G11 | Not observed | 2026-11-01 | 2.0.6-beta.2 | No positive observation; not a demonstrated failure. |
| G12 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G13 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| G14 | Not observed | 2026-11-01 | 2.0.6-beta.2 | No positive observation; not a demonstrated failure. |
| H01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| H02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| H03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| H04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| H05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| H06 | Not observed | 2026-11-01 | 2.0.6-beta.2 | No positive observation; not a demonstrated failure. |
| H07 | Not observed | 2026-11-01 | 2.0.6-beta.2 | No positive observation; not a demonstrated failure. |
| H08 | Not observed | 2026-11-01 | 2.0.6-beta.2 | No positive observation; not a demonstrated failure. |
| H09 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| H10 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N01 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N02 | Pass | 2026-11-01 | 2.1.0-beta.2 | Reported round predates the later short-fern appearance; that appearance needs a separate retest. |
| N03 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N04 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N05 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N06 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N07 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N08 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N09 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N10 | Pass | 2026-11-01 | 2.1.0-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| N11 | Pass | 2026-11-01 | 2.1.0-beta.2 | Includes paired-growth regression controls, without separately itemized grass/fern/seagrass transitions. |
| N12 | Not reported | 2026-11-01 | 2.1.0-beta.2 | Expanded dry-plant and later frosty-fern appearance controls need their own observations. |
| N13 | Not reported | 2026-11-01 | 2.1.0-beta.2 | No per-case report supplied. |
| N14 | Not reported | 2026-11-01 | 2.1.0-beta.2 | No per-case report supplied. |
| Q01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| Q02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| Q03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| Q04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| Q05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| Q06 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| Q07 | Not applicable | 2026-11-01 | 2.0.6-beta.2 | Setup not applicable in the reported round; not a pass. |
| Q08 | Pass | 2026-11-01 | 2.0.6-beta.2 | Broad result does not establish individual optional-mod combinations. |
| Q09 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| R01 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R02 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R03 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R04 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R05 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R06 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R07 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R08 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R09 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R10 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R11 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R12 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R13 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| R14 | Not reported | 2026-11-01 | 2.0.6-beta.2 | No per-case report supplied. |
| SB01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| SB02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| SB03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| SB04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| SB05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| SB06 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| SP01 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP02 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP03 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP04 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP05 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP06 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP07 | Pass | 2026-11-02 | 2.4.0 | User-reported test-ID result; individual controls were not itemized. |
| SP08 | Not applicable | 2026-11-02 | 2.4.0 | No reason supplied; not a reload, coexistence or multiplayer pass. |
| ST01 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST02 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST03 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST04 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST05 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST06 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST07 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| ST08 | Not run | - | 2.3.0 checklist | No manual case result recorded; introduction version is not a tested round. |
| T01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T06 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T07 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T08 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T09 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T10 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| T11 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W01 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W02 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W03 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W04 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W05 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W06 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W07 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W08 | Pass | 2026-11-01 | 2.0.6-beta.2 | User-reported test-ID result; individual controls were not itemized. |
| W09 | Pass | 2026-11-01 | 2.0.6-beta.2 | Axe report retained; steel-wool acquisition/use was not established. |
| W10 | Pass | 2026-11-01 | 2.0.6-beta.2 | Wax/axe report retained; source still registers steel wool; no proof of its removal. |

### Shared mineral-pile coverage

| Test ID | Result | Date | Artifact / round | Evidence / qualification |
| --- | --- | --- | --- | --- |
| MP01 | Not run | - | 2.6.0 checklist | Expanded inventory/placement/state/rendering; no manual evidence. |
| MP02 | Not run | - | 2.6.0 checklist | Native growth/bonemeal/head-body transitions; no manual evidence. |
| MP03 | Not run | - | 2.6.0 checklist | Harvest/contact/dripleaf/AI interactions; no manual evidence. |
| MP04 | Not run | - | 2.6.0 checklist | Cultivated soils, support loss and falling; no manual evidence. |
| MP05 | Not run | - | 2.6.0 checklist | Shoveling, mature/paired loot and replacement; no manual evidence. |
| MP06 | Not run | - | 2.6.0 checklist | Weather/config/reload/network/coexistence; no manual evidence. |

## Recording another round

Use the [result record](Feature-testing.md#return-results-by-test-id) for each
case and variant. Add dated evidence without overwriting an earlier round;
update the per-test record and summary together when a new explicit verdict
supersedes a current entry. Preserve qualifications, conflicting observations
and missing setup information. Do not convert a broad feature report into
individual passes or count an automated assertion as an in-game test.
