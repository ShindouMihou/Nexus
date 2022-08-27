package pw.mihou.nexus.core.enginex.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.commons.Pair;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.enginex.event.NexusEngineEvent;
import pw.mihou.nexus.core.enginex.event.NexusEngineQueuedEvent;
import pw.mihou.nexus.core.enginex.event.core.NexusEngineEventCore;
import pw.mihou.nexus.core.enginex.event.status.NexusEngineEventStatus;
import pw.mihou.nexus.core.enginex.facade.NexusEngineX;
import pw.mihou.nexus.core.exceptions.NexusFailedActionException;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NexusEngineXCore implements NexusEngineX {

    private final BlockingQueue<NexusEngineEvent> globalQueue = new LinkedBlockingQueue<>();
    private final Map<Integer, BlockingQueue<NexusEngineEvent>> localQueue = new ConcurrentHashMap<>();

    private final BlockingQueue<Pair<Predicate<DiscordApi>, NexusEngineEvent>> predicateQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock predicateQueueLock = new ReentrantLock();
    private final ReentrantLock globalProcessingLock = new ReentrantLock();
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
     * Gets the local queue for this shard. If it doesn't exist then it will add
     * a queue instead and return the newly created queue.
     *
     * @param shard The shard to get the queue of.
     * @return      The blocking queue for this shard.
     */
    private BlockingQueue<NexusEngineEvent> getLocalQueue(int shard) {
        return localQueue.computeIfAbsent(shard, key -> new LinkedBlockingQueue<>());
    }

    /**
     * An open executable method that is used by {@link pw.mihou.nexus.core.managers.NexusShardManager} to tell the EngineX
     * to proceed with sending requests to the specific shard.
     *
     * @param shard The shard to process the events.
     */
    public void onShardReady(DiscordApi shard) {
        NexusThreadPool.executorService.submit(() -> {
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

            predicateQueueLock.lock();
            try {
                while (!predicateQueue.isEmpty()) {
                    try {
                        Predicate<DiscordApi> predicate = predicateQueue.peek().getLeft();

                        if (!predicate.test(shard)) {
                            continue;
                        }

                        NexusEngineEvent event = Objects.requireNonNull(predicateQueue.poll()).getRight();
                        if (event != null) {
                            ((NexusEngineEventCore) event).process(shard);
                        }
                    } catch (Throwable exception) {
                        NexusCore.logger.error("An uncaught exception was received by Nexus' EngineX with the following stacktrace.");
                        exception.printStackTrace();
                    }
                }
            } finally {
                predicateQueueLock.unlock();
            }
        });

        NexusThreadPool.executorService.submit(() -> {
           globalProcessingLock.lock();
           try {
               while (!globalQueue.isEmpty()) {
                   try {
                       NexusEngineEvent event = globalQueue.poll();

                       if (event != null) {
                           ((NexusEngineEventCore) event).process(shard);
                       }
                   } catch (Throwable exception) {
                       NexusCore.logger.error("An uncaught exception was received by Nexus' EngineX with the following stacktrace.");
                       exception.printStackTrace();
                   }
               }
           } finally {
               globalProcessingLock.unlock();
           }
        });
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
    public NexusEngineEvent queue(Predicate<DiscordApi> predicate, NexusEngineQueuedEvent event) {
        NexusEngineEventCore engineEvent = new NexusEngineEventCore(event);
        DiscordApi $shard = nexus.getShardManager().asStream().filter(predicate).findFirst().orElse(null);

        if ($shard == null) {
            Pair<Predicate<DiscordApi>, NexusEngineEvent> queuedPair = Pair.of(predicate, engineEvent);
            predicateQueue.add(queuedPair);

            Duration expiration = nexus.getConfiguration().timeBeforeExpiringEngineRequests();
            if (!(expiration.isZero() || expiration.isNegative())) {
                NexusThreadPool.schedule(() -> {
                    if (engineEvent.status() == NexusEngineEventStatus.WAITING) {
                        boolean removeFromQueue = predicateQueue.remove(queuedPair);
                        NexusCore.logger.warn(
                                "An engine request that was specified for a shard was expired because the shard failed to take hold of the request before expiration. " +
                                        "[sacknowledged={}]",
                                removeFromQueue
                        );
                        engineEvent.expire();
                    }
                }, expiration.toMillis(), TimeUnit.MILLISECONDS);
            }
        } else {
            engineEvent.process($shard);
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
    public CompletableFuture<DiscordApi> await(int shard) {
        DiscordApi $shard = nexus.getShardManager().getShard(shard);

        if ($shard != null) {
            return CompletableFuture.completedFuture($shard);
        }

        CompletableFuture<DiscordApi> future = new CompletableFuture<>();
        failFutureOnExpire(queue(shard, future::complete), future);

        return future;
    }

    @Override
    public CompletableFuture<Server> await(long server) {
        DiscordApi $shard = nexus.getShardManager().getShardOf(server).orElse(null);

        if ($shard != null) {
            return CompletableFuture.completedFuture($shard.getServerById(server).orElseThrow());
        }

        CompletableFuture<Server> future = new CompletableFuture<>();
        failFutureOnExpire(
                queue(
                        (shard) -> shard.getServerById(server).isPresent(),
                        (shard) -> future.complete(shard.getServerById(server).orElseThrow())
                ),
                future
        );

        return future;
    }

    @Override
    public CompletableFuture<DiscordApi> awaitAvailable() {
        DiscordApi $shard = nexus.getShardManager().asStream().findFirst().orElse(null);

        if ($shard != null) {
            return CompletableFuture.completedFuture($shard);
        }

        CompletableFuture<DiscordApi> future = new CompletableFuture<>();
        failFutureOnExpire(queue(future::complete), future);

        return future;
    }

    @Override
    public <U> void failFutureOnExpire(NexusEngineEvent event, CompletableFuture<U> future) {
        event.addStatusChangeListener(($event, oldStatus, newStatus) -> {
            if (newStatus == NexusEngineEventStatus.EXPIRED || newStatus == NexusEngineEventStatus.STOPPED) {
                future.completeExceptionally(
                        new NexusFailedActionException("Failed to connect with the shard that was waited to complete this action.")
                );
            }
        });
    }

    @Override
    public void broadcast(Consumer<DiscordApi> event) {
        nexus.getShardManager()
                .asStream()
                .forEach(api -> CompletableFuture
                        .runAsync(() -> event.accept(api), NexusThreadPool.executorService));
    }
}
