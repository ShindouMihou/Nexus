package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<String>.plus(text: String): React.Writable<String> {
    this.update { it + text }
    return this
}
operator fun React.Writable<String>.plusAssign(text: String) {
    this.plus(text)
}