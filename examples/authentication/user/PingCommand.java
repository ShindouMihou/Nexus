package pw.mihou.nexus.commands;

import pw.mihou.nexus.core.reflective.annotations.Share;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors;

import java.util.List;

public class PingCommand implements NexusHandler {

    private final String name = "ping";
    private final String description = "An example of how a command can share custom fields to a middleware, etc.";

    private final List<String> middlewares = List.of(NexusCommonInterceptors.NEXUS_AUTH_USER_MIDDLEWARE);
    @Share private final List<Long> requiredUsers = List.of(584322030934032393L);

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow().setContent("Pong!").respond();
    }
}