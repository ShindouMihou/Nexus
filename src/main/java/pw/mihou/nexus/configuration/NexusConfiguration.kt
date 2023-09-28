package pw.mihou.nexus.configuration

import pw.mihou.nexus.configuration.modules.*

class NexusConfiguration internal constructor() {

    @JvmField val express = NexusExpressConfiguration()
    @JvmField val global = NexusGlobalConfiguration()
    @JvmField val commonsInterceptors = NexusCommonsInterceptorsConfiguration()
    @JvmField val loggingTemplates = NexusLoggingTemplatesConfiguration()
    @JvmField val launch = NexusLaunchConfiguration()
    @JvmField val interceptors = NexusInterceptorsConfiguration()

}