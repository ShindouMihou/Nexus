package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.core.assignment.NexusUuidAssigner;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;

public interface NexusCommandInterceptor {

    /**
     * Creates an anonymous middleware where the name of the middleware is generated randomly
     * by the framework at runtime.
     *
     * @param middleware The middleware functionality.
     * @return The name of the middleware to generated.
     */
    static String middleware(NexusMiddleware middleware) {
        String uuid = NexusUuidAssigner.request();

        if (NexusCommandInterceptorCore.has(uuid)) {
            NexusUuidAssigner.deny(uuid);
            return middleware(middleware);
        }

        NexusCommandInterceptorCore.addMiddleware(uuid, middleware);

        return uuid;
    }

    /**
     * Creates an anonymous afterware where the name of the middleware is generated randomly
     * by the framework at runtime.
     *
     * @param afterware The afterware functionality.
     * @return The name of the afterware to generated.
     */
    static String afterware(NexusAfterware afterware) {
        String uuid = NexusUuidAssigner.request();

        if (NexusCommandInterceptorCore.has(uuid)) {
            NexusUuidAssigner.deny(uuid);
            return afterware(afterware);
        }

        NexusCommandInterceptorCore.addAfterware(uuid, afterware);

        return uuid;
    }

    /**
     * Adds the middleware into the command interceptor storage.
     *
     * @param name The name of the middleware to add.
     * @param middleware The middleware functionality.
     * @return The name of the middleware for quick-use.
     */
    static String addMiddleware(String name, NexusMiddleware middleware) {
        NexusCommandInterceptorCore.addMiddleware(name, middleware);

        return name;
    }

    /**
     * Adds the afterware into the command interceptor storage.
     *
     * @param name The name of the afterware to add.
     * @param afterware The afterware functionality.
     * @return The name of the afterware for quick-use.
     */
    static String addAfterware(String name, NexusAfterware afterware) {
        NexusCommandInterceptorCore.addAfterware(name, afterware);

        return name;
    }

    /**
     * Adds a repository of command interceptors to the command interceptor
     * storage.
     *
     * @param repository    The repository to add.
     */
    static void addRepository(NexusInterceptorRepository repository) {
        NexusCommandInterceptorCore.addRepository(repository);
    }

}
