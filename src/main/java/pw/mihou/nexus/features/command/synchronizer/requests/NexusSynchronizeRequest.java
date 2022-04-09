package pw.mihou.nexus.features.command.synchronizer.requests;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandBuilder;
import java.util.List;

public record NexusSynchronizeRequest(
        DiscordApi shard,
        List<SlashCommandBuilder> slashCommands
) { }