"""Verify pinned wind/snowstorm artwork, native torch resources and both packaged jars."""
import argparse
import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = Path("src/main/resources")
ASSETS = "assets/immersive_weathering/"
PINS = ("f017b15a2b23a6474d03d8e4b4e9e244247ffa24", "66c883b5c1facba2219c9a31e1f93acfe2e333c9")
HASHES = {
    "textures/particle/freezing_wind/var1_01.png": "66a10d75444fe2d0c97a405a97b7072f626e55cb0c73eb0d7f978cb12afb3ffd",
    "textures/particle/freezing_wind/var1_02.png": "c6924f96ab4404c7d7edae5f6fd357b4e232464a06a0cfeb55f3ddd493bc6d03",
    "textures/particle/freezing_wind/var1_03.png": "8236b180de4ead1316267f35eb9633e2897e38b9d132c518f88dddb175397b8d",
    "textures/particle/freezing_wind/var2_01.png": "5c7f6ca144801e671b672af1d582f69fe8fbb69e1296809eac2469681211875b",
    "textures/particle/freezing_wind/var2_02.png": "ca191feb1418a0c9c74bcf6ea13b1d5db10a32d082f74f911077867726df9a8e",
    "textures/particle/freezing_wind/var2_03.png": "9c74050129762f7e0cbdea34f46b2f0af7d32cde23f7e9e772ccd3be02288943",
    "textures/block/unlit_torch.png": "4e423fac8446f350fa4af27fd5bebde791eb4b309b8988bed9baf5a557e7bba4",
    "textures/particle/snowstorm_grain/var0.png": "040799acfd2e753632cef5642ab07e0182a973cc990cbfcb6bf7d201f10cfa03",
    "textures/particle/snowstorm_grain/var1.png": "85fb6789e97a9385e823a6da6f6fbdd376c6e8165a449e8f1613ef34592afb2e",
    "textures/particle/snowstorm_grain/var2.png": "b685389739d5add8d9240511f15b60e3365262c9e557b80fc3bf352dc30564a2",
}
SOURCES = ("wind/FreezingWind.java", "wind/FreezingWindEntity.java", "wind/WindBlocks.java",
           "wind/WindSettings.java", "blocks/UnlitTorchBlock.java", "blocks/UnlitWallTorchBlock.java",
           "blocks/UnlitTorches.java", "client/particles/FreezingWindParticle.java",
           "client/SnowstormEffects.java", "client/particles/SnowstormGrainParticle.java",
           "snowstorm/Snowstorms.java")


def resource_errors(read):
    errors = []
    for path, digest in HASHES.items():
        try:
            data = read(ASSETS + path)
            if hashlib.sha256(data).hexdigest() != digest or not data.startswith(b"\x89PNG\r\n\x1a\n"):
                errors.append(f"{path}: pinned PNG mismatch")
        except (FileNotFoundError, KeyError):
            errors.append(f"{path}: missing")
    try:
        for name, folder, frames in (
            ("freezing_wind", "freezing_wind", [f"var1_0{i}" for i in range(1, 4)]),
            ("freezing_wind_flipped", "freezing_wind", [f"var2_0{i}" for i in range(1, 4)]),
            ("snowstorm_grain", "snowstorm_grain", [f"var{i}" for i in range(3)]),
        ):
            expected = {"textures": [f"immersive_weathering:{folder}/{frame}" for frame in frames]}
            if json.loads(read(ASSETS + f"particles/{name}.json")) != expected:
                errors.append(f"{name}: sprite inventory mismatch")
        for name in ("unlit_torch", "unlit_wall_torch", "unlit_soul_torch", "unlit_soul_wall_torch"):
            variants = json.loads(read(ASSETS + f"blockstates/{name}.json"))["variants"]
            if "wall" in name:
                if set(variants) != {f"facing={d}" for d in ("east", "north", "south", "west")}:
                    errors.append(f"{name}: wall orientation inventory mismatch")
            elif set(variants) != {""}:
                errors.append(f"{name}: standing variant mismatch")
            loot = json.loads(read(f"data/immersive_weathering/loot_tables/blocks/{name}.json"))
            expected = "minecraft:soul_torch" if "soul" in name else "minecraft:torch"
            if loot["pools"][0]["entries"] != [{"type": "minecraft:item", "name": expected}]:
                errors.append(f"{name}: native identity loot mismatch")
        notice = read("freezing-wind-NOTICE.txt").decode()
        for required in (*PINS, "TheDeathlyCow", "2026-11-02", "LGPL", "Pixabay", "No howls", "corresponding source"):
            if required not in notice:
                errors.append(f"NOTICE missing {required}")
        license_text = read("weather-adaptations-LGPL-3.0.txt").decode()
        if "GNU LESSER GENERAL PUBLIC LICENSE" not in license_text or "Version 3, 29 June 2007" not in license_text:
            errors.append("LGPL companion missing")
        # Git may normalize line endings; the original license text must not change.
        if hashlib.sha256(license_text.replace("\r\n", "\n").encode()).hexdigest() != "1a45b1d0a8603dfe2cfc644f9dab970b1762f92babe2aac6eb2f5d4572c4a680":
            errors.append("Upstream LGPL companion text differs")
    except (FileNotFoundError, KeyError, ValueError) as error:
        errors.append(f"wind resource/notice: {error}")
    return errors


def artifact_errors(path, root=ROOT, sources=False):
    with zipfile.ZipFile(path) as archive:
        errors = resource_errors(archive.read)
        for name in ("LICENSE", "COPYING", "ATTRIBUTIONS"):
            if archive.read(name) != (root / name).read_bytes():
                errors.append(f"{path}: {name} differs")
        for name in ("freezing-wind-NOTICE.txt", "weather-adaptations-LGPL-3.0.txt"):
            if archive.read(name) != (root / RESOURCES / name).read_bytes():
                errors.append(f"{path}: {name} differs")
        if sources:
            for source in SOURCES:
                relative = "com/ordana/immersive_weathering/" + source
                try:
                    text = archive.read(relative).decode()
                    if "class " + Path(source).stem not in text and "record " + Path(source).stem not in text:
                        errors.append(f"{relative}: missing corresponding class")
                    if source in ("wind/FreezingWind.java", "wind/FreezingWindEntity.java",
                                  "client/particles/FreezingWindParticle.java", "client/SnowstormEffects.java",
                                  "client/particles/SnowstormGrainParticle.java"):
                        for marker in ("TheDeathlyCow", "LGPL-3.0", "freezing-wind-NOTICE.txt"):
                            if marker not in text:
                                errors.append(f"{relative}: missing {marker}")
                except KeyError:
                    errors.append(f"{relative}: missing corresponding source")
        return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--jar", type=Path)
    parser.add_argument("--sources", type=Path)
    args = parser.parse_args()
    errors = resource_errors(lambda path: (ROOT / RESOURCES / path).read_bytes())
    for path, sources in ((args.jar, False), (args.sources, True)):
        if path:
            errors.extend(artifact_errors(path, sources=sources))
    for error in errors:
        print(error)
    if errors:
        return 1
    print("Wind resources: 10 pinned PNGs, torch identity, notices and corresponding source verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
