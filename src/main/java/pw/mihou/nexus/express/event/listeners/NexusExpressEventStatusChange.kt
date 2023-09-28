package pw.mihou.nexus.express.event.listeners

import pw.mihou.nexus.express.event.NexusExpressEvent
import pw.mihou.nexus.express.event.status.NexusExpressEventStatus

fun interface NexusExpressEventStatusChange {

    fun onStatusChange(event: NexusExpressEvent, oldStatus: NexusExpressEventStatus, newStatus: NexusExpressEventStatus)

}