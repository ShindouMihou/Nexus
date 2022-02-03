package pw.mihou.nexus.features.command.interceptors.core;

import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddlewareGate;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

public class NexusMiddlewareGateCore implements NexusMiddlewareGate {

    private final boolean state;
    private final NexusMessage response;

    /**
     * Creates a new Nexus Middleware Gate.
     *
     * @param state The state response of the gate.
     * @param response The text response of the gate.
     */
    public NexusMiddlewareGateCore(boolean state, NexusMessage response) {
        this.state = state;
        this.response = response;
    }

    /**
     * Is the command allowed to execute any further?
     *
     * @return Is the command allowed to execute any further?
     */
    public boolean isAllowed() {
        return state;
    }

    /**
     * Gets the response of the middleware.
     *
     * @return The response of the middleware, this can be null.
     */
    public NexusMessage getResponse() {
        return response;
    }
}
