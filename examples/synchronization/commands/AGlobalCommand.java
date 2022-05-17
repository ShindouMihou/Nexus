package pw.mihou.nexus.commands;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

public class AGlobalCommand implements NexusHandler {

    private final String name = "globalCommand";
    private final String description = "This is a global command that every server can use.";

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow()
                .setContent("Pong! Ping! Hello Global!")
                .respond();
    }
}
