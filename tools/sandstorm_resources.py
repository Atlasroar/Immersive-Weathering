"""Verify pinned storm assets/notices in source and both distribution jars (no dependencies)."""
import argparse
import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = Path("src/main/resources")
ASSETS = "assets/immersive_weathering/"
PIN = "d74712e9b5fc4348aa8ad9c417ce938daf8bc0a1"
HASHES = {
    "textures/particle/dust_grain/var0.png": "040799acfd2e753632cef5642ab07e0182a973cc990cbfcb6bf7d201f10cfa03",
    "textures/particle/dust_grain/var1.png": "85fb6789e97a9385e823a6da6f6fbdd376c6e8165a449e8f1613ef34592afb2e",
    "textures/particle/dust_grain/var2.png": "b685389739d5add8d9240511f15b60e3365262c9e557b80fc3bf352dc30564a2",
    "sounds/weather/sandstorm/sandstorm1.ogg": "a9d7825bc924eca4f4291eb78c39c71664dd1e4807bf9da4d672868153966366",
    "sounds/weather/sandstorm/sandstorm2.ogg": "b0b463aac98acd9465e59b8f0965f57a9d961ffd6b22fcee11348cdb614d69de",
    "sounds/weather/sandstorm/sandstorm3.ogg": "ecf6435e5512ac62ca8267b1d53fe0029d4409e8b173298919fa396ccc9974b8",
    "sounds/weather/sandstorm/sandstorm4.ogg": "768415101813484860b65774b2a2d44f679828edb6d41eecb4fe7fe0e734d00e",
    "sounds/weather/sandstorm/sandstorm5.ogg": "ad662679ae7164cbacf85525ca90b7b0a0a444e79db605b7f8b5f1aa5fa2c257",
    "sounds/weather/sandstorm/sandstorm6.ogg": "6598d6b04b89f7922f90676f5a18d978714c70a41cea92dda72aa002d4704c0f",
    "sounds/weather/sandstorm/sandstorm7.ogg": "16d154bd5d54ecf52147ccb13e8d7e97f18f49ed4faa2f14debb3257a68bb94d",
    "sounds/weather/sandstorm/sandstorm8.ogg": "03a15c9e7e89d6166a765847b3dbc536e408fac50b6b52f5750497c9eae85499",
    "sounds/weather/sandstorm/sandstorm9.ogg": "884672d6009a3c373111ef2a8dff87dfd2b73cb052ca1076a8b63492a7bc6bbf",
}
ADAPTED_SOURCES = (
    "sandstorm/Sandstorms.java",
    "sandstorm/SandAccumulation.java",
    "sandstorm/SandstormSlowing.java",
    "client/SandstormEffects.java",
    "client/WindTimer.java",
    "client/particles/DustGrainParticle.java",
)


def resource_errors(read):
    errors = []
    for path, digest in HASHES.items():
        try:
            data = read(ASSETS + path)
            if hashlib.sha256(data).hexdigest() != digest:
                errors.append(f"{path}: pinned asset hash mismatch")
            if path.endswith(".png") and not data.startswith(b"\x89PNG\r\n\x1a\n"):
                errors.append(f"{path}: not PNG")
            if path.endswith(".ogg") and (not data.startswith(b"OggS") or b"\x01vorbis" not in data[:100]):
                errors.append(f"{path}: not Ogg Vorbis")
        except (FileNotFoundError, KeyError):
            errors.append(f"{path}: missing")
    try:
        particle = json.loads(read(ASSETS + "particles/dust_grain.json"))
        expected = [f"immersive_weathering:dust_grain/var{i}" for i in range(3)]
        if particle != {"textures": expected}:
            errors.append("dust_grain.json: sprite inventory mismatch")
        sounds = json.loads(read(ASSETS + "sounds.json"))["weather.sandstorm"]
        expected = [f"immersive_weathering:weather/sandstorm/sandstorm{i}" for i in range(1, 10)]
        if sounds != {"subtitle": "sound.immersive_weathering.weather.sandstorm", "sounds": expected}:
            errors.append("sounds.json: wind inventory mismatch")
        lang = json.loads(read(ASSETS + "lang/en_us.json"))
        if lang["sound.immersive_weathering.weather.sandstorm"] != "Wind blows":
            errors.append("Wind subtitle missing")
        notice = read("sandstorm-NOTICE.txt").decode("utf-8")
        for required in (PIN, "TheDeathlyCow", "2026-11-02", "LGPL", "Pixabay", "Wagna", "CC0", "stale"):
            if required not in notice:
                errors.append(f"NOTICE: missing {required}")
    except (FileNotFoundError, KeyError, ValueError) as error:
        errors.append(f"storm resource/notice: {error}")
    return errors


def artifact_errors(path, root=ROOT, sources=False):
    with zipfile.ZipFile(path) as archive:
        errors = resource_errors(archive.read)
        for name in ("LICENSE", "COPYING", "ATTRIBUTIONS"):
            if archive.read(name) != (root / name).read_bytes():
                errors.append(f"{path}: {name} differs")
        if archive.read("sandstorm-NOTICE.txt") != (root / RESOURCES / "sandstorm-NOTICE.txt").read_bytes():
            errors.append(f"{path}: sandstorm notice differs")
        if sources:
            # Loom remaps Minecraft symbols to intermediary and normalizes line endings.
            # Check corresponding-source membership and preserved notices, not Mojang byte identity.
            for java in ADAPTED_SOURCES:
                relative = "com/ordana/immersive_weathering/" + java
                try:
                    text = archive.read(relative).decode("utf-8")
                    for marker in ("TheDeathlyCow", "LGPL-3.0", "2026-11-02", "sandstorm-NOTICE.txt",
                                   "class " + Path(java).stem):
                        if marker not in text:
                            errors.append(f"{path}: corresponding source/notice missing {marker}: {relative}")
                except KeyError:
                    errors.append(f"{path}: missing corresponding source: {relative}")
        return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="read-only check (default)")
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
    print("Sandstorm resources: 12 pinned assets, registrations and notices verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
