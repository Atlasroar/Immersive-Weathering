import json
import unittest

import snow_resources


class SnowResourceTests(unittest.TestCase):
    def test_explicit_vanilla_inventory(self):
        names = snow_resources.hosts()
        self.assertEqual(len(names), 42)
        self.assertEqual(len(set(names)), len(names))
        self.assertEqual(sum(name.endswith("_fence") for name in names), 12)
        self.assertEqual(sum(name.endswith("_wall") for name in names), 22)
        self.assertFalse(any(name.endswith(("_gate", "_slab", "_stairs")) for name in names))

    def test_generated_files_are_current(self):
        for path, data in snow_resources.outputs().items():
            with self.subTest(path=path):
                self.assertEqual(json.loads(path.read_text(encoding="utf-8")), data)

    def test_english_display_names_match_registered_hosts(self):
        language = json.loads((snow_resources.RESOURCES / "assets/immersive_weathering/lang/en_us.json")
                              .read_text(encoding="utf-8"))
        prefix = "block.immersive_weathering.snow_covered_"
        expected = {prefix + name: "Snow-Covered " + name.replace("_", " ").title()
                    for name in snow_resources.hosts()}
        self.assertEqual({key: value for key, value in language.items() if key.startswith(prefix)}, expected)

    def test_each_layer_has_exactly_one_snow_part(self):
        for name in snow_resources.hosts():
            parts = snow_resources.multipart(name)["multipart"]
            layers = [part for part in parts if "layers" in part.get("when", {})]
            self.assertEqual([part["when"]["layers"] for part in layers], [str(i) for i in range(1, 9)])
            for i, part in enumerate(layers, 1):
                self.assertEqual(part["apply"]["model"], f"immersive_weathering:block/host_snow_{i}")

    def test_host_models_and_connectivity_remain_vanilla(self):
        for name in snow_resources.hosts():
            host = [p for p in snow_resources.multipart(name)["multipart"] if "layers" not in p.get("when", {})]
            self.assertTrue(all(p["apply"]["model"].startswith(f"minecraft:block/{name}") for p in host))
            count = 5 if name.endswith("_fence") else 9 if name.endswith("_wall") else 1 if name in ("grass", "fern") else 2
            self.assertEqual(len(host), count)

    def test_static_snow_geometry_is_depth_separated_and_untinted(self):
        models = [data for path, data in snow_resources.outputs().items() if path.parent.name == "block"]
        self.assertEqual(len(models), 8)
        for layers, model in enumerate(models, 1):
            element = model["elements"][0]
            self.assertLess(element["from"][0], 0)
            self.assertGreater(element["to"][0], 16)
            self.assertGreater(element["to"][1], layers * 2)
            self.assertEqual(len(element["faces"]), 6)
            self.assertTrue(all("tintindex" not in face for face in element["faces"].values()))

    def test_semantic_tags_are_append_only(self):
        for path, data in snow_resources.outputs().items():
            if "tags" in path.parts:
                self.assertFalse(data["replace"])
                self.assertTrue(all(value.startswith("immersive_weathering:snow_covered_") for value in data["values"]))


if __name__ == "__main__":
    unittest.main()
