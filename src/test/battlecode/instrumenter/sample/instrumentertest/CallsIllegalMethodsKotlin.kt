@file:Suppress("PackageDirectoryMismatch")

package instrumentertest

import java.io.PrintStream

object CallsIllegalMethodsKotlin {
    // Kotlin doesn't have truly static initializer blocks.
    // The closest equivalent is static variable initializers.

    object CallsWait {
        @JvmField val value = java.lang.Object().wait()
    }

    object CallsClassForName {
        @JvmField val value = Class.forName("?!?!")
    }

    object CallsStringIntern {
        @JvmField val value = "unpaid".intern()
    }

    object CallsSystemNanoTime {
        @JvmField val value = System.nanoTime()
    }

    object CreatesFilePrintStream {
        @JvmField val value = PrintStream("???")
    }

}
