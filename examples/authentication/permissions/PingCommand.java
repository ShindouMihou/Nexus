package pw.mihou.nexus.commands;

import org.javacord.api.entity.permission.PermissionType;
import pw.mihou.nexus.core.reflective.annotations.Share;
import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors;

import java.util.List;

@NexusAttach
public class PingCommand implements NexusHandler {

    private final String name = "ping";
    private final String description = "An example of how a command can share custom fields to a middleware, etc.";

    private final List<String> middlewares = List.of(NexusCommonInterceptors.NEXUS_AUTH_PERMISSIONS_MIDDLEWARE);
    @Share private final List<PermissionType> requiredPermissions = List.of(PermissionType.ATTACH_FILE);

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow().setContent("Pong!").respond();
    }
}