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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NexusEngineXCore implements NexusEngineX {

    private final BlockingQueue<NexusEngineEvent> globalQueue = new LinkedBlockingQueue<>();
    private final Map<Integer, BlockingQueue<NexusEngineEvent>> localQueue = new ConcurrentHashMap<>();
    private final AtomicBoolean hasGlobalProcessed = new AtomicBoolean(false);
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
    public BlockingQueue<NexusEngineEvent> getGlobalQueue() {
        return globalQueue;
    }

    /**
     * An open executable method that is used by {@link pw.mihou.nexus.core.managers.NexusShardManager} to tell the EngineX
     * to proceed with sending requests to the specific shard.
     *
     * @param shard The shard to process the events.
     */
    public void onShardReady(DiscordApi shard) {
        CompletableFuture.runAsync(() -> {
            while (!getLocalQueue(shard.getCurrentShard()).isEmpty()) {
                try {
                    NexusEngineEvent event = getLocalQueue(shard.getCurrentShard()).poll();

                    if (event != null) {
                        ((NexusEngineEventCore) event).process(shard);
                    }
                } catch (Throwable exception) {
                    NexusCore.logger.error("An uncaught exception was received by Nexus' EngineX with the following stacktrace.");
                    exception.printStackTrace();
                }
            }
        });

        if (!hasGlobalProcessed.get() && !getGlobalQueue().isEmpty()) {
            hasGlobalProcessed.set(true);
            CompletableFuture.runAsync(() -> {
                while (!getGlobalQueue().isEmpty()) {
                    try {
                        NexusEngineEvent event = getGlobalQueue().poll();

                        if (event != null) {
                            ((NexusEngineEventCore) event).process(shard);
                        }
                    } catch (Throwable exception) {
                        NexusCore.logger.error("An uncaught exception was received by Nexus' EngineX with the following stacktrace.");
                        exception.printStackTrace();
                    }
                }
                hasGlobalProcessed.set(false);
            });
        }
    }

    /**
     * Gets the local queue for this shard. If it doesn't exist then it will add
     * a queue instead and return the newly created queue.
     *
     * @param shard The shard to get the queue of.
     * @return      The blocking queue for this shard.
     */
    public BlockingQueue<NexusEngineEvent> getLocalQueue(int shard) {
        if (!localQueue.containsKey(shard)) {
            localQueue.put(shard, new LinkedBlockingQueue<>());
        }

        return localQueue.get(shard);
    }

    @Override
    public NexusEngineEvent queue(int shard, NexusEngineQueuedEvent event) {
        NexusEngineEventCore engineEvent = new NexusEngineEventCore(event);

        if (nexus.getShardManager().getShard(shard) == null) {
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
        } else {
            engineEvent.process(nexus.getShardManager().getShard(shard));
        }

        return engineEvent;
    }

    @Override
    public NexusEngineEvent queue(NexusEngineQueuedEvent event) {
        NexusEngineEventCore engineEvent = new NexusEngineEventCore(event);

        if (nexus.getShardManager().size() == 0) {
            globalQueue.add(engineEvent);

            Duration expiration = nexus.getConfiguration().timeBeforeExpiringEngineRequests();
            if (!(expiration.isZero() || expiration.isNegative())) {
                NexusThreadPool.schedule(() -> {
                    if (engineEvent.status() == NexusEngineEventStatus.WAITING) {
                        boolean removeFromQueue = globalQueue.remove(engineEvent);
                        NexusCore.logger.warn(
                                "An engine request that was specified for a shard was expired because the shard failed to take hold of the request before expiration. " +
                                        "[acknowledged={}]",
                                removeFromQueue
                        );
                        engineEvent.expire();
                    }
                }, expiration.toMillis(), TimeUnit.MILLISECONDS);
            }
        } else {
            DiscordApi shard = nexus.getShardManager().asStream().findFirst().orElseThrow();
            engineEvent.process(shard);
        }

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
