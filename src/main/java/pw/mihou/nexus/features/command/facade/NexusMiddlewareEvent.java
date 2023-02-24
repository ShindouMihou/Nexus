package pw.mihou.nexus.features.command.facade;


import pw.mihou.nexus.features.messages.NexusMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface NexusMiddlewareEvent extends NexusCommandEvent {

    default void defer() {
        this.respondLater().join();
    }

    default void deferEphemeral() {
        this.respondLaterAsEphemeral().join();
    }

    /**
     * Tells the command interceptor handler to move forward with the next
     * middleware if there is any, otherwise executes the command code.
     */
    void next();

    /**
     * Stops further command execution. This cancels the command from executing
     * without sending a notice.
     */
    default void stop() {
        stop(null);
    }

    /**
     * Stops further command execution. This cancels the command from executing
     * and sends a notice to the user.
     */
    void stop(NexusMessage response);

    /**
     * Stops further command execution if the predicate returns a value
     * of {@link Boolean#TRUE} and allows execution if the predicate is a
     * {@link Boolean#FALSE}.
     *
     * @param predicate The predicate to evaluate.
     * @param response  The response to send if the evaluation is false.
     */
    default void stopIf(boolean predicate, @Nullable NexusMessage response) {
        if (predicate) {
            stop(response);
        }
    }

    /**
     * Stops further command execution if the predicate returns a value
     * of {@link Boolean#TRUE} and allows execution if the predicate is a
     * {@link Boolean#FALSE}.
     *
     * @param predicate The predicate to evaluate.
     */
    default void stopIf(boolean predicate) {
        stopIf(predicate, null);
    }

    /**
     * Stops further command execution if the predicate returns a value
     * of {@link Boolean#TRUE} and allows execution if the predicate is a
     * {@link Boolean#FALSE}.
     *
     * @param predicate The predicate to evaluate.
     */
    default void stopIf(@Nonnull Predicate<Void> predicate) {
        stopIf(predicate, null);
    }

    /**
     * Stops further command execution if the predicate returns a value
     * of {@link Boolean#TRUE} and allows execution if the predicate is a
     * {@link Boolean#FALSE}.
     *
     * @param predicate The predicate to evaluate.
     * @param response  The response to send if the evaluation is false.
     */
    default void stopIf(@Nonnull Predicate<Void> predicate, @Nullable NexusMessage response) {
        stopIf(predicate.test(null), response);
    }


}
