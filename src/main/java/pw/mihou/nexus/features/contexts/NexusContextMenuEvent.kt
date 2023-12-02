package pw.mihou.nexus.features.contexts

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.event.interaction.ApplicationCommandEvent
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.commons.Deferrable
import pw.mihou.nexus.features.commons.NexusInteractionEvent
import pw.mihou.nexus.features.messages.NexusMessage
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

class NexusContextMenuEvent<Event: ApplicationCommandEvent, Interaction: org.javacord.api.interaction.InteractionBase>(
    val contextMenu: NexusContextMenu,
    override val event: Event, override val interaction: Interaction
): NexusInteractionEvent<Event, Interaction> {
    private var updater: AtomicReference<CompletableFuture<InteractionOriginalResponseUpdater>?> = AtomicReference(null)
    override fun autoDefer(ephemeral: Boolean, response: Function<Void?, NexusMessage>): CompletableFuture<NexusAutoResponse> =
        Deferrable.autoDefer(event.interaction, updater, ephemeral, response)
}