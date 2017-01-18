@file:Suppress("PackageDirectoryMismatch")

package instrumentertest

import battlecode.common.Team
import java.util.*

object UsesEnumMapKotlin {

    @JvmField val value = EnumMap<Team, Int>(Team::class.java)

}