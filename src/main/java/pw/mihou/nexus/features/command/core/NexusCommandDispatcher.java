package pw.mihou.nexus.features.command.core;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore;
import pw.mihou.nexus.features.messages.core.NexusMessageCore;

import java.util.ArrayList;
import java.util.List;

public class NexusCommandDispatcher {

    /**
     * Dispatches one slash command create event of a command onto the given {@link NexusCommandCore}.
     * This performs the necessary middleware handling, dispatching to the listener and afterware handling.
     * <br>
     * This is synchronous by nature except when the event is dispatched to its respective listener and also
     * when the afterwares are executed.
     *
     * @param instance  The {@link NexusCommandCore} instance to dispatch the event towards.
     * @param event     The {@link SlashCommandCreateEvent} event to dispatch.
     */
    public static void dispatch(NexusCommandCore instance, SlashCommandCreateEvent event) {
        NexusCommandEvent nexusEvent = new NexusCommandEventCore(event, instance);
        List<String> middlewares = new ArrayList<>();
        middlewares.addAll(instance.core.getGlobalMiddlewares());
        middlewares.addAll(instance.middlewares);

        List<String> afterwares = new ArrayList<>();
        afterwares.addAll(instance.core.getGlobalAfterwares());
        afterwares.addAll(instance.afterwares);

        NexusMiddlewareGateCore middlewareGate = (NexusMiddlewareGateCore) NexusCommandInterceptorCore.interceptWithMany(middlewares, nexusEvent);

        if (middlewareGate != null) {
            NexusMessageCore middlewareResponse = ((NexusMessageCore) middlewareGate.response());
            if (middlewareResponse != null) {
                middlewareResponse
                        .convertTo(nexusEvent.respondNow())
                        .respond()
                        .exceptionally(ExceptionLogger.get());
            }
            return;
        }

        if (event.getSlashCommandInteraction().getChannel().isEmpty()) {
            NexusCore.logger.error(
                    "The channel of a slash command event is somehow not present; this is possibly a change in Discord's side " +
                    "and may need to be addressed, please send an issue @ https://github.com/ShindouMihou/Nexus"
            );
        }

        NexusThreadPool.executorService.submit(() -> {
            try {
                instance.handler.onEvent(nexusEvent);
            } catch (Throwable throwable) {
                NexusCore.logger.error("An uncaught exception was received by Nexus Command Dispatcher for the " +
                        "command " + instance.name + " with the following stacktrace.");
                throwable.printStackTrace();
            }
        });

        NexusThreadPool.executorService.submit(() -> NexusCommandInterceptorCore.interceptWithMany(afterwares, nexusEvent));
    }

}
