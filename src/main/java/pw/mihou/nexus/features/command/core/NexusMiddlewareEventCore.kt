package pw.mihou.nexus.features.command.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore
import pw.mihou.nexus.features.messages.NexusMessage
import java.util.concurrent.CompletableFuture

class NexusMiddlewareEventCore(val event: NexusCommandEvent, private val gate: NexusMiddlewareGateCore): NexusMiddlewareEvent {
    override fun getBaseEvent(): SlashCommandCreateEvent = event.baseEvent
    override fun getCommand(): NexusCommand = event.command
    override fun respondLater(): CompletableFuture<InteractionOriginalResponseUpdater> = event.respondLater()

    override fun respondLaterAsEphemeral(): CompletableFuture<InteractionOriginalResponseUpdater> = event.respondLaterAsEphemeral()
    override fun store(): MutableMap<String, Any> = event.store()

    override fun next() {
        gate.next()
    }

    override fun stop(response: NexusMessage?) {
        gate.stop(response)
    }
}