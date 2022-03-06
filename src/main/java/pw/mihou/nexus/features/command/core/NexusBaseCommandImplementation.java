package pw.mihou.nexus.features.command.core;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore;
import pw.mihou.nexus.features.messages.core.NexusMessageCore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record NexusBaseCommandImplementation(NexusCommandCore instance) {

    /**
     * Applies general restraints for slash commands.
     *
     * @param event The event instance to use.
     * @return Can the user use this command?
     */
    public boolean applyRestraints(SlashCommandCreateEvent event) {
        // We need this condition in case Discord decides to go through with their
        // optional channel thing which sounds odd.
        if (event.getSlashCommandInteraction().getChannel().isEmpty())
            throw new IllegalStateException("The channel is somehow not present; this is possibly a change in Discord's side " +
                    "and may need to be addressed, please send an issue @ https://github.com/ShindouMihou/Nexus");

        return true;
    }

    public void dispatch(SlashCommandCreateEvent event) {
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

        if (!applyRestraints(event)) {
            return;
        }

        CompletableFuture.runAsync(() -> instance.handler.onEvent(nexusEvent), NexusThreadPool.executorService)
                .thenAcceptAsync(unused -> NexusCommandInterceptorCore.interceptWithMany(afterwares, nexusEvent));
    }

}
