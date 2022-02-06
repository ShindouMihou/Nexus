package pw.mihou.nexus.core.assignment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NexusUuidAssigner {

    /**
     * These are the UUIDs that have reached a handshake with the
     * assigner which means these are allocated to a certain object.
     *
     * A UUID is universally unique by session and this ensures that
     * the Nexus instance does not encounter a UUID collision that could
     * lead fatal.
     *
     * Once a UUID is assigned, it will never be removed unless you somehow have
     * reached the limit of the UUID uniqueness in which by then, might as well restart
     * your JVM.
     */
    private static final Set<String> acceptedUUIDs = ConcurrentHashMap.newKeySet();

    /**
     * Attempts to perform a handshake over a single universally unique
     * identifier for this session. This returns true if the handshake was accepted
     * and false if the UUID is rejected because it exists.
     *
     * @param uuid The UUID to attempt.
     * @return Was the UUID accepted?
     */
    private static boolean handshake(String uuid) {
        return acceptedUUIDs.add(uuid);
    }

    /**
     * Requests for a universally unique identifier for this JVM session that a
     * Nexus object can use without worrying about UUID conflicts with another Nexus
     * object.
     *
     * @return A universally, unique identifier for this JVM.
     */
    public static String request() {
        String uuid = UUID.randomUUID().toString();

        return handshake(uuid) ? uuid : request();
    }
}
