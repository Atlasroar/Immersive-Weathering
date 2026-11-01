# Repository guidance

- Maintenance targets Minecraft 1.20.1 and Fabric. Do not add Forge, NeoForge,
  or multiloader obligations. Sources and resources live in the single root
  Fabric Loom project under `src/main`.
- The GitHub default branch is `atlasroar-fabric-1-20-1-bugs`. Retired branch
  history is preserved under `archive/2026-11-01/*` tags. Do not remove archive
  tags or recreate retired support branches without explicit user direction.
- Keep Moonlight unless a separately scoped change replaces its required
  behavior; it is not merely a multiloader bridge.
- Keep the version-controlled wiki in `docs/wiki` current in the same change
  as the implementation. Player-facing behavior belongs in `players`; technical
  contracts and architecture belong in `developers`.
- Follow `CONTRIBUTING.md` and the wiki maintenance checklist. Update relevant
  prose, examples, troubleshooting, and links when behavior or paths change.
- Run `python tools\wiki.py --write` after changing versions, dependency imports,
  config definitions, entrypoints, mixin registrations, or datapack inventories.
  Never hand-edit `docs/wiki/developers/Reference.md`.
- Run `python tools\wiki.py --check` and the tool's unittest suite before
  completing documentation changes. Automated checks do not verify prose or
  gameplay; inspect those manually and report unverified behavior honestly.
- The GitHub Wiki is an exported mirror, not the source of truth. When publishing
  documentation, follow the maintenance guide: push the matching source commit,
  export with that ref, review/push the Wiki checkout, and verify the remote.
- Preserve the upstream `LICENSE`, the GNU GPLv3 companion `COPYING`, and
  `ATTRIBUTIONS`. Do not change third-party license text or attribution.
