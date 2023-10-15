package pw.mihou.nexus.features.command.react

import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.entity.message.component.LowLevelComponent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.listener.GloballyAttachableListener
import org.javacord.api.listener.interaction.ButtonClickListener
import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.messages.NexusMessage

class React(private val ev: NexusCommandEvent) {
    private var message: NexusMessage = NexusMessage()
    fun view() = message

    fun render(component: Component.() -> Unit) {
        val element = Component()
        component(element)

        message = element.render(ev)
    }

    class Component {
        private var embeds: MutableList<EmbedBuilder> = mutableListOf()
        private var contents: String? = null
        private var components: MutableList<LowLevelComponent> = mutableListOf()
        private var listeners: MutableList<GloballyAttachableListener> = mutableListOf()

        fun render(event: NexusCommandEvent): NexusMessage {
            listeners.forEach { event.api.addListener(it) }
            return NexusMessage.with {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                components.chunked(3).map { ActionRow.of(it) }.forEach { this.addComponents(it) }
            }
        }

        fun Embed(embed: Embed.() -> Unit) {
            val element = Embed()
            embed(element)

            embeds.add(element.view())
        }

        fun Text(text: Text.() -> Unit) {
            val element = Text()
            text(element)

            contents = element.view()
        }

        fun Button(style: ButtonStyle = ButtonStyle.PRIMARY,
                   label: String,
                   emoji: String? = null,
                   disabled: Boolean = false,
                   onClick: ((event: ButtonClickEvent) -> Unit)? = {}) {
            val button = ButtonBuilder()
            button.setStyle(style)
            button.setLabel(label)

            if (emoji != null) {
                button.setEmoji(emoji)
            }

            button.setDisabled(disabled)

            val uuid = NexusUuidAssigner.request()
            button.setCustomId(uuid)

            if (onClick != null) {
                listeners += ButtonClickListener {
                    if (it.buttonInteraction.customId != uuid) {
                        return@ButtonClickListener
                    }

                    onClick(it)
                }
            }

            components += button.build()
        }

        fun UrlButton(label: String, url: String, emoji: String? = null) {
            val button = ButtonBuilder()
            button.setStyle(ButtonStyle.LINK)
            button.setLabel(label)
            button.setUrl(url)

            components += button.build()
        }

        inner class Text: TextStyles {
            private var content: String = ""
            fun view() = content
            fun Body(vararg nodes: String)  {
                content = nodes.joinToString("")
            }
        }

        inner class Embed: TextStyles {
            private val embed = EmbedBuilder()
            fun view() = embed

            fun Title(text: String) {
                embed.setTitle(text)
            }

            fun Body(vararg nodes: String) {
                embed.setDescription(nodes.joinToString(""))
            }
            fun Field(name: String, vararg nodes: String) {
                embed.addField(name, nodes.joinToString(""))
            }
            fun Image(url: String) {
                embed.setImage(url)
            }
            fun Thumbnail(url: String) {
                embed.setThumbnail(url)
            }
        }

        interface TextStyles {
            private fun renderTextStyles(bold: Boolean = false, underline: Boolean = false, italic: Boolean = false): Pair<String, String> {
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
                    suffix += "__"
                }

                if (italic && !bold) {
                    prefix += "*"
                    suffix += "*"
                }

                return prefix to suffix
            }

            fun p(text: String, bold: Boolean = false, underline: Boolean = false, italic: Boolean = false): String {
                val (prefix, suffix) = renderTextStyles(bold, underline, italic)
                return prefix + text + suffix
            }

            fun br(): String = "\n"

            fun link(text: String, href: String, bold: Boolean = false, underline: Boolean = false, italic: Boolean = false): String {
                val (prefix, suffix) = renderTextStyles(bold, underline, italic)
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
                    text += "${index + 1}. $node"
                }
                return text
            }
        }
    }
}
