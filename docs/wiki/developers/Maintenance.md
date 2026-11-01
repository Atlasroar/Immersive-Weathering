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

Republish after documentation changes intended for public readers. CI checks the
repository source but does not have Wiki publication credentials or automatically
push the mirror. A source update is not a completed public Wiki update until the
mirror has been exported, reviewed, pushed, and verified.
