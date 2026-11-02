import unittest
from wind_resources import ROOT, RESOURCES, ASSETS, HASHES, resource_errors


class WindResourceTest(unittest.TestCase):
    def read(self, path):
        return (ROOT / RESOURCES / path).read_bytes()

    def test_pinned_source(self):
        self.assertEqual([], resource_errors(self.read))

    def test_every_artwork_hash_is_guarded(self):
        for path in HASHES:
            with self.subTest(path=path):
                def corrupt(name):
                    return b"corrupt" if name == ASSETS + path else self.read(name)
                self.assertTrue(resource_errors(corrupt))

    def test_identity_loot_is_guarded(self):
        def incorrect(name):
            if name.endswith("/unlit_soul_wall_torch.json") and "loot_tables" in name:
                return self.read(name).replace(b"minecraft:soul_torch", b"minecraft:stick")
            return self.read(name)
        self.assertTrue(resource_errors(incorrect))

    def test_both_pins_required(self):
        def missing(name):
            return b"no upstream notice" if name == "freezing-wind-NOTICE.txt" else self.read(name)
        self.assertTrue(resource_errors(missing))


if __name__ == "__main__":
    unittest.main()
