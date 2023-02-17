package pw.mihou.nexus.core.managers.indexes

import pw.mihou.nexus.core.managers.records.NexusMetaIndex
import pw.mihou.nexus.features.command.annotation.IdentifiableAs

interface IndexStore {

    /**
     * Adds the [NexusMetaIndex] into the store which can be retrieved later on by methods such as
     * [get] when needed.
     * @param metaIndex the index to add into the index store.
     */
    fun add(metaIndex: NexusMetaIndex)

    /**
     * Gets the [NexusMetaIndex] from the store or an in-memory cache by the application command identifier.
     * @param applicationCommandId the application command identifier from Discord's side.
     * @return the [NexusMetaIndex] that was caught otherwise none.
     */
    operator fun get(applicationCommandId: Long): NexusMetaIndex?

    /**
     * Gets the [NexusMetaIndex] that matches the given specifications.
     * @param command the command name.
     * @param server the server that this command belongs.
     * @return the [NexusMetaIndex] that matches.
     */
    operator fun get(command: String, server: Long?): NexusMetaIndex?

    /**
     * Gets one command mention tag from the [IndexStore].
     * @param server the server to fetch the commands from, if any.
     * @param command the names of the commands to fetch.
     * @param override overrides the name of the command, used to add subcommands and related.
     * @param default the default value to use when there is no command like that.
     * @return the mention tags of the command.
     */
    fun mention(server: Long?, command: String, override: String? = null, default: String): String {
        val index = get(command, server) ?: return default
        return "</${override ?: index.command}:${index.applicationCommandId}>"
    }

    /**
     * Gets many command mention tags from the [IndexStore].
     * @param server the server to fetch the commands from, if any.
     * @param names the names of the commands to fetch.
     * @return the mention tags of each commands.
     */
    fun mention(server: Long?, vararg names: String): Map<String, String> {
        val indexes = many(server, *names)
        val map = mutableMapOf<String, String>()
        for (index in indexes) {
            map[index.command] = "</${index.command}:${index.applicationCommandId}>"
        }
        return map
    }

    /**
     * Gets one or more [NexusMetaIndex] from the store.
     * @param applicationCommandIds the application command identifiers from Discord's side.
     * @return the [NexusMetaIndex]es that matches.
     */
    fun many(vararg applicationCommandIds: Long): List<NexusMetaIndex>

    /**
     * Gets one or more [NexusMetaIndex] from the store.
     * @param server the server that these commands belongs to.
     * @param names the names of the commands to fetch.
     * @return the [NexusMetaIndex]es that matches.
     */
    fun many(server: Long?, vararg names: String): List<NexusMetaIndex>

    /**
     * Adds one or more [NexusMetaIndex] into the store, this is used in scenarios such as mass-synchronization which
     * offers more than one indexes at the same time.
     *
     * @param metaIndexes the indexes to add into the store.
     */
    fun addAll(metaIndexes: List<NexusMetaIndex>)

    /**
     * Gets all the [NexusMetaIndex] available in the store, this is used more when the command manager's indexes are
     * exported somewhere.
     *
     * @return all the [NexusMetaIndex] known in the store.
     */
    fun all(): List<NexusMetaIndex>

    /**
     * Clears all the known indexes in the database. This happens when the command manager performs a re-indexing which
     * happens when the developer themselves has called for it.
     */
    fun clear()

}