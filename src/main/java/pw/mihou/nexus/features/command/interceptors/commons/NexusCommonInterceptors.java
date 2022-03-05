package pw.mihou.nexus.features.command.interceptors.commons;

import pw.mihou.nexus.features.messages.facade.NexusMessage;
import pw.mihou.nexus.features.ratelimiter.core.NexusRatelimiterCore;

import static pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor.addMiddleware;

/**
 * This class exists purely to separate the static function of adding
 * all the common interceptors. You can use this as a reference for all built-in Nexus middlewares
 * and afterwares.
 */
public class NexusCommonInterceptors {

    public static final String NEXUS_AUTH_BOT_OWNER_MIDDLEWARE = "nexus.auth.bot.owner";
    public static final String NEXUS_AUTH_SERVER_OWNER_MIDDLEWARE = "nexus.auth.server.owner";
    public static final String NEXUS_GATE_SERVER = "nexus.gate.server";
    private static final String NEXUS_GATE_DMS = "nexus.gate.dms";
    private static final String NEXUS_RATELIMITER = "nexus.ratelimiter";

    static {
        addMiddleware(NEXUS_AUTH_BOT_OWNER_MIDDLEWARE, event -> event.stopIf(
                !event.getUser().isBotOwner(),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the bot owner to execute this command.")
        ));

        addMiddleware(NEXUS_AUTH_SERVER_OWNER_MIDDLEWARE, event -> event.stopIf(
                event.getServer().isPresent() && event.getServer().get().isOwner(event.getUser()),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the server owner to execute this command.")
        ));

        addMiddleware(NEXUS_GATE_SERVER, event -> event.stopIf(event.getServer().isEmpty()));
        addMiddleware(NEXUS_GATE_DMS, event -> event.stopIf(event.getServer().isPresent()));
        addMiddleware(NEXUS_RATELIMITER, new NexusRatelimiterCore());
    }

}
