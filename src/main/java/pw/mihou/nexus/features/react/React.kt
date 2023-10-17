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
typealias ReactComponent = React.Component.() -> Unit

/**
 * [React] is the React-Svelte inspired method of rendering (or sending) messages as response to various scenarios such
 * as message commands, slash commands, context menus and different kind of magic. We recommend using the available
 * `event.R` method instead as it is mostly designed to enable this to work for your situation, or instead use the
 * available `interaction.R` method for interactions.
 */
class React internal constructor(private val api: DiscordApi, private val renderMode: RenderMode) {
    private var rendered: Boolean = false

    internal var message: NexusMessage? = null
    internal var messageBuilder: MessageBuilder? = null

    private var unsubscribe: Unsubscribe = {}
    private var component: (Component.() -> Unit)? = null

    private var debounceTask: Cancellable? = null
    private var mutex = ReentrantLock()

    internal var resultingMessage: Message? = null

    private var firstRenderSubscribers = mutableListOf<RenderSubscription>()
    private var renderSubscribers = mutableListOf<RenderSubscription>()

    companion object {
        /**
         * Defines how long we should wait before proceeding to re-render the component, this is intended to ensure
         * that all other states being changed in that period is applied as well, preventing multiple unnecessary re-renders
         * which can be costly as we send HTTP requests to Discord.
         *
         * As a default, we recommend within 25ms to 250ms depending on how long the betweens of your state changes are,
         * you can go even lower as long as the states are being changed immediately, otherwise, keeping it as default
         * is recommended.
         */
        var debounceMillis = 25L
    }

    internal enum class RenderMode {
        Interaction,
        Message
    }

    /**
     * Subscribes a task to be ran whenever the component is being rendered, this happens on the initial render
     * and re-renders but takes a lower priority than the ones in [onInitialRender].
     * @param subscription the subscription to execute on render.
     */
    fun onRender(subscription: RenderSubscription) {
        renderSubscribers.add(subscription)
    }

    /**
     * Subscribes a task to be ran whenever the component is being rendered. This happens first before the actual
     * component being rendered, therefore, you can use this to load data before the component is actually rendered.
     * @param subscription the subscription to execute on first render.
     */
    fun onInitialRender(subscription: RenderSubscription) {
        firstRenderSubscribers.add(subscription)
    }

    /**
     * Renders the given component, this will also be used to re-render the component onwards. Note that using two
     * renders will result in the last executed render being used.
     * @param component the component to render.
     */
    fun render(component: ReactComponent) {
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

    /**
     * Creates a [Writable] that can react to changes of the value, allowing you to re-render the message
     * with the new states. Internally, this simply creates a [Writable] then adds a subscriber to re-render
     * the message whenever the state changes (debounced by [React.debounceMillis] milliseconds).
     *
     * We recommend using [Writable]'s constructor and then using [React.expand] to add the subscriber whenever you need
     * to create a [Writable] outside of the [React] scope. (View the source code of this method to see how it looks).
     *
     * To pass a [Writable], we recommend creating another variable that has the [Writable] itself as the value and another
     * one that uses `by` and then passing the [Writable] instead. (Refer to the wiki for this, as function parameters are not mutable
     * and delegated variables pass their [Writable.getValue] result, so changes cannot be listened).
     *
     * @param value the initial value.
     * @return a [Writable] with a [Subscription] that will re-render the [ReactComponent] when the state changes.
     */
    fun <T> writable(value: T): Writable<T> {
        val element = Writable(value)
        return expand(element)
    }

    /**
     * Adds a [Subscription] that enables the [ReactComponent] to be re-rendered whenever the value of the [Writable]
     * changes, this is what [writable] uses internally to react to changes.
     * @param writable the writable to subscribe.
     * @return the [Writable] with the re-render subscription attached.
     */
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

    /**
     * Writable are the equivalent to state in React.js, or [writable] in Svelte (otherwise known as `$state` in Svelte Runes),
     * these are simply properties that will execute subscribed tasks whenever the property changes, enabling reactivity.
     * [React] uses this to support re-rendering the [ReactComponent] whenever a state changes, allowing developers to write
     * incredibly reactive yet beautifully simple code that are similar to Svelte.
     *
     * We recommend using the constructor method to create a [Writable] for use cases outside of [React] scope, otherwise
     * use the [writable] method to create a [Writable] inside of [React] scope which will have a re-render subscription.
     * (Read the wiki for more information).
     *
     */
    class Writable<T>(value: T) {
        private val subscribers: MutableList<Subscription<T>> = mutableListOf()
        private val _value: AtomicReference<T> = AtomicReference(value)

        /**
         * Gets the value of this [Writable]. This is intended to be used for delegation. You may be looking for
         * [get] instead which allows you to directly get the value.
         */
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return _value.get()
        }

        /**
         * Sets the value of this [Writable]. This is intended to be used for delegation. You may be looking for
         * [set] or [getAndUpdate] instead which allows you to manipulate the [Writable]'s value.
         */
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            set(value)
        }

        /**
         * Manipulates the value of the [Writable].
         * This will run all the subscriptions asynchronously after the value has been changed, ensuring that
         * all subscriptions are executed without interfering or delaying one another.
         *
         * When performing things such as increment, decrements, or anything that requires the current value, we
         * recommend using [getAndUpdate] instead which will allow you to atomically update the value.
         *
         * @param value the new value of the [Writable].
         */
        fun set(value: T) {
            val oldValue = _value.get()
            _value.set(value)

            subscribers.forEach { Nexus.launcher.launch { it(oldValue, value) } }
        }

        /**
         * Atomically updates the value of the [Writable]. This is recommended to use when manipulating the value of, say
         * a numerical value, for instance, incrementing, decrementing, multiplying, etc. as this is performed atomically
         * which stops a lot of thread-unsafety.
         *
         * Similar to [set], this executes all the subscriptions asynchronously.
         * @param updater the updater to update the value of the [Writable].
         */
        fun getAndUpdate(updater: (T) -> T) {
            val oldValue = _value.get()
            _value.getAndUpdate(updater)

            val value = _value.get()
            subscribers.forEach { Nexus.launcher.launch { it(oldValue, value) } }
        }

        /**
         * Gets the current value of the [Writable]. If you need to listen to changes to the value,
         * use the [subscribe] method instead to subscribe to changes.
         *
         * @return the value of the [Writable].
         */
        fun get(): T = _value.get()

        /**
         * Subscribes to changes to the value of the [Writable]. This is ran asynchronously after the value has
         * been changed.
         *
         * @param subscription the task to execute upon a change to the value is detected.
         * @return an [Unsubscribe] method to unsubscribe the [Subscription].
         */
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

    /**
     * An internal class of [React]. You do not need to touch this at all, and it is not recommended to even create
     * this by yourself as it will do nothing.
     */
    class Component internal constructor() {
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

        private fun chunkComponents(): List<ActionRow> {
            val actionRows = mutableListOf<ActionRow>()
            var lowLevelComponents = mutableListOf<LowLevelComponent>()

            for ((index, component) in components.withIndex()) {
                if (component.isSelectMenu) {
                    actionRows += ActionRow.of(component)
                    continue
                } else {
                    if (lowLevelComponents.size >= 3) {
                        actionRows += ActionRow.of(lowLevelComponents)
                        lowLevelComponents = mutableListOf()
                    }

                    lowLevelComponents += component
                }

                if (index == (components.size - 1) && lowLevelComponents.size <= 3) {
                    actionRows += ActionRow.of(lowLevelComponents)
                    lowLevelComponents = mutableListOf()
                }
            }

            if (lowLevelComponents.isNotEmpty() && lowLevelComponents.size <= 3) {
                actionRows += ActionRow.of(lowLevelComponents)
            }

            return actionRows
        }

        fun render(api: DiscordApi): Pair<Unsubscribe, NexusMessage> {
            return attachListeners(api) to NexusMessage.with {
                this.removeAllEmbeds()
                this.addEmbeds(embeds)

                if (contents != null) {
                    this.setContent(contents)
                }

                chunkComponents().forEach { this.addComponents(it) }
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
                chunkComponents().forEach { this.addComponents(it) }
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
                chunkComponents().forEach { this.addComponents(it) }
            }
            return attachListeners(api)
        }
    }
}