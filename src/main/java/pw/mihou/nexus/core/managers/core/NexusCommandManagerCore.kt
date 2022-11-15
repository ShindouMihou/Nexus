package pw.mihou.nexus.core.managers.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.ApplicationCommand
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.info
import pw.mihou.nexus.core.managers.facade.NexusCommandManager
import pw.mihou.nexus.core.managers.indexes.IndexStore
import pw.mihou.nexus.core.managers.indexes.defaults.InMemoryIndexStore
import pw.mihou.nexus.core.managers.indexes.exceptions.IndexIdentifierConflictException
import pw.mihou.nexus.core.managers.records.NexusMetaIndex
import pw.mihou.nexus.features.command.core.NexusCommandCore
import pw.mihou.nexus.features.command.facade.NexusCommand
import java.util.*

class NexusCommandManagerCore internal constructor() : NexusCommandManager  {
    private val commandsDelegate: MutableMap<String, NexusCommand> = HashMap()
    override val commands: Collection<NexusCommand>
        get() = commandsDelegate.values

    override var indexStore: IndexStore = InMemoryIndexStore()

    override fun add(command: NexusCommand): NexusCommandManager {
        if (commandsDelegate[command.uuid] != null)
            throw IndexIdentifierConflictException(command)

        commandsDelegate[(command as NexusCommandCore).uuid] = command
        return this
    }

    override operator fun get(applicationId: Long): NexusCommand? = indexStore[applicationId]?.take()
    override operator fun get(uuid: String): NexusCommand? = commandsDelegate[uuid]
    override operator fun get(name: String, server: Long?): NexusCommand? {
        return commands.firstOrNull { command ->
            when (server) {
                null -> {
                    command.name.equals(name, ignoreCase = true)
                }
                else -> {
                    command.name.equals(name, ignoreCase = true) && command.serverIds.contains(server)
                }
            }
        }
    }

    override fun export(): List<NexusMetaIndex> {
        return indexStore.all()
    }

    private fun toIndex(applicationCommandId: Long, command: NexusCommand, server: Long?): NexusMetaIndex {
        return NexusMetaIndex(command = command.uuid, applicationCommandId = applicationCommandId, server = server)
    }

    /**
     * This performs indexing based on the data analyzed from the [SlashCommandCreateEvent] and
     * returns the results for post-processing.
     *
     * @param event The event to handle.
     */
    fun acceptEvent(event: SlashCommandCreateEvent): NexusCommand? {
        val interaction = event.slashCommandInteraction

        if (get(interaction.commandId) != null) {
            return get(interaction.commandId)
        }

        return if (interaction.server.isPresent) {
            val server = interaction.server.get().id

            return when (val command = get(interaction.commandName, server)) {
                null -> index(interaction, get(interaction.commandName, null), null)
                else -> index(interaction, command, server)
            }

        } else index(interaction, get(interaction.commandName, null), null)
    }

    /**
     * An internal method that is used by [acceptEvent] as a short-hand to index the command if the command is
     * present and also return the command at the same time.
     *
     * @param interaction the interaction received from the event.
     * @param command the command that was identified.
     * @param server the server that the command is associated or was called.
     *
     * @return the command.
     */
    private fun index(interaction: SlashCommandInteraction, command: NexusCommand?, server: Long?): NexusCommand? {
        return command?.apply { indexStore.add(toIndex(interaction.commandId, this, server)) }
    }

    override fun index() {
        Nexus.configuration.loggingTemplates.INDEXING_COMMANDS.info()

        val start = System.currentTimeMillis()
        Nexus.express
            .awaitAvailable()
            .thenAcceptAsync { shard ->
                val slashCommands: Set<SlashCommand> = shard.globalSlashCommands.join()

                // Clearing the entire index store to make sure that we don't have any outdated indexes.
                indexStore.clear()
                for (slashCommand in slashCommands) {
                    index(slashCommand)
                }

                val servers: MutableSet<Long> = HashSet()
                for (serverCommand in serverCommands) {
                    servers.addAll(serverCommand.serverIds)
                }

                for (server in servers) {
                    if (server == 0L) continue

                    val slashCommandSet: Set<SlashCommand> = Nexus.express
                        .await(server)
                        .thenComposeAsync { it.slashCommands }
                        .join()

                    for (slashCommand in slashCommandSet) {
                        index(slashCommand)
                    }
                }

                Nexus.configuration.loggingTemplates.COMMANDS_INDXED(System.currentTimeMillis() - start).info()
            }
            .exceptionally(ExceptionLogger.get())
            .join()
    }

    private fun manifest(command: NexusCommand, snowflake: Long, server: Long?) =
        toIndex(applicationCommandId = snowflake, command = command, server = server)

    override fun index(command: NexusCommand, snowflake: Long, server: Long?) {
        indexStore.add(toIndex(applicationCommandId = snowflake, command = command, server = server))
    }

    override fun index(applicationCommandList: Set<ApplicationCommand>) {
        val indexes = mutableListOf<NexusMetaIndex>()
        for (applicationCommand in applicationCommandList) {
            val serverId: Long? = applicationCommand.serverId.orElse(null)

            if (serverId == null) {
                for (command in commands) {
                    if (command.serverIds.isNotEmpty()) continue
                    if (!command.name.equals(applicationCommand.name, ignoreCase = true)) continue

                    indexes.add(manifest(command, applicationCommand.id, null))
                    break
                }
            } else {
                for (command in commands) {
                    if (!command.name.equals(applicationCommand.name, ignoreCase = true)) continue
                    if (!command.serverIds.contains(serverId)) continue

                    indexes.add(manifest(command, applicationCommand.id, serverId))
                    break
                }
            }
        }

        indexStore.addAll(indexes)
    }

    override fun index(applicationCommand: ApplicationCommand) {
        val serverId: Long? = applicationCommand.serverId.orElse(null)

        if (serverId == null) {
            for (command in commands) {
                if (command.serverIds.isNotEmpty()) continue
                if (!command.name.equals(applicationCommand.name, ignoreCase = true)) continue

                index(command, applicationCommand.id, null)
                break
            }
            return
        } else {
            for (command in commands) {
                if (!command.name.equals(applicationCommand.name, ignoreCase = true)) continue
                if (!command.serverIds.contains(serverId)) continue

                index(command, applicationCommand.id, serverId)
                break
            }
        }
    }
}