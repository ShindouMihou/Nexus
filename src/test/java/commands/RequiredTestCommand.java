package commands;

import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

@NexusAttach
public class RequiredTestCommand implements NexusHandler {

    private final String name = null;
    private final String description = null;

    @Override
    public void onEvent(NexusCommandEvent event) {

    }
}
