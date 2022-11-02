package pw.mihou.nexus.features.command.interceptors.commons.core;

import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository;
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core.NexusRatelimiterCore;

import static pw.mihou.nexus.features.command.interceptors.commons.NexusCommonInterceptors.*;

public class NexusCommonInterceptorsCore extends NexusInterceptorRepository {

    @Override
    public void define() {
        middleware(NEXUS_GATE_SERVER, event -> event.stopIf(event.getServer().isEmpty()));
        middleware(NEXUS_GATE_DMS, event -> event.stopIf(event.getServer().isPresent()));
        middleware(NEXUS_RATELIMITER, new NexusRatelimiterCore());
    }

}
