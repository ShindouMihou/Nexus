package pw.mihou.nexus.features.command.responders;

import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NexusResponderRepository {

    private final Map<Long, InteractionOriginalResponseUpdater> responders = new ConcurrentHashMap<>();

    /**
     * Gets the current {@link InteractionOriginalResponseUpdater} for the specific interaction
     * if available from another middleware otherwise requests for a new {@link InteractionOriginalResponseUpdater}
     * that can be used instead.
     * <br><br>
     * Not to be confused with {@link NexusResponderRepository#get(Interaction)} which deliberately
     * destroys the interaction that is being stored after being requested. This is intended for middlewares to
     * prevent Discord's Interaction has failed while processing a heavy task.
     *
     * @param interaction   The interaction to reference.
     * @return              The {@link InteractionOriginalResponseUpdater} if present otherwise requests for one.
     */
    public CompletableFuture<InteractionOriginalResponseUpdater> peek(Interaction interaction) {
        if (responders.containsKey(interaction.getId())) {
            return CompletableFuture.completedFuture(responders.get(interaction.getId()));
        }

        return interaction.respondLater()
                .thenApply(responseUpdater -> {
                    responders.put(interaction.getId(), responseUpdater);
                    return responseUpdater;
                });
    }

    /**
     * Gets the current {@link InteractionOriginalResponseUpdater} for the specific interaction if
     * available from another middleware otherwise requests for a new {@link InteractionOriginalResponseUpdater}
     * that can be used instead.
     *
     * @param interaction   The interaction to reference for.
     * @return              The {@link InteractionOriginalResponseUpdater} if present otherwise requests for one.
     */
    public CompletableFuture<InteractionOriginalResponseUpdater> get(Interaction interaction) {
        if (responders.containsKey(interaction.getId())) {
            return CompletableFuture.completedFuture(responders.remove(interaction.getId()));
        }

        return interaction.respondLater()
                .thenApply(responseUpdater -> {
                    responders.put(interaction.getId(), responseUpdater);
                    return responseUpdater;
                });
    }

    /**
     * Gets the current {@link InteractionOriginalResponseUpdater} for the specific interaction if
     * available from another middleware otherwise requests for a new {@link InteractionOriginalResponseUpdater}
     * that can be used instead.
     *
     * @param interaction   The interaction to reference for.
     * @return              The {@link InteractionOriginalResponseUpdater} if present otherwise requests for one.
     */
    public CompletableFuture<InteractionOriginalResponseUpdater> getEphemereal(Interaction interaction) {
        if (responders.containsKey(interaction.getId())) {
            return CompletableFuture.completedFuture(responders.remove(interaction.getId()));
        }

        return interaction.respondLater(true)
                .thenApply(responseUpdater -> {
                    responders.put(interaction.getId(), responseUpdater);
                    return responseUpdater;
                });
    }

}
