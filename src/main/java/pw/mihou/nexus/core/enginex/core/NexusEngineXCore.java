package pw.mihou.nexus.core.enginex.core;

import org.javacord.api.DiscordApi;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.enginex.event.NexusEngineEvent;
import pw.mihou.nexus.core.enginex.event.NexusEngineQueuedEvent;
import pw.mihou.nexus.core.enginex.event.core.NexusEngineEventCore;
import pw.mihou.nexus.core.enginex.event.status.NexusEngineEventStatus;
import pw.mihou.nexus.core.enginex.facade.NexusEngineX;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class NexusEngineXCore implements NexusEngineX {

    private final ConcurrentLinkedQueue<NexusEngineEvent> globalQueue = new ConcurrentLinkedQueue<>();
    private final Map<Long, ConcurrentLinkedQueue<NexusEngineEvent>> localQueue = new ConcurrentHashMap<>();
    private final Nexus nexus;

    /**
     * Creates a new {@link NexusEngineX} instance that can be used to broadcast, queue
     * specific events into specific shards or global shards.
     *
     * @param nexus The {@link Nexus} event to handle this event for.
     */
    public NexusEngineXCore(Nexus nexus) {
        this.nexus = nexus;
    }

    /**
     * Gets the global blocking queue that is dedicated for all shards to
     * accept at any point of time when needed.
     *
     * @return  The blocking queue that any shard can accept.
     */
    public ConcurrentLinkedQueue<NexusEngineEvent> getGlobalQueue() {
        return globalQueue;
    }

    /**
     * Gets the local queue for this shard. If it doesn't exist then it will add
     * a queue instead and return the newly created queue.
     *
     * @param shard The shard to get the queue of.
     * @return      The blocking queue for this shard.
     */
    public ConcurrentLinkedQueue<NexusEngineEvent> getLocalQueue(long shard) {
        if (!localQueue.containsKey(shard)) {
            localQueue.put(shard, new ConcurrentLinkedQueue<>());
        }

        return localQueue.get(shard);
    }

    @Override
    public NexusEngineEvent queue(long shard, NexusEngineQueuedEvent event) {
        NexusEngineEventCore engineEvent = new NexusEngineEventCore(event);
        getLocalQueue(shard).add(engineEvent);

        Duration expiration = nexus.getConfiguration().timeBeforeExpiringEngineRequests();
        if (!(expiration.isZero() || expiration.isNegative())) {
            NexusThreadPool.schedule(() -> {
                if (engineEvent.status() == NexusEngineEventStatus.WAITING) {
                    boolean removeFromQueue = localQueue.get(shard).remove(engineEvent);
                    NexusCore.logger.warn(
                            "An engine request that was specified for a shard was expired because the shard failed to take hold of the request before expiration. " +
                                    "[shard={};acknowledged={}]",
                            shard, removeFromQueue
                    );
                    engineEvent.expire();
                }
            }, expiration.toMillis(), TimeUnit.MILLISECONDS);
        }

        return engineEvent;
    }

    @Override
    public NexusEngineEvent queue(NexusEngineQueuedEvent event) {
        NexusEngineEvent engineEvent = new NexusEngineEventCore(event);
        globalQueue.add(engineEvent);

        return engineEvent;
    }

    @Override
    public void broadcast(Consumer<DiscordApi> event) {
        nexus.getShardManager()
                .asStream()
                .forEach(api -> CompletableFuture
                        .runAsync(() -> event.accept(api), NexusThreadPool.executorService));
    }
}
