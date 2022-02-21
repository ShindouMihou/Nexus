package pw.mihou.nexus.commands;

import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

import java.util.List;

@NexusAttach
public class ADynamicCommand implements NexusHandler {

    private final String name = "dynamic";
    private final String description = "A dynamic server slash command!";

    // 0L is recognized by Nexus as a switch to recognize this command as
    // a server slash command. It is ignored in any sort of updates.
    private final List<Long> serverIds = List.of(
            0L
    );

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow()
                .setContent("Dyna-dynam-iteee!")
                .respond();
    }
}
