"""Generate deterministic dry-host snow blockstates and semantic tags (no copied models/textures)."""

import argparse
import json
import re
from pathlib import Path
from pile_resources import all_hosts, immersive_plants, extra_plant_parts, structural_hosts

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/com/ordana/immersive_weathering/snow/SnowHosts.java"
RESOURCES = ROOT / "src/main/resources"


def hosts():
    return all_hosts()


def immersive_hosts():
    return immersive_plants()


def host_parts(name, frosty=True):
    parts = []
    if name.endswith("_fence"):
        parts.append({"apply": {"model": f"minecraft:block/{name}_post"}})
        for direction, angle in zip(("north", "east", "south", "west"), (0, 90, 180, 270)):
            bamboo = name == "bamboo_fence"
            model = {"model": f"minecraft:block/{name}_side" + (f"_{direction}" if bamboo else ""), "uvlock": not bamboo}
            if angle and not bamboo:
                model["y"] = angle
            parts.append({"when": {direction: "true"}, "apply": model})
    elif name.endswith("_wall") and name in structural_hosts():
        parts.append({"when": {"up": "true"}, "apply": {"model": f"minecraft:block/{name}_post"}})
        for side in ("low", "tall"):
            for direction, angle in zip(("north", "east", "south", "west"), (0, 90, 180, 270)):
                suffix = "_side" if side == "low" else "_side_tall"
                model = {"model": f"minecraft:block/{name}{suffix}", "uvlock": True}
                if angle:
                    model["y"] = angle
                parts.append({"when": {direction: side}, "apply": model})
    elif name in ("grass", "fern"):
        model = f"immersive_weathering:block/frosty_{name}" if frosty else f"minecraft:block/{name}"
        parts.append({"apply": {"model": model}})
    elif name == "pink_petals":
        for amount in range(1, 5):
            for direction, angle in zip(("north", "east", "south", "west"), (0, 90, 180, 270)):
                model = {"model": f"minecraft:block/pink_petals_{amount}"}
                if angle:
                    model["y"] = angle
                parts.append({"when": {"facing": direction, "flower_amount": "|".join(str(i) for i in range(amount, 5))},
                              "apply": model})
    elif name == "mangrove_propagule":
        parts.append({"when": {"hanging": "false"}, "apply": {"model": "minecraft:block/mangrove_propagule"}})
        for age in range(5):
            parts.append({"when": {"hanging": "true", "age": str(age)},
                          "apply": {"model": f"minecraft:block/mangrove_propagule_hanging_{age}"}})
    elif name == "weeds":
        for age in range(8):
            parts.append({"when": {"age": str(age)}, "apply": {"model": f"immersive_weathering:block/weeds_{age}"}})
    elif name not in ("tall_grass", "large_fern", "sunflower", "lilac", "rose_bush", "peony"):
        parts.extend(extra_plant_parts(name))
    else:
        for half, suffix in (("lower", "bottom"), ("upper", "top")):
            parts.append({"when": {"half": half}, "apply": {"model": f"minecraft:block/{name}_{suffix}"}})
    return parts


def multipart(name):
    parts = host_parts(name)
    for layers in range(1, 9):
        parts.append({"when": {"layers": str(layers)}, "apply": {
            "model": f"immersive_weathering:block/host_snow_{layers}"}})
    return {"multipart": parts}


def cover_model(texture, layers, key="cover"):
    # A 1/1024-block depth bias hides coplanar host post/arm surfaces under the cover.
    epsilon = 1 / 64
    faces = {}
    for side in ("down", "up", "north", "south", "west", "east"):
        faces[side] = {"texture": f"#{key}", "uv": [0, 0 if side in ("up", "down") else 16 - layers * 2, 16, 16]}
        if side != "up" or layers == 8:
            faces[side]["cullface"] = side
    return {
        "textures": {key: texture, "particle": texture},
        "elements": [{"from": [-epsilon, 0, -epsilon],
                      "to": [16 + epsilon, layers * 2 + epsilon, 16 + epsilon], "faces": faces}],
    }


def outputs():
    names = hosts()
    result = {
        RESOURCES / f"assets/immersive_weathering/blockstates/snow_covered_{name}.json": multipart(name)
        for name in names
    }
    fences = [n for n in names if n.endswith("_fence")]
    walls = [n for n in names if n.endswith("_wall") and n in structural_hosts()]
    wood = [n for n in fences if n != "nether_brick_fence"]
    plants = [n for n in names if n not in fences and n not in walls]
    tall_flowers = ["sunflower", "lilac", "rose_bush", "peony"]
    small_flowers = ["dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip",
                     "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley",
                     "wither_rose", "torchflower"]
    tags = {
        "fences": fences, "wooden_fences": wood, "walls": walls,
        "mineable/axe": wood + ["grass", "fern", "tall_grass", "large_fern"],
        "mineable/pickaxe": walls + ["nether_brick_fence"],
        "tall_flowers": tall_flowers,
        "small_flowers": small_flowers,
        "flowers": small_flowers + tall_flowers + ["pink_petals"],
        "saplings": [n for n in plants if n.endswith("_sapling")] + ["mangrove_propagule"],
        "sword_efficient": plants,
    }
    # Separate IW-owned tags are referenced by append-only vanilla tag files.
    for tag, values in tags.items():
        result[RESOURCES / f"data/immersive_weathering/tags/blocks/snow_hosts/{tag}.json"] = {
            "replace": False, "values": [f"immersive_weathering:snow_covered_{n}" for n in values]
        }
    for layers in range(1, 9):
        result[RESOURCES / f"assets/immersive_weathering/models/block/host_snow_{layers}.json"] = cover_model(
            "minecraft:block/snow", layers, "snow")
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    errors = []
    for path, data in outputs().items():
        text = json.dumps(data, indent=2) + "\n"
        if args.check:
            if not path.is_file() or path.read_text(encoding="utf-8") != text:
                errors.append(str(path.relative_to(ROOT)))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8", newline="\n")
    if errors:
        raise SystemExit("Stale generated snow resources: " + ", ".join(errors))
    print(f"Snow resources {'checked' if args.check else 'generated'} for {len(hosts())} explicit dry hosts.")


if __name__ == "__main__":
    main()
