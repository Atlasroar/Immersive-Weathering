"""Generate deterministic vanilla-host snow blockstates and semantic tags (no copied models/textures)."""

import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/com/ordana/immersive_weathering/snow/SnowHosts.java"
RESOURCES = ROOT / "src/main/resources"


def hosts():
    source = SOURCE.read_text(encoding="utf-8").split("return List.of(", 1)[1].split(");", 1)[0]
    return [name.lower() for name in re.findall(r"Blocks\.([A-Z_]+)", source)]


def multipart(name):
    parts = []
    if name.endswith("_fence"):
        parts.append({"apply": {"model": f"minecraft:block/{name}_post"}})
        for direction, angle in zip(("north", "east", "south", "west"), (0, 90, 180, 270)):
            bamboo = name == "bamboo_fence"
            model = {"model": f"minecraft:block/{name}_side" + (f"_{direction}" if bamboo else ""), "uvlock": not bamboo}
            if angle and not bamboo:
                model["y"] = angle
            parts.append({"when": {direction: "true"}, "apply": model})
    elif name.endswith("_wall"):
        parts.append({"when": {"up": "true"}, "apply": {"model": f"minecraft:block/{name}_post"}})
        for side in ("low", "tall"):
            for direction, angle in zip(("north", "east", "south", "west"), (0, 90, 180, 270)):
                suffix = "_side" if side == "low" else "_side_tall"
                model = {"model": f"minecraft:block/{name}{suffix}", "uvlock": True}
                if angle:
                    model["y"] = angle
                parts.append({"when": {direction: side}, "apply": model})
    elif name in ("grass", "fern"):
        parts.append({"apply": {"model": f"minecraft:block/{name}"}})
    else:
        for half, suffix in (("lower", "bottom"), ("upper", "top")):
            parts.append({"when": {"half": half}, "apply": {"model": f"minecraft:block/{name}_{suffix}"}})
    for layers in range(1, 9):
        parts.append({"when": {"layers": str(layers)}, "apply": {
            "model": f"immersive_weathering:block/host_snow_{layers}"}})
    return {"multipart": parts}


def outputs():
    names = hosts()
    result = {
        RESOURCES / f"assets/immersive_weathering/blockstates/snow_covered_{name}.json": multipart(name)
        for name in names
    }
    fences = [n for n in names if n.endswith("_fence")]
    walls = [n for n in names if n.endswith("_wall")]
    wood = [n for n in fences if n != "nether_brick_fence"]
    tags = {
        "fences": fences, "wooden_fences": wood, "walls": walls,
        "mineable/axe": wood + ["grass", "fern", "tall_grass", "large_fern"],
        "mineable/pickaxe": walls + ["nether_brick_fence"],
        "tall_flowers": ["sunflower", "lilac", "rose_bush", "peony"],
        "sword_efficient": ["grass", "fern", "tall_grass", "large_fern", "sunflower", "lilac", "rose_bush", "peony"],
    }
    # Separate IW-owned tags are referenced by append-only vanilla tag files.
    for tag, values in tags.items():
        result[RESOURCES / f"data/immersive_weathering/tags/blocks/snow_hosts/{tag}.json"] = {
            "replace": False, "values": [f"immersive_weathering:snow_covered_{n}" for n in values]
        }
    for layers in range(1, 9):
        # A 1/1024-block depth bias hides coplanar host post/arm surfaces under the snow.
        # This is static vanilla-format geometry, not a renderer/FRAPI dependency.
        epsilon = 1 / 64
        faces = {}
        for side in ("down", "up", "north", "south", "west", "east"):
            faces[side] = {"texture": "#snow", "uv": [0, 0 if side in ("up", "down") else 16 - layers * 2, 16, 16]}
            if side != "up" or layers == 8:
                faces[side]["cullface"] = side
        result[RESOURCES / f"assets/immersive_weathering/models/block/host_snow_{layers}.json"] = {
            "textures": {"snow": "minecraft:block/snow", "particle": "minecraft:block/snow"},
            "elements": [{"from": [-epsilon, 0, -epsilon],
                          "to": [16 + epsilon, layers * 2 + epsilon, 16 + epsilon], "faces": faces}],
        }
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
    print(f"Snow resources {'checked' if args.check else 'generated'} for {len(hosts())} explicit vanilla hosts.")


if __name__ == "__main__":
    main()
