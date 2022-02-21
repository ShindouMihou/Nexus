package pw.mihou.nexus.core.enginex.facade;

import org.javacord.api.DiscordApi;

public interface NexusDiscordShard {

    /**
     * Unwraps the {@link NexusDiscordShard} to gain access to the actual
     * {@link DiscordApi} instance that is being wrapped inside.
     *
     * @return  The {@link DiscordApi} that is being handled by this instance.
     */
    DiscordApi asDiscordApi();

}
