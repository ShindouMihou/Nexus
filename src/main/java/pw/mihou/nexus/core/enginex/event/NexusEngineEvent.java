package pw.mihou.nexus.core.enginex.event;

import pw.mihou.nexus.core.enginex.event.listeners.NexusEngineEventStatusChange;
import pw.mihou.nexus.core.enginex.event.status.NexusEngineEventStatus;

public interface NexusEngineEvent {

    /**
     * Cancels this event from executing. This changes the status from {@link NexusEngineEventStatus#WAITING} to
     * {@link NexusEngineEventStatus#STOPPED}, this is ignored if the cancel was executed during any other status
     * other than waiting.
     */
    void cancel();

    /**
     * Gets the current status of this event.
     *
     * @return  The currents status of this event.
     */
    NexusEngineEventStatus status();

    /**
     * Adds a status change listener for this event.
     *
     * @param event The procedures to execute whenever a status change occurs.
     * @return      The {@link NexusEngineEvent} for chain-calling methods.
     */
    NexusEngineEvent addStatusChangeListener(NexusEngineEventStatusChange event);

}
