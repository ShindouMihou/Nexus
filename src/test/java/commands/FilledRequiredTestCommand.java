package commands;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.inheritance.Inherits;

public class FilledRequiredTestCommand implements NexusHandler {

    private final String name = "fulfilled";
    private final String description = "fulfilled";

    @Override
    public void onEvent(NexusCommandEvent event) {

    }
}
