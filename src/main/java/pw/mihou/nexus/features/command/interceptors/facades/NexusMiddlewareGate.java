package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.messages.facade.NexusMessage;

import javax.annotation.Nullable;

public interface NexusMiddlewareGate {

    /**
     * Tells the command interceptor handler to move forward with the next
     * middleware if there is any, otherwise executes the command code.
     */
    void next();

    /**
     * Stops further command execution. This cancels the command from executing
     * and sends a notice to the user.
     */
    void stop(@Nullable NexusMessage response);

    /**
     * Stops further command execution. This cancels the command from executing
     * without sending a notice.
     */
    void stop();

}
