package pw.mihou.nexus.core.enginex.event;

import org.javacord.api.DiscordApi;
import pw.mihou.nexus.core.enginex.event.media.NexusEngineEventWriteableStore;

public interface NexusEngineQueuedEvent {

    /**
     * Executed whenever it needs to be processed by the specific shard that
     * is responsible for handling this event.
     *
     * @param api   The {@link DiscordApi} that is handling this event.
     * @param store The {@link NexusEngineEventWriteableStore} that can be used to store
     *              and access data shared between the event and the queuer.
     */
    void onEvent(DiscordApi api, NexusEngineEventWriteableStore store);

}
