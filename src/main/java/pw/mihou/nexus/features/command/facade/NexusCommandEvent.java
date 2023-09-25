package pw.mihou.nexus.features.command.facade;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.util.logging.ExceptionLogger;
import org.jetbrains.annotations.NotNull;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore;
import pw.mihou.nexus.features.command.responses.NexusAutoResponse;
import pw.mihou.nexus.features.commons.NexusInteractionEvent;
import pw.mihou.nexus.features.messages.NexusMessage;
import pw.mihou.nexus.sharding.NexusShardingManager;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface NexusCommandEvent extends NexusInteractionEvent<SlashCommandCreateEvent, SlashCommandInteraction> {

    /**
     * Gets the base event that was received from Javacord. This is usually nothing
     * of use to the end-user.
     *
     * @return The base event that was received.
     * @deprecated Standardized methods across the framework.
     * @see NexusCommandEvent#getEvent()
     */
    @Deprecated(forRemoval = true)
    default SlashCommandCreateEvent getBaseEvent() {
        return getEvent();
    }

    default SlashCommandInteraction getInteraction() {
        return getEvent().getSlashCommandInteraction();
    }

    @NotNull
    SlashCommandCreateEvent getEvent();

    /**
     * Gets the command that was executed.
     *
     * @return The command that was executed.
     */
    NexusCommand getCommand();

    /**
     * Gets the {@link NexusShardingManager} that is associated with the {@link Nexus} instance.
     *
     * @return The Nexus Shard Manager associated with this Nexus instance.
     */
    default NexusShardingManager getShardManager() {
        return Nexus.getShardingManager();
    }

    /**
     * Gets the immediate response builder for this command and adds the
     * {@link MessageFlag#EPHEMERAL} flag ahead of time.
     *
     * @return The immediate response builder associated with this command with the
     * ephemeral flag added.
     * @deprecated Standardized methods across the framework.
     * @see NexusCommandEvent#respondNowEphemerally
     */
    @Deprecated(forRemoval = true)
    default InteractionImmediateResponseBuilder respondNowAsEphemeral() {
        return respondNowEphemerally();
    }

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command with the
     * ephemeral flag attached.
     *
     * @return The {@link InteractionOriginalResponseUpdater} with the ephemeral flag attached.
     * @deprecated Standardized methods across the framework.
     * @see NexusCommandEvent#respondLaterEphemerally
     */
    @Deprecated(forRemoval = true)
    CompletableFuture<InteractionOriginalResponseUpdater> respondLaterAsEphemeral();

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The {@link InteractionOriginalResponseUpdater} for this interaction.
     * @deprecated Standardized methods across the framework.
     * @see NexusCommandEvent#respondLaterEphemerallyIf(boolean)
     */
    @Deprecated(forRemoval = true)
    default CompletableFuture<InteractionOriginalResponseUpdater> respondLaterAsEphemeralIf(boolean predicate) {
        return respondLaterEphemerallyIf(predicate);
    }

    /**
     * Gets the {@link InteractionOriginalResponseUpdater} associated with this command with the ephemeral flag
     * attached if the predicate is true.
     *
     * @param predicate The predicate to determine whether to use ephemeral response or not.
     * @return The {@link InteractionOriginalResponseUpdater} for this interaction.
     * @deprecated Standardized methods across the framework.
     * @see NexusCommandEvent#respondLaterEphemerallyIf(Predicate)
     */
    @Deprecated(forRemoval = true)
    default CompletableFuture<InteractionOriginalResponseUpdater> respondLaterAsEphemeralIf(Predicate<Void> predicate) {
        return respondLaterEphemerallyIf(predicate.test(null));
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
     * Gets the value of the given key from the {@link NexusCommandEvent#store()}.
     *
     * @param key   The key to get from the {@link NexusCommandEvent#store()}.*
     * @return The value mapped with the key in {@link NexusCommandEvent#store()}, otherwise null.
     */
    default Object get(String key) {
        return store().get(key);
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

    /**
     * Activates one or more middlewares and executes the success consumer once all middlewares succeed without
     * throwing an issue. This does not support {@link InteractionOriginalResponseUpdater} or related, it will use
     * {@link InteractionImmediateResponseBuilder} instead.
     *
     * @param middlewares   the middlewares to activate.
     * @param success       what to do when all the middlewares succeed.
     */
    default void middlewares(List<String> middlewares, Consumer<Void> success) {
        NexusMiddlewareGateCore middlewareGate = NexusCommandInterceptorCore.execute(this,
                NexusCommandInterceptorCore.middlewares(middlewares));

        if (middlewareGate == null) {
            success.accept(null);
            return;
        }

        NexusMessage response = middlewareGate.response();
        if (response != null) {
            InteractionImmediateResponseBuilder responder = respondNow();
            if (response.getEphemeral()) {
                responder = respondNowEphemerally();
            }
            response.into(responder).respond().exceptionally(ExceptionLogger.get());
        }
    }

    /**
     * Automatically answers either deferred or non-deferred based on circumstances, to configure the time that it should
     * consider before deferring (this is based on time now - (interaction creation time - auto defer time)), you can
     * modify {@link pw.mihou.nexus.configuration.modules.NexusGlobalConfiguration#autoDeferAfterMilliseconds}.
     *
     * @param ephemeral whether to respond ephemerally or not.
     * @param response the response to send to Discord.
     * @return the response from Discord.
     */
    CompletableFuture<NexusAutoResponse> autoDefer(boolean ephemeral, Function<Void, NexusMessage> response);

}
