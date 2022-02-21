package pw.mihou.nexus.core.enginex.event.listeners;

import pw.mihou.nexus.core.enginex.event.NexusEngineEvent;
import pw.mihou.nexus.core.enginex.event.status.NexusEngineEventStatus;

public interface NexusEngineEventStatusChange {

    /**
     * Executed whenever a {@link NexusEngineEvent} changes its status quo from
     * one status such as {@link NexusEngineEventStatus#WAITING} to another status such as
     * {@link NexusEngineEventStatus#PROCESSING}.
     *
     * @param event     The event that broadcasted this event.
     * @param oldStatus The old status of the event.
     * @param newStatus The new status of the event.
     */
    void onStatusChange(
            NexusEngineEvent event,
            NexusEngineEventStatus oldStatus,
            NexusEngineEventStatus newStatus
    );

}
