package pw.mihou.nexus.core.managers;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.enginex.core.NexusEngineXCore;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class NexusShardManager {

    private final ConcurrentHashMap<Integer, DiscordApi> shards;
    private final Nexus nexus;

    /**
     * This creates a new Shard Manager that is then utilized by
     * {@link pw.mihou.nexus.Nexus}.
     *
     * @param shards The shards to utilize.
     */
    public NexusShardManager(Nexus nexus, DiscordApi... shards) {
        this(nexus);
        Arrays.stream(shards).forEach(this::put);
    }

    /**
     * Creates a new {@link  NexusShardManager} without any shards. This allows more
     * flexibility over how the shards are added.
     */
    public NexusShardManager(Nexus nexus) {
        this.nexus = nexus;
        this.shards = new ConcurrentHashMap<>();
    }

    /**
     * Gets the shard at the specific shard number.
     *
     * @param number The number of the shard to fetch.
     * @return The shard with the shard number specified.
     */
    @Nullable
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
     * Calculates which shard the given server belongs to, given a total number of shards.
     * <br><br>
     * This uses the formula <b>((serverId >> 22) % totalShards)</b> which is the specified formula for
     * calculating the shard of a "guild" by Discord.
     *
     * @param serverId The id of the server to calculate.
     * @param totalShards The total number of shards as per formula.
     * @return The shard which the given server should belong to, given a total number of shards.
     */
    public int shardOf(long serverId, int totalShards) {
        return (int) ((serverId >> 22) % totalShards);
    }

    /**
     * Gets the given server from any of the shards if there is any shard
     * responsible for that given server.
     *
     * @param id The id of the server to get.
     * @return The server instance, if present.
     */
    public Optional<Server> getServerBy(long id) {
        return getShardOf(id).flatMap(shard -> shard.getServerById(id));
    }

    /**
     * Adds or replaces the shard registered on the shard manager.
     * This is recommended to do during restarts of a shard.
     *
     * @param api The Discord API to store.
     */
    public void put(DiscordApi api) {
        this.shards.put(api.getCurrentShard(), api);

        ((NexusEngineXCore) nexus.getEngineX()).onShardReady(api);
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

    /**
     * Gets the current size of the shard manager.
     *
     * @return  The current size of the shard manager.
     */
    public int size() {
        return shards.size();
    }
}
