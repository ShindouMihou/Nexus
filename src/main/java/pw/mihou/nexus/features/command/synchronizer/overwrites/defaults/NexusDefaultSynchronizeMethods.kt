package pw.mihou.nexus.features.command.synchronizer.overwrites.defaults

import org.javacord.api.DiscordApi
import org.javacord.api.interaction.ApplicationCommand
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.SlashCommandBuilder
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.debug
import pw.mihou.nexus.core.exceptions.NexusFailedActionException
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.synchronizer.overwrites.NexusSynchronizeMethods
import java.util.concurrent.CompletableFuture

object NexusDefaultSynchronizeMethods : NexusSynchronizeMethods {

    override fun bulkOverwriteGlobal(shard: DiscordApi, slashCommands: Set<SlashCommandBuilder>) =
        shard
            .bulkOverwriteGlobalApplicationCommands(slashCommands)
            .and { Nexus.configuration.loggingTemplates.GLOBAL_COMMANDS_SYNCHRONIZED(it).debug() }

    override fun bulkOverwriteServer(shard: DiscordApi, slashCommands: Set<SlashCommandBuilder>, serverId: Long) =
        shard
            .bulkOverwriteServerApplicationCommands(serverId, slashCommands)
            .and { Nexus.configuration.loggingTemplates.SERVER_COMMANDS_SYNCHRONIZED(serverId, it).debug() }

    override fun deleteForServer(shard: DiscordApi, command: NexusCommand, serverId: Long): CompletableFuture<Void> {
        if (shard.getServerById(serverId).isEmpty) {
            return getServerNotFoundErrorFrom(shard, serverId)
        }

        val server = shard.getServerById(serverId).orElseThrow()
        return server.slashCommands.thenCompose { slashCommands ->
            val slashCommand = find(command, from = slashCommands) ?: return@thenCompose CompletableFuture.completedFuture(null)
            return@thenCompose slashCommand.delete().thenAccept {
                Nexus.configuration.loggingTemplates.SERVER_COMMAND_DELETED(serverId, slashCommand).debug()
            }
        }
    }

    override fun updateForServer(shard: DiscordApi, command: NexusCommand, serverId: Long): CompletableFuture<ApplicationCommand> {
        if (shard.getServerById(serverId).isEmpty) {
            return getServerNotFoundErrorFrom(shard, serverId)
        }

        val server = shard.getServerById(serverId).orElseThrow()
        return server.slashCommands.thenCompose { slashCommands ->
            val slashCommand = find(command, from = slashCommands) ?: return@thenCompose createForServer(shard, command, serverId)
            return@thenCompose command.asSlashCommandUpdater(slashCommand.id)
                .updateForServer(shard, serverId)
                .and { Nexus.configuration.loggingTemplates.SERVER_COMMAND_UPDATED(serverId, it).debug() }
                .thenApply { it as ApplicationCommand }
        }
    }

    override fun createForServer(shard: DiscordApi, command: NexusCommand, serverId: Long) =
        command
            .asSlashCommand()
            .createForServer(shard, serverId)
            .and { Nexus.configuration.loggingTemplates.SERVER_COMMAND_CREATED(serverId, it).debug() }
            .thenApply { it as ApplicationCommand }

    private fun find(command: NexusCommand, from: Set<SlashCommand>): SlashCommand? =
        from.firstOrNull { `$command` -> `$command`.name.equals(command.name, ignoreCase = true) }

    private fun <Type> CompletableFuture<Type>.and(`do`: (Type) -> Unit): CompletableFuture<Type> =
        this.thenApply { `do`(it); return@thenApply it }

    private fun <U> getServerNotFoundErrorFrom(shard: DiscordApi, serverId: Long): CompletableFuture<U> =
        CompletableFuture.failedFuture(NexusFailedActionException(
            "An action failed for Nexus Synchronizer. The server (" + serverId + ")" +
                    " cannot be found on the shard calculated (" + shard.currentShard + "). " +
                    "Is the total shard number value wrong?"
        ))
}