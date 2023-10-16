package pw.mihou.nexus.features.react

import org.javacord.api.DiscordApi
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.LowLevelComponent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.listener.GloballyAttachableListener
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.features.messages.NexusMessage
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KProperty

typealias Subscription<T> = (oldValue: T, newValue: T) -> Unit
typealias Unsubscribe = () -> Unit

typealias RenderSubscription = () -> Unit

class React internal constructor(private val api: DiscordApi, private val renderMode: RenderMode) {
    internal var rendered: Boolean = false

    internal var message: NexusMessage? = null
    internal var messageBuilder: MessageBuilder? = null

    private var unsubscribe: Unsubscribe = {}
    private var component: (Component.() -> Unit)? = null

    private var debounceTask: Cancellable? = null
    private var mutex = ReentrantLock()

    internal var resultingMessage: Message? = null

    internal var firstRenderSubscribers = mutableListOf<RenderSubscription>()
    internal var renderSubscribers = mutableListOf<RenderSubscription>()

    companion object {
        var debounceMillis = 250L
    }

    internal enum class RenderMode {
        Interaction,
        Message
    }

    fun onRender(subscription: RenderSubscription) {
        renderSubscribers.add(subscription)
    }

    fun onInitialRender(subscription: RenderSubscription) {
        firstRenderSubscribers.add(subscription)
    }

    fun render(component: Component.() -> Unit) {
        val element = apply(component)

        when(renderMode) {
            RenderMode.Interaction -> {
                val (unsubscribe, message) = element.render(api)
                this.message = message
                this.unsubscribe = unsubscribe
            }
            RenderMode.Message -> {
                val builder = MessageBuilder()
                val unsubscribe = element.render(builder, api)

                this.messageBuilder = builder
                this.unsubscribe = unsubscribe
            }
        }
        this.rendered = true
    }

    private fun apply(component: Component.() -> Unit): Component {
        this.component = component
        val element = Component()

        if (!rendered) {
            firstRenderSubscribers.forEach { it() }
        }

        renderSubscribers.forEach { it() }

        component(element)
        return element
    }

    fun <T> writable(value: T): Writable<T> {
        val element = Writable(value)
        return expand(element)
    }

    fun <T> expand(writable: Writable<T>): Writable<T> {
        writable.subscribe { _, _ ->
            if (!mutex.tryLock()) return@subscribe
            val component = this.component ?: return@subscribe
            debounceTask?.cancel(false)
            debounceTask = Nexus.launch.scheduler.launch(debounceMillis) {
                this.unsubscribe()

                debounceTask = null

                val message = resultingMessage
                if (message != null) {
                    val updater = message.createUpdater()
                    val view = apply(component)
                    this.unsubscribe = view.render(updater, api)
                    updater.replaceMessage()
                }
            }
            mutex.unlock()
        }
        return writable
    }

    class Writable<T>(value: T) {
        private val subscribers: MutableList<Subscription<T>> = mutableListOf()
        private val _value: AtomicReference<T> = AtomicReference(value)
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return _value.get()
        }
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            set(value)
        }
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
        internal var embeds: MutableList<EmbedBuilder> = mutableListOf()
        internal var contents: String? = null
        internal var components: MutableList<LowLevelComponent> = mutableListOf()
        internal var listeners: MutableList<GloballyAttachableListener> = mutableListOf()
        internal var uuids: MutableList<String> = mutableListOf()

        private fun attachListeners(api: DiscordApi): Unsubscribe {
            val listenerManagers = listeners.map { api.addListener(it) }
            return {
                listenerManagers.forEach { managers -> managers.forEach { it.remove() } }
                uuids.forEach { NexusUuidAssigner.deny(it) }
                uuids.clear()
            }
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

        // TODO: Reduce code duplication by using MessageBuilderBase (currently package-private)
        //       https://discord.com/channels/151037561152733184/151326093482262528/1163425854186065951
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

        fun render(builder: MessageBuilder, api: DiscordApi): Unsubscribe {
            builder.apply {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }
                components.chunked(3).map { ActionRow.of(it) }.forEach { this.addComponents(it) }
            }
            return attachListeners(api)
        }
    }
}