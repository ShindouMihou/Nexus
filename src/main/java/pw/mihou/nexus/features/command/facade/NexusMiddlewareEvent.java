package pw.mihou.nexus.features.command.facade;

import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddlewareGate;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

import java.util.concurrent.CompletableFuture;

public interface NexusMiddlewareEvent extends NexusCommandEvent {

    /**
     * A middleware-only function that tells Discord that the response will be
     * taking more than three-seconds because the middleware has to process tasks
     * that can take more than three-second limit.
     */
    default CompletableFuture<Void> askDelayedResponse() {
            return CompletableFuture.allOf(
                    getNexus().getResponderRepository().peek(getBaseEvent().getInteraction())
            );
    }

    /**
     * Tells the command interceptor handler to move forward with the next
     * middleware if there is any, otherwise executes the command code.
     *
     * @return  The {@link NexusMiddlewareGate} that should be returned
     * in the function.
     */
    default NexusMiddlewareGate next() {
        return NexusMiddlewareGate.next();
    }

    /**
     * Stops further command execution. This cancels the command from executing
     * without sending a notice.
     *
     * @return The middleware gate to use.
     */
    default NexusMiddlewareGate stop() {
        return NexusMiddlewareGate.stop();
    }

    /**
     * Stops further command execution. This cancels the command from executing
     * and sends a notice to the user.
     *
     * @return The middleware gate to use.
     */
    default NexusMiddlewareGate stop(NexusMessage response) {
        return NexusMiddlewareGate.stop(response);
    }

}
