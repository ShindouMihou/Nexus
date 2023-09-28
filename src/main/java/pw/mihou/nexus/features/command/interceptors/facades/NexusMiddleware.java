package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent;
import pw.mihou.nexus.features.messages.NexusMessage;

public interface NexusMiddleware extends NexusCommandInterceptor {

    /**
     * This is executed before the rate-limiter which is always before the command execution.
     * You can prevent further execution by using {@link NexusMiddlewareEvent#stop(NexusMessage)} or
     * {@link NexusMiddlewareEvent#stop()}, by default, it will always be using the {@link NexusMiddlewareGate#next()}.
     *
     * @param event The event that was received by Nexus.
     */
    void onBeforeCommand(NexusMiddlewareEvent event);

}
