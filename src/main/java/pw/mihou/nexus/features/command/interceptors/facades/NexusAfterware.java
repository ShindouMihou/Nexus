package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;

public interface NexusAfterware extends NexusCommandInterceptor {

    /**
     * This is executed after the command was executed. You can expect functionality such as
     * `respondNow` and similar to be expired.
     *
     * @param event The event that was received by Nexus.
     */
    void onAfterCommandExecution(NexusCommandEvent event);

}
