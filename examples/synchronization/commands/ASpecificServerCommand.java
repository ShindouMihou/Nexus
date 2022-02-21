package pw.mihou.nexus.commands;

import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

import java.util.List;

@NexusAttach
public class ASpecificServerCommand implements NexusHandler {

    private final String name = "specificServer";
    private final String description = "This is a command dedicated to a specific server!";
    private final List<Long> serverIds = List.of(
            807084089013174272L
    );

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow()
                .setContent("This command is dedicated to this server!")
                .respond();
    }
}
