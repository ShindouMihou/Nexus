package pw.mihou.nexus.features.command.interceptors.commons.core;

import pw.mihou.nexus.features.command.interceptors.commons.modules.auth.NexusAuthMiddleware;
import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository;
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core.NexusRatelimiterCore;

import static pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors.*;

public class NexusCommonInterceptorsCore extends NexusInterceptorRepository {

    @Override
    public void define() {
        middleware(NEXUS_AUTH_BOT_OWNER_MIDDLEWARE, NexusAuthMiddleware::onBotOwnerAuthenticationMiddleware);
        middleware(NEXUS_AUTH_SERVER_OWNER_MIDDLEWARE, NexusAuthMiddleware::onServerOwnerAuthenticationMiddleware);
        middleware(NEXUS_AUTH_PERMISSIONS_MIDDLEWARE, NexusAuthMiddleware::onPermissionsAuthenticationMiddleware);
        middleware(NEXUS_AUTH_ROLES_MIDDLEWARE, NexusAuthMiddleware::onRoleAuthenticationMiddleware);
        middleware(NEXUS_AUTH_USER_MIDDLEWARE, NexusAuthMiddleware::onUserAuthenticationMiddleware);
        middleware(NEXUS_GATE_SERVER, event -> event.stopIf(event.getServer().isEmpty()));
        middleware(NEXUS_GATE_DMS, event -> event.stopIf(event.getServer().isPresent()));
        middleware(NEXUS_RATELIMITER, new NexusRatelimiterCore());
    }

}
