package pw.mihou.nexus.features.command.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

class NexusCommandEventCore(private val event: SlashCommandCreateEvent, private val command: NexusCommand) : NexusCommandEvent {
    private val store: Map<String, Any> = HashMap()
    var updater: AtomicReference<CompletableFuture<InteractionOriginalResponseUpdater>?> = AtomicReference(null)

    override fun getBaseEvent() = event
    override fun getCommand() = command
    override fun store() = store

    override fun respondLater(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return updater.updateAndGet { interaction.respondLater() }!!
    }

    override fun respondLaterAsEphemeral(): CompletableFuture<InteractionOriginalResponseUpdater> {
        return updater.updateAndGet { interaction.respondLater(true) }!!
    }
}