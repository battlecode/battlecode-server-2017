@file:Suppress("PackageDirectoryMismatch")

package instrumentertest

import java.util.*

class KtClass

object KtObject {
    val VAL = 65
    const val CONST = "const"

    fun doStuff() {
    }

}

fun foo(str: String): Int {
    return str.length
}

fun run() {

    foo(KtObject.CONST)

    foo("hello world" + KtObject.VAL)

    val list = ArrayList<String>()

    list.size

    list.add("String")

    KtObject.doStuff()

    KtClass()

}
