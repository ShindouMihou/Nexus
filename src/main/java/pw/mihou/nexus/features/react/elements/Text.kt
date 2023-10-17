package pw.mihou.nexus.features.react.elements

import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.react.styles.TextStyles

fun React.Component.Text(text: Text.() -> Unit) {
    val element = Text()
    text(element)

    contents = element.view()
}

class Text: TextStyles {
    private var content: String = ""
    fun view() = content
    fun Body(vararg nodes: String)  {
        content = nodes.joinToString("")
    }
    fun SpacedBody(vararg nodes: String) {
        content = nodes.joinToString("\n")
    }
}