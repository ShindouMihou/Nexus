package pw.mihou.nexus.features.paginator.facade;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
