# Licensing and attribution

[Wiki home](../README.md) | [Players](../players/README.md) | [Developers](README.md)

## Upstream license

This fork retains the upstream GNU Lesser General Public License, version 3,
verbatim in [LICENSE](../../../LICENSE). The existing file was already identical
to the upstream `1.20.0-multiloader` license when this wiki was introduced; no
relicensing or replacement of author notices was necessary.

LGPLv3 incorporates GPLv3 terms with additional permissions. The unmodified
GNU GPLv3 text is supplied in [COPYING](../../../COPYING), obtained from
[GNU's official license text](https://www.gnu.org/licenses/gpl-3.0.txt).
These two documents provide the license terms; this guide is not legal advice.

Fabric metadata declares `LGPLv3`. Preserve upstream author credits and applicable
notices when redistributing modifications. Do not infer an "or later" grant merely
from the LGPL text explaining how such grants work.

## Third-party material

[ATTRIBUTIONS](../../../ATTRIBUTIONS) credits InspectorJ's icicle cracking sounds
and identifies their CC BY 3.0 license. Retain that notice and its source/license
links. Do not assume every external asset inherits the code's LGPL license.

## Scorchful sandstorm adaptation

The 2.3.0 storm integration pins
[TheDeathlyCow/Scorchful d74712e9b5fc4348aa8ad9c417ce938daf8bc0a1](https://github.com/TheDeathlyCow/scorchful/tree/d74712e9b5fc4348aa8ad9c417ce938daf8bc0a1),
not a moving branch. Its root LICENSE is LGPLv3; its pinned manifest's ARR value
is stale according to the integrating maintainer's explicit clarification that
the author and distribution pages confirm intended LGPLv3. The user selected
use of those project assets under that intended license. No CC0 license is
invented and third-party rights are not reassigned.

[sandstorm-NOTICE.txt](../../../src/main/resources/sandstorm-NOTICE.txt) records
the original author/source, feature commit, source paths, 2026-11-02 modification
date, adaptations and limitations. Three PNG dust sprites and nine Ogg wind
files are byte-identical to the pin; the particle JSON changes namespace.
[The resource guard](../../../tools/sandstorm_resources.py) stores/checks all
twelve original SHA-256 values, registration/subtitle inventory and notice,
including the remapped and sources artifacts. Modified Java/resources and notices
are included in the corresponding sources jar; existing LICENSE, COPYING and
the original InspectorJ attribution are retained unchanged, with the new
provenance **appended** to ATTRIBUTIONS.

Pinned source has no per-file third-party wind mapping. Later upstream CREDITS
mentions Pixabay generally and a Wagna sandfall (CC0), but does not map these
nine wind files to either. Preserve that provenance limit: do not label these
winds Wagna/CC0 or assert that all external audio automatically inherits the
code license. Retain any additional verified applicable notice if discovered.
No unrelated Scorchful armor, banner, cauldron or worldgen assets are imported.

## Freezing wind and snowstorm adaptation

Unreleased integration pins
[Frostiful f017b15a2b23a6474d03d8e4b4e9e244247ffa24](https://github.com/TheDeathlyCow/frostiful/tree/f017b15a2b23a6474d03d8e4b4e9e244247ffa24)
and [Immersive Storms 66c883b5c1facba2219c9a31e1f93acfe2e333c9](https://github.com/TheDeathlyCow/immersive-storms/tree/66c883b5c1facba2219c9a31e1f93acfe2e333c9),
both by TheDeathlyCow under repository-declared LGPLv3. This is a standalone
source/presentation adaptation, not a runtime dependency. Immersive Storms at
that pin targets modern Minecraft/Java, not a nonexistent 1.20.1 port.

[freezing-wind-NOTICE.txt](../../../src/main/resources/freezing-wind-NOTICE.txt)
lists exact upstream roots/paths, imported six wind frames/frozen-torch PNG and
three shared grain PNGs, introduction commits, modifications, corresponding IW
source and limitations. The namespaces/JSON and native torch identity/loot are
adapted; the ten PNGs remain byte-identical.
[weather-adaptations-LGPL-3.0.txt](../../../src/main/resources/weather-adaptations-LGPL-3.0.txt)
retains the upstream license verbatim; original LICENSE/COPYING/InspectorJ and
Scorchful notices remain, with attribution appended rather than replaced.
`tools/wind_resources.py` verifies PNG SHA-256, native torch loot/orientations,
notices and corresponding source in the binary and sources jars.

The six gust frames postdate Frostiful's LGPL license change; no artwork
carve-out or specific third-party claim was found. Immersive Storms explicitly
declares project LGPL in LICENSE/README without an art exception. This is the
repository-declared provenance basis, **not forensic certification of each pixel**
or an "or later"/CC0 assertion. Shared dust artwork is honestly identified as
shared, not an upstream snow-exclusive sprite sheet.

**No new recordings** are imported: no Frostiful howls/woosh or Immersive Storms
soft/strong/sandstorm winds. Unmapped third-party audio provenance/redistribution
terms remain unresolved; generic later Pixabay/CC0 credits are not reassigned
to those recordings. The user's selected alternative reuses IW's existing
`weather.sandstorm` event; its already-recorded provenance limits remain above.

## Distribution checklist

Keep source available under applicable terms, preserve notices, identify
modifications as required, and accompany distributions with required license and
attribution material. Audit packaged artifacts as well as the repository: adding
license documents to the repository is not proof that every build artifact
already embeds them.

The beta's remapped jar and sources jar both embed the exact repository
`LICENSE`, `COPYING`, and `ATTRIBUTIONS`; `tools/fabric.py` compares their bytes.
Moonlight **1.20-2.12.11** declares `gplv3` in its artifact metadata and is
resolved as an external Fabric
dependency, not redistributed inside IW. Historical binary copies are removed
from the active source tree; archive tags retain the historical repository.

Check licenses before introducing dependency code, textures, audio, translations,
or other third-party material. Prefer referring to an external dependency over
copying its implementation without a license review.
