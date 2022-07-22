package pw.mihou.nexus.features.paginator.facade;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface NexusPaginatorInstance<I> {

    /**
     * Gets the parent paginator that this is referencing from.
     *
     * @return The parent paginator that is being referenced.
     */
    NexusPaginator<I> getParent();

    /**
     * Gets the items that are being referenced by this instance.
     *
     * @return The items that are being referenced by this pagination instance.
     */
    default List<I> getItems() {
        return getParent().getItems();
    }

    /**
     * Gets the universally unique identifier of this instance.
     *
     * @return The universally unique identifier of this instance
     */
    String getUUID();

    /**
     * Gets the pagination message that was received from the {@link NexusPaginator}.
     * <br>
     * This is always null on the {@link NexusPaginatorEvents#onInit(InteractionOriginalResponseUpdater, NexusPaginatorCursor)}.
     *
     * @return The pagination message which can be used to edit, etc.
     */
    default CompletableFuture<Message> getMessage() {
        return getApi().getMessageById(getMessageId(), getApi().getTextChannelById(getChannelId()).orElseThrow(AssertionError::new));
    }

    /**
     * Destroys this instance, this doesn't remove any buttons. All the buttons will simply
     * not work when this is destroyed but you can still destroy it yourself.
     */
    default void destroy() {
        getParent().destroy(this);
    }

    /**
     * Destroys this instance but keeps the buttons, this doesn't perform any other execution other
     * than calling the destroy method of this paginator after some time.
     *
     * @param duration The time after calling this method before destroying this instance.
     */
    default void destroyAndKeepButtonsAfter(Duration duration) {
        NexusThreadPool.schedule(this::destroy, duration.toMillis(), TimeUnit.MILLISECONDS);
    }


    /**
     * Destroys this instance and executes the onDisabled parameter after a given amount of time. Unlike the
     * method {@link NexusPaginatorInstance#destroyAndKeepButtonsAfter(Duration)}, this will allow you to
     * perform your own actions after the paginator is destroyed.
     *
     * @param duration The time after calling this method before destroying this instance.
     * @param onDisabled What to execute after the paginator is destroyed.
     * @return A future that indicates the completion of the destruction of this instance.
     */
    default CompletableFuture<Void> destroyAndExecuteAfter(Duration duration, NexusPaginatorDisabledEvent<I> onDisabled) {
        CompletableFuture<Void> completion = new CompletableFuture<>();
        NexusThreadPool.schedule(() -> {
            try {
                destroy();

                Message message = getMessage().join();
                onDisabled.onDisabled(message, this);
                completion.complete(null);
            } catch (Throwable exception) {
                completion.completeExceptionally(exception);
            }
        }, duration.toMillis(), TimeUnit.MILLISECONDS);

        return completion;
    }

    /**
     * Destroys this instance and executes the onDisabled parameter after a given amount of time. Unlike the
     * method {@link NexusPaginatorInstance#destroyAndExecuteAfter(Duration, NexusPaginatorDisabledEvent)}, this will
     * remove all the buttons after the paginator instance is destroyed.
     *
     * @param duration The time after calling this method before destroying this instance.
     * @return A future that indicates the completion of the destruction of this instance.
     */
    default CompletableFuture<Void> destroyAndRemoveButtonsAfter(Duration duration) {
        return destroyAndExecuteAfter(duration, (message, instance) -> {
            instance.getParent().destroy(instance);
            message.createUpdater().removeAllComponents().applyChanges().join();
        });
    }

    /**
     * Gets the shard where the message was sent.
     *
     * @return The shard where the message was sent.
     */
    DiscordApi getApi();

    /**
     * Gets the channel where the message was sent.
     *
     * @return The channel where the message was sent.
     */
    long getChannelId();

    /**
     * Gets the message id of the message received from {@link NexusPaginator}.
     *
     * @return The message id of the pagination message.
     */
    long getMessageId();

}
