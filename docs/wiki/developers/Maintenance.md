# Wiki maintenance

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Ownership

The person changing implementation owns the matching wiki update. Review both in
the same pull request. The repository copy is the source of truth; the GitHub
Wiki tab is a generated publication, not an independently maintained copy.
Wiki edits are not automatically synchronized back to the repository.

Keep the two audience hubs distinct:

- **Players:** available features, practical steps, configuration, safety,
  compatibility limitations, and verified workarounds.
- **Developers:** execution paths, contracts, source ownership, dependency roles,
  build/reproduction steps, and migration implications.

Every wiki page must link to wiki home and both audience hubs, except that a hub
labels its own audience rather than linking to itself.

The audience hub tables define the exported sidebar reading order. Keep player
pages ordered Getting started, Features and interactions, Configuration, then
Troubleshooting. Test instructions and results belong to the developer guide.
New pages not yet linked in a hub are appended in deterministic filename order
so they cannot silently disappear from the sidebar.
The exported former player test-sheet URL remains a short landing page for
links in published release notes; it is not listed as a player sidebar page.

## Manual testing pages

Keep [replication instructions](Feature-testing.md) reusable and independent
of a mod release. Preserve test IDs, setup steps, expected behavior and negative
controls, but do not add verdict columns or release-round narratives there.
Put tested artifacts, dates, outcomes and coverage limitations in the
[results tables](Test-results.md). Update summary counts with the per-ID records.
Never turn a broad feature report into itemized passes, erase an older failure
without recording its follow-up, or equate automated fixtures with gameplay.

## Update checklist

| Change | Documentation that needs attention |
| --- | --- |
| Gameplay/tool behavior | Player features/instructions, troubleshooting, developer interaction contracts |
| Bug fixed or reproduced | Linked issue evidence and player limitation/workaround; do not mark unrelated symptoms fixed |
| Config/default/bounds | Player settings advice, developer synchronization/migration notes, generated reference |
| Versions/dependencies | Installation/build advice, Moonlight contracts, generated reference |
| Source paths/entrypoints/mixins | Architecture links, registered-source reference, execution-path explanation |
| Data or generated assets | Datapack examples, generator/placement contracts, reference inventory |
| Fabric-only migration | Scope statements, module/build instructions, supported integrations; only describe completed steps as completed |
| Licenses/assets | Notices, attribution, and licensing guide |

## Automated reference and checks

`python tools\wiki.py --write` generates `Reference.md` directly from repository
inputs. `--check` compares that output without modifying it, validates relative
Markdown destinations in the maintained documentation, and checks the audience
navigation. The script also verifies that source config suppliers were captured
instead of silently omitting unfamiliar definitions.

The documentation workflow runs those checks plus standard-library unit tests on
every push/pull request. There is no date stamp or network/API dependency in the
generated page; identical source produces identical output.
Both build and documentation workflows also accept manual `workflow_dispatch`
runs for an explicit branch when automatic events do not produce checks.
Inventories sort case-sensitively by POSIX-rendered path, independent of the
host's Windows/Linux path-comparison rules.

`python tools\fabric.py --check` rejects IW-owned platform residue in active
source/build surfaces, legacy source modules, and bundled historical binaries.
After building, add `--jar` and `--sources` paths to verify names, expanded Fabric
metadata, mixin membership, remapped access widening, forbidden classes/metadata,
and byte-identical license notices. Historical prose and retained Moonlight API
names are intentionally not rejected.

## Limits

Automation does not determine whether prose is true, whether a new feature needs
instructions, whether an external issue is closed, or whether Minecraft behavior
works. Reviewers must evaluate semantic documentation impact. The PR template
requires that impact to be addressed even when no prose update is necessary.

The troubleshooting table is a dated initial report catalog with live links,
not a claim that all reports remain open forever. Add verified fix/workaround
information when working on those systems. Do not schedule an agent to edit
documentation unattended or invent runtime verification.

## Publish to the GitHub Wiki

After the matching repository documentation has been committed and pushed:

1. Clone or fast-forward the repository's `.wiki.git` checkout. Use a separate
   directory, not the source wiki directory.
2. Run reference/link checks and export with the corresponding published commit:

   ```powershell
   python tools\wiki.py --check
   python tools\wiki.py --export "C:\path\to\wiki-checkout" --repo-url https://github.com/Atlasroar/Immersive-Weathering --ref PUBLISHED_COMMIT_SHA
   ```

3. Review the Wiki checkout's diff, commit it, and push its existing branch.
   Authenticate using the normal GitHub Git credential setup; do not put tokens
   in commands, remote URLs, or documentation.
4. Verify the remote commit and the published Home, Players, and Developers pages.

The exporter flattens source pages to audience-prefixed Wiki names, rewrites
internal navigation to Wiki URLs, and points source links at the specified
repository commit. It generates the sidebar/footer and records its owned files
in `.wiki-published-pages.json`. It preserves unrelated pages and removes only
obsolete pages from that inventory. Review changes rather than force-pushing
over concurrent Wiki edits.
The source root is resolved before collecting pages so relative/short Windows
paths and linked destinations use the same canonical paths and reading order.

Republish after documentation changes intended for public readers. CI checks the
repository source but does not have Wiki publication credentials or automatically
push the mirror. A source update is not a completed public Wiki update until the
mirror has been exported, reviewed, pushed, and verified.
