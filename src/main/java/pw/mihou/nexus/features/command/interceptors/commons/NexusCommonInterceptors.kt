package pw.mihou.nexus.features.command.interceptors.commons

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.interceptors.commons.modules.NexusIgnoreExpiredEventsMiddleware
import pw.mihou.nexus.features.command.interceptors.commons.modules.NexusLogAfterware
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core.NexusRatelimiterCore
import java.time.Instant

object NexusCommonInterceptors {
    @JvmField val NEXUS_GATE_SERVER = Nexus.interceptors.middleware("nexus.gate.server") {
        event -> event.stopIf(event.server.isEmpty)
    }
    @JvmField val NEXUS_GATE_DMS = Nexus.interceptors.middleware("nexus.gate.dms") { event ->
        event.stopIf(event.server.isPresent)
    }
    @JvmField val NEXUS_RATELIMITER = Nexus.interceptors.middleware(null, NexusRatelimiterCore())
    @JvmField val NEXUS_LOG = Nexus.interceptors.afterware(null, NexusLogAfterware)
    @JvmField val NEXUS_IGNORE_EXPIRED_EVENTS = Nexus.interceptors.middleware(null, NexusIgnoreExpiredEventsMiddleware)
}