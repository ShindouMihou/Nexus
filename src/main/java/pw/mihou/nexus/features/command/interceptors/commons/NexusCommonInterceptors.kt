package pw.mihou.nexus.features.command.interceptors.commons

import pw.mihou.nexus.features.command.interceptors.annotations.Name
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core.NexusRatelimiterCore
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware

object NexusCommonInterceptors {
    @Name("nexus.gate.server")
    val NEXUS_GATE_SERVER = NexusMiddleware{ event -> event.stopIf(event.server.isEmpty) }

    @Name("nexus.gate.dms")
    val NEXUS_GATE_DMS = NexusMiddleware{ event -> event.stopIf(event.server.isPresent) }

    @Name("nexus.ratelimiter")
    val NEXUS_RATELIMITER = NexusRatelimiterCore()
}