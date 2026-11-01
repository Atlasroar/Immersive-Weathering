# Contributing

This fork targets Minecraft 1.20.1 on Fabric. Read the
[developer guide](docs/wiki/developers/README.md) before changing code.

## A complete change

1. Reproduce the problem on Fabric with recorded dependency versions. An upstream
   report is evidence to investigate, not proof of current behavior.
2. Trace the entrypoint, implementation, relevant configuration, datapack,
   generated resources, and client/server boundaries.
3. Make a focused change and validate it with the smallest appropriate build and
   reproduction. Preserve unrelated behavior and block/entity state.
4. Update the [player guide](docs/wiki/players/README.md) for changed gameplay,
   settings, installation, or workarounds; update the developer pages for changed
   interfaces, source locations, dependency roles, or development instructions.
5. Regenerate the reference and run the documentation checks:

   ```powershell
   python tools\wiki.py --write
   python tools\wiki.py --check
   python -m unittest discover -s tools -p "test_*.py"
   ```

6. Describe documentation impact in the pull request. If no prose changes are
   needed, explain why. Do not present a regeneration-only change as documenting
   a new behavior.

See [development and validation](docs/wiki/developers/Development.md),
[adding features](docs/wiki/developers/Contributing.md), and
[wiki maintenance](docs/wiki/developers/Maintenance.md).

## License and attribution

Preserve [LICENSE](LICENSE), [COPYING](COPYING), and [ATTRIBUTIONS](ATTRIBUTIONS).
Do not import code, textures, or sounds without checking their license and
retaining the required notices. Contributions must be compatible with the
project's existing licensing.
