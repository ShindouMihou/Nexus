package pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core;

import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent;
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.facade.NexusRatelimitData;
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.facade.NexusRatelimiter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NexusRatelimiterCore implements NexusRatelimiter {

    private final Map<Entity, NexusRatelimitData> ratelimits = new ConcurrentHashMap<>();

    /**
     * Ratelimits a user on a specific server, or on their private channel for a
     * specific command.
     *
     * @param user      The user to rate-limit.
     * @param server    The server or private channel to rate-limit on.
     * @param command   The command to rate-limit on.
     * @return          The results from the rate-limit attempt.
     */
    private AccessorRatelimitData ratelimit(long user, long server, NexusCommandCore command) {
        Entity key = new Entity(command.uuid, user);

        if (!ratelimits.containsKey(key)) {
            ratelimits.put(key, new NexusRatelimitDataCore(user));
        }

        NexusRatelimitDataCore entity = (NexusRatelimitDataCore) ratelimits.get(key);
        if (entity.isRatelimitedOn(server)) {
            // This if-statement defines the section where the user
            // is rate-limited.
            if (getRemainingSecondsFrom(server, entity, command) > 0) {
                if (!entity.isNotifiedOn(server)) {
                    entity.notified(server);
                    return new AccessorRatelimitData(false, true, getRemainingSecondsFrom(server, entity, command));
                }

                return new AccessorRatelimitData(true, true, getRemainingSecondsFrom(server, entity, command));
            }

            // This statement means that the user is still regarded as rate-limited by the
            // rate-limiter despite the cooldown expiring.
            entity.release(server);
        }

        entity.ratelimit(server);
        return new AccessorRatelimitData(false, false, -1);
    }

    /**
     * Gets the remaining seconds from the data provided.
     *
     * @param server The ID of the server.
     * @param dataCore The rate-limit core data.
     * @param commandCore The Nexus Command Core data.
     * @return The remaining seconds before the rate-limit should be released.
     */
    private long getRemainingSecondsFrom(long server, NexusRatelimitDataCore dataCore, NexusCommandCore commandCore) {
        return (TimeUnit.MILLISECONDS.toSeconds(
                dataCore.getRatelimitedTimestampInMillisOn(server)
                        +
                        commandCore.cooldown.toMillis()
        )) - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    @Override
    public Optional<NexusRatelimitData> get(NexusCommand command, long user) {
        return Optional.ofNullable(ratelimits.getOrDefault(new Entity(((NexusCommandCore) command).uuid, user), null));
    }

    @Override
    public void onBeforeCommand(NexusMiddlewareEvent event) {
        AccessorRatelimitData ratelimitData = ratelimit(event.getUserId(), event.getServerId().orElse(event.getUserId()),
                (NexusCommandCore) event.getCommand());

        if (ratelimitData.ratelimited()) {
            if (!ratelimitData.notified()) {
                event.stop
                        (((NexusCore) event.getNexus())
                                .getMessageConfiguration().onRatelimited(event, ratelimitData.remaining())
                );
                return;
            }

            event.stop();
        }
    }

    private record AccessorRatelimitData(boolean notified, boolean ratelimited, long remaining) {
    }

    private record Entity(String commandUUID, long user) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entity entity = (Entity) o;
            return user == entity.user && Objects.equals(commandUUID, entity.commandUUID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(commandUUID, user);
        }

    }

}
