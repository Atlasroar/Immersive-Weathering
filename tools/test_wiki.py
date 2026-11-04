import re
import tempfile
import unittest
from collections import Counter
from pathlib import Path, PurePosixPath, PureWindowsPath

import wiki


class WikiTests(unittest.TestCase):
    def test_properties_preserve_values(self):
        self.assertEqual(wiki.properties("# note\n x = a=b\n ! note\n"), {"x": "a=b"})

    def test_path_sorting_is_case_sensitive_and_identical_on_windows_and_linux(self):
        names = ["configs/CommonConfigs.java", "blocks/LeafPileBlock.java", "ImmersiveWeathering.java"]
        expected = ["ImmersiveWeathering.java", "blocks/LeafPileBlock.java", "configs/CommonConfigs.java"]
        for kind in (PurePosixPath, PureWindowsPath):
            paths = [kind(name) for name in names]
            self.assertEqual([path.as_posix() for path in sorted(paths, key=wiki.path_sort_key)], expected)

    def test_coordinates_exclude_resource_expansion_properties(self):
        source = '''
modImplementation("curse.maven:selene-499980:5619072")
inputs.property "moonlight_min_version", project.moonlight_min_version
'''
        self.assertEqual(wiki.moonlight_coordinates(source), ["curse.maven:selene-499980:5619072"])

    def test_config_sections_comments_and_nested_values(self):
        source = '''
public static final Supplier<List<String>> TEST;
public static Supplier<Double> RATE;
builder.push("general");
TEST = builder.comment("description (with parentheses)")
    .define("blacklist", List.of());
builder.push("visuals");
RATE = builder.define("rate", 0.08f, 0f, 1f);
builder.pop();
builder.pop();
'''
        self.assertEqual(wiki.config_rows(source), [
            ("general.blacklist", "List.of()", "TEST"),
            ("general.visuals.rate", "0.08f, 0f, 1f", "RATE"),
        ])

    def test_unrecognized_config_definition_fails(self):
        with self.assertRaisesRegex(ValueError, "missing"):
            wiki.config_rows("public static Supplier<Boolean> FLAG;\nFLAG = anotherBuilder.define();")

    def test_common_config_has_no_unregistered_placeholder_tail(self):
        source = (wiki.ROOT / wiki.JAVA_ROOT / "configs" / "CommonConfigs.java").read_text(encoding="utf-8")
        self.assertNotRegex(source, r"throw new AssertionError|LeafPileMode|VITRIFIED_LAVA")

    def test_unbalanced_sections_fail(self):
        with self.assertRaisesRegex(ValueError, "pop"):
            wiki.config_rows("builder.pop();")
        with self.assertRaisesRegex(ValueError, "Unclosed"):
            wiki.config_rows('builder.push("general");')

    def test_links_ignore_fenced_examples_and_inline_code(self):
        self.assertEqual(wiki.markdown_targets(
            "[real](README.md)\n```markdown\n[example](missing.md)\n```\n"
            "`[inline](missing.md)`\n![image](image.png)\n"
        ), ["README.md", "image.png"])

    def test_relative_links_and_external_urls(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "target file.md").write_text("# Target", encoding="utf-8")
            page = root / "page.md"
            page.write_text("[ok](target%20file.md#target) [web](https://example.com) "
                            "[bad](missing.md) [escape](../outside.md)", encoding="utf-8")
            errors = wiki.link_errors(root, page)
            self.assertEqual(len(errors), 2)
            self.assertIn("missing.md", errors[0])
            self.assertIn("../outside.md", errors[1])

    def test_generation_is_deterministic_and_reference_is_current(self):
        generated = wiki.generate(wiki.ROOT)
        self.assertEqual(generated, wiki.generate(wiki.ROOT))
        self.assertEqual(wiki.check(wiki.ROOT, generated), [])

    def test_reference_uses_root_layout_and_expanded_runtime_requirements(self):
        generated = wiki.generate(wiki.ROOT)
        self.assertIn("Build project: root only (no subprojects).", generated)
        self.assertIn("src/main/resources/fabric.mod.json", generated)
        self.assertIn("| `moonlight` | `>=1.20-2.12.11` |", generated)
        self.assertNotIn("common/src", generated)
        self.assertNotIn("fabric/src", generated)
        self.assertNotIn("${fabric_loader_version}", generated)

    def test_stale_reference_is_detected(self):
        errors = wiki.check(wiki.ROOT, wiki.generate(wiki.ROOT) + "\nchanged")
        self.assertTrue(any("stale" in error for error in errors))

    def test_wiki_slugs_separate_audiences(self):
        self.assertEqual(wiki.wiki_slug(Path("README.md")), "Home")
        self.assertEqual(wiki.wiki_slug(Path("players") / "README.md"), "Players")
        self.assertEqual(wiki.wiki_slug(Path("developers") / "README.md"), "Developers")
        self.assertEqual(wiki.wiki_slug(Path("players") / "Features.md"), "Players-Features")

    def test_export_translates_links_without_changing_code_examples(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            page = root / "docs" / "wiki" / "players" / "Example.md"
            page.parent.mkdir(parents=True)
            (root / "LICENSE").write_text("license", encoding="utf-8")
            page.write_text(
                "[home](../README.md)\n[source](../../../LICENSE)\n"
                "```markdown\n[example](../../../LICENSE)\n```\n"
                "`[inline](../../../LICENSE)`\n", encoding="utf-8",
            )
            rendered = wiki.wiki_content(root, page, "https://github.com/test/repo", "abc123")
            self.assertIn("[home](https://github.com/test/repo/wiki/Home)", rendered)
            self.assertIn("[source](https://github.com/test/repo/blob/abc123/LICENSE)", rendered)
            self.assertIn("[example](../../../LICENSE)", rendered)
            self.assertIn("`[inline](../../../LICENSE)`", rendered)

    def test_export_translates_code_formatted_labels_but_not_inline_examples(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            page = root / "docs" / "wiki" / "developers" / "Example.md"
            page.parent.mkdir(parents=True)
            (root / "LICENSE").write_text("license", encoding="utf-8")
            page.write_text(
                "[`source`](../../../LICENSE)\n[`home`](../README.md)\n"
                "``[`example`](../../../LICENSE)``\n", encoding="utf-8",
            )
            rendered = wiki.wiki_content(root, page, "https://github.com/test/repo", "abc123")
            self.assertIn("[`source`](https://github.com/test/repo/blob/abc123/LICENSE)", rendered)
            self.assertIn("[`home`](https://github.com/test/repo/wiki/Home)", rendered)
            self.assertIn("``[`example`](../../../LICENSE)``", rendered)

    def test_export_preserves_unmanaged_pages_and_removes_only_tracked_pages(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            (output / "Existing.md").write_text("Keep this page", encoding="utf-8")
            (output / "Obsolete.md").write_text("Previous export", encoding="utf-8")
            (output / ".wiki-published-pages.json").write_text('["Obsolete.md"]', encoding="utf-8")
            wiki.export_wiki(wiki.ROOT, output, "https://github.com/test/repo", "abc123")
            self.assertEqual((output / "Existing.md").read_text(encoding="utf-8"), "Keep this page")
            self.assertFalse((output / "Obsolete.md").exists())
            self.assertTrue((output / "Players.md").is_file())
            self.assertTrue((output / "Developers.md").is_file())
            self.assertTrue((output / "_Sidebar.md").is_file())

    def test_export_rejects_unsafe_tracking_paths(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            (output / ".wiki-published-pages.json").write_text('["../outside.md"]', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "Invalid"):
                wiki.export_wiki(wiki.ROOT, output, "https://github.com/test/repo", "abc123")

    def test_sidebar_uses_hub_order_and_keeps_unlisted_pages(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory) / "source"
            pages = root / "docs" / "wiki"
            (pages / "players").mkdir(parents=True)
            (pages / "developers").mkdir()
            (pages / "README.md").write_text("# Home\n", encoding="utf-8")
            (pages / "players" / "README.md").write_text(
                "# Players\n[Z](Z.md) [A](A.md) [Z again](Z.md)\n",
                encoding="utf-8",
            )
            (pages / "developers" / "README.md").write_text("# Developers\n", encoding="utf-8")
            for name in ("A", "B", "Z"):
                (pages / "players" / f"{name}.md").write_text(f"# {name}\n", encoding="utf-8")
            output = Path(directory) / "output"
            wiki.export_wiki(root / ".." / root.name, output, "https://github.com/test/repo", "abc123")
            sidebar = (output / "_Sidebar.md").read_text(encoding="utf-8")
            self.assertLess(sidebar.index("- [Z]"), sidebar.index("- [A]"))
            self.assertLess(sidebar.index("- [A]"), sidebar.index("- [B]"))
            self.assertEqual(sidebar.count("- [Z]"), 1)

    def test_sidebar_separates_players_and_developer_testing(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            wiki.export_wiki(wiki.ROOT, output, "https://github.com/test/repo", "abc123")
            sidebar = (output / "_Sidebar.md").read_text(encoding="utf-8")
            players, developers = sidebar.split("## [Developers]", 1)
            self.assertEqual(re.findall(r"^- \[([^\]]+)\]", players, re.M), [
                "Getting started", "Features and interactions", "Configuration", "Troubleshooting",
            ])
            self.assertIn("Developers-Feature-testing", developers)
            self.assertIn("Developers-Test-results", developers)
            self.assertLess(developers.index("Developers-Development"),
                            developers.index("Developers-Feature-testing"))
            self.assertLess(developers.index("Developers-Feature-testing"),
                            developers.index("Developers-Test-results"))
            self.assertNotIn("Players-", developers)
            legacy = (output / "Players-Feature-testing.md").read_text(encoding="utf-8")
            self.assertIn("Developers-Feature-testing", legacy)
            self.assertIn("Developers-Test-results", legacy)
            self.assertNotIn("Manual verdict", legacy)

    def test_procedures_and_results_have_matching_ids_and_summary(self):
        pages = wiki.ROOT / "docs" / "wiki" / "developers"
        procedures = (pages / "Feature-testing.md").read_text(encoding="utf-8")
        results = (pages / "Test-results.md").read_text(encoding="utf-8")
        procedure_ids = re.findall(r"^### ([A-Z]+\d{2})$", procedures, re.M)
        procedure_ids += re.findall(r"^\| (SB\d{2}) \|", procedures, re.M)
        rows = re.findall(r"^\| ([A-Z]+\d{2}) \| ([^|]+) \|", results, re.M)
        original_ids = {
            f"{prefix}{number:02}"
            for prefix, total in (
                ("W", 10), ("T", 11), ("G", 14), ("C", 13), ("H", 10),
                ("R", 14), ("Q", 9), ("SB", 6), ("N", 14), ("F", 6),
                ("ST", 8), ("SP", 8), ("FW", 8),
            )
            for number in range(1, total + 1)
        }
        self.assertTrue(original_ids.issubset(procedure_ids))
        self.assertEqual(len(procedure_ids), len(set(procedure_ids)))
        self.assertEqual(len(rows), len({case for case, _ in rows}))
        self.assertEqual(set(procedure_ids), {case for case, _ in rows})
        counts = Counter(result.strip() for _, result in rows)
        summary = {status: int(count) for status, count in
                   re.findall(r"^\| ([^|]+) \| (\d+) \|", results, re.M)}
        self.assertEqual(dict(counts), summary)
        self.assertIn(f"| **Total** | **{len(rows)}** |", results)
        self.assertNotIn("Not run", procedures)
        self.assertNotIn("Manual verdict", procedures)
        self.assertNotRegex(procedures, r"\b2\.\d+(?:\.\d+)?(?:-beta\.\d+)?\b|beta\.\d")


if __name__ == "__main__":
    unittest.main()
