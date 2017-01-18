@file:Suppress("PackageDirectoryMismatch")

package instrumentertest

object UsesLambdaKotlin {

    fun bar(func: (Int) -> Int): String {
        return func(5).toString()
    }

    @JvmStatic fun run() {

        val lambda = { it: Int -> it * 2 }

        lambda(4)

        bar(lambda)

        bar { it + 3 }

    }

}
