package pw.mihou.nexus.features.command.interceptors.commons.core;

import pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors;
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor;
import pw.mihou.nexus.features.messages.facade.NexusMessage;
import pw.mihou.nexus.features.ratelimiter.core.NexusRatelimiterCore;

public class NexusCommonInterceptorsCore extends NexusCommonInterceptors {

    public static void addAll() {
        NexusCommandInterceptor.addMiddleware(NEXUS_AUTH_BOT_OWNER_MIDDLEWARE, event -> event.stopIf(
                !event.getUser().isBotOwner(),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the bot owner to execute this command.")
        ));

        NexusCommandInterceptor.addMiddleware(NEXUS_AUTH_SERVER_OWNER_MIDDLEWARE, event -> event.stopIf(
                event.getServer().isPresent() && event.getServer().get().isOwner(event.getUser()),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the server owner to execute this command.")
        ));

        NexusCommandInterceptor.addMiddleware(NEXUS_GATE_SERVER, event -> event.stopIf(event.getServer().isEmpty()));
        NexusCommandInterceptor.addMiddleware(NEXUS_GATE_DMS, event -> event.stopIf(event.getServer().isPresent()));
        NexusCommandInterceptor.addMiddleware(NEXUS_RATELIMITER, new NexusRatelimiterCore());
    }

}
