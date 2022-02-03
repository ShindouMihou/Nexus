package pw.mihou.nexus.core.managers;

import org.javacord.api.DiscordApi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class NexusShardManager {

    private final ConcurrentHashMap<Integer, DiscordApi> shards = new ConcurrentHashMap<>();

    /**
     * This creates a new Shard Manager that is then utilized by
     * {@link pw.mihou.nexus.Nexus}.
     *
     * @param shards The shards to utilize.
     */
    public NexusShardManager(DiscordApi... shards) {
        Arrays.stream(shards)
                .forEach(discordApi -> this.shards.put(discordApi.getCurrentShard(), discordApi));
    }

    /**
     * Gets the shard at the specific shard number.
     *
     * @param number The number of the shard to fetch.
     * @return The shard with the shard number specified.
     */
    public DiscordApi getShard(int number) {
        return shards.get(number);
    }

    /**
     * Gets the shard that is responsible for the specific server.
     *
     * @param server The ID of the server to lookup.
     * @return The shard responsible for the server, if present.
     */
    public Optional<DiscordApi> getShardOf(long server) {
        return asStream()
                .filter(discordApi -> discordApi.getServerById(server).isPresent())
                .findFirst();
    }

    /**
     * Adds or replaces the shard registered on the shard manager.
     * This is recommended to do during restarts of a shard.
     *
     * @param api The Discord API to store.
     */
    public void put(DiscordApi api) {
        this.shards.put(api.getCurrentShard(), api);
    }

    /**
     * Removes the shard with the specific shard key.
     *
     * @param shard The number of the shard to remove.
     */
    public void remove(int shard) {
        this.shards.remove(shard);
    }

    /**
     * Retrieves all the {@link DiscordApi} shards and transforms
     * them into an Array Stream.
     *
     * @return A stream of all the shards registered in the shard manager.
     */
    public Stream<DiscordApi> asStream() {
        return shards.values().stream();
    }

    /**
     * Retrieves all the {@link DiscordApi} shards and transforms them
     * into a {@link Collection}.
     *
     * @return A {@link Collection} of all the shards registered in the shard manager.
     */
    public Collection<DiscordApi> asCollection() {
        return shards.values();
    }
}
