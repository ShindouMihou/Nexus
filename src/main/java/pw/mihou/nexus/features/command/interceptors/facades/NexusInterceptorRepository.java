package pw.mihou.nexus.features.command.interceptors.facades;

import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;

/**
 * {@link NexusInterceptorRepository} is an extendable interface that can be used
 * to define one or more interceptors which calling the register function.
 */
public abstract class NexusInterceptorRepository {

    /**
     * Defines one or more interceptors. This method will be called
     * upon startup.
     */
    public abstract void define();

    /**
     * Adds the middleware into the command interceptor storage.
     *
     * @param name The name of the middleware to add.
     * @param middleware The middleware functionality.
     */
     public void middleware(String name, NexusMiddleware middleware) {
        NexusCommandInterceptorCore.addMiddleware(name, middleware);
    }

    /**
     * Adds the afterware into the command interceptor storage.
     *
     * @param name The name of the afterware to add.
     * @param afterware The afterware functionality.
     */
    public void afterware(String name, NexusAfterware afterware) {
        NexusCommandInterceptorCore.addAfterware(name, afterware);
    }

}
