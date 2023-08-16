package pw.mihou.nexus.express.event.listeners

import pw.mihou.nexus.express.event.NexusExpressEvent

fun interface NexusExpressEventStatusListener {
    fun onStatusChange(event: NexusExpressEvent)
}