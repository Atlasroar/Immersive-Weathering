import tempfile
import unittest
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


if __name__ == "__main__":
    unittest.main()
