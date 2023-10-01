package pw.mihou.nexus.features.command.interceptors.commons.modules

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.annotations.Name
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware
import java.time.Instant

@Name("nexus.ignore::expired")
object NexusIgnoreExpiredEventsMiddleware: NexusMiddleware {

    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val CYAN = "\u001B[36m"

    /**
     * Whether to log a WARN message whenever an event is stopped due to being expired or not.
     */
    @JvmField @Volatile var logExpiredEvents = true

    /**
     * The maximum amount of milliseconds ahead before an event is considered expired.
     */
    @JvmField @Volatile var maximumMillisBeforeExpired = 2_800L

    override fun onBeforeCommand(event: NexusMiddlewareEvent) {
        val maximum = event.interaction.creationTimestamp.plusMillis(maximumMillisBeforeExpired)
        val now = Instant.now()

        if (now.isAfter(maximum)) {
            if (logExpiredEvents) {
                val trespassedMillis = maximum.toEpochMilli() - now.toEpochMilli()
                Nexus.logger.warn(
                    "$RED EXPIRED_EVENT: $RESET " +
                            "${CYAN}command=${RESET}${event.interaction.fullCommandName} " +
                            "${CYAN}user=${RESET}${event.user.discriminatedName} " +
                            "${RED}trespassed_millis=${RESET}${trespassedMillis}ms $RESET " +
                            "Stopped a command from executing due to being received way before we can defer."
                )
            }
            event.stop()
        }
    }

}