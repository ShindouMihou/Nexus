package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;

public interface NexusCommandInterceptor {

    /**
     * Adds the middleware into the command interceptor storage.
     *
     * @param name The name of the middleware to add.
     * @param middleware The middleware functionality.
     */
    static void addMiddleware(String name, NexusMiddleware middleware) {
        NexusCommandInterceptorCore.addMiddleware(name, middleware);
    }

    /**
     * Adds the afterware into the command interceptor storage.
     *
     * @param name The name of the afterware to add.
     * @param afterware The afterware functionality.
     */
     static void addAfterware(String name, NexusAfterware afterware) {
        NexusCommandInterceptorCore.addAfterware(name, afterware);
    }

}
