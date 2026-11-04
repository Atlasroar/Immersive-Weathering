import json
import unittest

import sand_resources
import snow_resources


class SandResourceTests(unittest.TestCase):
    def test_exact_host_inventory_and_generated_files(self):
        names = sand_resources.hosts()
        self.assertEqual(names, snow_resources.hosts())
        self.assertEqual(sum(name.endswith("_fence") for name in names), 12)
        self.assertTrue({"dead_bush", "dune_grass", "ivy", "moss", "wheat", "cactus"}.issubset(names))
        for path, data in sand_resources.outputs().items():
            with self.subTest(path=path):
                self.assertEqual(json.loads(path.read_text(encoding="utf-8")), data)

    def test_both_colors_keep_native_host_and_one_part_per_layer(self):
        for path, data in sand_resources.outputs().items():
            if "multipart" not in data:
                continue
            parts = data["multipart"]
            layers = [part for part in parts if "layers" in part.get("when", {})]
            self.assertEqual([part["when"]["layers"] for part in layers], [str(i) for i in range(1, 9)])
            name = path.stem.split("_covered_", 1)[1]
            self.assertEqual([part for part in parts if part not in layers],
                             snow_resources.host_parts(name, frosty=False))
            self.assertTrue(all("red_sand" in part["apply"]["model"] for part in layers)
                            if path.stem.startswith("red_sand_") else
                            all("red_sand" not in part["apply"]["model"] for part in layers))

    def test_no_new_textures_and_depth_separated_untinted_geometry(self):
        models = [data for path, data in sand_resources.outputs().items() if path.parent.name == "block"]
        self.assertEqual(len(models), 16)
        for data in models:
            self.assertIn(data["textures"]["cover"], ("minecraft:block/sand", "minecraft:block/red_sand"))
            element = data["elements"][0]
            self.assertLess(element["from"][0], 0)
            self.assertGreater(element["to"][0], 16)
            self.assertEqual(len(element["faces"]), 6)
            self.assertTrue(all("tintindex" not in face for face in element["faces"].values()))

    def test_tags_are_append_only_and_referenced_by_vanilla(self):
        for name in ("fences", "wooden_fences", "walls", "mineable/axe", "mineable/pickaxe", "sword_efficient"):
            path = sand_resources.RESOURCES / f"data/minecraft/tags/blocks/{name}.json"
            data = json.loads(path.read_text(encoding="utf-8"))
            self.assertFalse(data["replace"])
            self.assertIn(f"#immersive_weathering:sand_hosts/{name}", data["values"])
        for path, data in sand_resources.outputs().items():
            if "tags" in path.parts:
                self.assertFalse(data["replace"])
                self.assertEqual(len(set(data["values"])), len(data["values"]))

    def test_display_names_and_controls_are_localized(self):
        language = json.loads((sand_resources.RESOURCES / "assets/immersive_weathering/lang/en_us.json")
                              .read_text(encoding="utf-8"))
        for color, title in (("sand", "Sand"), ("red_sand", "Red Sand")):
            expected = {f"block.immersive_weathering.{color}_covered_{name}":
                        f"{title}-Covered " + name.replace("_", " ").title() for name in sand_resources.hosts()}
            prefix = f"block.immersive_weathering.{color}_covered_"
            self.assertEqual({key: value for key, value in language.items() if key.startswith(prefix)}, expected)
        for section, keys in {
            "sand": ("host_mixing", "replace_piles", "rain_erosion", "rain_erosion_chance"),
            "fire": ("thunder_dampening", "thunder_spread_multiplier", "thunder_extinguish_chance"),
        }.items():
            for key in keys:
                prefix = f"config.immersive_weathering.{section}.{key}"
                self.assertTrue(language[prefix])
                self.assertTrue(language[prefix + ".descriptionKey"])


if __name__ == "__main__":
    unittest.main()
