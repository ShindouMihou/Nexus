package pw.mihou.nexus.features.paginator;

import org.javacord.api.entity.message.component.Button;
import pw.mihou.nexus.features.paginator.core.NexusPaginatorCore;
import pw.mihou.nexus.features.paginator.enums.NexusPaginatorButtonAssignment;
import pw.mihou.nexus.features.paginator.facade.NexusPaginator;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NexusPaginatorBuilder<I> {

    private List<I> items;
    private NexusPaginatorEvents<I> events;
    private final Map<NexusPaginatorButtonAssignment, Button> buttons = new HashMap<>();

    /**
     * Creates a new {@link  NexusPaginatorBuilder} .
     *
     * @param items The items to paginate.
     */
    public NexusPaginatorBuilder(List<I> items) {
        this.items = items;
    }

    /**
     * Sets the items to paginate.
     *
     * @param items The items to paginate.
     * @return The {@link  NexusPaginatorBuilder} for chain-calling methods.
     */
    public NexusPaginatorBuilder<I> setItems(List<I> items) {
        this.items = items;
        return this;
    }

    /**
     * Sets the handler for the paginate events.
     *
     * @param eventHandler The handler for all the paginate events.
     * @return The {@link  NexusPaginatorBuilder} for chain-calling methods.
     */
    public NexusPaginatorBuilder<I> setEventHandler(NexusPaginatorEvents<I> eventHandler) {
        this.events = eventHandler;
        return this;
    }


    /**
     * Sets the button template style for the assignment.
     *
     * @param assignment    The assignment for this button.
     * @param button        The button template to use for this assignment.
     * @return The {@link  NexusPaginatorBuilder} for chain-calling methods.
     */
    public NexusPaginatorBuilder<I> setButton(NexusPaginatorButtonAssignment assignment, Button button) {
        this.buttons.put(assignment, button);
        return this;
    }

    /**
     * Creates the {@link NexusPaginator} with the specifications defined.
     *
     * @return The {@link NexusPaginator} with the specifications defined.
     */
    public NexusPaginator<I> build() {
        if (events == null) {
            throw new IllegalArgumentException("A Nexus Paginator requires at least the paginate events to be handled.");
        }

        return new NexusPaginatorCore<>(items, events, buttons);
    }

}
