package pw.mihou.nexus.features.command.interceptors.core;

import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddlewareGate;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NexusMiddlewareGateCore implements NexusMiddlewareGate {

    private final AtomicBoolean state = new AtomicBoolean(true);
    private final AtomicReference<NexusMessage> response = new AtomicReference<>(null);

    /**
     * Sets the state for this middleware.
     *
     * @param next  Should the event be allowed to go next?
     */
    private void setState(boolean next) {
        state.set(next);
    }

    /**
     * Sets the response for this middleware gate. This assumes that the middleware
     * will be rejected since a response can only be set when a middleware rejects
     * an event.
     *
     * @param response  The response to send to the end-user.
     */
    private void setResponse(@Nullable NexusMessage response) {
        setState(false);
        this.response.set(response);
    }

    /**
     * Is the command allowed to execute any further?
     *
     * @return Is the command allowed to execute any further?
     */
    public boolean isAllowed() {
        return state.get();
    }

    @Override
    public void next() {
        setState(true);
    }

    @Override
    public void stop(@Nullable NexusMessage response) {
        setResponse(response);
    }

    @Override
    public void stop() {
        setState(false);
    }

    /**
     * Gets the response of the middleware.
     *
     * @return The response of the middleware, this can be null.
     */
    @Nullable
    public NexusMessage response() {
        return response.get();
    }
}
