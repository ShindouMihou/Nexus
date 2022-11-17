package pw.mihou.nexus.features.command.responders

import org.javacord.api.interaction.Interaction
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import java.util.concurrent.CompletableFuture

class NexusResponderRepository {

    private val responders = mutableMapOf<Long, InteractionOriginalResponseUpdater>()

    /**
     * Gets the current [InteractionOriginalResponseUpdater] for the specific interaction
     * if available from another middleware otherwise requests for a new [InteractionOriginalResponseUpdater]
     * that can be used instead.
     * <br></br><br></br>
     * Not to be confused with [NexusResponderRepository.get] which deliberately
     * destroys the interaction that is being stored after being requested. This is intended for middlewares to
     * prevent Discord's Interaction has failed while processing a heavy task.
     *
     * @param interaction   The interaction to reference.
     * @return              The [InteractionOriginalResponseUpdater] if present otherwise requests for one.
     */
    fun peek(interaction: Interaction): CompletableFuture<InteractionOriginalResponseUpdater> = synchronized(interaction) {
        if (responders.containsKey(interaction.id)) { CompletableFuture.completedFuture(responders[interaction.id]) }
        else interaction.respondLater().and { responders[interaction.id] = it }
    }

    /**
     * Gets the current [InteractionOriginalResponseUpdater] for the specific interaction
     * if available from another middleware otherwise requests for a new [InteractionOriginalResponseUpdater]
     * that can be used instead.
     * <br></br><br></br>
     * Not to be confused with [NexusResponderRepository.get] which deliberately
     * destroys the interaction that is being stored after being requested. This is intended for middlewares to
     * prevent Discord's Interaction has failed while processing a heavy task.
     *
     * @param interaction   The interaction to reference.
     * @return              The [InteractionOriginalResponseUpdater] if present otherwise requests for one.
     */
    fun peekEphemeral(interaction: Interaction): CompletableFuture<InteractionOriginalResponseUpdater> = synchronized(interaction) {
        if (responders.containsKey(interaction.id)) { CompletableFuture.completedFuture(responders[interaction.id]) }
        else interaction.respondLater(true).and { responders[interaction.id] = it }
    }

    /**
     * Gets the current [InteractionOriginalResponseUpdater] for the specific interaction if
     * available from another middleware otherwise requests for a new [InteractionOriginalResponseUpdater]
     * that can be used instead.
     *
     * @param interaction   The interaction to reference for.
     * @return              The [InteractionOriginalResponseUpdater] if present otherwise requests for one.
     */
    operator fun get(interaction: Interaction) = synchronized(interaction) {
        if (responders.containsKey(interaction.id)) { CompletableFuture.completedFuture(responders.remove(interaction.id)) }
        else interaction.respondLater()
    }

    /**
     * Gets the current [InteractionOriginalResponseUpdater] for the specific interaction if
     * available from another middleware otherwise requests for a new [InteractionOriginalResponseUpdater]
     * that can be used instead.
     *
     * @param interaction   The interaction to reference for.
     * @return              The [InteractionOriginalResponseUpdater] if present otherwise requests for one.
     */
    fun getEphemereal(interaction: Interaction): CompletableFuture<InteractionOriginalResponseUpdater> = synchronized(interaction) {
        if (responders.containsKey(interaction.id)) { CompletableFuture.completedFuture(responders.remove(interaction.id)) }
        else interaction.respondLater(true)
    }

    private fun <Type> CompletableFuture<Type>.and(`do`: (Type) -> Unit): CompletableFuture<Type> =
        this.thenApply { `do`(it); return@thenApply it }
}