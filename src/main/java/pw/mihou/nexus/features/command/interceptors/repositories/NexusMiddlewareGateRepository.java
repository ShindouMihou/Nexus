package pw.mihou.nexus.features.command.interceptors.repositories;

import org.javacord.api.interaction.Interaction;
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NexusMiddlewareGateRepository {

    private static final Map<Long, NexusMiddlewareGateCore> gates = new ConcurrentHashMap<>();

    /**
     * Releases the {@link NexusMiddlewareGateCore} associated with the
     * interaction.
     *
     * @param interaction   The interaction whose {@link NexusMiddlewareGateCore} should
     *                      be released.
     */
    public static NexusMiddlewareGateCore release(Interaction interaction) {
        return gates.remove(interaction.getId());
    }

    /**
     * Gets the {@link NexusMiddlewareGateCore} associated with the interaction.
     *
     * @param interaction   The interaction to reference from.
     * @return              The {@link NexusMiddlewareGateCore} associated with this
     * specific interaction.
     */
    public static NexusMiddlewareGateCore get(Interaction interaction) {
        if (!gates.containsKey(interaction.getId())) {
            gates.put(interaction.getId(), new NexusMiddlewareGateCore());
        }

        return gates.get(interaction.getId());
    }

}
