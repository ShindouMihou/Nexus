package pw.mihou.nexus.features.command.synchronizer;

import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.enginex.facade.NexusEngineX;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public record NexusSynchronizer(
        Nexus nexus
) {

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

            engineX.queue((int) ((serverId >> 22) % totalShards), (api, store) -> {
                if (api.getServerById(serverId).isEmpty()) {
                    NexusCore.logger.error(
                            "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? [shard={};id={}]",
                            api.getCurrentShard(),
                            serverId
                    );
                    return;
                }

                Server server = api.getServerById(serverId).orElseThrow();
                server.getSlashCommands()
                        .join()
                        .stream()
                        .filter(slashCommand -> slashCommand.getName().equalsIgnoreCase(command.getName()))
                        .findFirst()
                        .ifPresent(slashCommand -> slashCommand.deleteForServer(server).thenAccept(unused -> {
                            NexusCore.logger.debug("A command has completed deletion. [server={}, command={}]", serverId, slashCommand.getName());
                            serverMappedFutures.get(serverId).complete(null);
                        }).exceptionally(throwable -> {
                                    if (throwable != null) {
                                        serverMappedFutures.get(serverId).completeExceptionally(throwable);
                                    }

                                    return null;
                                }));
            });
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
        engineX.queue((int) ((serverId >> 22) % totalShards), (api, store) -> {
            if (api.getServerById(serverId).isEmpty()) {
                NexusCore.logger.error(
                        "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? [shard={};id={}]",
                        api.getCurrentShard(),
                        serverId
                );
                future.completeExceptionally(
                        new IllegalStateException(
                                "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? " +
                                        "[shard=" + api.getCurrentShard() + ";id=" + serverId + "]"
                        )
                );
                return;
            }

            Server server = api.getServerById(serverId).orElseThrow();
            api.bulkOverwriteServerApplicationCommands(server, slashCommandBuilders)
                    .thenAccept(applicationCommands -> {
                        NexusCore.logger.debug("A server has completed synchronization. [server={}, size={}]", serverId, applicationCommands.size());
                        future.complete(null);
                    })
                    .exceptionally(throwable -> {
                        if (throwable != null) {
                            future.completeExceptionally(throwable);
                        }

                        return null;
                    });
        });

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

            engineX.queue((int) ((serverId >> 22) % totalShards), (api, store) -> {
                if (api.getServerById(serverId).isEmpty()) {
                    NexusCore.logger.error(
                            "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? [shard={};id={}]",
                            api.getCurrentShard(),
                            serverId
                    );
                    serverMappedFutures.get(serverId).completeExceptionally(
                            new IllegalStateException(
                                    "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? " +
                                            "[shard=" + api.getCurrentShard() + ";id=" + serverId + "]"
                            )
                    );
                    return;
                }

                Server server = api.getServerById(serverId).orElseThrow();
                List<SlashCommand> commands = server.getSlashCommands().join();

                Optional<SlashCommand> matchingCommand = commands.stream()
                        .filter(slashCommand -> slashCommand.getName().equalsIgnoreCase(command.getName()))
                        .findFirst();

                if (matchingCommand.isPresent()) {
                    command.asSlashCommandUpdater(matchingCommand.get().getId()).updateForServer(server)
                            .thenAccept(slashCommand -> {
                                NexusCore.logger.debug("A command has completed synchronization. [server={}, command={}]", serverId, slashCommand.getName());
                                serverMappedFutures.get(serverId).complete(null);
                            })
                            .exceptionally(throwable -> {
                                if (throwable != null) {
                                    serverMappedFutures.get(serverId).completeExceptionally(throwable);
                                }

                                return null;
                            });
                } else {
                    command.asSlashCommand().createForServer(server)
                            .thenAccept(slashCommand -> {
                                NexusCore.logger.debug("A command has completed synchronization. [server={}, command={}]", serverId, slashCommand.getName());
                                serverMappedFutures.get(serverId).complete(null);
                            })
                            .exceptionally(throwable -> {
                                if (throwable != null) {
                                    serverMappedFutures.get(serverId).completeExceptionally(throwable);
                                }

                                return null;
                            });
                }
            });
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
                (api, store) -> api.bulkOverwriteGlobalApplicationCommands(
                        manager.getCommands()
                                .stream()
                                .filter(nexusCommand -> nexusCommand.getServerIds().isEmpty())
                                .map(NexusCommand::asSlashCommand).toList()
                ).thenAccept(applicationCommands -> {
                    NexusCore.logger.debug("Global commands completed synchronization. [size={}]", applicationCommands.size());
                    globalFuture.complete(null);
                }).exceptionally(throwable -> {
                    if (throwable != null) {
                        globalFuture.completeExceptionally(throwable);
                    }

                    return null;
                })
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

            engineX.queue((int) ((serverId >> 22) % totalShards), (api, store) -> {
                if (api.getServerById(serverId).isEmpty()) {
                    NexusCore.logger.error(
                            "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? [shard={};id={}]",
                            api.getCurrentShard(),
                            serverId
                    );
                    serverMappedFutures.get(serverId).completeExceptionally(
                            new IllegalStateException(
                                    "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? " +
                                            "[shard=" + api.getCurrentShard() + ";id=" + serverId + "]"
                            )
                    );
                    return;
                }

                Server server = api.getServerById(serverId).orElseThrow();
                api.bulkOverwriteServerApplicationCommands(server, slashCommandBuilders)
                        .thenAccept(applicationCommands -> {
                            NexusCore.logger.debug("A server has completed synchronization. [server={}, size={}]", serverId, applicationCommands.size());
                            serverMappedFutures.get(serverId).complete(null);
                        })
                        .exceptionally(throwable -> {
                            if (throwable != null) {
                                serverMappedFutures.get(serverId).completeExceptionally(throwable);
                            }

                            return null;
                        });
            });
        });

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(globalFuture);
        futures.addAll(serverMappedFutures.values());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
