"""Generate native-model sand covers for the explicit dry vanilla host inventory."""

import argparse
import json
import re
from pathlib import Path

from snow_resources import cover_model, host_parts
from pile_resources import all_hosts

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"


def hosts():
    return all_hosts()


def outputs():
    result = {}
    for color in ("sand", "red_sand"):
        for name in hosts():
            parts = host_parts(name, frosty=False)
            parts.extend({"when": {"layers": str(layers)}, "apply": {
                "model": f"immersive_weathering:block/host_{color}_{layers}"}} for layers in range(1, 9))
            result[RESOURCES / f"assets/immersive_weathering/blockstates/{color}_covered_{name}.json"] = {"multipart": parts}
        for layers in range(1, 9):
            result[RESOURCES / f"assets/immersive_weathering/models/block/host_{color}_{layers}.json"] = cover_model(
                f"minecraft:block/{color}", layers)
    fences = [name for name in hosts() if name.endswith("_fence")]
    wood = [name for name in fences if name != "nether_brick_fence"]
    walls = [name for name in hosts() if name.endswith("_wall") and name != "hanging_roots_wall"]
    plants = [name for name in hosts() if name not in fences and name not in walls]
    for tag, names in {
        "fences": fences, "wooden_fences": wood, "walls": walls, "mineable/axe": wood,
        "mineable/pickaxe": walls + ["nether_brick_fence"], "sword_efficient": plants,
    }.items():
        result[RESOURCES / f"data/immersive_weathering/tags/blocks/sand_hosts/{tag}.json"] = {
            "replace": False, "values": [
                f"immersive_weathering:{color}_covered_{name}" for color in ("sand", "red_sand") for name in names]
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
        raise SystemExit("Stale generated sand resources: " + ", ".join(errors))
    print(f"Sand resources {'checked' if args.check else 'generated'} for {len(hosts())} dry hosts in both colors.")


if __name__ == "__main__":
    main()
