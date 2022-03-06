package commands;

import pw.mihou.nexus.core.reflective.annotations.Share;
import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

@NexusAttach
public class HasSharedFieldsCommand implements NexusHandler {

    private final String name = "ping";
    private final String description = "Hello, shared fields!";

    @Share private final String oneSharedField = "This should be visible to middlewares and afterwares.";
    private final String oneNonSharedField = "This should not be visible to middlewares and afterwares.";

    @Override
    public void onEvent(NexusCommandEvent event) {

    }
}
