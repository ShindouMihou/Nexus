package pw.mihou.nexus.features.command.synchronizer.overwrites

import org.javacord.api.DiscordApi
import org.javacord.api.interaction.ApplicationCommand
import org.javacord.api.interaction.ApplicationCommandBuilder
import org.javacord.api.interaction.SlashCommandBuilder
import pw.mihou.nexus.features.command.facade.NexusCommand
import java.util.concurrent.CompletableFuture

interface NexusSynchronizeMethods {
    fun bulkOverwriteGlobal(
        shard: DiscordApi,
        applicationCommands: Set<ApplicationCommandBuilder<*, *, *>>
    ): CompletableFuture<Set<ApplicationCommand>>

    fun bulkOverwriteServer(
        shard: DiscordApi,
        applicationCommands: Set<ApplicationCommandBuilder<*, *, *>>,
        serverId: Long
    ): CompletableFuture<Set<ApplicationCommand>>

    fun deleteForServer(shard: DiscordApi, command: NexusCommand, serverId: Long): CompletableFuture<Void>
    fun updateForServer(
        shard: DiscordApi,
        command: NexusCommand,
        serverId: Long
    ): CompletableFuture<ApplicationCommand>

    fun createForServer(
        shard: DiscordApi,
        command: NexusCommand,
        serverId: Long
    ): CompletableFuture<ApplicationCommand>
}
