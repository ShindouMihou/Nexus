package pw.mihou.nexus.core.managers.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.interaction.ApplicationCommand
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
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

    private val indexStore: IndexStore = InMemoryIndexStore()

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
        Nexus.configuration.global.logger
            .info("Nexus is now performing command indexing, this will delay your boot time by a few seconds " +
                    "but improve performance and precision in look-ups...")

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

                Nexus.configuration.global.logger.info("All global and server slash commands are now indexed." +
                        " It took ${System.currentTimeMillis() - start} milliseconds to complete indexing.")
            }
            .exceptionally(ExceptionLogger.get())
            .join()
    }

    override fun index(command: NexusCommand, snowflake: Long, server: Long?) {
        indexStore.add(toIndex(applicationCommandId = snowflake, command = command, server = server))
    }

    override fun index(applicationCommand: ApplicationCommand) {
        val serverId = applicationCommand.serverId.orElse(-1L)

        if (serverId == -1L) {
            for (command in commands) {
                if (command.serverIds.isNotEmpty()) continue
                if (!command.name.equals(command.name, ignoreCase = true)) continue

                index(command, applicationCommand.applicationId, applicationCommand.serverId.orElse(null))
                break
            }
            return
        }

        for (command in commands) {
            if (command.serverIds.contains(serverId)) continue
            if (!command.name.equals(command.name, ignoreCase = true)) continue

            index(command, applicationCommand.applicationId, applicationCommand.serverId.orElse(null))
            break
        }
    }
}