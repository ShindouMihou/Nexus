package pw.mihou.nexus.core.enginex.event.core;

import org.javacord.api.DiscordApi;
import pw.mihou.nexus.core.enginex.event.NexusEngineEvent;
import pw.mihou.nexus.core.enginex.event.NexusEngineQueuedEvent;
import pw.mihou.nexus.core.enginex.event.listeners.NexusEngineEventStatusChange;
import pw.mihou.nexus.core.enginex.event.media.NexusEngineEventWriteableStore;
import pw.mihou.nexus.core.enginex.event.status.NexusEngineEventStatus;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class NexusEngineEventCore implements NexusEngineEvent {

    private final AtomicReference<NexusEngineEventStatus> status = new AtomicReference<>(NexusEngineEventStatus.WAITING);
    private final NexusEngineQueuedEvent event;
    private final List<NexusEngineEventStatusChange> listeners = new ArrayList<>();
    private final NexusEngineEventWriteableStore store = new NexusEngineEventWriteableStore(new ConcurrentHashMap<>());

    public NexusEngineEventCore(NexusEngineQueuedEvent event) {
        this.event = event;
    }

    @Override
    public void cancel() {
        if (status() == NexusEngineEventStatus.WAITING) {
            changeStatus(NexusEngineEventStatus.STOPPED);
        }
    }

    /**
     * Expires this event and stops it from proceeding in any
     * form of way.
     */
    public void expire() {
        if (status() == NexusEngineEventStatus.WAITING) {
            changeStatus(NexusEngineEventStatus.EXPIRED);
        }
    }

    /**
     * Applies a new status to this event
     * @param newStatus The new status to use for this event.
     */
    private void changeStatus(NexusEngineEventStatus newStatus) {
        NexusEngineEventStatus oldStatus = status.get();
        status.set(newStatus);

        listeners.forEach(listener -> CompletableFuture
                .runAsync(() -> listener.onStatusChange(this, oldStatus, newStatus), NexusThreadPool.executorService));
    }

    /**
     * Proceeds to process the event if it is still available to process.
     *
     * @param api   The shard that will be processing the event.
     */
    public void process(DiscordApi api) {
        if (status.get() == NexusEngineEventStatus.STOPPED || status.get() == NexusEngineEventStatus.EXPIRED) {
            return;
        }

        changeStatus(NexusEngineEventStatus.PROCESSING);
        CompletableFuture.runAsync(() -> event.onEvent(api, store), NexusThreadPool.executorService)
                .thenAccept(unused -> changeStatus(NexusEngineEventStatus.FINISHED));
    }

    @Override
    public NexusEngineEventStatus status() {
        return status.get();
    }

    @Override
    public NexusEngineEvent addStatusChangeListener(NexusEngineEventStatusChange event) {
        listeners.add(event);
        return this;
    }
}
