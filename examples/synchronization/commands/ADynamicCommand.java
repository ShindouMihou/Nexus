package pw.mihou.nexus.commands;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

import java.util.List;

public class ADynamicCommand implements NexusHandler {

    private final String name = "dynamic";
    private final String description = "A dynamic server slash command!";

    // 0L is recognized by Nexus as a switch to recognize this command as
    // a server slash command. It is ignored in any sort of updates.
    //
    // For more verbosity, Nexus has this as a public static field.
    private final List<Long> serverIds = NexusCommand.with(NexusCommand.PLACEHOLDER_SERVER_ID);

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNowWith("Dyna-dynam-iteee!");
    }
}
