package pw.mihou.nexus.features.command.responses

import org.javacord.api.entity.message.Message
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import java.util.concurrent.CompletableFuture

data class NexusAutoResponse internal constructor(
    val updater: InteractionOriginalResponseUpdater,
    val message: Message?
) {
    fun getOrRequestMessage(): CompletableFuture<Message> =
        if (message == null) updater.update()
        else CompletableFuture.completedFuture(message)
}
