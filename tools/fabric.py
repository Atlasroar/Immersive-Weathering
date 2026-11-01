"""Check IW-owned Fabric-only source and distribution contracts without dependencies."""

import argparse
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path

from wiki import ROOT, properties


RESOURCES = Path("src") / "main" / "resources"
NOTICES = ("LICENSE", "COPYING", "ATTRIBUTIONS")
FORBIDDEN = re.compile(
    r"\b(?:architectury|forge|minecraftforge|neoforge|neoforged|quark|quilt|quiltmc)\b"
    r"|ExpectPlatform|PlatformOnly|QuarkPlugin|SereneSeasonsCompat"
    r"|IWPlatformStuff|\b(?:ifForge|ifFabric|getPlatform)\s*\(",
    re.IGNORECASE,
)
FORBIDDEN_CLASS = re.compile(
    rb"dev/architectury/|net/minecraftforge/|net/neoforged/|org/quiltmc/"
    rb"|IWPlatformStuff|QuarkPlugin|SereneSeasonsCompat|transformProduction"
)
SEMVER = re.compile(
    r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)"
    r"(?:-(?P<prerelease>[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?"
    r"(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?"
)


def version_errors(version: str, minecraft_version: str) -> list[str]:
    errors = []
    match = SEMVER.fullmatch(version)
    if match is None:
        errors.append("gradle.properties: mod_version must use Semantic Versioning")
    elif match["prerelease"]:
        if any(part.isdigit() and len(part) > 1 and part.startswith("0")
               for part in match["prerelease"].split(".")):
            errors.append("gradle.properties: numeric prerelease identifiers must not have leading zeroes")
    if version.startswith(minecraft_version + "-"):
        errors.append("gradle.properties: Minecraft compatibility must not prefix the mod version")
    return errors


def tracked_paths(root: Path) -> list[Path]:
    result = subprocess.run(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
        cwd=root, check=True, stdout=subprocess.PIPE,
    )
    return sorted({Path(name.decode("utf-8")) for name in result.stdout.split(b"\0") if name})


def source_errors(root: Path, paths: list[Path] | None = None) -> list[str]:
    errors = []
    for relative in tracked_paths(root) if paths is None else sorted(paths):
        path = root / relative
        if not path.is_file():
            continue
        if relative.parts[0] in ("common", "fabric", "forge", "iw versions", ".architectury-transformer"):
            errors.append(f"{relative}: obsolete module or artifact directory")
        if path.suffix.lower() in (".jar", ".class", ".zip") and relative != (
            Path("gradle") / "wrapper" / "gradle-wrapper.jar"
        ):
            errors.append(f"{relative}: bundled binary is not the Gradle wrapper")
        if path.suffix == ".java" and relative.parts[0] != "src":
            errors.append(f"{relative}: Java source must live under the root src tree")
        active = relative.parts[0] == "src" or path.suffix in (".java", ".gradle") or relative in (
            Path("build.gradle"), Path("settings.gradle"), Path("gradle.properties"),
            Path(".github") / "workflows" / "build.yml",
        )
        if active and path.suffix in (".java", ".json", ".gradle", ".properties", ".yml", ".toml"):
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                if FORBIDDEN.search(line):
                    errors.append(f"{relative}:{number}: IW-owned platform residue")
        if active and path.name in ("mods.toml", "neoforge.mods.toml", "quilt.mod.json"):
            errors.append(f"{relative}: non-Fabric metadata")
    settings = (root / "settings.gradle").read_text(encoding="utf-8")
    if re.search(r"\binclude(?:Build)?\s*\(", settings):
        errors.append("settings.gradle: expected one root project without included builds/modules")
    build = (root / "build.gradle").read_text(encoding="utf-8")
    if not re.search(r'\bid\s+["\']fabric-loom["\']', build):
        errors.append("build.gradle: root Fabric Loom plugin is missing")
    manifest = json.loads((root / RESOURCES / "fabric.mod.json").read_text(encoding="utf-8"))
    errors.extend(metadata_errors(root, manifest, expanded=False))
    return errors


def metadata_errors(root: Path, manifest: dict, expanded: bool = True) -> list[str]:
    props = properties((root / "gradle.properties").read_text(encoding="utf-8"))
    errors = version_errors(props["mod_version"], props["minecraft_version"])
    expected = {
        "id": props["mod_id"], "version": props["mod_version"] if expanded else "${version}",
        "environment": "*", "license": "LGPLv3",
        "accessWidener": "immersive_weathering.accesswidener",
    }
    for key, value in expected.items():
        if manifest.get(key) != value:
            errors.append(f"fabric.mod.json: {key} must be {value!r}")
    dependencies = {
        "minecraft": "1.20.1", "java": ">=17", "fabric-api": "*",
        "fabricloader": ">=" + (props["fabric_loader_version"] if expanded else "${fabric_loader_version}"),
        "moonlight": ">=" + (props["moonlight_min_version"] if expanded else "${moonlight_min_version}"),
    }
    if manifest.get("depends") != dependencies:
        errors.append("fabric.mod.json: runtime dependencies do not match the Fabric contract")
    entries = {
        "main": ["com.ordana.immersive_weathering.fabric.ImmersiveWeatheringFabric"],
        "emi": ["com.ordana.immersive_weathering.integrations.EmiIntegration"],
        "modmenu": ["com.ordana.immersive_weathering.integrations.fabric.ModMenuCompat"],
    }
    if manifest.get("entrypoints") != entries:
        errors.append("fabric.mod.json: entrypoints changed; review side-safe/optional initialization")
    if manifest.get("mixins") != [
        "immersive_weathering.mixins.json", "immersive_weathering-gameplay.mixins.json",
    ]:
        errors.append("fabric.mod.json: registered mixin manifests changed")
    return errors


def artifact_errors(root: Path, path: Path, sources: bool = False) -> list[str]:
    props = properties((root / "gradle.properties").read_text(encoding="utf-8"))
    expected_name = f"{props['mod_id']}-{props['mod_version']}{'-sources' if sources else ''}.jar"
    errors = []
    if path.name != expected_name:
        errors.append(f"{path}: expected artifact name {expected_name}")
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            errors.append(f"{path}: duplicate archive entries")
        for name in names:
            if FORBIDDEN.search(name) or name.endswith(("mods.toml", "quilt.mod.json")):
                errors.append(f"{path}: forbidden platform entry {name}")
            if name.startswith("META-INF/jars/") or name.endswith((".jar", ".zip")):
                errors.append(f"{path}: bundled dependency/archive {name}")
            if not sources and name.endswith(".class") and FORBIDDEN_CLASS.search(archive.read(name)):
                errors.append(f"{path}: forbidden platform class reference in {name}")
            if sources and name.endswith(".java") and FORBIDDEN.search(archive.read(name).decode("utf-8")):
                errors.append(f"{path}: forbidden platform source in {name}")
        for notice in NOTICES:
            if notice not in names or archive.read(notice) != (root / notice).read_bytes():
                errors.append(f"{path}: {notice} is missing or differs from the repository")
        if "fabric.mod.json" not in names:
            return [*errors, f"{path}: missing fabric.mod.json"]
        manifest = json.loads(archive.read("fabric.mod.json"))
        errors.extend(f"{path}: {error}" for error in metadata_errors(root, manifest))
        extension = ".java" if sources else ".class"
        for entries in manifest.get("entrypoints", {}).values():
            for entry in entries:
                if entry.replace(".", "/") + extension not in names:
                    errors.append(f"{path}: missing entrypoint {entry}{extension}")
        for config_name in manifest.get("mixins", []):
            if config_name not in names:
                errors.append(f"{path}: missing mixin manifest {config_name}")
                continue
            config = json.loads(archive.read(config_name))
            original = json.loads((root / RESOURCES / config_name).read_text(encoding="utf-8"))
            for key in ("package", "required", "compatibilityLevel", "mixins", "client", "server", "injectors"):
                if config.get(key) != original.get(key):
                    errors.append(f"{path}: mixin {config_name} changed {key}")
            for side in ("mixins", "client", "server"):
                for mixin in config.get(side, []):
                    entry = (config["package"] + "." + mixin).replace(".", "/") + extension
                    if entry not in names:
                        errors.append(f"{path}: missing registered mixin {entry}")
            if not sources and config.get("refmap") not in names:
                errors.append(f"{path}: missing refmap for {config_name}")
        widener = manifest.get("accessWidener")
        if widener not in names:
            errors.append(f"{path}: missing access widener")
        else:
            header = archive.read(widener).decode("utf-8").splitlines()[0].split()
            if header != ["accessWidener", "v2", "named" if sources else "intermediary"]:
                errors.append(f"{path}: access widener has wrong mapping namespace")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", required=True)
    parser.add_argument("--jar", type=Path, help="Remapped installable jar")
    parser.add_argument("--sources", type=Path, help="Matching remapped sources jar")
    args = parser.parse_args()
    errors = source_errors(ROOT)
    if args.jar:
        errors.extend(artifact_errors(ROOT, args.jar))
    if args.sources:
        errors.extend(artifact_errors(ROOT, args.sources, sources=True))
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print("Fabric-only source and requested artifact contracts are valid.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
