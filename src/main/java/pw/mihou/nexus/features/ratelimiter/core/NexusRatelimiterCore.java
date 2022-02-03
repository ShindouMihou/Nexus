package pw.mihou.nexus.features.ratelimiter.core;

import pw.mihou.nexus.commons.Pair;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.ratelimiter.facade.NexusRatelimitData;
import pw.mihou.nexus.features.ratelimiter.facade.NexusRatelimiter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NexusRatelimiterCore implements NexusRatelimiter {

    private final Map<Entity, NexusRatelimitData> ratelimits = new ConcurrentHashMap<>();

    public void ratelimit(long user, long server, NexusCommandCore command, Consumer<Long> onRatelimited, Consumer<NexusRatelimitData> onSuccess) {
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
                    onRatelimited.accept(getRemainingSecondsFrom(server, entity, command));
                    entity.notified(server);
                }

                return;
            }

            // This statement means that the user is still regarded as rate-limited by the
            // rate-limiter despite the cooldown expiring.
            entity.release(server);
        }

        entity.ratelimit(server);
        onSuccess.accept(entity);
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

    public static class Entity {
        private final String commandUUID;
        private final long user;

        /**
         * Creates an {@link Entity} which is used internally as a user-command
         * mapping key.
         *
         * @param commandUUID The UUID of the command, pre-generated.
         * @param user The ID of the user.
         */
        public Entity(String commandUUID, long user) {
            this.commandUUID = commandUUID;
            this.user = user;
        }

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
