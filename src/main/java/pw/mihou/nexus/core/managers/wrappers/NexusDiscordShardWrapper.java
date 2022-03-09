package pw.mihou.nexus.core.managers.wrappers;

import org.javacord.api.DiscordApi;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.enginex.core.NexusEngineXCore;
import pw.mihou.nexus.core.enginex.event.core.NexusEngineEventCore;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NexusDiscordShardWrapper {

    private final AtomicBoolean active = new AtomicBoolean(true);
    private final DiscordApi api;

    /**
     * Creates a new {@link NexusDiscordShardWrapper} which wraps around a
     * shard to handle {@link pw.mihou.nexus.core.enginex.event.NexusEngineEvent}s from the
     * EngineX engine.
     *
     * @param nexus The {@link Nexus} instance to base from.
     * @param api   The {@link DiscordApi} that is being wrapped.
     */
    public NexusDiscordShardWrapper(NexusCore nexus, DiscordApi api) {
        this.api = api;

        NexusEngineXCore engineX = ((NexusEngineXCore) nexus.getEngineX());
        NexusThreadPool.schedule(() -> CompletableFuture.runAsync(() -> {
            while (active.get()) {
                NexusEngineEventCore globalEvent = (NexusEngineEventCore) engineX.getGlobalQueue().poll();
                if (globalEvent != null) {
                    globalEvent.process(api);
                }

                NexusEngineEventCore localEvent = (NexusEngineEventCore) engineX.getLocalQueue(api.getCurrentShard()).poll();
                if (localEvent != null) {
                    localEvent.process(api);
                }
            }
        }, NexusThreadPool.executorService), 2, TimeUnit.SECONDS);
    }

    /**
     * Gets the {@link DiscordApi} that is being wrapped by this instance.
     *
     * @return  The {@link DiscordApi} being wrapped by this instance.
     */
    public DiscordApi api() {
        return api;
    }

    /**
     * Disables the looping function of this shard which constantly pools for any
     * new events that it can receive.
     */
    public void disable() {
        active.set(false);
    }

}
