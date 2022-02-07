package pw.mihou.nexus.features.paginator.facade;

import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NexusPaginator<I> {

    /**
     * Gets the instances referencing this {@link  NexusPaginator} that are alive.
     *
     * @return All the alive {@link NexusPaginatorInstance} that references
     * this paginator.
     */
    List<NexusPaginatorInstance> getInstances();

    /**
     * Gets the items that are being paginated by this Paginator instance.
     *
     * @return All the items that are being referenced by this
     * Paginator instance.
     */
    List<I> getItems();

    /**
     * Stops an instance paginator from working, it will not remove the message but instead will
     * destroy the buttons and removes the listener.
     *
     * @param message The paginated message with all the buttons.
     */
    void destroy(Message message);

    /**
     * Stops this paginator from working all-in-all which unlike the {@link NexusPaginator#destroy(Message)} does not remove
     * any buttons but instead just removes all the instances and the listeners for this paginator.
     */
    void destroy();

    /**
     * Stops the instance from working, it will not remove any buttons but just tells the {@link NexusPaginator} to
     * remove knowledge of this instance. This is just the same as {@link NexusPaginator#destroy(String)} but
     * with extra steps.
     *
     * @param instance The {@link NexusPaginatorInstance} to destroy.
     */
    void destroy(NexusPaginatorInstance<I> instance);

    /**
     * Stops the instance from working, it will not remove any buttons but just tells the {@link NexusPaginator} to
     * remove knowledge of this instance.
     *
     * @param instanceUuid The {@link NexusPaginatorInstance}'s UUID to destroy.
     */
    void destroy(String instanceUuid);

    /**
     * Performs the necessary instructions to get the paginator up and running then sends the
     * initial message.
     *
     * @param interaction       The interaction to base this pagination.
     * @param responseUpdater   The response to send to the end-user.
     * @throws IllegalArgumentException This is thrown whenever the paginator notices that the initial response
     * and other events are not configured.
     *
     * @return The instance created from this preparation.
     */
    CompletableFuture<NexusPaginatorInstance<I>> send(Interaction interaction, InteractionOriginalResponseUpdater responseUpdater);

}
