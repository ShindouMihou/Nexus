package pw.mihou.nexus.features.command.synchronizer

import org.javacord.api.interaction.SlashCommandBuilder
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.core.managers.facade.NexusCommandManager
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.synchronizer.overwrites.NexusSynchronizeMethods
import pw.mihou.nexus.features.command.synchronizer.overwrites.defaults.NexusDefaultSynchronizeMethods
import java.util.concurrent.CompletableFuture

class NexusSynchronizer internal constructor() {

    @Volatile var methods: NexusSynchronizeMethods = NexusDefaultSynchronizeMethods

    /**
     * Deletes a command from a specific server.
     *
     * @param command       The command to delete.
     * @param servers       The servers to delete the command towards.
     * @param totalShards   The total amount of shards for this bot. This is used to
     *                      for sharding formula.
     * @return  A future to indicate the completion of this task.
     */
    fun delete(command: NexusCommand, totalShards: Int, vararg servers: Long): CompletableFuture<Void> {
        val serverMappedFutures: MutableMap<Long, CompletableFuture<Void>> = HashMap()

        for (serverId in servers) {
            if (serverMappedFutures.containsKey(serverId)) continue

            val future: CompletableFuture<Void> = Nexus.express
                .await(Nexus.sharding.calculate(serverId, totalShards))
                .thenCompose { shard -> methods.deleteForServer(shard, command, serverId) }

            serverMappedFutures[serverId] = future
        }

        return CompletableFuture.allOf(*serverMappedFutures.values.toTypedArray<CompletableFuture<*>>())
    }

    /**
     * Batch updates all commands that supports a specific server. This completely overrides the
     * server command list and can be used to clear any server slash commands of the bot for that
     * specific server.
     *
     * @param server the given guild snowflake to perform updates upon.
     * @return  A future to indicate progress of this task.
     */
    fun batchUpdate(server: Long): CompletableFuture<Void> {
        val manager: NexusCommandManager = Nexus.commandManager
        val serverCommands = manager.commandsAssociatedWith(server).map { command -> command.asSlashCommand() }.toHashSet()

        return Nexus.express.awaitAvailable()
            .thenCompose { shard -> methods.bulkOverwriteServer(shard, serverCommands, server) }
            .thenAccept(manager::index)
    }

    /**
     * Upserts a command to a specific server.
     *
     * @param command       The command to upsert.
     * @param servers       The servers to upsert the command towards.
     * @param totalShards   The total amount of shards for this bot. This is used to
     * for sharding formula.
     * @return  A future to indicate progress of this task.
     */
    fun upsert(command: NexusCommand, totalShards: Int, vararg servers: Long): CompletableFuture<Void> {
        val serverMappedFutures = mutableMapOf<Long, CompletableFuture<Void>>()

        for (server in servers) {
            if (serverMappedFutures.containsKey(server)) continue

            val future: CompletableFuture<Void> = Nexus.express
                .await(Nexus.sharding.calculate(server, totalShards))
                .thenCompose { shard -> methods.updateForServer(shard, command, server) }
                .thenAccept { `$command` -> Nexus.commandManager.index(command, `$command`.applicationId, `$command`.serverId.orElse(null)) }

            serverMappedFutures[server] = future
        }

        return CompletableFuture.allOf(*serverMappedFutures.values.toTypedArray<CompletableFuture<*>>())
    }

    /**
     * Synchronizes all the server commands and global commands with the use of
     * [org.javacord.api.DiscordApi.bulkOverwriteGlobalApplicationCommands] and
     * [org.javacord.api.DiscordApi.bulkOverwriteServerApplicationCommands]. This does not
     * take any regards to any changes and pushes an override without any care.
     *
     * @return A future to indicate the progress of the synchronization task.
     */
    fun synchronize(): CompletableFuture<Void> {
        val manager: NexusCommandManager = Nexus.commandManager

        val serverCommands = manager.serverCommands
        val globalCommands = manager.globalCommands.map { command -> command.asSlashCommand() }.toHashSet()

        val globalFuture: CompletableFuture<Void> = Nexus.express
            .awaitAvailable()
            .thenCompose { shard -> methods.bulkOverwriteGlobal(shard, globalCommands) }
            .thenAccept(manager::index)

        if (serverCommands.isEmpty()) {
            return globalFuture
        }

        val serverMappedCommands: MutableMap<Long, MutableSet<SlashCommandBuilder>> = mutableMapOf()
        val serverMappedFutures: MutableMap<Long, CompletableFuture<Void>> = mutableMapOf()

        for (`$command` in serverCommands) {
            for (serverId in `$command`.serverIds) {
                if (serverId == NexusCommand.PLACEHOLDER_SERVER_ID) continue
                if (!serverMappedCommands.containsKey(serverId)) {
                    serverMappedCommands[serverId] = HashSet()
                }

                serverMappedCommands[serverId]!!.add(`$command`.asSlashCommand())
            }
        }

        serverMappedCommands.forEach { (server, builders) ->
            if (server == NexusCommand.PLACEHOLDER_SERVER_ID) return@forEach

            val future: CompletableFuture<Void> = Nexus.express
                .awaitAvailable()
                .thenCompose { shard -> methods.bulkOverwriteServer(shard, builders, server) }
                .thenAccept(manager::index)

            serverMappedFutures[server] = future
        }

        val futures: List<CompletableFuture<Void>> = mutableListOf<CompletableFuture<Void>>().let {
            it.add(globalFuture)
            it.addAll(serverMappedFutures.values)

            it.toList()
        }

        return CompletableFuture.allOf(*futures.toTypedArray<CompletableFuture<*>>())
    }

}