package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

public interface NexusMiddlewareGate {

    /**
     * Allows the command to execute forward.
     *
     * @return The middleware gate to use.
     */
    static NexusMiddlewareGate next() {
        return new NexusMiddlewareGateCore(true, null);
    }

    /**
     * Stops further command execution. This cancels the command from executing
     * and sends a notice to the user.
     *
     * @return The middleware gate to use.
     */
    static NexusMiddlewareGate stop(NexusMessage response) {
        return new NexusMiddlewareGateCore(false, response);
    }

    /**
     * Stops further command execution. This cancels the command from executing
     * without sending a notice.
     *
     * @return The middleware gate to use.
     */
    static NexusMiddlewareGate stop() {
        return stop(null);
    }

}
