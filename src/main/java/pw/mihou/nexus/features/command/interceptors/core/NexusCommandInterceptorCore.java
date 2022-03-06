package pw.mihou.nexus.features.command.interceptors.core;

import pw.mihou.nexus.features.command.core.NexusMiddlewareEventCore;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.interceptors.facades.*;
import pw.mihou.nexus.features.command.interceptors.repositories.NexusMiddlewareGateRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NexusCommandInterceptorCore {

    private static final Map<String, NexusCommandInterceptor> interceptors = new HashMap<>();

    /**
     * Adds the middleware into the command interceptor storage.
     *
     * @param name The name of the middleware to add.
     * @param middleware The middleware functionality.
     */
    public static void addMiddleware(String name, NexusMiddleware middleware) {
        interceptors.put(name, middleware);
    }

    /**
     * Adds a repository of command interceptors to the command interceptor
     * storage.
     *
     * @param repository    The repository to add.
     */
    public static void addRepository(NexusInterceptorRepository repository) {
        repository.define();
    }

    /**
     * Adds the afterware into the command interceptor storage.
     *
     * @param name The name of the afterware to add.
     * @param afterware The afterware functionality.
     */
    public static void addAfterware(String name, NexusAfterware afterware) {
        interceptors.put(name, afterware);
    }

    /**
     * An internal method that is used to execute a command interceptor based on its
     * type.
     *
     * @param name The name of the command interceptor.
     * @param event The event being intercepted.
     */
    private static void interceptWith(String name, NexusCommandEvent event) {
        try {
            NexusCommandInterceptor interceptor = interceptors.getOrDefault(name, null);

            if (interceptor == null) {
                return;
            }

            if (interceptor instanceof NexusMiddleware) {
                ((NexusMiddleware) interceptor).onBeforeCommand(new NexusMiddlewareEventCore(event));
            } else if (interceptor instanceof NexusAfterware){
                ((NexusAfterware) interceptor).onAfterCommandExecution(event);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * An internal method that assesses multiple command interceptors and expects a
     * proper boolean response.
     *
     * @param names The names of the command interceptors to execute.
     * @param event The event to execute.
     * @return Are all interceptors agreeing with the command execution?
     */
    public static NexusMiddlewareGate interceptWithMany(List<String> names, NexusCommandEvent event) {
        // This is intentionally a for-loop since we want to stop at a specific point.
        NexusMiddlewareGateCore gate = NexusMiddlewareGateRepository.get(event.getBaseEvent().getInteraction());
        for (String name : names) {
            interceptWith(name, event);
            if (!gate.allowed()) {
                return NexusMiddlewareGateRepository.release(event.getBaseEvent().getInteraction());
            }
        }

        NexusMiddlewareGateRepository.release(event.getBaseEvent().getInteraction());
        return null;
    }

}
