package pw.mihou.nexus.features.command.interceptors

import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.core.reflective.NexusReflection
import pw.mihou.nexus.features.command.interceptors.annotations.Name
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore
import pw.mihou.nexus.features.command.interceptors.facades.NexusAfterware
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware

object NexusCommandInterceptors {
    private val interceptors = NexusCommandInterceptorCore

    /**
     * Tries to get the declared name through the annotation of the class.
     * @param clazz the class to analyze.
     * @return the name from the class or null.
     */
    private fun tryGetNameFrom(clazz: Class<*>): String? {
        if (clazz.isAnnotationPresent(Name::class.java)) {
            return clazz.getAnnotation(Name::class.java).value
        }
        return null
    }

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
        @Suppress("NAME_SHADOWING") var name = name
        if (name == null) {
            name = tryGetNameFrom(middleware::class.java)
        }
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
        @Suppress("NAME_SHADOWING") var name = name
        if (name == null) {
            name = tryGetNameFrom(afterware::class.java)
        }
        val uuid = name ?: NexusUuidAssigner.request()
        if (name == null && NexusCommandInterceptorCore.has(uuid))  {
            return afterware(null, afterware)
        }
        interceptors.addAfterware(uuid, afterware)
        return uuid
    }

    /**
     * Adds the provided repository to the registry. In this new mechanism, we load all the [NexusAfterware] and
     * [NexusMiddleware] variables in the class into the registry with a name based on their field name or the name
     * provided using [Name] annotation.
     *
     * @param repository the repository to add.
     */
    fun add(repository: Any) {
        NexusReflection.accumulate(repository) { field ->
            if (field.type != NexusMiddleware::class.java && field.type != NexusAfterware::class.java) return@accumulate
            val name =
                if (field.isAnnotationPresent(Name::class.java)) field.getAnnotation(Name::class.java).value
                else field.name

            if (field.type == NexusMiddleware::class.java || field.type.isAssignableFrom(NexusMiddleware::class.java)) {
                interceptors.addMiddleware(name, field.get(repository) as NexusMiddleware)
                return@accumulate
            }

            if (field.type == NexusAfterware::class.java || field.type.isAssignableFrom(NexusAfterware::class.java)) {
                interceptors.addAfterware(name, field.get(repository) as NexusAfterware)
                return@accumulate
            }
        }
    }
}