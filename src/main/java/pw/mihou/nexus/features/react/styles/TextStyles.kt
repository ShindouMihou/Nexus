package pw.mihou.nexus.features.react.styles

import pw.mihou.nexus.features.react.React
import java.time.Instant

interface TextStyles {
    private fun renderTextStyles(bold: Boolean = false, underline: Boolean = false, italic: Boolean = false,
                                 strikethrough: Boolean = false, spoiler: Boolean): Pair<String, String> {
        var prefix = ""
        var suffix = ""

        if (bold) {
            prefix += "**"
            suffix += "**"
            if (italic) {
                prefix += "*"
                suffix += "*"
            }
        }

        if (underline) {
            prefix += "__"
            suffix = "__$suffix"
        }

        if (italic && !bold) {
            prefix += "*"
            suffix = "*$suffix"
        }

        if (strikethrough) {
            prefix += "~~"
            suffix = "~~$suffix"
        }

        if (spoiler) {
            prefix += "`"
            suffix = "`$suffix"
        }

        return prefix to suffix
    }

    fun p(text: String, bold: Boolean = false, underline: Boolean = false, italic: Boolean = false,
          strikethrough: Boolean = false, spoiler: Boolean = false): String {
        val (prefix, suffix) = renderTextStyles(bold, underline, italic, strikethrough, spoiler)
        return prefix + text + suffix
    }

    fun bold(text: String) = "**$text**"
    fun italic(text: String) = "*$text*"
    fun mark(text: String) = "`$text`"
    fun del(text: String) = "~~$text~~"

    fun br(): String = "\n"

    fun link(text: String, href: String, bold: Boolean = false, underline: Boolean = false, italic: Boolean = false,
             strikethrough: Boolean = false, spoiler: Boolean = false): String {
        val (prefix, suffix) = renderTextStyles(bold, underline, italic, strikethrough, spoiler)
        return "$prefix[$text]($href)$suffix"
    }

    fun h1(text: String): String {
        return "# $text"
    }
    fun h2(text: String): String {
        return "## $text"
    }
    fun h3(text: String): String {
        return "### $text"
    }
    fun ul(vararg nodes: String): String {
        return nodes.joinToString("\n") { "* $it" }
    }
    fun ol(vararg nodes: String): String {
        var text = ""
        for ((index, node) in nodes.withIndex()) {
            text += "${index + 1}. $node\n"
        }
        return text
    }
    fun codeblock(language: String, vararg nodes: String): String {
        return "```$language\n${nodes.joinToString("")}\n```"
    }
    fun blockquote(vararg nodes: String): String {
        return nodes.joinToString("\n") { "> $it"}
    }
    fun time(instant: Instant, format: TimeFormat = TimeFormat.RELATIVE) = "<t:${instant.epochSecond}:${format.suffix}>"
    fun time(instant: React.Writable<Instant>, format: TimeFormat = TimeFormat.RELATIVE) = time(instant.get(), format)
}