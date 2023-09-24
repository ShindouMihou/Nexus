package pw.mihou.nexus.core.managers.records

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.contexts.NexusContextMenu

data class NexusMetaIndex(val command: String, val applicationCommandId: Long, val server: Long?) {
    fun takeCommand(): NexusCommand? = Nexus.commandManager[command]
    fun takeContextMenu(): NexusContextMenu? = Nexus.commandManager.getContextMenu(command)
}
