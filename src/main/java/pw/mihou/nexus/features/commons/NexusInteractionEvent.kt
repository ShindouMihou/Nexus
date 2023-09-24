package pw.mihou.nexus.features.commons

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User
import org.javacord.api.event.interaction.ApplicationCommandEvent
import org.javacord.api.interaction.ApplicationCommandInteraction
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import kotlin.NoSuchElementException

interface NexusInteractionEvent<Event: ApplicationCommandEvent, Interaction: ApplicationCommandInteraction> {

    /**
     * Gets the base event that was received from Javacord. This is usually nothing
     * of use to the end-user.
     *
     * @return The base event that was received.
     */
    val event: Event

    /**
     * Gets the interaction received from Javacord.
     *
     * @return The interaction that was received from Javacord.
     */
    @Suppress("UNCHECKED_CAST")
    val interaction get() = event.interaction as Interaction

    /**
     * Gets the user received from Javacord.
     *
     * @return The user that was received from Javacord.
     */
    val user: User get() = interaction.user

    /**
     * Gets the ID of the user that executed the interaction.
     *
     * @return The ID of the user who executed this interaction.
     */
    val userId: Long get() = user.id

    /**
     * Gets the channel that isn't an optional. This exists primarily because there is no currently possible
     * way for the text channel to be optional in Discord Application Commands.
     *
     * @return The text channel that is being used.
     */
    val channel: TextChannel get() = interaction.channel.orElseThrow {
        NoSuchElementException("It seems like the text channel is no longer always available. Please create an issue on https://github.com/ShindouMihou/Nexus")
    }

    /**
     * Gets the ID of the text channel where the interaction was executed.
     *
     * @return The ID of the text channel where the interaction was executed.
     */
    val channelId: Long get() = channel.id
    val serverTextChannel: Optional<ServerTextChannel> get() = channel.asServerTextChannel()

    /**
     * Gets the server instance.
     *
     * @return The server instance.
     */
    val server: Optional<Server> get() = interaction.server

    /**
     * Gets the ID of the server.
     *
     * @return The ID of the server.
     */
    val serverId: Optional<Long> get() = server.map { it.id }

    /**
     * Gets the Discord API shard that was in charge of this event.
     *
     * @return The Discord API shard that was in charge of this event.
     */
    val api: DiscordApi get() = event.api

    /**
     * Gets the immediate response for this interaction.
     *
     * @return The immediate response builder associated with this interaction.
     */
    fun respondNow(): InteractionImmediateResponseBuilder =
        interaction.createImmediateResponder()

    /**
     * Gets the immediate response builder for this interaction and adds the
     * [MessageFlag.EPHEMERAL] flag ahead of time.
     *
     * @return The immediate response builder associated with this interaction with the
     * ephemeral flag added.
     */
    fun respondNowEphemerally(): InteractionImmediateResponseBuilder =
        interaction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)

    /**
     * A short-hand expression of sending a non-ephemeral response to Discord with the given content. This does not
     * handle the exceptions, please handle the exceptions accordingly.
     *
     * @param content the content to send as a response.
     * @return A future that contains the updater to update the response when needed.
     */
    fun respondNowWith(content: String): CompletableFuture<InteractionOriginalResponseUpdater> =
        respondNow().setContent(content).respond()

    /**
     * A short-hand expression of sending a non-ephemeral response to Discord with the given embeds. This does not
     * handle the exceptions, please handle the exceptions accordingly.
     *
     * @param embeds the embeds to send as a response.
     * @return A future that contains the updater to update the response when needed.
     */
    fun respondNowWith(vararg embeds: EmbedBuilder): CompletableFuture<InteractionOriginalResponseUpdater> =
        respondNow().addEmbeds(*embeds).respond()

    /**
     * A short-hand expression of sending an ephemeral response to Discord with the given content. This does not
     * handle the exceptions, please handle the exceptions accordingly.
     *
     * @param content the content to send as a response.
     * @return A future that contains the updater to update the response when needed.
     */
    fun respondNowEphemerallyWith(content: String): CompletableFuture<InteractionOriginalResponseUpdater> =
        respondNowEphemerally().setContent(content).respond()

    /**
     * A short-hand expression of sending an ephemeral response to Discord with the given embeds. This does not
     * handle the exceptions, please handle the exceptions accordingly.
     *
     * @param embeds the embeds to send as a response.
     * @return A future that contains the updater to update the response when needed.
     */
    fun respondNowEphemerallyWith(vararg embeds: EmbedBuilder): CompletableFuture<InteractionOriginalResponseUpdater> =
        respondNowEphemerally().addEmbeds(*embeds).respond()

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this interaction.
     *
     * @return The [InteractionOriginalResponseUpdater].
     */
    fun respondLater(): CompletableFuture<InteractionOriginalResponseUpdater> =
        interaction.respondLater()

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this interaction with the
     * ephemeral flag attached.
     *
     * @return The [InteractionOriginalResponseUpdater] with the ephemeral flag attached.
     */
    fun respondLaterEphemerally(): CompletableFuture<InteractionOriginalResponseUpdater> =
        interaction.respondLater(true)

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this interaction with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The [InteractionOriginalResponseUpdater] for this interaction.
     */
    fun respondLaterEphemerallyIf(predicate: Boolean) = if (predicate) respondLaterEphemerally() else respondLater()

    /**
     * Gets the [InteractionOriginalResponseUpdater] associated with this interaction with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The [InteractionOriginalResponseUpdater] for this interaction.
     */
    fun respondLaterEphemerallyIf(predicate: Predicate<Void?>) = respondLaterEphemerallyIf(predicate.test(null))
}