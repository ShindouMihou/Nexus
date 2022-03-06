package pw.mihou.nexus.features.command.interceptors.commons.core;

import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository;
import pw.mihou.nexus.features.messages.facade.NexusMessage;
import pw.mihou.nexus.features.ratelimiter.core.NexusRatelimiterCore;

import static pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors.*;

public class NexusCommonInterceptorsCore extends NexusInterceptorRepository {

    @Override
    public void define() {
        middleware(NEXUS_AUTH_BOT_OWNER_MIDDLEWARE, event -> event.stopIf(
                !event.getUser().isBotOwner(),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the bot owner to execute this command.")
        ));

        middleware(NEXUS_AUTH_SERVER_OWNER_MIDDLEWARE, event -> event.stopIf(
                event.getServer().isPresent() && event.getServer().get().isOwner(event.getUser()),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the server owner to execute this command.")
        ));

        middleware(NEXUS_GATE_SERVER, event -> event.stopIf(event.getServer().isEmpty()));
        middleware(NEXUS_GATE_DMS, event -> event.stopIf(event.getServer().isPresent()));
        middleware(NEXUS_RATELIMITER, new NexusRatelimiterCore());
    }

}
