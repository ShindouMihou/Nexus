package pw.mihou.nexus.core.managers.records

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommand

data class NexusMetaIndex(val command: String, val applicationCommandId: Long, val server: Long?) {
    fun take(): NexusCommand? = Nexus.commandManager[command]
}
