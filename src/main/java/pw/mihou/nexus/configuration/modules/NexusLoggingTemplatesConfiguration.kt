package pw.mihou.nexus.configuration.modules

import org.javacord.api.interaction.ApplicationCommand
import org.javacord.api.interaction.SlashCommand
import pw.mihou.nexus.Nexus

class NexusLoggingTemplatesConfiguration internal constructor() {

    @get:JvmSynthetic
    @set:JvmName("setGlobalCommandsSynchronizedMessage")
    var GLOBAL_COMMANDS_SYNCHRONIZED: (commands: Set<ApplicationCommand>) -> String =
        { commands -> "All global commands have been pushed to Discord successfully. {size=" + commands.size + "}" }

    @get:JvmSynthetic
    @set:JvmName("setServerCommandsSynchronizedMessage")
    var SERVER_COMMANDS_SYNCHRONIZED: (server: Long, commands: Set<ApplicationCommand>) -> String =
        { server, commands -> "All server commands for $server has been pushed to Discord successfully. {size=" + commands.size + "}"}

    @get:JvmSynthetic
    @set:JvmName("setServerCommandDeletedMessage")
    var SERVER_COMMAND_DELETED: (server: Long, command: SlashCommand) -> String =
        { server, command -> command.name + " server command has been removed. {server=$server}" }

    @get:JvmSynthetic
    @set:JvmName("setServerCommandUpdatedMessage")
    var SERVER_COMMAND_UPDATED: (server: Long, command: SlashCommand) -> String =
        { server, command -> command.name + " server command has been updated. {server=$server}" }

    @get:JvmSynthetic
    @set:JvmName("setServerCommandCreatedMessage")
    var SERVER_COMMAND_CREATED: (server: Long, command: SlashCommand) -> String =
        { server, command -> command.name + " server command has been created. {server=$server}" }

    @get:JvmSynthetic
    @set:JvmName("setCommandsIndexedMessage")
    var COMMANDS_INDXED: (timeTakenInMilliseconds: Long) -> String =
        { millis -> "All commands have been indexed and stored in the index store. {timeTaken=$millis}" }

    @get:JvmSynthetic
    @set:JvmName("setIndexingCommandsMessage")
    var INDEXING_COMMANDS = "All commands are now being queued for indexing, this will take some time especially with large bots, but will allow for " +
            "server-specific slash command mentions, faster and more precise command matching..."

}

@JvmSynthetic
internal fun String.debug() = Nexus.logger.debug(this)
@JvmSynthetic
internal fun String.info() = Nexus.logger.info(this)