package pw.mihou.nexus.core.managers.core

import org.javacord.api.event.interaction.MessageContextMenuCommandEvent
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.event.interaction.UserContextMenuCommandEvent
import org.javacord.api.interaction.ApplicationCommand
import org.javacord.api.interaction.ApplicationCommandInteraction
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.info
import pw.mihou.nexus.features.commons.NexusApplicationCommand
import pw.mihou.nexus.core.managers.facade.NexusCommandManager
import pw.mihou.nexus.core.managers.indexes.IndexStore
import pw.mihou.nexus.core.managers.indexes.defaults.InMemoryIndexStore
import pw.mihou.nexus.core.managers.indexes.exceptions.IndexIdentifierConflictException
import pw.mihou.nexus.core.managers.records.NexusMetaIndex
import pw.mihou.nexus.features.command.core.NexusCommandCore
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.contexts.enums.ContextMenuKinds
import pw.mihou.nexus.features.contexts.NexusContextMenu
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class NexusCommandManagerCore internal constructor() : NexusCommandManager  {
    private val commandsDelegate: MutableMap<String, NexusCommand> = HashMap()
    private val contextMenusDelegate: MutableMap<String, NexusContextMenu> = HashMap()

    override val contextMenus: Collection<NexusContextMenu>
        get() = contextMenusDelegate.values

    override val commands: Collection<NexusCommand>
        get() = commandsDelegate.values

    override var indexStore: IndexStore = InMemoryIndexStore()

    override fun add(command: NexusCommand): NexusCommandManager {
        if (commandsDelegate[command.uuid] != null)
            throw IndexIdentifierConflictException(command.name)

        commandsDelegate[(command as NexusCommandCore).uuid] = command
        return this
    }

    override fun add(contextMenu: NexusContextMenu): NexusCommandManager {
        if (contextMenusDelegate[contextMenu.uuid] != null)
            throw IndexIdentifierConflictException(contextMenu.name)

        contextMenusDelegate[contextMenu.uuid] = contextMenu
        return this
    }

    override operator fun get(applicationId: Long): NexusCommand? = indexStore[applicationId]?.takeCommand()
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

    override fun getContextMenu(applicationId: Long): NexusContextMenu? {
        return indexStore[applicationId]?.takeContextMenu()
    }

    override fun getContextMenu(uuid: String): NexusContextMenu? {
        return contextMenusDelegate[uuid]
    }

    override fun getContextMenu(name: String, kind: ContextMenuKinds, server: Long?): NexusContextMenu? {
        return contextMenus.firstOrNull { contextMenu ->
            when(server) {
                null -> {
                    contextMenu.name.equals(name, ignoreCase = true) && contextMenu.kind == kind
                }
                else -> {
                    contextMenu.name.equals(name, ignoreCase = true) && contextMenu.serverIds.contains(server) && contextMenu.kind == kind
                }
            }
        }
    }

    override fun export(): List<NexusMetaIndex> {
        return indexStore.all()
    }

    private fun toIndex(applicationCommandId: Long, command: String, server: Long?): NexusMetaIndex {
        return NexusMetaIndex(command = command, applicationCommandId = applicationCommandId, server = server)
    }

    fun acceptEvent(event: SlashCommandCreateEvent): NexusCommand? {
        val interaction = event.slashCommandInteraction

        val indexedCommand = get(interaction.commandId)
        if (indexedCommand != null) {
            return indexedCommand
        }

        return if (interaction.server.isPresent) {
            val server = interaction.server.get().id

            return when (val command = get(interaction.commandName, server)) {
                null -> index(interaction, get(interaction.commandName, null), null)
                else -> index(interaction, command, server)
            }

        } else index(interaction, get(interaction.commandName, null), null)
    }

    fun acceptEvent(event: UserContextMenuCommandEvent): NexusContextMenu? {
        return acceptContextMenuEvent(ContextMenuKinds.USER, event.userContextMenuInteraction)
    }

    fun acceptEvent(event: MessageContextMenuCommandEvent): NexusContextMenu? {
        return acceptContextMenuEvent(ContextMenuKinds.MESSAGE, event.messageContextMenuInteraction)
    }

    private fun acceptContextMenuEvent(kind: ContextMenuKinds, interaction: ApplicationCommandInteraction): NexusContextMenu? {
        val indexedContextMenu  = getContextMenu(interaction.commandId)
        if (indexedContextMenu != null) {
            return indexedContextMenu
        }

        return if (interaction.server.isPresent) {
            val server = interaction.server.get().id

            return when (val contextMenu = getContextMenu(interaction.commandName, kind, server)) {
                null -> index(interaction, getContextMenu(interaction.commandName, kind, null), null)
                else -> index(interaction, contextMenu, server)
            }
        } else index(interaction, getContextMenu(interaction.commandName, kind, null), null)
    }

    private fun <Command: NexusApplicationCommand> index(interaction: ApplicationCommandInteraction, uuid: Command?, server: Long?): Command? {
        return uuid?.apply { indexStore.add(toIndex(interaction.commandId, this.uuid, server)) }
    }

    override fun index() {
        Nexus.configuration.loggingTemplates.INDEXING_COMMANDS.info()

        val start = System.currentTimeMillis()
        Nexus.express
            .awaitAvailable()
            .thenAcceptAsync { shard ->
                val applicationCommands  = shard.globalApplicationCommands.join()
                indexStore.clear()
                for (applicationCommand in applicationCommands) {
                    index(applicationCommand)
                }

                val servers: MutableSet<Long> = HashSet()
                for (serverCommand in serverCommands) {
                    servers.addAll(serverCommand.serverIds)
                }
                for (serverContextMenu in serverContextMenus) {
                    servers.addAll(serverContextMenu.serverIds)
                }

                for (server in servers) {
                    if (server == 0L) continue
                    val applicationCommandSet: Set<ApplicationCommand>  = Nexus.express
                        .await(server)
                        .thenComposeAsync { it.api.getServerApplicationCommands(it) }
                        .join()

                    for (applicationCommand in applicationCommandSet) {
                        index(applicationCommand)
                    }
                }

                Nexus.configuration.loggingTemplates.COMMANDS_INDXED(System.currentTimeMillis() - start).info()
            }
            .exceptionally(ExceptionLogger.get())
            .join()
    }

    private fun <Command: NexusApplicationCommand> manifest(command: Command, snowflake: Long, server: Long?) =
        toIndex(applicationCommandId = snowflake, command = command.uuid, server = server)

    override fun <Command: NexusApplicationCommand> index(command: Command, snowflake: Long, server: Long?) {
        indexStore.add(toIndex(applicationCommandId = snowflake, command = command.uuid, server = server))
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