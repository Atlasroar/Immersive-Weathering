import json
import unittest
from pathlib import Path

import sandstorm_resources as storm


class SandstormResourcesTests(unittest.TestCase):
    def read(self, path):
        return (storm.ROOT / storm.RESOURCES / path).read_bytes()

    def test_pinned_inventory_and_notices(self):
        self.assertEqual(storm.resource_errors(self.read), [])
        self.assertEqual(len(storm.HASHES), 12)

    def test_asset_corruption_is_detected(self):
        for name in storm.HASHES:
            path = storm.ASSETS + name
            with self.subTest(asset=name):
                def corrupt(key):
                    return b"corrupt" if key == path else self.read(key)
                self.assertTrue(storm.resource_errors(corrupt))

    def test_missing_asset_is_detected(self):
        def missing(path):
            if path.endswith("sandstorm9.ogg"):
                raise FileNotFoundError(path)
            return self.read(path)
        self.assertTrue(storm.resource_errors(missing))

    def test_biome_and_immunity_tags(self):
        root = storm.ROOT / storm.RESOURCES / "data/immersive_weathering/tags"
        def tag(path):
            return json.loads((root / path).read_text(encoding="utf-8"))["values"]
        self.assertEqual(tag(Path("worldgen/biome/regular_sandstorms.json")),
                         ["minecraft:desert", {"id": "#c:desert", "required": False}])
        self.assertEqual(tag(Path("worldgen/biome/red_sandstorms.json")),
                         ["#minecraft:is_badlands", {"id": "#c:badlands", "required": False}])
        self.assertEqual(tag(Path("entity_types/sandstorm_slow_immune.json")),
                         ["minecraft:camel", {"id": "#c:bosses", "required": False}])
        self.assertIn("minecraft:warm_ocean", tag(Path("worldgen/biome/has_sandstorm.json")))

    def test_no_new_dependency_or_entrypoint(self):
        metadata = json.loads(self.read("fabric.mod.json"))
        self.assertEqual(set(metadata["depends"]), {"fabricloader", "fabric-api", "minecraft", "java", "moonlight"})
        build = (storm.ROOT / "build.gradle").read_text(encoding="utf-8")
        dependency_body = build.split("dependencies {", 1)[1].split("processResources", 1)[0]
        for name in ("scorchful", "thermoo", "cardinal", "cca", "satin"):
            self.assertNotIn(name, dependency_body.lower())
        self.assertNotIn("client", metadata["entrypoints"])

    def test_common_and_client_mixins_are_separated(self):
        manifest = json.loads(self.read("immersive_weathering-gameplay.mixins.json"))
        self.assertIn("SandstormLivingMixin", manifest["mixins"])
        self.assertEqual(manifest["client"], ["client.SandstormFogMixin", "client.SandstormCloudMixin"])
        self.assertIn("ServerLevelMixin", manifest["mixins"])

    def test_config_translations_cover_every_control(self):
        lang = json.loads(self.read("assets/immersive_weathering/lang/en_us.json"))
        for key in ("deposition", "layer_cap", "slowing", "slow_amount", "particles", "particle_range",
                    "particle_rarity", "fog", "fog_start", "fog_end", "hide_clouds", "sounds"):
            prefix = "config.immersive_weathering.sandstorms." + key
            self.assertIn(prefix, lang)
            self.assertIn(prefix + ".descriptionKey", lang)


if __name__ == "__main__":
    unittest.main()
