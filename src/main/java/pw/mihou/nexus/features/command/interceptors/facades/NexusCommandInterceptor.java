package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.assignment.NexusUuidAssigner;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;

public interface NexusCommandInterceptor {

    /**
     * Creates an anonymous middleware where the name of the middleware is generated randomly
     * by the framework at runtime.
     *
     * @param middleware The middleware functionality.
     * @return The name of the middleware to generated.
     * @see Nexus#getInterceptors
     */
    @Deprecated(forRemoval = true)
    static String middleware(NexusMiddleware middleware) {
        return Nexus.getInterceptors().middleware(middleware);
    }

    /**
     * Creates an anonymous afterware where the name of the middleware is generated randomly
     * by the framework at runtime.
     *
     * @param afterware The afterware functionality.
     * @return The name of the afterware to generated.
     * @see Nexus#getInterceptors
     */
    @Deprecated(forRemoval = true)
    static String afterware(NexusAfterware afterware) {
        return Nexus.getInterceptors().afterware(afterware);

    }

    /**
     * Adds the middleware into the command interceptor storage.
     *
     * @param name The name of the middleware to add.
     * @param middleware The middleware functionality.
     * @return The name of the middleware for quick-use.
     * @see Nexus#getInterceptors
     */
    @Deprecated(forRemoval = true)
    static String addMiddleware(String name, NexusMiddleware middleware) {
        return Nexus.getInterceptors().middleware(name, middleware);
    }

    /**
     * Adds the afterware into the command interceptor storage.
     *
     * @param name The name of the afterware to add.
     * @param afterware The afterware functionality.
     * @return The name of the afterware for quick-use.
     * @see Nexus#getInterceptors
     */
    @Deprecated(forRemoval = true)
    static String addAfterware(String name, NexusAfterware afterware) {
        return Nexus.getInterceptors().afterware(name, afterware);

    }

    /**
     * Adds a repository of command interceptors to the command interceptor
     * storage.
     *
     * @param repository    The repository to add.
     * @see Nexus#getInterceptors
     */
    @Deprecated(forRemoval = true)
    static void addRepository(NexusInterceptorRepository repository) {
        Nexus.getInterceptors().add(repository);
    }

}
