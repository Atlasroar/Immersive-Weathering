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
