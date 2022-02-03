package pw.mihou.nexus.features.ratelimiter.core;

import pw.mihou.nexus.features.ratelimiter.facade.NexusRatelimitData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NexusRatelimitDataCore implements NexusRatelimitData {

    private final long user;
    private final ConcurrentHashMap<Long, Server> serverNodes = new ConcurrentHashMap<>();

    public NexusRatelimitDataCore(long user) {
        this.user = user;
    }

    /**
     * Collects the ID of the user who owns this data instance, otherwise known as the person who
     * is being rate-limited.
     *
     * @return The ID of the user being rate-limited.
     */
    public long getUser() {
        return this.user;
    }

    @Override
    public boolean isRatelimitedOn(long server) {
        return serverNodes.containsKey(server);
    }

    @Override
    public boolean isNotifiedOn(long server) {
        return this.serverNodes.containsKey(server) && this.serverNodes.get(server).notified.get();
    }

    @Override
    public long getRatelimitedTimestampInMillisOn(long server) {
        return this.serverNodes.containsKey(server) ? this.serverNodes.get(server).timestamp : 0L;
    }

    /**
     * Releases the user from the rate-limit on the specific server.
     *
     * @param server The ID of the server.
     */
    public void release(long server) {
        this.serverNodes.remove(server);
    }

    /**
     * Ratelimits the user for this instance on the specific server.
     *
     * @param server The ID of the server.
     */
    public void ratelimit(long server) {
        this.serverNodes.put(server, new Server());
    }

    /**
     * Switches the flip for notified user towards true which means the user has been
     * notified of their cooldown.
     *
     * @param server The ID of the server.
     */
    public void notified(long server) {
        if (!serverNodes.containsKey(server))
            return;

        serverNodes.get(server).notified.set(true);
    }

    public class Server {

        public final AtomicBoolean notified = new AtomicBoolean(false);
        public final long timestamp = System.currentTimeMillis();

    }

}
