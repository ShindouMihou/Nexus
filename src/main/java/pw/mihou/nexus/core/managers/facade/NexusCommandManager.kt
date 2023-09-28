package pw.mihou.nexus.core.managers.facade

import org.javacord.api.interaction.ApplicationCommand
import pw.mihou.nexus.core.managers.indexes.IndexStore
import pw.mihou.nexus.core.managers.records.NexusMetaIndex
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.commons.NexusApplicationCommand
import pw.mihou.nexus.features.contexts.enums.ContextMenuKinds
import pw.mihou.nexus.features.contexts.NexusContextMenu
import kotlin.collections.HashSet

interface NexusCommandManager {
    /**
     * Gets all the commands that are stored inside the Nexus registry
     * of commands.
     *
     * @return All the commands stored in Nexus's command registry.
     */
    val commands: Collection<NexusCommand>

    /**
     * Gets all the context menus that are stored inside the Nexus registry.
     */
    val contextMenus: Collection<NexusContextMenu>

    /**
     * An index store is a store that is being utilized to store [NexusMetaIndex] that allows Nexus to match commands faster
     * than with regular O(N) methods. Nexus will auto-index all commands that do not have an index already, therefore, there is not
     * much needed to change.
     *
     * You can, however, change this to a persistent in-memory store that Nexus will use (be sure to use some in-memory caching
     * for performance reasons).
     */
    var indexStore: IndexStore

    /**
     * Gets all the global commands that are stored inside the Nexus registry of commands.
     * <br></br><br></br>
     * In this scenario, the definition of a global command is a command that does not have an association
     * with a server.
     *
     * @return All the global commands that were created inside the registry.
     */
    val globalCommands: Set<NexusCommand>
        get() {
            val commands: MutableSet<NexusCommand> = HashSet()
            
            for (command in this.commands) {
                if (command.isServerCommand) continue
                commands.add(command)
            }
            
            return commands
        }

    /**
     * Gets all the global context menus that are stored inside the Nexus registry.
     */
    val globalContextMenus: Set<NexusContextMenu>
        get() {
            val contextMenus: MutableSet<NexusContextMenu> = HashSet()
            for (contextMenu in this.contextMenus) {
                if (contextMenu.isServerOnly) continue
                contextMenus.add(contextMenu)
            }
            return contextMenus
        }

    /**
     * Gets all the server commands that are stored inside the Nexus registry of commands.
     * <br></br><br></br>
     * In this scenario, the definition of a server command is a command that does have an association
     * with a server.
     *
     * @return All the server commands that were created inside the registry.
     */
    val serverCommands: Set<NexusCommand>
        get() {
            val commands: MutableSet<NexusCommand> = HashSet()
            
            for (command in this.commands) {
                if (!command.isServerCommand) continue
                commands.add(command)
            }
            
            return commands
        }

    /**
     * Gets all the server-locked context menus that are stored inside the Nexus registry.
     */
    val serverContextMenus: Set<NexusContextMenu>
        get() {
            val contextMenus: MutableSet<NexusContextMenu> = HashSet()
            for (contextMenu in this.contextMenus) {
                if (!contextMenu.isServerOnly) continue
                contextMenus.add(contextMenu)
            }
            return contextMenus
        }

    /**
     * Gets all the commands that have an association with the given server.
     * <br></br><br></br>
     * This method does a complete O(n) loop over the commands to identify any commands that matches the
     * [List.contains] predicate over its server ids.
     *
     * @param server The server to find all associated commands of.
     * @return All associated commands of the given server.
     */
    fun commandsAssociatedWith(server: Long): Set<NexusCommand> {
        val commands: MutableSet<NexusCommand> = HashSet()
        
        for (command in this.commands) {
            if (!command.serverIds.contains(server)) continue
            commands.add(command)
        }
        
        return commands
    }

    /**
     * Gets all the context menus that have an association with the given server.
     * <br></br><br></br>
     * This method does a complete O(n) loop over the context menus to identify any context menus that matches the
     * [List.contains] predicate over its server ids.
     *
     * @param server The server to find all associated context menus of.
     * @return All associated context menus of the given server.
     */
    fun contextMenusAssociatedWith(server: Long): Set<NexusContextMenu> {
        val contextMenus: MutableSet<NexusContextMenu> = HashSet()
        for (contextMenu in this.contextMenus) {
            if (!contextMenu.serverIds.contains(server)) continue
            contextMenus.add(contextMenu)
        }
        return contextMenus
    }

    fun add(command: NexusCommand): NexusCommandManager
    fun add(contextMenu: NexusContextMenu): NexusCommandManager

    operator fun get(applicationId: Long): NexusCommand?
    operator fun get(uuid: String): NexusCommand?
    operator fun get(name: String, server: Long? = null): NexusCommand?

    fun getContextMenu(applicationId: Long): NexusContextMenu?
    fun getContextMenu(uuid: String): NexusContextMenu?
    fun getContextMenu(name: String, kind: ContextMenuKinds, server: Long? = null): NexusContextMenu?

    /**
     * Exports the indexes that was created which can then be used to create a database copy of the given indexes.
     * <br></br><br></br>
     * It is not recommended to use this for any other purposes other than creating a database copy because this creates
     * more garbage for the garbage collector.
     *
     * @return A snapshot of the indexes that the command manager has.
     */
    fun export(): List<NexusMetaIndex>

    /**
     * This indexes all the commands whether it'd be global or server commands to increase
     * performance and precision of slash commands.
     */
    fun index()

    /**
     * Creates an index mapping of the given command and the given slash command snowflake.
     * <br></br><br></br>
     * You can use this method to index commands from your database.
     *
     * @param command   The command that will be associated with the given snowflake.
     * @param snowflake The snowflake that will be associated with the given command.
     * @param server    The server where this index should be associated with, can be null to mean global command.
     */
    fun <Command: NexusApplicationCommand> index(command: Command, snowflake: Long, server: Long?)

    /**
     * Creates an index of all the slash commands provided. This will map all the commands based on properties
     * that matches e.g. the name (since a command can only have one global and one server command that have the same name)
     * and the server property if available.
     *
     * @param applicationCommandList the command list to use for indexing.
     */
    fun index(applicationCommandList: Set<ApplicationCommand>)

    /**
     * Creates an index of the given slash command provided. This will map all the command based on the property
     * that matches e.g. the name (since a command can only have one global and one server command that have the same name)
     * and the server property  if available.
     *
     * @param applicationCommand The command to index.
     */
    fun index(applicationCommand: ApplicationCommand)

    fun mentionMany(server: Long?, vararg commands: String) =
        indexStore.mentionMany(server, *commands)
    fun mentionOne(server: Long?, command: String, override: String? = null, default: String) =
        indexStore.mentionOne(server, command, override, default)
}