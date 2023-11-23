package pw.mihou.nexus.features.commons

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.interaction.Interaction
import org.javacord.api.interaction.InteractionBase
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.messages.NexusMessage
import pw.mihou.nexus.features.react.React
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

object Deferrable {
    internal fun <Interaction: InteractionBase> autoDefer(
        interaction: Interaction,
        updater: AtomicReference<CompletableFuture<InteractionOriginalResponseUpdater>?>,
        ephemeral: Boolean,
        response: Function<Void?, NexusMessage>
    ): CompletableFuture<NexusAutoResponse> {
        var task: Cancellable? = null
        val deferredTaskRan = AtomicBoolean(false)
        if (updater.get() == null) {
            val timeUntil = Instant.now().toEpochMilli() - interaction.creationTimestamp
                .minusMillis(Nexus.configuration.global.autoDeferAfterMilliseconds)
                .toEpochMilli()

            task = Nexus.launch.scheduler.launch(timeUntil) {
                if (updater.get() == null) {
                    updater.updateAndGet { interaction.respondLater(ephemeral) }!!.exceptionally(ExceptionLogger.get())
                }
                deferredTaskRan.set(true)
            }
        }
        val future = CompletableFuture<NexusAutoResponse>()
        Nexus.launcher.launch {
            try {
                val message = response.apply(null)
                if (!deferredTaskRan.get() && task != null) {
                    task.cancel(false)
                }
                @Suppress("NAME_SHADOWING") val updater = updater.get()
                if (updater == null) {
                    val responder = interaction.createImmediateResponder()
                    if (ephemeral) {
                        responder.setFlags(MessageFlag.EPHEMERAL)
                    }
                    message.into(responder).respond()
                        .thenAccept { r -> future.complete(NexusAutoResponse(r, null)) }
                        .exceptionally { exception ->
                            future.completeExceptionally(exception)
                            return@exceptionally null
                        }
                } else {
                    val completedUpdater = updater.join()
                    message.into(completedUpdater).update()
                        .thenAccept { r -> future.complete(NexusAutoResponse(completedUpdater, r)) }
                        .exceptionally { exception ->
                            future.completeExceptionally(exception)
                            return@exceptionally null
                        }
                }
            } catch (exception: Exception) {
                future.completeExceptionally(exception)
            }
        }
        return future
    }
}

/**
 * Automatically answers either deferred or non-deferred based on circumstances, to configure the time that it should
 * consider before deferring (this is based on time now - (interaction creation time - auto defer time)), you can
 * modify [pw.mihou.nexus.configuration.modules.NexusGlobalConfiguration.autoDeferAfterMilliseconds].
 *
 * For slash commands and context menus, we recommend using the Nexus event's methods instead which will enable better coordination
 * with middlewares.
 *
 * @param ephemeral whether to respond ephemerally or not.
 * @param response the response to send to Discord.
 * @return the response from Discord.
 */
fun <Interaction: InteractionBase> Interaction.autoDefer(ephemeral: Boolean, response: Function<Void?, NexusMessage>): CompletableFuture<NexusAutoResponse> =
    Deferrable.autoDefer<Interaction>(this, AtomicReference(null), ephemeral, response)

/**
 * An experimental feature to use the new Nexus.R rendering mechanism to render Discord messages
 * with a syntax similar to a template engine that sports states (writables) that can easily update message
 * upon state changes.
 *
 * This internally uses [autoDefer] to handle sending of the response, ensuring that we can handle long-running renders
 * and others that may happen due to situations such as data fetching, etc.
 *
 * @param ephemeral whether to send the response as ephemeral or not.
 * @param react the entire procedure over how rendering the response works.
 */
@JvmSynthetic
fun <Interaction: InteractionBase> Interaction.R(ephemeral: Boolean, react: React.() -> Unit): CompletableFuture<NexusAutoResponse> {
    val r = React(this.api, React.RenderMode.Interaction)
    return autoDefer(ephemeral) {
        react(r)

        return@autoDefer r.message!!
    }.thenApply {
        val message = it.getOrRequestMessage().join()

        r.interactionUpdater = it.updater
        r.acknowledgeUpdate(message)

        return@thenApply it
    }
}