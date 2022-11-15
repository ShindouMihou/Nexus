package pw.mihou.nexus.configuration

import pw.mihou.nexus.configuration.modules.NexusCommonsInterceptorsConfiguration
import pw.mihou.nexus.configuration.modules.NexusExpressConfiguration
import pw.mihou.nexus.configuration.modules.NexusGlobalConfiguration
import pw.mihou.nexus.configuration.modules.NexusLoggingTemplatesConfiguration

class NexusConfiguration internal constructor() {

    @JvmField val express = NexusExpressConfiguration()
    @JvmField val global = NexusGlobalConfiguration()
    @JvmField val commonsInterceptors = NexusCommonsInterceptorsConfiguration()
    @JvmField val loggingTemplates = NexusLoggingTemplatesConfiguration()

}