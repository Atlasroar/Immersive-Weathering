## 1.20.1-2.0.6-beta.1

- Migrated to one root Fabric Loom project for Minecraft 1.20.1 and Java 17.
- Removed IW-owned Forge modules, platform annotations/bridges, Quark leftovers,
  unfinished seasonal bridge, transformer output, and historical binary copies.
- Kept Moonlight's required registration, config, event, networking, and dynamic
  resource behavior; retained existing Fabric flammability and stable content IDs.
- Read the actual liquid fluid field directly and register biome features through
  Fabric BiomeModifications without platform indirection.
- Updated Fabric API/Loader and optional development integrations; depend on
  separately installed Fabric Moonlight 1.20-2.12.11.
- Added deterministic Fabric source/artifact checks and embedded license and
  attribution notices in both remapped and sources jars.
- Updated the player/developer wiki and root build/CI instructions.

This is a migration prerelease after 2.0.5, not a blanket fix for the gameplay
issue catalog. Use backed-up or disposable worlds.

## 1.20.1-2.0.5

Added an area-loaded check to fluid generators to prevent an unknown blocking
issue when fluids are near unloaded chunks.