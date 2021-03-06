package pw.mihou.nexus.features.command.facade;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.managers.NexusShardManager;
import pw.mihou.nexus.features.command.core.NexusCommandCore;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface NexusCommandEvent {

    /**
     * Gets the base event that was received from Javacord. This is usually nothing
     * of use to the end-user.
     *
     * @return The base event that was received.
     */
    SlashCommandCreateEvent getBaseEvent();

    /**
     * Gets the interaction received from Javacord.
     *
     * @return The interaction that was received from Javacord.
     */
    default SlashCommandInteraction getInteraction() {
       return getBaseEvent().getSlashCommandInteraction();
    }

    /**
     * Gets the user received from Javacord.
     *
     * @return The user that was received from Javacord.
     */
    default User getUser() {
        return getInteraction().getUser();
    }

    /**
     * Gets the channel that isn't an optional. This exists primarily because there is no currently possible
     * way for the text channel to be optional in Discord Slash Commands.
     *
     * @return The text channel that is being used.
     */
    default TextChannel getChannel() {
        return getInteraction().getChannel().orElseThrow(() -> new NoSuchElementException(
                "It seems like the text channel is no longer always available. Please create an issue on https://github.com/ShindouMihou/Nexus"
        ));
    }

    /**
     * Gets the server instance.
     *
     * @return The server instance.
     */
    default Optional<Server> getServer() {
        return getInteraction().getServer();
    }

    /**
     * Gets the ID of the server.
     *
     * @return The ID of the server.
     */
    default Optional<Long> getServerId() {
        return getInteraction().getServer().map(Server::getId);
    }

    /**
     * Gets the ID of the text channel where the command was executed.
     *
     * @return The ID of the text channel where the command was executed.
     */
    default Long getChannelId() {
        return getChannel().getId();
    }

    /**
     * Gets the ID of the user that executed the command.
     *
     * @return The ID of the user who executed this command.
     */
    default Long getUserId() {
        return getUser().getId();
    }

    /**
     * Gets the text channel as a server text channel.
     *
     * @return The text channel as a server text channel.
     */
    default Optional<ServerTextChannel> getServerTextChannel() {
        return getChannel().asServerTextChannel();
    }
    /**
     * Gets the command that was executed.
     *
     * @return The command that was executed.
     */
    NexusCommand getCommand();

    /**
     * Gets the {@link Nexus} instance that was in charge
     * of handling this command.
     *
     * @return The Nexus instance that was in charge of this command.
     */
    default Nexus getNexus() {
        return ((NexusCommandCore) getCommand()).core;
    }

    /**
     * Gets the Discord API shard that was in charge of this event.
     *
     * @return The Discord API shard that was in charge of this event.
     */
    default DiscordApi getApi() {
        return getBaseEvent().getApi();
    }

    /**
     * Gets the {@link NexusShardManager} that is associated with the
     * {@link Nexus} instance.
     *
     * @return The Nexus Shard Manager associated with this Nexus instance.
     */
    default NexusShardManager getShardManager() {
        return getNexus().getShardManager();
    }

    /**
     * Gets the options that were brought with this command.
     *
     * @return All the options of this command.
     */
    default List<SlashCommandInteractionOption> getOptions() {
        return getInteraction().getOptions();
    }

    /**
     * Gets the options of a subcommand.
     *
     * @param name The name of the subcommand to search for.
     * @return The options of a subcommand, if present.
     */
    default Optional<List<SlashCommandInteractionOption>> getSubcommandOptions(String name) {
        return getInteraction().getOptionByName(name).map(SlashCommandInteractionOption::getOptions);
    }

    /**
     * Gets the immediate response for this command.
     *
     * @return The immediate response builder associated with this command.
     */
    default InteractionImmediateResponseBuilder respondNow() {
        return getInteraction().createImmediateResponder();
    }

    /**
     * Gets the immediate response builder for this command and adds the
     * {@link MessageFlag#EPHEMERAL} flag ahead of time.
     *
     * @return The immediate response builder associated with this command with the
     * ephemeral flag added.
     */
    default InteractionImmediateResponseBuilder respondNowAsEphemeral() {
        return respondNow().setFlags(MessageFlag.EPHEMERAL);
    }

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command.
     *
     * @return The {@link InteractionOriginalResponseUpdater}.
     */
    default CompletableFuture<InteractionOriginalResponseUpdater> respondLater() {
        return getNexus()
                .getResponderRepository()
                .get(getBaseEvent().getInteraction());
    }

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command with the
     * ephemeral flag attached.
     *
     * @return The {@link InteractionOriginalResponseUpdater} with the ephemeral flag attached.
     */
    default CompletableFuture<InteractionOriginalResponseUpdater> respondLaterAsEphemeral() {
        return getNexus()
                .getResponderRepository()
                .getEphemereal(getBaseEvent().getInteraction());
    }

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The {@link InteractionOriginalResponseUpdater} for this interaction.
     */
    default CompletableFuture<InteractionOriginalResponseUpdater> respondLaterAsEphemeralIf(boolean predicate) {
        if (predicate) {
            return respondLaterAsEphemeral();
        }

        return respondLater();
    }

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The {@link InteractionOriginalResponseUpdater} for this interaction.
     */
    default CompletableFuture<InteractionOriginalResponseUpdater> respondLaterAsEphemeralIf(Predicate<Void> predicate) {
        return respondLaterAsEphemeralIf(predicate.test(null));
    }

    /**
     * Gets the event local store that is used to contain shared fields accessible from middlewares, command and
     * afterwares themselves. You can use this to store data such as whether the command was completed successfully
     * or other related.
     *
     * @return The event-local store from this event.
     */
    Map<String, Object> store();

    /**
     * Gets the value of the given key from the {@link NexusCommandEvent#store()} and maps it into the type given
     * if possible, otherwise returns null.
     *
     * @param key   The key to get from the {@link NexusCommandEvent#store()}.
     * @param type  The type expected of the value.
     * @param <T>   The type expected of the value.
     *
     * @return The value mapped with the key in {@link NexusCommandEvent#store()} mapped to the type, otherwise null.
     */
    default <T> T get(String key, Class<T> type) {
        if (!store().containsKey(key))
            return null;

        Object object = store().get(key);

        if (type.isAssignableFrom(object.getClass())) {
            return type.cast(object);
        }

        return null;
    }

    /**
     * A short-hand expression for placing a key-value pair to {@link NexusCommandEvent#store()}.
     *
     * @param key   The key to insert to the store.
     * @param value The value to insert to the store.
     */
    default void store(String key, Object value) {
        store().put(key, value);
    }

}
