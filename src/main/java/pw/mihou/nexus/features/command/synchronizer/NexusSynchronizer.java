package pw.mihou.nexus.features.command.synchronizer;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandBuilder;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.enginex.facade.NexusEngineX;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.synchronizer.overwrites.NexusSynchronizeMethods;
import pw.mihou.nexus.features.command.synchronizer.overwrites.defaults.NexusDefaultSynchronizeMethods;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public record NexusSynchronizer(Nexus nexus) {

    public static volatile NexusSynchronizeMethods SYNCHRONIZE_METHODS = new NexusDefaultSynchronizeMethods();

    /**
     * Deletes a command from a specific server.
     *
     * @param command       The command to delete.
     * @param serverIds     The servers to delete the command towards.
     * @param totalShards   The total amount of shards for this bot. This is used to
     *                      for sharding formula.
     * @return  A future to indicate the completion of this task.
     */
    public CompletableFuture<Void> delete(NexusCommand command, int totalShards, long... serverIds) {
        Map<Long, CompletableFuture<Void>> serverMappedFutures = new HashMap<>();

        for (long serverId : serverIds) {
            if (serverMappedFutures.containsKey(serverId)) continue;

            CompletableFuture<Void> future = nexus.getEngineX()
                    .await(nexus.getShardManager().shardOf(serverId, totalShards))
                    .thenCompose(shard -> SYNCHRONIZE_METHODS.deleteForServer(shard, command, serverId));

            serverMappedFutures.put(serverId, future);
        }

        return CompletableFuture.allOf(serverMappedFutures.values().toArray(new CompletableFuture[0]));
    }

    /**
     * Batch updates all commands that supports a specific server. This completely overrides the
     * server command list and can be used to clear any server slash commands of the bot for that
     * specific server.
     *
     * @param totalShards   The total amount of shards for this bot. This is used to
     *                      for sharding formula.
     * @param serverId      The server to batch upsert the commands onto.
     * @return  A future to indicate progress of this task.
     */
    public CompletableFuture<Void> batchUpdate(long serverId, int totalShards) {
        return nexus.getEngineX().await(nexus.getShardManager().shardOf(serverId, totalShards))
                .thenCompose(shard -> batchUpdate(serverId, shard));
    }

    /**
     * Batch updates all commands that supports a specific server. This completely overrides the
     * server command list and can be used to clear any server slash commands of the bot for that
     * specific server.
     *
     * @param shard         The shard to use for updating the server's commands.
     * @param serverId      The server to batch upsert the commands onto.
     * @return  A future to indicate progress of this task.
     */
    public CompletableFuture<Void> batchUpdate(long serverId, DiscordApi shard) {
        NexusCommandManager manager = nexus.getCommandManager();

        Set<SlashCommandBuilder> serverCommands = manager.getCommandsAssociatedWith(serverId).stream()
                .map(NexusCommand::asSlashCommand)
                .collect(Collectors.toSet());

        return SYNCHRONIZE_METHODS.bulkOverwriteServer(shard, serverCommands, serverId).thenAccept(manager::index);
    }

    /**
     * Upserts a command to a specific server.
     *
     * @param command       The command to upsert.
     * @param serverIds     The servers to upsert the command towards.
     * @param totalShards   The total amount of shards for this bot. This is used to
     *                      for sharding formula.
     * @return  A future to indicate progress of this task.
     */
    public CompletableFuture<Void> upsert(NexusCommand command, int totalShards, long... serverIds) {
        Map<Long, CompletableFuture<Void>> serverMappedFutures = new HashMap<>();

        for (long serverId : serverIds) {
            if (serverMappedFutures.containsKey(serverId)) continue;

            CompletableFuture<Void> future = nexus.getEngineX()
                    .await(nexus.getShardManager().shardOf(serverId, totalShards))
                    .thenCompose(shard -> SYNCHRONIZE_METHODS.updateForServer(shard, command, serverId))
                    .thenAccept($command -> nexus.getCommandManager().index(command, $command.getApplicationId()));

            serverMappedFutures.put(serverId, future);
        }

        return CompletableFuture.allOf(serverMappedFutures.values().toArray(new CompletableFuture[0]));
    }

    /**
     * Synchronizes all the server commands and global commands with the use of
     * {@link org.javacord.api.DiscordApi#bulkOverwriteGlobalApplicationCommands(Set)} and
     * {@link org.javacord.api.DiscordApi#bulkOverwriteServerApplicationCommands(Server, Set)}. This does not
     * take any regards to any changes and pushes an override without any care.
     *
     * @param totalShards   The total amount of shards on the bot, used for sharding formula.
     * @return A future to indicate the progress of the synchronization task.
     */
    public CompletableFuture<Void> synchronize(int totalShards) {
        NexusCommandManager manager = nexus.getCommandManager();

        Set<NexusCommand> serverCommands = manager.getServerCommands();
        Set<SlashCommandBuilder> globalCommands = manager.getGlobalCommands().stream().map(NexusCommand::asSlashCommand)
                .collect(Collectors.toSet());

        NexusEngineX engineX = nexus.getEngineX();
        CompletableFuture<Void> globalFuture = engineX.awaitAvailable()
                .thenCompose(shard -> SYNCHRONIZE_METHODS.bulkOverwriteGlobal(shard, globalCommands))
                .thenAccept(manager::index);

        if (serverCommands.isEmpty()) {
            return globalFuture;
        }

        Map<Long, Set<SlashCommandBuilder>> serverMappedCommands = new HashMap<>();
        Map<Long, CompletableFuture<Void>> serverMappedFutures = new HashMap<>();

        for (NexusCommand $command : serverCommands) {
            for (long serverId : $command.getServerIds()) {
                if (serverId == NexusCommand.PLACEHOLDER_SERVER_ID) continue;

                if (!serverMappedCommands.containsKey(serverId)) {
                    serverMappedCommands.put(serverId, new HashSet<>());
                }

                serverMappedCommands.get(serverId).add($command.asSlashCommand());
            }
        }

        serverMappedCommands.forEach((server, builders) -> {
            if (server == NexusCommand.PLACEHOLDER_SERVER_ID) return;

            CompletableFuture<Void> future = engineX.await(nexus.getShardManager().shardOf(server, totalShards))
                    .thenCompose(shard -> SYNCHRONIZE_METHODS.bulkOverwriteServer(shard, builders, server))
                    .thenAccept(manager::index);

            serverMappedFutures.put(server, future);
        });

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(globalFuture);
        futures.addAll(serverMappedFutures.values());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
