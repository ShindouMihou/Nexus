package pw.mihou.nexus.features.command.core

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.commons.Deferrable
import pw.mihou.nexus.features.messages.NexusMessage
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

class NexusCommandEventCore(override val event: SlashCommandCreateEvent, override val command: NexusCommand) : NexusCommandEvent {
    private val store: MutableMap<String, Any> = HashMap()
    var updater: AtomicReference<CompletableFuture<InteractionOriginalResponseUpdater>?> = AtomicReference(null)

    override fun store(): MutableMap<String, Any> = store

    override fun autoDefer(ephemeral: Boolean, response: Function<Void?, NexusMessage>): CompletableFuture<NexusAutoResponse> =
        Deferrable.autoDefer(this, updater, ephemeral, response)

    override fun respondLater(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return updater.updateAndGet { interaction.respondLater() }!!
    }

    override fun respondLaterEphemerally(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return updater.updateAndGet { interaction.respondLater(true) }!!
    }
}