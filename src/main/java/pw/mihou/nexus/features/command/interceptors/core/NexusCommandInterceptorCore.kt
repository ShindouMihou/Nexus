package pw.mihou.nexus.features.command.interceptors.core

import pw.mihou.nexus.features.command.interceptors.facades.NexusAfterware
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.core.NexusMiddlewareEventCore
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.validation.middleware.OptionValidationMiddleware

internal object NexusCommandInterceptorCore {

    private val interceptors: MutableMap<String, NexusCommandInterceptor> = mutableMapOf(
        OptionValidationMiddleware.NAME to OptionValidationMiddleware
    )

    /**
     * Adds one middleware to [Nexus].
     * @param name the name of the middleware.
     * @param middleware the middleware to add.
     */
    @JvmStatic
    fun addMiddleware(name: String, middleware: NexusMiddleware) {
        interceptors[name] = middleware
    }

    /**
     * Adds one afterware to [Nexus].
     * @param name the name of the afterware.
     * @param afterware the afterware to add.
     */
    @JvmStatic
    fun addAfterware(name: String, afterware: NexusAfterware) {
        interceptors[name] = afterware
    }

    @JvmStatic
    fun has(name: String) = interceptors.containsKey(name)

    @JvmStatic
    fun hasAfterware(name: String) = interceptors.containsKey(name) && interceptors[name] is NexusAfterware


    @JvmStatic
    fun hasMiddleware(name: String) = interceptors.containsKey(name) && interceptors[name] is NexusMiddleware

    @JvmStatic
    fun middlewares(names: List<String>): Map<String, NexusMiddleware> = names
        .map { it to interceptors[it] }
        .filter { it.second != null && it.second is NexusMiddleware }
        .associate { it.first to it.second as NexusMiddleware }

    internal fun afterwares(names: List<String>): List<NexusAfterware> = names
        .map { interceptors[it] }
        .filter { it != null && it is NexusAfterware }
        .map { it as NexusAfterware }

    @JvmStatic
    fun execute(event: NexusCommandEvent, middlewares: Map<String, NexusMiddleware>): NexusMiddlewareGateCore? {
        val gate = NexusMiddlewareGateCore()
        for ((name, middleware) in middlewares) {
            try {
                middleware.onBeforeCommand(NexusMiddlewareEventCore(event, gate))
                if (!gate.isAllowed) {
                    event.store(NexusAfterware.BLOCKING_MIDDLEWARE_KEY, name)
                    return gate
                }
            } catch (exception: Exception) {
                Nexus.logger.error("An uncaught exception was caught while trying to execute a middleware.", exception)
            }
        }
        return null
    }

    internal fun execute(event: NexusCommandEvent, afterwares: List<NexusAfterware>, dispatched: Boolean = true) {
        for (afterware in afterwares) {
            try {
                if (dispatched) {
                    afterware.onAfterCommandExecution(event)
                } else {
                    afterware.onFailedDispatch(event)
                }
            } catch (exception: Exception) {
                Nexus.logger.error("An uncaught exception was caught while trying to execute an afterware.", exception)
            }
        }
    }

}