package pw.mihou.nexus.core.managers.facade

import org.javacord.api.interaction.ApplicationCommand
import pw.mihou.nexus.core.managers.records.NexusMetaIndex
import pw.mihou.nexus.features.command.facade.NexusCommand
import java.util.*
import java.util.function.Function

interface NexusCommandManager {
    /**
     * Gets all the commands that are stored inside the Nexus registry
     * of commands.
     *
     * @return All the commands stored in Nexus's command registry.
     */
    val commands: Collection<NexusCommand>

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

    fun add(command: NexusCommand): NexusCommandManager
    operator fun get(applicationId: Long): NexusCommand?
    operator fun get(uuid: String): NexusCommand?
    operator fun get(name: String, server: Long? = null): NexusCommand?

    /**
     * Exports the indexes that was created which can then be used to create a database copy of the given indexes.
     * <br></br><br></br>
     * It is not recommended to use this for any other purposes other than creating a database copy because this creates
     * more garbage for the garbage collector.
     *
     * @return A snapshot of the in-memory indexes that the command manager has.
     */
    fun export(): List<NexusMetaIndex>

    /**
     * Creates a new fresh in-memory index map by using the [NexusCommandManager.index] method before creating
     * an export of the given indexes with the [NexusCommandManager.export] command.
     *
     * @return A snapshot of the in-memory indexes that the command manager has.
     */
    fun indexThenExport(): List<NexusMetaIndex> {
        index()
        return export()
    }

    /**
     * This indexes all the commands whether it'd be global or server commands to increase
     * performance and precision of slash commands.
     */
    fun index()

    /**
     * Creates an in-memory index mapping of the given command and the given slash command snowflake.
     * <br></br><br></br>
     * You can use this method to index commands from your database.
     *
     * @param command   The command that will be associated with the given snowflake.
     * @param snowflake The snowflake that will be associated with the given command.
     * @param server    The server where this index should be associated with, can be null to mean global command.
     */
    fun index(command: NexusCommand, snowflake: Long, server: Long?)

    /**
     * Massively creates an in-memory index mapping for all the given command using the indexer reducer
     * provided below.
     * <br></br><br></br>
     * This is a completely synchronous operation and could cause major blocking on the application if you use
     * it daringly.
     *
     * @param indexer The indexer reducer to collectively find the index of the commands.
     */
    fun index(indexer: Function<NexusCommand, NexusMetaIndex>) {
        for (command in commands) {
            val metaIndex = indexer.apply(command)
            index(command, metaIndex.applicationCommandId, metaIndex.server)
        }
    }

    /**
     * Creates an in-memory index of all the slash commands provided. This will map all the commands based on properties
     * that matches e.g. the name (since a command can only have one global and one server command that have the same name)
     * and the server property if available.
     *
     * @param applicationCommandList the command list to use for indexing.
     */
    fun index(applicationCommandList: Set<ApplicationCommand>) {
        for (applicationCommand in applicationCommandList) {
            index(applicationCommand)
        }
    }

    /**
     * Creates an in-memory index of the given slash command provided. This will map all the command based on the property
     * that matches e.g. the name (since a command can only have one global and one server command that have the same name)
     * and the server property  if available.
     *
     * @param applicationCommand The command to index.
     */
    fun index(applicationCommand: ApplicationCommand)
}