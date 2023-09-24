package pw.mihou.nexus.features.command.core

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.messages.NexusMessage
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

class NexusCommandEventCore(override val event: SlashCommandCreateEvent, private val command: NexusCommand) : NexusCommandEvent {
    private val store: Map<String, Any> = HashMap()
    var updater: AtomicReference<CompletableFuture<InteractionOriginalResponseUpdater>?> = AtomicReference(null)

    override fun getCommand() = command
    override fun store() = store
    override fun autoDefer(ephemeral: Boolean, response: Function<Void?, NexusMessage>): CompletableFuture<NexusAutoResponse> {
        var task: Cancellable? = null
        val deferredTaskRan = AtomicBoolean(false)
        if (updater.get() == null) {
            val timeUntil = Instant.now().toEpochMilli() - event.interaction.creationTimestamp
                .minusMillis(Nexus.configuration.global.autoDeferAfterMilliseconds)
                .toEpochMilli()

            task = Nexus.launch.scheduler.launch(timeUntil) {
                if (updater.get() == null) {
                    respondLaterEphemerallyIf(ephemeral).exceptionally(ExceptionLogger.get())
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
                val updater = updater.get()
                if (updater == null) {
                    val responder = respondNow()
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
                        .thenAccept { r -> future.complete(NexusAutoResponse(null, r)) }
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

    override fun respondLater(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return updater.updateAndGet { interaction.respondLater() }!!
    }

    override fun respondLaterAsEphemeral(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return updater.updateAndGet { interaction.respondLater(true) }!!
    }
}