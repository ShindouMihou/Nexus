package pw.mihou.nexus.features.command.react.elements

import pw.mihou.nexus.features.command.react.React
import pw.mihou.nexus.features.command.react.styles.TextStyles

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
}