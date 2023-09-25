package pw.mihou.nexus.features.command.facade

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.Nexus.sharding
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore.execute
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore.middlewares
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.commons.NexusInteractionEvent
import pw.mihou.nexus.features.messages.NexusMessage
import pw.mihou.nexus.sharding.NexusShardingManager
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

@JvmDefaultWithCompatibility
interface NexusCommandEvent : NexusInteractionEvent<SlashCommandCreateEvent, SlashCommandInteraction> {

    /**
     * Gets the base event that was received from Javacord. This is usually nothing
     * of use to the end-user.
     *
     * @return The base event that was received.
     * @see NexusCommandEvent.event
     */
    @get:Deprecated("Standardized methods across the framework.", ReplaceWith("getEvent()"))
    val baseEvent: SlashCommandCreateEvent get() = event

    override val interaction: SlashCommandInteraction get() = event.slashCommandInteraction
    override val event: SlashCommandCreateEvent

    /**
     * Gets the command that was executed.
     *
     * @return The command that was executed.
     */
    val command: NexusCommand

    /**
     * Gets the [NexusShardingManager] that is associated with the [Nexus] instance.
     *
     * @return The Nexus Shard Manager associated with this Nexus instance.
     */
    val shardManager: NexusShardingManager get() = sharding

    /**
     * Gets the immediate response builder for this command and adds the
     * [MessageFlag.EPHEMERAL] flag ahead of time.
     *
     * @return The immediate response builder associated with this command with the
     * ephemeral flag added.
     * @see NexusCommandEvent.respondNowEphemerally
     */
    @Deprecated("Standardized methods across the framework.", ReplaceWith("respondNowEphemerally()"))
    fun respondNowAsEphemeral(): InteractionImmediateResponseBuilder {
        return respondNowEphemerally()
    }

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this command with the
     * ephemeral flag attached.
     *
     * @return The [InteractionOriginalResponseUpdater] with the ephemeral flag attached.
     * @see NexusCommandEvent.respondLaterEphemerally
     */
    @Deprecated("Standardized methods across the framework.", ReplaceWith("respondLaterEphemerally()"))
    fun respondLaterAsEphemeral(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return respondLaterEphemerally()
    }

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this command with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The [InteractionOriginalResponseUpdater] for this interaction.
     * @see NexusCommandEvent.respondLaterEphemerallyIf
     */
    @Deprecated("Standardized methods across the framework.", ReplaceWith("respondLaterEphemerallyIf()"))
    fun respondLaterAsEphemeralIf(predicate: Boolean): CompletableFuture<InteractionOriginalResponseUpdater> {
        return respondLaterEphemerallyIf(predicate)
    }

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this command with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The [InteractionOriginalResponseUpdater] for this interaction.
     * @see NexusCommandEvent.respondLaterEphemerallyIf
     */
    @Deprecated("Standardized methods across the framework.", ReplaceWith("respondLaterEphemerallyIf()"))
    fun respondLaterAsEphemeralIf(predicate: Predicate<Void?>): CompletableFuture<InteractionOriginalResponseUpdater> {
        return respondLaterEphemerallyIf(predicate.test(null))
    }

    /**
     * Gets the event local store that is used to contain shared fields accessible from middlewares, command and
     * afterwares themselves. You can use this to store data such as whether the command was completed successfully
     * or other related.
     *
     * @return The event-local store from this event.
     */
    fun store(): MutableMap<String, Any>

    /**
     * Gets the value of the given key from the [NexusCommandEvent.store] and maps it into the type given
     * if possible, otherwise returns null.
     *
     * @param key   The key to get from the [NexusCommandEvent.store].
     * @param type  The type expected of the value.
     * @param <T>   The type expected of the value.
     *
     * @return The value mapped with the key in [NexusCommandEvent.store] mapped to the type, otherwise null.
    </T> */
    operator fun <T> get(key: String, type: Class<T>): T? {
        if (!store().containsKey(key)) return null
        val `object` = store()[key]
        return if (type.isAssignableFrom(`object`!!.javaClass)) { type.cast(`object`) } else null
    }

    /**
     * Gets the value of the given key from the [NexusCommandEvent.store].
     *
     * @param key   The key to get from the [NexusCommandEvent.store].*
     * @return The value mapped with the key in [NexusCommandEvent.store], otherwise null.
     */
    operator fun get(key: String): Any? {
        return store()[key]
    }

    /**
     * A short-hand expression for placing a key-value pair to [NexusCommandEvent.store].
     *
     * @param key   The key to insert to the store.
     * @param value The value to insert to the store.
     */
    fun store(key: String, value: Any) {
        store()[key] = value
    }

    /**
     * Activates one or more middlewares and executes the success consumer once all middlewares succeed without
     * throwing an issue. This does not support [InteractionOriginalResponseUpdater] or related, it will use
     * [InteractionImmediateResponseBuilder] instead.
     *
     * @param middlewares   the middlewares to activate.
     * @param success       what to do when all the middlewares succeed.
     */
    fun middlewares(middlewares: List<String>, success: Consumer<Void?>) {
        val middlewareGate = execute(
            this,
            middlewares(middlewares)
        )
        if (middlewareGate == null) {
            success.accept(null)
            return
        }
        val response = middlewareGate.response()
        if (response != null) {
            var responder = respondNow()
            if (response.ephemeral) {
                responder = respondNowEphemerally()
            }
            response.into(responder).respond().exceptionally(ExceptionLogger.get())
        }
    }

    /**
     * Automatically answers either deferred or non-deferred based on circumstances, to configure the time that it should
     * consider before deferring (this is based on time now - (interaction creation time - auto defer time)), you can
     * modify [pw.mihou.nexus.configuration.modules.NexusGlobalConfiguration.autoDeferAfterMilliseconds].
     *
     * @param ephemeral whether to respond ephemerally or not.
     * @param response the response to send to Discord.
     * @return the response from Discord.
     */
    fun autoDefer(ephemeral: Boolean, response: Function<Void?, NexusMessage>): CompletableFuture<NexusAutoResponse>
}
