"""Shared dry plant inventory and model references for all three mineral covers."""

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
PLANTS = ROOT / "src/main/java/com/ordana/immersive_weathering/piles/PilePlants.java"


def vanilla_plants():
    source = PLANTS.read_text(encoding="utf-8").split("public static Map", 1)[0]
    return [name.lower() for name in re.findall(r"Blocks\.([A-Z_]+)", source)]


def immersive_plants():
    source = PLANTS.read_text(encoding="utf-8").split("return Map.of(", 1)[1].split(");", 1)[0]
    return sorted(re.findall(r'"([a-z_]+)", ModBlocks\.', source))


def structural_hosts():
    source = (ROOT / "src/main/java/com/ordana/immersive_weathering/snow/SnowHosts.java").read_text(encoding="utf-8")
    source = source.split("return List.of(", 1)[1].split(");", 1)[0]
    return [name.lower() for name in re.findall(r"Blocks\.([A-Z_]+)", source)]


def all_hosts():
    return structural_hosts() + vanilla_plants() + immersive_plants()


def multipart_parts(data):
    if "multipart" in data:
        return data["multipart"]
    result = []
    for conditions, models in data["variants"].items():
        part = {"apply": models}
        if conditions:
            part["when"] = dict(condition.split("=", 1) for condition in conditions.split(","))
        result.append(part)
    return result


def extra_plant_parts(name):
    def part(model, **conditions):
        return {"when": {key: str(value) for key, value in conditions.items()},
                "apply": {"model": f"minecraft:block/{model}"}}

    if name in immersive_plants():
        path = RESOURCES / f"assets/immersive_weathering/blockstates/{name}.json"
        return multipart_parts(json.loads(path.read_text(encoding="utf-8")))
    stages = {
        "wheat": list(range(8)), "carrots": [0, 0, 1, 1, 2, 2, 2, 3],
        "potatoes": [0, 0, 1, 1, 2, 2, 2, 3], "beetroots": list(range(4)),
        "torchflower_crop": [0, 1], "melon_stem": list(range(8)), "pumpkin_stem": list(range(8)),
        "sweet_berry_bush": list(range(4)), "nether_wart": [0, 1, 1, 2],
    }
    if name in stages:
        return [part(f"{name}_stage{stage}", age=age) for age, stage in enumerate(stages[name])]
    if name == "pitcher_crop":
        return [part(f"pitcher_crop_{suffix}_stage_{age}", age=age, half=half)
                for age in range(5) for half, suffix in (("lower", "bottom"), ("upper", "top"))]
    if name == "pitcher_plant":
        return [part(f"pitcher_plant_{suffix}", half=half) for half, suffix in (("lower", "bottom"), ("upper", "top"))]
    if name in ("cave_vines", "cave_vines_plant"):
        return [part(name + suffix, berries=berries) for berries, suffix in (("false", ""), ("true", "_lit"))]
    if name == "chorus_flower":
        return [part(name + ("_dead" if age == 5 else ""), age=age) for age in range(6)]
    if name == "bamboo":
        result = [{"when": {"age": str(age)}, "apply": [
            {"model": f"minecraft:block/bamboo{variant}_age{age}"} for variant in range(1, 5)]}
                  for age in range(2)]
        return result + [part(f"bamboo_{leaves}_leaves", leaves=leaves) for leaves in ("small", "large")]
    directions = ("north", "east", "south", "west")
    if name in ("cocoa", "attached_melon_stem", "attached_pumpkin_stem", "big_dripleaf",
                "big_dripleaf_stem", "small_dripleaf"):
        result = []
        for direction, angle in zip(directions, (0, 90, 180, 270)):
            if name == "cocoa":
                options = [(f"cocoa_stage{age}", {"age": str(age)}) for age in range(3)]
                angle = (angle + 180) % 360
            elif name.startswith("attached_"):
                options = [(name, {})]
                angle = (angle + 90) % 360
            elif name == "big_dripleaf":
                options = [(name + (f"_{tilt}_tilt" if tilt in ("partial", "full") else ""), {"tilt": tilt})
                           for tilt in ("none", "unstable", "partial", "full")]
            elif name == "small_dripleaf":
                options = [(name + suffix, {"half": half}) for half, suffix in (("lower", "_bottom"), ("upper", "_top"))]
            else:
                options = [(name, {})]
            for model, conditions in options:
                entry = part(model, facing=direction, **conditions)
                if angle:
                    entry["apply"]["y"] = angle
                result.append(entry)
        return result
    if name in ("vine", "glow_lichen", "chorus_plant"):
        faces = directions + ("up",) + (() if name == "vine" else ("down",))
        rotations = ({}, {"y": 90, "uvlock": True}, {"y": 180, "uvlock": True},
                     {"y": 270, "uvlock": True}, {"x": 270, "uvlock": True}, {"x": 90, "uvlock": True})
        result = []
        for face, rotation in zip(faces, rotations):
            entry = part(name + ("_side" if name == "chorus_plant" else ""), **{face: "true"})
            entry["apply"].update(rotation)
            result.append(entry)
            if name != "chorus_plant":
                result.append({"when": {key: "false" for key in faces}, "apply": dict(entry["apply"])})
            else:
                result.append({"when": {face: "false"}, "apply": [
                    {"model": f"minecraft:block/chorus_plant_noside{suffix}", **rotation,
                     **({"weight": 2} if suffix == "" else {})} for suffix in ("", "1", "2", "3")]})
        return result
    return [{"apply": {"model": f"minecraft:block/{name}"}}]
