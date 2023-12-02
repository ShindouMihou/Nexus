package pw.mihou.nexus.features.react.elements

import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.react.styles.TextStyles

fun React.Component.Text(text: Text.() -> Unit) {
    val element = Text()
    text(element)

    contents = element.content
}

class Text: TextStyles {
    internal var content: String = ""
    fun Body(vararg nodes: String)  {
        content = nodes.joinToString("")
    }
    fun SpacedBody(vararg nodes: String) {
        content = nodes.joinToString("\n")
    }
    fun Body(spaced: Boolean = false, builder: MutableList<String>.() -> Unit) {
        val backing = mutableListOf<String>()
        builder(backing)
        content = if (spaced) backing.joinToString("\n") else backing.joinToString()
    }
}