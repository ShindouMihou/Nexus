package pw.mihou.nexus.core.enginex.event;

import org.javacord.api.DiscordApi;

public interface NexusEngineQueuedEvent {

    /**
     * Executed whenever it needs to be processed by the specific shard that
     * is responsible for handling this event.
     *
     * @param api   The {@link DiscordApi} that is handling this event.
     */
    void onEvent(DiscordApi api);

}
