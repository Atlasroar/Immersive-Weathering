import json
import tempfile
import unittest
import zipfile
from pathlib import Path

import fabric
import wiki


class FabricTests(unittest.TestCase):
    def manifest(self):
        manifest = json.loads((wiki.ROOT / fabric.RESOURCES / "fabric.mod.json").read_text(encoding="utf-8"))
        props = wiki.properties((wiki.ROOT / "gradle.properties").read_text(encoding="utf-8"))
        for token, key in (
            ("${version}", "mod_version"), ("${fabric_loader_version}", "fabric_loader_version"),
            ("${moonlight_min_version}", "moonlight_min_version"),
        ):
            manifest = json.loads(json.dumps(manifest).replace(token, props[key]))
        return manifest

    def test_current_source_contract(self):
        self.assertEqual(fabric.source_errors(wiki.ROOT), [])

    def test_source_checks_cover_nonstandard_sources_and_binaries_not_historical_prose(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            paths = [
                Path("build.gradle"), Path("settings.gradle"), Path("gradle.properties"),
                fabric.RESOURCES / "fabric.mod.json", Path("legacy") / "Bridge.java",
                Path("old.jar"), Path("docs") / "History.md",
            ]
            for relative in paths[:4]:
                destination = root / relative
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes((wiki.ROOT / relative).read_bytes())
            for relative, text in (
                (paths[4], "import net.minecraftforge.event.Event;"),
                (paths[5], "binary"),
                (paths[6], "Forge, Architectury and Quark were removed. Moonlight remains."),
            ):
                destination = root / relative
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_text(text, encoding="utf-8")
            errors = fabric.source_errors(root, paths)
            self.assertTrue(any("root src" in error for error in errors))
            self.assertTrue(any("platform residue" in error for error in errors))
            self.assertTrue(any("bundled binary" in error for error in errors))
            self.assertFalse(any("History.md" in error for error in errors))

    def test_moonlight_apis_are_allowed_but_iw_bridges_are_not(self):
        self.assertIsNone(fabric.FORBIDDEN.search("net.mehvahdjukaar.moonlight.api.platform.RegHelper"))
        for text in ("@ExpectPlatform", "PlatformOnly", "IWPlatformStuff", "quark:vertical_brick_slab",
                     "net.minecraftforge.events", "getPlatform().ifFabric", "architectury.common.json"):
            with self.subTest(text=text):
                self.assertIsNotNone(fabric.FORBIDDEN.search(text))

    def test_metadata_rejects_wrong_version_dependencies_and_client_entrypoint(self):
        manifest = self.manifest()
        self.assertEqual(fabric.metadata_errors(wiki.ROOT, manifest), [])
        manifest["version"] = "old"
        manifest["depends"]["minecraft"] = ">=1.20.1"
        manifest["entrypoints"]["client"] = ["com.ordana.immersive_weathering.ImmersiveWeatheringClient"]
        errors = fabric.metadata_errors(wiki.ROOT, manifest)
        self.assertEqual(len(errors), 3)

    def write_fixture(self, path, sources=False, extra=None, missing_notice=None):
        manifest = self.manifest()
        extension = ".java" if sources else ".class"
        entries = {"fabric.mod.json": json.dumps(manifest)}
        for notice in fabric.NOTICES:
            if notice != missing_notice:
                entries[notice] = (wiki.ROOT / notice).read_bytes()
        for entrypoints in manifest["entrypoints"].values():
            for entry in entrypoints:
                entries[entry.replace(".", "/") + extension] = b"fixture"
        for name in manifest["mixins"]:
            config = json.loads((wiki.ROOT / fabric.RESOURCES / name).read_text(encoding="utf-8"))
            if not sources:
                config["refmap"] = "fixture-refmap.json"
                entries["fixture-refmap.json"] = "{}"
            entries[name] = json.dumps(config)
            for side in ("mixins", "client", "server"):
                for mixin in config.get(side, []):
                    entry = (config["package"] + "." + mixin).replace(".", "/") + extension
                    entries[entry] = b"fixture"
        entries[manifest["accessWidener"]] = "accessWidener v2 " + ("named" if sources else "intermediary")
        entries.update(extra or {})
        with zipfile.ZipFile(path, "w") as archive:
            for name, data in entries.items():
                archive.writestr(name, data)

    def test_artifacts_accept_valid_contracts_and_reject_residue_or_missing_notices(self):
        props = wiki.properties((wiki.ROOT / "gradle.properties").read_text(encoding="utf-8"))
        with tempfile.TemporaryDirectory() as directory:
            jar = Path(directory) / f"{props['mod_id']}-{props['mod_version']}.jar"
            self.write_fixture(jar)
            self.assertEqual(fabric.artifact_errors(wiki.ROOT, jar), [])
            self.write_fixture(jar, extra={
                "META-INF/mods.toml": "legacy",
                "com/ordana/Bridge.class": b"net/minecraftforge/event",
                "META-INF/jars/dependency.jar": b"binary",
            }, missing_notice="COPYING")
            errors = fabric.artifact_errors(wiki.ROOT, jar)
            for expected in ("mods.toml", "platform class", "bundled dependency", "COPYING"):
                self.assertTrue(any(expected in error for error in errors), errors)
            sources = Path(directory) / f"{props['mod_id']}-{props['mod_version']}-sources.jar"
            self.write_fixture(sources, sources=True)
            self.assertEqual(fabric.artifact_errors(wiki.ROOT, sources, sources=True), [])

    def test_wrong_artifact_name_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            jar = Path(directory) / "dev.jar"
            self.write_fixture(jar)
            self.assertTrue(any("expected artifact name" in error
                                for error in fabric.artifact_errors(wiki.ROOT, jar)))


if __name__ == "__main__":
    unittest.main()
