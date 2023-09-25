package pw.mihou.nexus.features.command.facade

import pw.mihou.nexus.features.messages.NexusMessage
import java.util.function.Predicate
import kotlin.Boolean

@JvmDefaultWithCompatibility
interface NexusMiddlewareEvent: NexusCommandEvent {
    fun defer(ephemeral: Boolean = false) {
        this.respondLaterEphemerallyIf(ephemeral).join()
    }

    /**
     * Tells the command interceptor handler to move forward with the next
     * middleware if there is any, otherwise executes the command code.
     */
    fun next()

    /**
     * Stops further command execution. This cancels the command from executing
     * without sending a notice.
     */
    fun stop() {
        stop(null)
    }

    /**
     * Stops further command execution. This cancels the command from executing
     * and sends a notice to the user.
     */
    fun stop(response: NexusMessage?)

    /**
     * Stops further command execution if the predicate returns a value
     * of `true` and allows execution if the predicate is a `false`.
     *
     * @param predicate The predicate to evaluate.
     * @param response  The response to send if the evaluation is false.
     */
    fun stopIf(predicate: Boolean, response: NexusMessage?) {
        if (predicate) {
            stop(response)
        }
    }

    /**
     * Stops further command execution if the predicate returns a value
     * of `true` and allows execution if the predicate is a
     * `false`.
     *
     * @param predicate The predicate to evaluate.
     */
    fun stopIf(predicate: Boolean) {
        stopIf(predicate, null)
    }

    /**
     * Stops further command execution if the predicate returns a value
     * of `true` and allows execution if the predicate is a
     * `false`.
     *
     * @param predicate The predicate to evaluate.
     */
    fun stopIf(predicate: Predicate<Void?>) {
        stopIf(predicate, null)
    }

    /**
     * Stops further command execution if the predicate returns a value
     * of `true` and allows execution if the predicate is a
     * `false`.
     *
     * @param predicate The predicate to evaluate.
     * @param response  The response to send if the evaluation is false.
     */
    fun stopIf(predicate: Predicate<Void?>, response: NexusMessage?) {
        stopIf(predicate.test(null), response)
    }
}