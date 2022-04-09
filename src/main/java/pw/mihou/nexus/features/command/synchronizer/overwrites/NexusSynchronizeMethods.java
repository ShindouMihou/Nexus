package pw.mihou.nexus.features.command.synchronizer.overwrites;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NexusSynchronizeMethods {

    CompletableFuture<Void> bulkOverwriteGlobal(DiscordApi shard, List<SlashCommandBuilder> slashCommands);

    void bulkOverwriteServer(DiscordApi shard, List<SlashCommandBuilder> slashCommands,
                                                long serverId, CompletableFuture<Void> future);

    void deleteForServer(DiscordApi shard, NexusCommand command, long serverId, CompletableFuture<Void> future);

    void updateForServer(DiscordApi shard, NexusCommand command, long serverId, CompletableFuture<Void> future);

    void createForServer(DiscordApi shard, NexusCommand command, long serverId, CompletableFuture<Void> future);

}
