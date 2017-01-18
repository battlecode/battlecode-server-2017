@file:Suppress("PackageDirectoryMismatch")

package instrumentertest

import java.util.*

object LegalMethodReferenceKotlin {

    @JvmStatic
    fun run() {

        @Suppress("UNUSED_VARIABLE")
        val randomSupplier = { Random() }

    }

}
