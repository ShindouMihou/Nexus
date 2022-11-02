package pw.mihou.nexus.configuration

import pw.mihou.nexus.configuration.modules.NexusCommonsInterceptorsConfiguration
import pw.mihou.nexus.configuration.modules.NexusExpressConfiguration
import pw.mihou.nexus.configuration.modules.NexusGlobalConfiguration

class NexusConfiguration internal constructor() {

    val express = NexusExpressConfiguration()
    val global = NexusGlobalConfiguration()
    val commonsInterceptors = NexusCommonsInterceptorsConfiguration()

}