package pw.mihou.nexus.features.paginator.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import pw.mihou.nexus.core.assignment.NexusUuidAssigner;
import pw.mihou.nexus.features.paginator.facade.NexusPaginator;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorInstance;

import java.util.concurrent.atomic.AtomicInteger;

public class NexusPaginatorInstanceCore<I> implements NexusPaginatorInstance<I> {

    private final String uuid = NexusUuidAssigner.request();
    private final AtomicInteger cursor = new AtomicInteger();
    private final NexusPaginator<I> parent;

    /**
     * This message is only used as a reference to get data such
     * as text channel id, message id and the shard since this tends to get
     * outdated fast.
     */
    private Message message;

    /**
     * Creates a new {@link  NexusPaginatorInstance} which is done to allow
     * reusable instances of a {@link  NexusPaginator}.
     *
     * @param parent The parent {@link NexusPaginator} to reference from.
     */
    public NexusPaginatorInstanceCore(NexusPaginator<I> parent) {
        this.parent = parent;
    }

    /**
     * Sets the paginated message of this instance. This is an internal method and
     * should be used only by the internals.
     *
     * @param message The paginated message of this instance.
     */
    public NexusPaginatorInstance<I> setMessage(Message message) {
        this.message = message;
        return this;
    }

    @Override
    public NexusPaginator<I> getParent() {
        return parent;
    }

    /**
     * Gets the cursor of this instance.
     *
     * @return The cursor of this instance.
     */
    public AtomicInteger getCursor() {
        return cursor;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public DiscordApi getApi() {
        return message.getApi();
    }

    @Override
    public long getChannelId() {
        return message.getChannel().getId();
    }

    @Override
    public long getMessageId() {
        return message.getId();
    }
}
