package pw.mihou.nexus.features.command.synchronizer.overwrites.defaults;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.synchronizer.overwrites.NexusSynchronizeMethods;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NexusDefaultSynchronizeMethods implements NexusSynchronizeMethods {

    @Override
    public CompletableFuture<Void> bulkOverwriteGlobal(DiscordApi shard, List<SlashCommandBuilder> slashCommands) {
        CompletableFuture<Void> globalFuture = new CompletableFuture<>();

        shard.bulkOverwriteGlobalApplicationCommands(slashCommands).thenAccept(applicationCommands -> {
            NexusCore.logger.debug("Global commands completed synchronization. [size={}]", applicationCommands.size());
            globalFuture.complete(null);
        }).exceptionally(throwable -> {
            globalFuture.completeExceptionally(throwable);
            return null;
        });

        return globalFuture;
    }

    @Override
    public void bulkOverwriteServer(DiscordApi shard, List<SlashCommandBuilder> slashCommands,
                                                       long serverId, CompletableFuture<Void> future) {
        if (shard.getServerById(serverId).isEmpty()) {
            future.completeExceptionally(
                    new IllegalStateException(
                            "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? " +
                                    "[shard=" + shard.getCurrentShard() + ";id=" + serverId + "]"
                    )
            );
            return;
        }

        Server server = shard.getServerById(serverId).orElseThrow();
        shard.bulkOverwriteServerApplicationCommands(server, slashCommands)
                .thenAccept(applicationCommands -> {
                    NexusCore.logger.debug("A server has completed synchronization. [server={}, size={}]", serverId, applicationCommands.size());
                    future.complete(null);
                })
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
    }

    @Override
    public void deleteForServer(DiscordApi api, NexusCommand command, long serverId, CompletableFuture<Void> future) {
        if (api.getServerById(serverId).isEmpty()) {
            future.completeExceptionally(
                    new IllegalStateException(
                            "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? " +
                                    "[shard=" + api.getCurrentShard() + ";id=" + serverId + "]"
                    )
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
                    future.complete(null);
                }).exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                }));
    }

    @Override
    public void updateForServer(DiscordApi api, NexusCommand command, long serverId, CompletableFuture<Void> future) {
        if (api.getServerById(serverId).isEmpty()) {
            future.completeExceptionally(
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
                        future.complete(null);
                    })
                    .exceptionally(throwable -> {
                        future.completeExceptionally(throwable);
                        return null;
                    });
        } else {
            createForServer(api, command, serverId, future);
        }
    }

    @Override
    public void createForServer(DiscordApi api, NexusCommand command, long serverId, CompletableFuture<Void> future) {
        if (api.getServerById(serverId).isEmpty()) {
            future.completeExceptionally(
                    new IllegalStateException(
                            "Failed to synchronize commands for server, not found on the shard calculated. Is the total shard number value wrong? " +
                                    "[shard=" + api.getCurrentShard() + ";id=" + serverId + "]"
                    )
            );
            return;
        }

        Server server = api.getServerById(serverId).orElseThrow();
        command.asSlashCommand().createForServer(server)
                .thenAccept(slashCommand -> {
                    NexusCore.logger.debug("A command has completed synchronization. [server={}, command={}]", serverId, slashCommand.getName());
                    future.complete(null);
                })
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
    }
}
