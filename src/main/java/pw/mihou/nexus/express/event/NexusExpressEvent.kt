package pw.mihou.nexus.express.event

import pw.mihou.nexus.express.event.listeners.NexusExpressEventStatusChange
import pw.mihou.nexus.express.event.status.NexusExpressEventStatus

interface NexusExpressEvent {

    /**
     * Signals the event to be cancelled. This method changes the status of the
     * event from [NexusExpressEventStatus.WAITING] to [NexusExpressEventStatus.STOPPED].
     *
     * The signal will be ignored if the cancel was executed when the status is not equivalent to [NexusExpressEventStatus.WAITING].
     */
    fun cancel()
    fun status(): NexusExpressEventStatus
    fun addStatusChangeListener(event: NexusExpressEventStatusChange)

}