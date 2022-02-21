package pw.mihou.nexus.core.enginex.facade;

import org.javacord.api.DiscordApi;
import pw.mihou.nexus.core.enginex.event.NexusEngineEvent;
import pw.mihou.nexus.core.enginex.event.NexusEngineQueuedEvent;

import java.util.function.Consumer;

public interface NexusEngineX {

    /**
     * Queues an event to be executed by the specific shard that
     * it is specified for.
     *
     * @param shard The shard to handle this event.
     * @param event The event to execute for this shard.
     * @return      The controller and status viewer for the event.
     */
    NexusEngineEvent queue(long shard, NexusEngineQueuedEvent event);

    /**
     * Queues an event to be executed by any specific shard that is available
     * to take the event.
     *
     * @param event The event to execute by a shard.
     * @return      The controller and status viewer for the event.
     */
    NexusEngineEvent queue(NexusEngineQueuedEvent event);

    /**
     * Broadcasts the event for all shards to execute, this doesn't wait for any shards
     * that aren't available during the time that it was executed.
     *
     * @param event The event to broadcast to all shards.
     */
    void broadcast(Consumer<DiscordApi> event);

}
