package pw.mihou.nexus.features.command.synchronizer;

import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandBuilder;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.enginex.facade.NexusEngineX;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.synchronizer.overwrites.NexusSynchronizeMethods;
import pw.mihou.nexus.features.command.synchronizer.overwrites.defaults.NexusDefaultSynchronizeMethods;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public record NexusSynchronizer(
        Nexus nexus
) {

    public static final AtomicReference<NexusSynchronizeMethods> SYNCHRONIZE_METHODS = new AtomicReference<>(new NexusDefaultSynchronizeMethods());

    /**
     * Deletes a command to a specific server.
     *
     * @param command       The command to delete.
     * @param serverIds     The servers to delete the command towards.
     * @param totalShards   The total amount of shards for this bot. This is used to
     *                      for sharding formula.
     * @return  A future to indicate progress of this task.
     */
    public CompletableFuture<Void> delete(NexusCommand command, int totalShards, long... serverIds) {
        Map<Long, CompletableFuture<Void>> serverMappedFutures = new HashMap<>();
        NexusEngineX engineX = ((NexusCore) nexus).getEngineX();
        for (long serverId : serverIds) {
            if (!serverMappedFutures.containsKey(serverId)) {
                serverMappedFutures.put(serverId, new CompletableFuture<>());
            }

            engineX.queue(
                    (int) ((serverId >> 22) % totalShards),
                    (api, store) -> SYNCHRONIZE_METHODS.get().deleteForServer(api, command, serverId, serverMappedFutures.get(serverId))
            );
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
        NexusCommandManager manager = nexus.getCommandManager();
        CompletableFuture<Void> future = new CompletableFuture<>();
        List<NexusCommand> serverCommands = manager.getCommands()
                .stream()
                .filter(nexusCommand -> !nexusCommand.getServerIds().isEmpty() && nexusCommand.getServerIds().contains(serverId))
                .toList();

        NexusEngineX engineX = ((NexusCore) nexus).getEngineX();
        List<SlashCommandBuilder> slashCommandBuilders = new ArrayList<>();
        serverCommands.forEach(command -> slashCommandBuilders.add(command.asSlashCommand()));
        engineX.queue(
                (int) ((serverId >> 22) % totalShards),
                (api, store) -> SYNCHRONIZE_METHODS.get().bulkOverwriteServer(api, slashCommandBuilders, serverId, future)
        );

        return future;
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
        NexusEngineX engineX = ((NexusCore) nexus).getEngineX();
        for (long serverId : serverIds) {
            if (!serverMappedFutures.containsKey(serverId)) {
                serverMappedFutures.put(serverId, new CompletableFuture<>());
            }

            engineX.queue(
                    (int) ((serverId >> 22) % totalShards),
                    (api, store) -> SYNCHRONIZE_METHODS.get().updateForServer(api, command, serverId, serverMappedFutures.get(serverId))
            );
        }

        return CompletableFuture.allOf(serverMappedFutures.values().toArray(new CompletableFuture[0]));
    }

    /**
     * Synchronizes all the server commands and global commands with the use of
     * {@link org.javacord.api.DiscordApi#bulkOverwriteGlobalApplicationCommands(List)} and
     * {@link org.javacord.api.DiscordApi#bulkOverwriteServerApplicationCommands(Server, List)}. This does not
     * take any regards to any changes and pushes an override without any care.
     *
     * @param totalShards   The total amount of shards on the bot, used for sharding formula.
     * @return A future to indicate the progress of the synchronization task.
     */
    public CompletableFuture<Void> synchronize(int totalShards) {
        NexusCommandManager manager = nexus.getCommandManager();
        CompletableFuture<Void> globalFuture = new CompletableFuture<>();

        // 0L is acceptable as a placeholder definition for "This is a server command but only register when a server other than zero is up".
        List<NexusCommand> serverCommands = manager.getCommands()
                .stream()
                .filter(nexusCommand -> !nexusCommand.getServerIds().isEmpty()
                        && !(nexusCommand.getServerIds().size() == 1 && nexusCommand.getServerIds().get(0) == 0)
                )
                .toList();

        NexusEngineX engineX = ((NexusCore) nexus).getEngineX();
        engineX.queue(
                (api, store) -> SYNCHRONIZE_METHODS.get().bulkOverwriteGlobal(api, manager.getCommands()
                        .stream()
                        .filter(nexusCommand -> nexusCommand.getServerIds().isEmpty())
                        .map(NexusCommand::asSlashCommand).toList())
        );

        Map<Long, List<SlashCommandBuilder>> serverMappedCommands = new HashMap<>();
        Map<Long, CompletableFuture<Void>> serverMappedFutures = new HashMap<>();
        serverCommands.forEach(nexusCommand -> nexusCommand.getServerIds().forEach(serverId -> {
            if (serverId == 0L) {
                return;
            }

            if (!serverMappedCommands.containsKey(serverId)) {
                serverMappedCommands.put(serverId, new ArrayList<>());
            }

            serverMappedCommands.get(serverId).add(nexusCommand.asSlashCommand());
        }));

        serverMappedCommands.forEach((serverId, slashCommandBuilders) -> {
            if (serverId == 0L) {
                return;
            }

            if (!serverMappedFutures.containsKey(serverId)) {
                serverMappedFutures.put(serverId, new CompletableFuture<>());
            }

            engineX.queue((int) (
                    (serverId >> 22) % totalShards),
                    (api, store) -> SYNCHRONIZE_METHODS.get().bulkOverwriteServer(api, slashCommandBuilders, serverId, serverMappedFutures.get(serverId))
            );
        });

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(globalFuture);
        futures.addAll(serverMappedFutures.values());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
