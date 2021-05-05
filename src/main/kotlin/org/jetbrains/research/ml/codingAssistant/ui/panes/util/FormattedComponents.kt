package org.jetbrains.research.ml.codingAssistant.ui.panes.util


interface Formatter<T> {
    fun format(t: T): T
}


class LowerCaseFormatter : Formatter<String> {
    override fun format(t: String): String {
        return t.toLowerCase()
    }
}

class LowerCaseWithColonFormatter : Formatter<String> {
    override fun format(t: String): String {
//  Drop all ':' to be sure there will be only one ':'
        val noColonsString = t.dropLastWhile { it == ':' }
        return "${noColonsString.toLowerCase()}:"
    }
}

class CapitalCaseFormatter : Formatter<String> {
    override fun format(t: String): String {
        return t.capitalize()
    }

}
