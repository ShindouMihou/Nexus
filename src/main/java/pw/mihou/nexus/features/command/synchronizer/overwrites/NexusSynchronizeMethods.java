package pw.mihou.nexus.features.command.synchronizer.overwrites;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface NexusSynchronizeMethods {

    CompletableFuture<Set<ApplicationCommand>> bulkOverwriteGlobal(DiscordApi shard,
                                                                   Set<SlashCommandBuilder> slashCommands);

    CompletableFuture<Set<ApplicationCommand>> bulkOverwriteServer(DiscordApi shard, Set<SlashCommandBuilder> slashCommands,
                                                                   long serverId);

    CompletableFuture<Void> deleteForServer(DiscordApi shard, NexusCommand command, long serverId);

    CompletableFuture<ApplicationCommand> updateForServer(DiscordApi shard, NexusCommand command, long serverId);

    CompletableFuture<ApplicationCommand> createForServer(DiscordApi shard, NexusCommand command, long serverId);

}
