package pw.mihou.nexus.core.enginex.facade;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import pw.mihou.nexus.core.enginex.event.NexusEngineEvent;
import pw.mihou.nexus.core.enginex.event.NexusEngineQueuedEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NexusEngineX {

    /**
     * Queues an event to be executed by the specific shard that
     * it is specified for.
     *
     * @param shard The shard to handle this event.
     * @param event The event to execute for this shard.
     * @return      The controller and status viewer for the event.
     */
    NexusEngineEvent queue(int shard, NexusEngineQueuedEvent event);

    /**
     * Queues an event to be executed by the shard that matches the given predicate.
     *
     * @param predicate The predicate that the shard should match.
     * @param event     The event to execute for this shard.
     * @return          The controller and status viewer for the event.
     */
    NexusEngineEvent queue(Predicate<DiscordApi> predicate, NexusEngineQueuedEvent event);

    /**
     * Queues an event to be executed by any specific shard that is available
     * to take the event.
     *
     * @param event The event to execute by a shard.
     * @return      The controller and status viewer for the event.
     */
    NexusEngineEvent queue(NexusEngineQueuedEvent event);

    /**
     * Creates an awaiting listener to wait for a given shard to be ready and returns
     * the shard itself if it is ready.
     *
     * @param shard The shard number to wait to complete.
     * @return The {@link DiscordApi} instance of the given shard.
     */
    CompletableFuture<DiscordApi> await(int shard);

    /**
     * Creates an awaiting listener to wait for a given shard that has the given server.
     *
     * @param server The server to wait for a shard to contain.
     * @return The {@link DiscordApi} instance of the given shard.
     */
    CompletableFuture<Server> await(long server);

    /**
     * Creates an awaiting listener to wait for any available shards to be ready and returns
     * the shard that is available.
     *
     * @return The {@link DiscordApi} available to take any action.
     */
    CompletableFuture<DiscordApi> awaitAvailable();

    /**
     * A short-hand method to cause a failed future whenever the event has expired or has failed to be
     * processed which can happen at times.
     * <br><br>
     * It is recommended to use EngineX with CompletableFutures as this provides an extra kill-switch
     * whenever a shard somehow isn't available after the expiration time.
     *
     * @param event The event that was queued.
     * @param future The future to fail when the status of the event has failed.
     * @param <U> A random type.
     */
    <U> void failFutureOnExpire(NexusEngineEvent event, CompletableFuture<U> future);

    /**
     * Broadcasts the event for all shards to execute, this doesn't wait for any shards
     * that aren't available during the time that it was executed.
     *
     * @param event The event to broadcast to all shards.
     */
    void broadcast(Consumer<DiscordApi> event);

}
