package pw.mihou.nexus.features.command.react

import org.javacord.api.DiscordApi
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.entity.message.component.LowLevelComponent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.listener.GloballyAttachableListener
import org.javacord.api.listener.interaction.ButtonClickListener
import org.javacord.api.util.event.ListenerManager
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.messages.NexusMessage
import java.awt.Color
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

typealias Subscription<T> = (oldValue: T, newValue: T) -> Unit
typealias Unsubscribe = () -> Unit

class React(private val api: DiscordApi) {
    private var message: NexusMessage = NexusMessage()
    private var unsubscribe: Unsubscribe = {}
    private var component: (Component.() -> Unit)? = null

    private var debounceTask: Cancellable? = null
    private var mutex = ReentrantLock()

    internal var __private__message: Message? = null

    fun view() = message

    fun render(component: Component.() -> Unit) {
        val element =  apply(component)

        val (unsubscribe, message) = element.render(api)
        this.message = message
        this.unsubscribe = unsubscribe
    }

    private fun apply(component: Component.() -> Unit): Component {
        this.component = component
        val element = Component()
        component(element)
        return element
    }

    fun <T> writable(value: T): Writable<T> {
        val element = Writable(value)
        element.subscribe { _, _ ->
            if (!mutex.tryLock()) return@subscribe
            val component = this.component ?: return@subscribe
            debounceTask?.cancel(false)
            debounceTask = Nexus.launch.scheduler.launch(250) {
                this.unsubscribe()

                debounceTask = null

                val message = __private__message
                if (message != null) {
                    val updater = message.createUpdater()
                    val view = apply(component)
                    this.unsubscribe = view.render(updater, api)
                    updater.replaceMessage()
                }
            }
            mutex.unlock()
        }
        return element
    }

    class Writable<T>(value: T) {
        private val subscribers: MutableList<Subscription<T>> = mutableListOf()
        private val _value: AtomicReference<T> = AtomicReference(value)
        fun set(value: T) {
            val oldValue = _value.get()
            _value.set(value)

            subscribers.forEach { Nexus.launcher.launch { it(oldValue, value) } }
        }
        fun getAndUpdate(updater: (T) -> T) {
            val oldValue = _value.get()
            _value.getAndUpdate(updater)

            val value = _value.get()
            subscribers.forEach { Nexus.launcher.launch { it(oldValue, value) } }
        }
        fun get(): T = _value.get()
        fun subscribe(subscription: Subscription<T>): Unsubscribe {
            subscribers.add(subscription)
            return { subscribers.remove(subscription) }
        }
        override fun toString(): String {
            return _value.get().toString()
        }

        override fun hashCode(): Int {
            return _value.get().hashCode()
        }

        override fun equals(other: Any?): Boolean {
            val value = _value.get()
            if (other == null && value == null) return true
            if (other == null) return false
            return value == other
        }
    }

    class Component {
        private var embeds: MutableList<EmbedBuilder> = mutableListOf()
        private var contents: String? = null
        private var components: MutableList<LowLevelComponent> = mutableListOf()
        private var listeners: MutableList<GloballyAttachableListener> = mutableListOf()

        private fun attachListeners(api: DiscordApi): Unsubscribe {
            val listenerManagers = listeners.map { api.addListener(it) }
            return { listenerManagers.forEach { managers -> managers.forEach { it.remove() } } }
        }

        fun render(api: DiscordApi): Pair<Unsubscribe, NexusMessage> {
            return attachListeners(api) to NexusMessage.with {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                components.chunked(3).map { ActionRow.of(it) }.forEach { this.addComponents(it) }
            }
        }

        fun render(updater: MessageUpdater, api: DiscordApi): Unsubscribe {
            updater.apply {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                components.chunked(3).map { ActionRow.of(it) }.forEach { this.addComponents(it) }
            }
            return attachListeners(api)
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
            fun Color(color: Color) {
                embed.setColor(color)
            }
            fun Timestamp(timestamp: Instant) {
                embed.setTimestamp(timestamp)
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
                    text += "${index + 1}. $node\n"
                }
                return text
            }
            fun time(instant: Instant, format: TimeFormat = TimeFormat.RELATIVE) = "<t:${instant.epochSecond}:${format.suffix}>"
            fun time(instant: Writable<Instant>, format: TimeFormat = TimeFormat.RELATIVE) = time(instant.get(), format)
        }
    }
}

enum class TimeFormat(val suffix: String) {
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    LONG_DATE_WITH_SHORT_TIME("f"),
    LONG_DATE_WITH_DAY_OF_WEEK_AND_SHORT_TIME("F"),
    RELATIVE("R");
}