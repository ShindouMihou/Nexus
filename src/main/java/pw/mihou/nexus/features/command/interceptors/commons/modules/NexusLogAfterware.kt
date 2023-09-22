package pw.mihou.nexus.features.command.interceptors.commons.modules

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.facades.NexusAfterware
import java.text.NumberFormat
import java.time.Instant

object NexusLogAfterware: NexusAfterware {

    private const val RESET = "\u001B[0m"
    private const val CYAN = "\u001B[36m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    override fun onAfterCommandExecution(event: NexusCommandEvent) {
        val elapsed = Instant.now().toEpochMilli() - event.interaction.creationTimestamp.toEpochMilli()
        Nexus.logger.info("" +
                "${CYAN}command=$RESET${event.interaction.fullCommandName} " +
                "${CYAN}user=$RESET${event.user.discriminatedName} " +
                "Dispatched within ${if (elapsed > 2500) RED else GREEN}${NumberFormat.getInstance().format(elapsed)}ms" +
                "$RESET.")
    }
}