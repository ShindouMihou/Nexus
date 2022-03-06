package commands;

import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors;

import java.util.List;

@NexusAttach
public class HasMiddlewaresCommand implements NexusHandler {

    private final String name = "ping";
    private final String description = "Hello, middleware fields!";

    private final List<String> middlewares = List.of(NexusCommonInterceptors.NEXUS_RATELIMITER);

    @Override
    public void onEvent(NexusCommandEvent event) {

    }
}
