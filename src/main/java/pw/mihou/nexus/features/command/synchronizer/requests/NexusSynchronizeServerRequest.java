package pw.mihou.nexus.features.command.synchronizer.requests;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommandBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record NexusSynchronizeServerRequest(
        DiscordApi shard,
        List<SlashCommandBuilder> slashCommands,
        long serverId,
        CompletableFuture<List<ApplicationCommand>> future
) { }
