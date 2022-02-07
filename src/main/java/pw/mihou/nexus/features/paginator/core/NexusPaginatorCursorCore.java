package pw.mihou.nexus.features.paginator.core;

import pw.mihou.nexus.features.paginator.facade.NexusPaginatorCursor;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorInstance;

public class NexusPaginatorCursorCore<I> implements NexusPaginatorCursor<I> {

    private final int position;
    private final NexusPaginatorInstanceCore<I> parent;

    /**
     * Creates a new immutable {@link  NexusPaginatorCursorCore} that can be used to
     * gather data about a specific event.
     *
     * @param position  The current position of the cursor.
     * @param parent    The parent instance of this cursor.
     */
    public NexusPaginatorCursorCore(int position, NexusPaginatorInstanceCore<I> parent) {
        this.position = position;
        this.parent = parent;
    }

    @Override
    public NexusPaginatorInstance<I> parent() {
        return parent;
    }

    @Override
    public int getRawPosition() {
        return position;
    }

    @Override
    public I getItem() {
        return parent.getParent().getItems().get(getRawPosition());
    }
}
