package pw.mihou.nexus.features.command.interceptors

import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore
import pw.mihou.nexus.features.command.interceptors.facades.NexusAfterware
import pw.mihou.nexus.features.command.interceptors.facades.NexusInterceptorRepository
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware

object NexusCommandInterceptors {
    private val interceptors = NexusCommandInterceptorCore


    /**
     * Adds the provided middleware into the registry, when there is no name provided, the framework
     * will generate one on your behalf using [NexusUuidAssigner].
     *
     * @param name the name of the middleware, null to auto-generate a random one.
     * @param middleware the middleware to add.
     * @return the name of the middleware.
     */
    @JvmOverloads
    fun middleware(name: String? = null, middleware: NexusMiddleware): String {
        val uuid = name ?: NexusUuidAssigner.request()
        if (name == null && NexusCommandInterceptorCore.has(uuid))  {
            return middleware(null, middleware)
        }
        interceptors.addMiddleware(uuid, middleware)
        return uuid
    }

    /**
     * Adds the provided afterware into the registry, when there is no name provided, the framework
     * will generate one on your behalf using [NexusUuidAssigner].
     *
     * @param name the name of the afterware, null to auto-generate a random one.
     * @param afterware the afterware to add.
     * @return the name of the afterware.
     */
    @JvmOverloads
    fun afterware(name: String? = null, afterware: NexusAfterware): String {
        val uuid = name ?: NexusUuidAssigner.request()
        if (name == null && NexusCommandInterceptorCore.has(uuid))  {
            return afterware(null, afterware)
        }
        interceptors.addAfterware(uuid, afterware)
        return uuid
    }

    /**
     * Adds the provided repository to the registry. This calls the [NexusInterceptorRepository.define] method,
     * adding all the middlewares and afterwares inside.
     * @param repository the repository to add.
     */
    fun add(repository: NexusInterceptorRepository) {
        interceptors.addRepository(repository)
    }
}