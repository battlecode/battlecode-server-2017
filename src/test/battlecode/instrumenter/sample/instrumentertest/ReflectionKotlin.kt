@file:Suppress("PackageDirectoryMismatch")

package instrumentertest

object ReflectionKotlin {

    @JvmField val value = ReflectionKotlin::class.java.classLoader

}
