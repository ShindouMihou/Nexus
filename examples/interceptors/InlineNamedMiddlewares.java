package pw.mihou.nexus;

import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor;

import java.util.List;

public class InlineNamedMiddlewares implements NexusHandler {

    private final String name = "ping";
    private final String description = "An example of how a command can share custom fields to a middleware, etc.";

    private final List<String> middlewares = List.of(
            NexusCommandInterceptor.addMiddleware("nexus.auth.server", (event) -> event.stopIf(event.getServer().isEmpty()))
    );

    @Override
    public void onEvent(NexusCommandEvent event) {
        event.respondNow().setContent("Pong!").respond();
    }
}