package pw.mihou.nexus.core.managers.indexes.defaults

import pw.mihou.nexus.core.managers.indexes.IndexStore
import pw.mihou.nexus.core.managers.records.NexusMetaIndex

class InMemoryIndexStore: IndexStore {

    private val indexes: MutableMap<Long, NexusMetaIndex> = mutableMapOf()

    override fun add(metaIndex: NexusMetaIndex) {
        indexes[metaIndex.applicationCommandId] = metaIndex
    }

    override operator fun get(applicationCommandId: Long): NexusMetaIndex? = indexes[applicationCommandId]
    override fun get(command: String, server: Long?): NexusMetaIndex? {
        return indexes.values.firstOrNull { it.command == command && it.server == server }
    }

    override fun many(vararg applicationCommandIds: Long): List<NexusMetaIndex> {
        return applicationCommandIds.map(indexes::get).filterNotNull().toList()
    }

    override fun many(server: Long?, vararg names: String): List<NexusMetaIndex> {
        return indexes.values.filter { it.server == server && names.contains(it.command) }.toList()
    }

    override fun addAll(metaIndexes: List<NexusMetaIndex>) {
        for (metaIndex in metaIndexes) {
            indexes[metaIndex.applicationCommandId] = metaIndex
        }
    }

    override fun all(): List<NexusMetaIndex> = indexes.values.toList()

    override fun clear() {
        indexes.clear()
    }
}