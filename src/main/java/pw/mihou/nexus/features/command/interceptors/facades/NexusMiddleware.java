package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;

public interface NexusMiddleware extends NexusCommandInterceptor {

    /**
     * This is executed before the rate-limiter which is always before the command execution.
     * You can prevent further execution by returning a false as a value otherwise allow execution with
     * a true value.
     *
     * @param event The event that was received by Nexus.
     * @return A boolean that indicates whether to allow the command to execute any further.
     */
    NexusMiddlewareGate onBeforeCommand(NexusCommandEvent event);

}
