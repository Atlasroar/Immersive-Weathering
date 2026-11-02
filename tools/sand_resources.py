"""Generate native-model sand covers for the explicit dry vanilla host inventory."""

import argparse
import json
import re
from pathlib import Path

from snow_resources import cover_model, host_parts

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"


def hosts():
    source = (ROOT / "src/main/java/com/ordana/immersive_weathering/sand/SandHosts.java").read_text(encoding="utf-8")
    inventory = source.split("return List.of(", 1)[1].split(");", 1)[0]
    return [name.lower() for name in re.findall(r"Blocks\.([A-Z_]+)", inventory)]


def outputs():
    result = {}
    for color in ("sand", "red_sand"):
        for name in hosts():
            parts = host_parts(name)
            parts.extend({"when": {"layers": str(layers)}, "apply": {
                "model": f"immersive_weathering:block/host_{color}_{layers}"}} for layers in range(1, 9))
            result[RESOURCES / f"assets/immersive_weathering/blockstates/{color}_covered_{name}.json"] = {"multipart": parts}
        for layers in range(1, 9):
            result[RESOURCES / f"assets/immersive_weathering/models/block/host_{color}_{layers}.json"] = cover_model(
                f"minecraft:block/{color}", layers)
    fences = [name for name in hosts() if name.endswith("_fence")]
    wood = [name for name in fences if name != "nether_brick_fence"]
    for tag, names in {
        "fences": fences, "wooden_fences": wood, "mineable/axe": wood,
        "mineable/pickaxe": ["nether_brick_fence"], "sword_efficient": ["dead_bush"],
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
