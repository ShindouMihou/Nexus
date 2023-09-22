package pw.mihou.nexus.features.command.interceptors.commons.modules

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.interceptors.facades.NexusAfterware
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant

object NexusLogAfterware: NexusAfterware {

    private const val RESET = "\u001B[0m"
    private const val CYAN = "\u001B[36m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    override fun onAfterCommandExecution(event: NexusCommandEvent) {
        val elapsed = Instant.now().toEpochMilli() - event.interaction.creationTimestamp.toEpochMilli()
        val ratelimited = if ((event["nexus::is_ratelimited"] as? Boolean != null)) "${CYAN}ratelimited=${RESET}false" else ""
        Nexus.logger.info("${GREEN}DISPATCHED: $RESET" +
                "${CYAN}command=$RESET${event.interaction.fullCommandName} " +
                "${CYAN}user=$RESET${event.user.discriminatedName} " +
                "$ratelimited " +
                "Dispatched within ${if (elapsed > 2500) RED else GREEN}${NumberFormat.getInstance().format(elapsed)}ms" +
                "$RESET.")
    }

    override fun onFailedDispatch(event: NexusCommandEvent) {
        val elapsed = Instant.now().toEpochMilli() - event.interaction.creationTimestamp.toEpochMilli()

        val isRatelimited = event["nexus::is_ratelimited"] as? Boolean
        val ratelimitRemaining = event["nexus::ratelimit_remaining"] as? Long

        val ratelimited =
            if (isRatelimited != null) "${CYAN}ratelimited=${if(isRatelimited) RED else GREEN}${isRatelimited}$RESET "
            else ""

        val ratelimitedUntil =
            if (isRatelimited != null && ratelimitRemaining != null && isRatelimited)
                "${CYAN}ratelimited_until=$RESET${NumberFormat.getInstance().format(ratelimitRemaining)}s$RESET "
            else ""

        Nexus.logger.info("${RED}FAILED_DISPATCH: $RESET" +
                "${CYAN}command=$RESET${event.interaction.fullCommandName} " +
                "${CYAN}user=$RESET${event.user.discriminatedName} " +
                "$ratelimited$ratelimitedUntil" +
                "Failed to dispatch, likely due to a middleware rejecting the request. " +
                "It took ${if (elapsed > 2500) RED else GREEN}${NumberFormat.getInstance().format(elapsed)}ms" +
                "$RESET.")
    }
}