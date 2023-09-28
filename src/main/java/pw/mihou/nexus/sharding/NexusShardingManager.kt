package pw.mihou.nexus.sharding

import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.express.core.NexusExpressCore
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

class NexusShardingManager internal constructor() {

    private val shards = ConcurrentHashMap<Int, DiscordApi>()

    /**
     * Gets the current size of the sharding manager.
     * @return the current size of the sharding manager (shows how many shards are registered in the manager).
     */
    val size: Int get() = shards.size

    /**
     * Gets the shard with the given number.
     *
     * @param number the number of the shard.
     * @return the shard associated with the number.
     */
    operator fun get(number: Int): DiscordApi? = shards[number]

    /**
     * Associates a shard based on the number provided, this overrides if the shard already exists.
     * @param shard the [DiscordApi] to add to the shard manager.
     */
     fun set(shard: DiscordApi) {
        shards[shard.currentShard] = shard
        (Nexus.express as NexusExpressCore).ready(shard)
    }

    /**
     * Gets all the shards in a collection.
     * @return all the [DiscordApi] in a collection.
     */
    fun collection(): Collection<DiscordApi> = shards.values

    /**
     * Finds a shard that matches the predicate provided.
     *
     * It is recommended to use [pw.mihou.nexus.express.NexusExpress.queue] instead to wait for the shard since this method
     * will scan the current known shards to see if any matches the predicate.
     *
     * @param predicate the predicate that the shard must match.
     * @return the shard that matches the predicate if any.
     */
    fun find(predicate: Predicate<DiscordApi>) = shards.values.firstOrNull { shard ->
        predicate.test(shard)
    }

    /**
     * Finds the shard that the server belongs to.
     *
     * It is recommended to use [pw.mihou.nexus.express.NexusExpress.await] instead to wait for the shard since this method will
     * scan the current known shards to see if any of the shards have the server.
     *
     * @param server the server to find.
     * @return the shard that is responsible for the server.
     */
    fun shard(server: Long): DiscordApi? = find { shard -> shard.getServerById(server).isPresent }

    /**
     * Gets the [Server] instance by searching all currently known shards to see if any of them are responsible for the server.
     *
     * It is recommended to use [pw.mihou.nexus.express.NexusExpress.await] instead to wait  for the server since this method will scan
     * the current known shards to see if any of the shards have the server.
     *
     * @param id the id of the server.
     * @return the [Server] instance that was found if any.
     */
    fun server(id: Long): Server? = shard(server = id)?.getServerById(id)?.orElse(null)

    /**
     * Removes the shard from the registry.
     *
     * @param shard the number of the shard.
     */
    fun remove(shard: Int) {
        this.shards.remove(shard)
    }

    /**
     * Calculates the shard that the snowflake belongs by using the formula ((snowflake >> 22) % totalShards) which is
     * what Discord describes as the formula for shards.
     *
     * @param snowflake the snowflake or discord identifier.
     * @param totalShards the total amount of shards.
     * @return the shard that the snowflake belongs to.
     */
    fun calculate(snowflake: Long, totalShards: Int): Int = ((snowflake shr 22) % totalShards).toInt()

    /**
     * Calculates the shard that the snowflake belongs by using the formula ((snowflake >> 22) % totalShards) which is
     * what Discord describes as the formula for shards.
     *
     * Unlike the other method, this uses the internal known current amount of shards which can lead to miscalculations especially
     * if all shards are not available. It is recommended to specify the total shards instead.
     *
     * @param snowflake the snowflake or discord identifier.
     * @return the shard that the snowflake belongs to.
     */
    fun calculate(snowflake: Long) = calculate(snowflake, shards.size)

}