package pw.mihou.nexus.features.command.interceptors.core

import pw.mihou.nexus.features.command.interceptors.facades.NexusAfterware
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor
import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.core.NexusMiddlewareEventCore
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddlewareGate
import java.util.stream.Stream

internal object NexusCommandInterceptorCore {

    private val interceptors: MutableMap<String, NexusCommandInterceptor> = mutableMapOf()

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

    /**
     * Adds one repository of command interceptors to [Nexus].
     * @param repository the repository to add.
     */
    @JvmStatic
    fun addRepository(repository: NexusInterceptorRepository) {
        repository.define()
    }

    @JvmStatic
    fun has(name: String) = interceptors.containsKey(name)

    @JvmStatic
    fun hasAfterware(name: String) = interceptors.containsKey(name) && interceptors[name] is NexusAfterware


    @JvmStatic
    fun hasMiddleware(name: String) = interceptors.containsKey(name) && interceptors[name] is NexusMiddleware

    @JvmStatic
    fun middlewares(names: List<String>): List<NexusMiddleware> = names
        .map { interceptors[it] }
        .filter { it != null && it is NexusMiddleware }
        .map { it as NexusMiddleware }

    @JvmStatic
    fun afterwares(names: List<String>): List<NexusAfterware> = names
        .map { interceptors[it] }
        .filter { it != null && it is NexusAfterware }
        .map { it as NexusAfterware }

    @JvmStatic
    fun execute(event: NexusCommandEvent, middlewares: List<NexusMiddleware>): NexusMiddlewareGateCore? {
        val gate = NexusMiddlewareGateCore()
        for (middleware in middlewares) {
            try {
                middleware.onBeforeCommand(NexusMiddlewareEventCore(event, gate))
                if (!gate.isAllowed) {
                    return gate
                }
            } catch (exception: Exception) {
                Nexus.logger.error("An uncaught exception was caught while trying to execute a middleware.")
                exception.printStackTrace()
            }
        }
        return null
    }

    fun execute(event: NexusCommandEvent, afterwares: List<NexusAfterware>) {
        for (afterware in afterwares) {
            try {
                afterware.onAfterCommandExecution(event)
            } catch (exception: Exception) {
                Nexus.logger.error("An uncaught exception was caught while trying to execute a afterware.")
                exception.printStackTrace()
            }
        }
    }

}