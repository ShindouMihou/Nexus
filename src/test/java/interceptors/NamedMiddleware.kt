package interceptors

import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.annotations.Name
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware

@Name("pw.mihou.middleware")
object NamedMiddleware:  NexusMiddleware {
    override fun onBeforeCommand(event: NexusMiddlewareEvent) {

    }
}