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