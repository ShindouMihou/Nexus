package pw.mihou.nexus.commands;

import pw.mihou.nexus.core.reflective.annotations.Share;
import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;

@NexusAttach
public class PingCommand implements NexusHandler {

    private final String name = "ping";
    private final String description = "An example of how a command can share custom fields to a middleware, etc.";

    private final List<String> middlewares = List.of("nexus.demo.sharedfields");
    @Share private final String oneSharedField = "If this is empty, then it should be stopped by the middleware.";
    private final String onePrivateField = "If this is present, then it should be stopped by the middleware.";

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow().setContent("Pong!").respond();
    }
}