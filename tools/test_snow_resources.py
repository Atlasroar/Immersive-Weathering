import json
import unittest

import snow_resources


class SnowResourceTests(unittest.TestCase):
    def test_explicit_dry_host_inventory(self):
        names = snow_resources.hosts()
        self.assertEqual(len(names), 72)
        self.assertEqual(len(set(names)), len(names))
        self.assertEqual(sum(name.endswith("_fence") for name in names), 12)
        self.assertEqual(sum(name.endswith("_wall") for name in names), 22)
        self.assertFalse(any(name.endswith(("_gate", "_slab", "_stairs")) for name in names))
        self.assertEqual(set(snow_resources.immersive_hosts()), {"weeds", "dune_grass", "frosty_grass", "frosty_fern"})
        self.assertTrue({"poppy", "torchflower", "pink_petals", "oak_sapling", "mangrove_propagule",
                         "brown_mushroom", "red_mushroom", "dead_bush"}.issubset(names))
        self.assertTrue({"wheat", "carrots", "seagrass", "vine"}.isdisjoint(names))

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
            namespace = "immersive_weathering" if name in ("grass", "fern") or name in snow_resources.immersive_hosts() else "minecraft"
            model_name = f"frosty_{name}" if name in ("grass", "fern") else name
            self.assertTrue(all(p["apply"]["model"].startswith(f"{namespace}:block/{model_name}") for p in host))
            count = (5 if name.endswith("_fence") else 9 if name.endswith("_wall")
                     else 16 if name == "pink_petals" else 6 if name == "mangrove_propagule"
                     else 8 if name == "weeds"
                     else 2 if name in ("tall_grass", "large_fern", "sunflower", "lilac", "rose_bush", "peony") else 1)
            self.assertEqual(len(host), count)

    def test_covered_grass_and_fern_use_matching_frosty_models(self):
        for name in ("grass", "fern"):
            parts = snow_resources.multipart(name)["multipart"]
            host = [part for part in parts if "layers" not in part.get("when", {})]
            self.assertEqual(host, [{"apply": {"model": f"immersive_weathering:block/frosty_{name}"}}])
            model = snow_resources.RESOURCES / f"assets/immersive_weathering/models/block/frosty_{name}.json"
            data = json.loads(model.read_text(encoding="utf-8"))
            self.assertEqual(data["parent"], "immersive_weathering:block/frosty_tinted_cross")
        for name in ("tall_grass", "large_fern"):
            parts = snow_resources.multipart(name)["multipart"]
            host = [part for part in parts if "layers" not in part.get("when", {})]
            self.assertTrue(all(part["apply"]["model"].startswith(f"minecraft:block/{name}_") for part in host))

    def test_common_snow_controls_have_names_and_descriptions(self):
        language = json.loads((snow_resources.RESOURCES / "assets/immersive_weathering/lang/en_us.json")
                              .read_text(encoding="utf-8"))
        source = (snow_resources.ROOT / "src/main/java/com/ordana/immersive_weathering/configs/CommonConfigs.java").read_text(encoding="utf-8")
        section = source.split('builder.push("snow");', 1)[1].split("builder.pop();", 1)[0]
        for key in ("host_mixing", "falling", "replace_partial_layers", "accumulation",
                    "accumulate_without_thunder", "column_cap"):
            self.assertIn(f'.define("{key}",', section)
            self.assertIn(f"config.immersive_weathering.snow.{key}", language)
            self.assertIn(f"config.immersive_weathering.snow.{key}.descriptionKey", language)
        self.assertEqual(section.count("builder.comment("), 6)

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
        for name in ("small_flowers", "flowers", "saplings"):
            path = snow_resources.RESOURCES / f"data/minecraft/tags/blocks/{name}.json"
            data = json.loads(path.read_text(encoding="utf-8"))
            self.assertFalse(data["replace"])
            self.assertIn(f"#immersive_weathering:snow_hosts/{name}", data["values"])
        original_flowers = {"immersive_weathering:flowering_azalea_leaf_pile",
                            "immersive_weathering:azalea_flower_pile"}
        small_flowers = json.loads((snow_resources.RESOURCES / "data/minecraft/tags/blocks/small_flowers.json")
                                  .read_text(encoding="utf-8"))
        self.assertTrue(original_flowers.issubset(small_flowers["values"]))


if __name__ == "__main__":
    unittest.main()
