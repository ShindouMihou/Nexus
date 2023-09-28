package pw.mihou.nexus.express.event

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.express.event.listeners.NexusExpressEventStatusChange
import pw.mihou.nexus.express.event.listeners.NexusExpressEventStatusListener
import pw.mihou.nexus.express.event.status.NexusExpressEventStatus

interface NexusExpressEvent {

    /**
     * Signals the event to be cancelled. This method changes the status of the
     * event from [NexusExpressEventStatus.WAITING] to [NexusExpressEventStatus.STOPPED].
     *
     * The signal will be ignored if the cancel was executed when the status is not equivalent to [NexusExpressEventStatus.WAITING].
     */
    fun cancel()

    /**
     * Gets the current status of the event.
     * @return the current status of the event.
     */
    fun status(): NexusExpressEventStatus

    /**
     * Adds a [NexusExpressEventStatusChange] listener to the event, allowing you to receive events
     * about when the status of the event changes.
     * @param event the listener to use to listen to the event.
     */
    fun addStatusChangeListener(event: NexusExpressEventStatusChange)

    /**
     * Adds a specific [NexusExpressEventStatusListener] that can listen to specific, multiple status changes
     * and react to it. This is a short-hand method of [addStatusChangeListener] and is used to handle specific
     * status change events.
     * @param ev the listener to use to listen to the event.
     * @param statuses the new statuses that will trigger the listener.
     */
    fun addStatusChangeListener(ev: NexusExpressEventStatusListener, vararg statuses: NexusExpressEventStatus) {
        this.addStatusChangeListener status@{ event, _, newStatus ->
            for (status in statuses) {
                if (newStatus == status) {
                    try {
                        ev.onStatusChange(event)
                    } catch (ex: Exception) {
                        Nexus.logger.error("Caught an uncaught exception in a Express Way listener.", ex)
                    }
                }
            }
        }
    }

    /**
     * Adds a [NexusExpressEventStatusListener] that listens specifically to events that causes the listener to
     * cancel, such in the case of a [cancel] or an expire. If you want to listen specifically to a call to [cancel],
     * we recommend using [addStoppedListener] listener instead.
     * @param ev the listener to use to listen to the event.
     */
    fun addCancelListener(ev: NexusExpressEventStatusListener) =
        this.addStatusChangeListener(ev, NexusExpressEventStatus.STOPPED, NexusExpressEventStatus.EXPIRED)

    /**
     * Adds a [NexusExpressEventStatusListener] that listens specifically to when the event is finished processing.
     * @param ev the listener to use to listen to the event.
     */
    fun addFinishedListener(ev: NexusExpressEventStatusListener) =
        this.addStatusChangeListener(ev, NexusExpressEventStatus.FINISHED)

    /**
     * Adds a [NexusExpressEventStatusListener] that listens specifically to when the event expired while waiting.
     * @param ev the listener to use to listen to the event.
     */
    fun addExpiredListener(ev: NexusExpressEventStatusListener) =
        this.addStatusChangeListener(ev, NexusExpressEventStatus.EXPIRED)

    /**
     * Adds a [NexusExpressEventStatusListener] that listens specifically to events that causes the listener to
     * [cancel]. If you want to listen specifically to a call to [cancel] and expire, we recommend using
     * [addCancelListener] listener instead which listens to both cancel and expire.
     * @param ev the listener to use to listen to the event.
     */
    fun addStoppedListener(ev: NexusExpressEventStatusListener) =
        this.addStatusChangeListener(ev, NexusExpressEventStatus.STOPPED)

}