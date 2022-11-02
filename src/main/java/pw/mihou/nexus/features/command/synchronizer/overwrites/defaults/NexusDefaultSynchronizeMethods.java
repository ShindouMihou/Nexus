package pw.mihou.nexus.features.command.synchronizer.overwrites.defaults;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.exceptions.NexusFailedActionException;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.synchronizer.overwrites.NexusSynchronizeMethods;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NexusDefaultSynchronizeMethods implements NexusSynchronizeMethods {

    @Override
    public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteGlobal(DiscordApi shard, Set<SlashCommandBuilder> slashCommands) {
        return shard.bulkOverwriteGlobalApplicationCommands(slashCommands).thenApply(applicationCommands -> {
            Nexus.getLogger().debug("All global commands have been synchronized. [size={}]", applicationCommands.size());
            return applicationCommands;
        });
    }

    @Override
    public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteServer(DiscordApi shard, Set<SlashCommandBuilder> slashCommands, long serverId) {
        return shard.bulkOverwriteServerApplicationCommands(serverId, slashCommands).thenApply(applicationCommands -> {
            Nexus.getLogger().debug("All commands with relation for server {} have been synchronized.", serverId);
            return applicationCommands;
        });
    }

    @Override
    public CompletableFuture<Void> deleteForServer(DiscordApi shard, NexusCommand command, long serverId) {
        if (shard.getServerById(serverId).isEmpty()) {
            return getServerNotFoundErrorFrom(shard, serverId);
        }

        Server server = shard.getServerById(serverId).orElseThrow();
        return server.getSlashCommands().thenCompose(slashCommands -> {
            SlashCommand slashCommand = slashCommands.stream()
                    .filter($command -> $command.getName().equalsIgnoreCase(command.getName()))
                    .findAny()
                    .orElse(null);

            if (slashCommand == null) {
                return CompletableFuture.completedFuture(null);
            }

            return slashCommand.deleteForServer(serverId)
                    .thenAccept((unused) ->
                            Nexus.getLogger().debug("The command ({}) has been removed from the server ({}).",
                                    command.getName(), serverId)
                    );
        });
    }

    @Override
    public CompletableFuture<ApplicationCommand> updateForServer(DiscordApi shard, NexusCommand command, long serverId) {
        if (shard.getServerById(serverId).isEmpty()) {
            return getServerNotFoundErrorFrom(shard, serverId);
        }

        Server server = shard.getServerById(serverId).orElseThrow();
        return server.getSlashCommands().thenCompose(slashCommands -> {
            SlashCommand slashCommand = slashCommands.stream()
                    .filter($command -> $command.getName().equalsIgnoreCase(command.getName()))
                    .findAny()
                    .orElse(null);

            if (slashCommand == null) {
                return createForServer(shard, command, serverId);
            }

            return command.asSlashCommandUpdater(slashCommand.getId())
                    .updateForServer(shard, serverId)
                    .thenApply(resultingCommand -> {
                        Nexus.getLogger().debug("The command ({}) has been updated for the server ({}).", command.getName(), serverId);
                        return resultingCommand;
                    });
        });
    }

    @Override
    public CompletableFuture<ApplicationCommand> createForServer(DiscordApi shard, NexusCommand command, long serverId) {
        return command.asSlashCommand().createForServer(shard, serverId).thenApply(slashCommand -> {
            Nexus.getLogger().debug("The command ({}) has been created for the server ({}).", command.getName(),
                    serverId);
            return slashCommand;
        });
    }

    /**
     * Creates a simple {@link CompletableFuture} with a failed future that indicates the server cannot be found.
     *
     * @return A failed future indicating the server cannot be found.
     * @param <U> The return type intended to contain.
     */
    private <U> CompletableFuture<U> getServerNotFoundErrorFrom(DiscordApi shard, long serverId) {
        return CompletableFuture.failedFuture(
                new NexusFailedActionException(
                        "An action failed for Nexus Synchronizer. The server (" + serverId + ")" +
                        " cannot be found on the shard calculated (" + shard.getCurrentShard() +"). " +
                        "Is the total shard number value wrong?"
                )
        );
    }
}
