package pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.facade;

import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware;

import java.util.Optional;

public interface NexusRatelimiter extends NexusMiddleware {

    /**
     * Retrieves the rate-limit data for the specific user, if present.
     *
     * @param command The command to fetch.
     * @param user The ID of the user.
     * @return The rate-limit data of the user, if present.
     */
    Optional<NexusRatelimitData> get(NexusCommand command, long user);

}
