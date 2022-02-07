package pw.mihou.nexus.features.paginator.facade;

import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NexusPaginatorCursor<I> {

    /**
     * Gets the parent paginator instance of this cursor.
     *
     * @return The parent paginator instance.
     */
    NexusPaginatorInstance<I> parent();

    /**
     * Gets the pagination message that was received from the {@link NexusPaginator}.
     * This is always null on the {@link NexusPaginatorEvents#onInit(InteractionOriginalResponseUpdater, NexusPaginatorCursor)}.
     *
     * @return The pagination message which can be used to edit, etc.
     */
    default CompletableFuture<Message> getMessage() {
        return parent().getMessage();
    }

    /**
     * Gets the raw position of this cursor which isn't meant to be displayed.
     *
     * @return The raw position of this cursor.
     */
    int getRawPosition();

    /**
     * Gets the displayable position of this cursor, this is used for displaying the
     * page number unlike {@link NexusPaginatorCursor#getRawPosition()} which is meant for
     * {@link NexusPaginatorCursor#getItems()}.
     *
     * @return The displayable position of the cursor.
     */
    default int getDisplayablePosition() {
        return getRawPosition() + 1;
    }

    /**
     * Gets the displayable maximum pages of this cursor.
     *
     * @return The maximum amount of pages left in this cursor.
     */
    default int getMaximumPages() {
        return getItems().size();
    }

    /**
     * Gets the items that is being referenced by the paginator instance.
     *
     * @return The items that is being referenced by the paginator instance.
     */
    default List<I> getItems() {
        return parent().getItems();
    }

    /**
     * Gets the item that is located in the position of
     * this cursor. This should never return null unless if
     * the item is null itself.
     *
     * @return The item of this cursor.
     */
    I getItem();

}
