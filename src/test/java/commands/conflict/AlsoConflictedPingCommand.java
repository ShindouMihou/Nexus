package commands.conflict;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

public class AlsoConflictedPingCommand implements NexusHandler {
    private final String name = "ping";
    private final String description = "Ping pong!";

    @Override
    public void onEvent(NexusCommandEvent event) {
    }
}
