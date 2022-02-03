package pw.mihou.nexus.features.ratelimiter.facade;

public interface NexusRatelimitData {

    /**
     * Collects the ID of the user who owns this data instance, otherwise known as the person who
     * is being rate-limited.
     *
     * @return The ID of the user being rate-limited.
     */
    long getUser();

    /**
     * Is this user rate-limited on the specified server?
     *
     * @param server The ID of the server.
     * @return Is this user rate-limited on the specified server?
     */
    boolean isRatelimitedOn(long server);

    /**
     * Is this user already notified about the rate-limit on the specified server?
     *
     * @param server The ID of the server.
     * @return Is this user notified about the rate-limit on the specified server?
     */
    boolean isNotifiedOn(long server);

    /**
     * Retrieves the timestamp when the user was rate-limited on the specified server.
     *
     * @param server The ID of the server.
     * @return The timestamp when the user was rate-limited on the specific server in milliseconds.
     */
    long getRatelimitedTimestampInMillisOn(long server);

}
