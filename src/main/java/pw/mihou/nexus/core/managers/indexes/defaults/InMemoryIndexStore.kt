package pw.mihou.nexus.core.managers.indexes.defaults

import pw.mihou.nexus.core.managers.indexes.IndexStore
import pw.mihou.nexus.core.managers.records.NexusMetaIndex

class InMemoryIndexStore: IndexStore {

    private val indexes: MutableMap<Long, NexusMetaIndex> = mutableMapOf()

    override fun add(metaIndex: NexusMetaIndex) {
        indexes[metaIndex.applicationCommandId] = metaIndex
    }

    override operator fun get(applicationCommandId: Long): NexusMetaIndex? = indexes[applicationCommandId]

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