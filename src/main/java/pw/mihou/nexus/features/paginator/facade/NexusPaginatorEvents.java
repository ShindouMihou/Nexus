package pw.mihou.nexus.features.paginator.facade;

import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
public interface NexusPaginatorEvents<I> {

    /**
     * Executed whenever the page of the paginator first starts.
     *
     * @param updater The original response updater that was received.
     * @param cursor The cursor that contains the event information.
     * @return The response to send to the end-user.
     */
    InteractionOriginalResponseUpdater onInit(InteractionOriginalResponseUpdater updater, NexusPaginatorCursor<I> cursor);

    /**
     * Executed whenever the page of the paginator changes. <br>
     * It is up to you how you wish to handle the interaction.
     *
     * @param cursor The cursor that contains the event information.
     */
    void onPageChange(NexusPaginatorCursor<I> cursor, ButtonClickEvent event);

    /**
     * Executed whenever the page of the user selects an item. <br>
     * It is up to you how you wish to handle the interaction.
     *
     * <br><br>
     * This can be ignored and is not required since if this is not used then
     * nothing will happen.
     *
     * @param cursor The cursor that contains the event information.
     */
     default void onSelect(NexusPaginatorCursor<I> cursor, ButtonClickEvent event) {
     }

    /**
     * Executed whenever the page of the user cancels the paginator. <br>
     * It is up to you how you wish to handle the interaction.
     *
     * <br><br>
     * This can be ignored and is not required since if this is not used then
     * nothing will happen.
     *
     * @param cursor The cursor that contains the event information.
     */
    default void onCancel(NexusPaginatorCursor<I> cursor, ButtonClickEvent event) {
    }


}
