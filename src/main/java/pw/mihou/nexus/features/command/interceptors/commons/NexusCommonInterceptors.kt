package pw.mihou.nexus.features.command.interceptors.commons

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.interceptors.commons.modules.NexusLogAfterware
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core.NexusRatelimiterCore

object NexusCommonInterceptors {
    val NEXUS_GATE_SERVER = Nexus.interceptors.middleware("nexus.gate.server") {
        event -> event.stopIf(event.server.isEmpty)
    }
    val NEXUS_GATE_DMS = Nexus.interceptors.middleware("nexus.gate.dms") { event ->
        event.stopIf(event.server.isPresent)
    }
    val NEXUS_RATELIMITER = Nexus.interceptors.middleware("nexus.ratelimiter", NexusRatelimiterCore())
    val NEXUS_LOG = Nexus.interceptors.afterware("nexus.log", NexusLogAfterware)
}