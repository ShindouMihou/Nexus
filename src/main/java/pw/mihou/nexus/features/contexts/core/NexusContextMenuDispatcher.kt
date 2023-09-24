package pw.mihou.nexus.features.contexts.core

import org.javacord.api.event.interaction.MessageContextMenuCommandEvent
import org.javacord.api.event.interaction.UserContextMenuCommandEvent
import org.javacord.api.interaction.MessageContextMenuInteraction
import org.javacord.api.interaction.UserContextMenuInteraction
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.contexts.NexusContextMenu
import pw.mihou.nexus.features.contexts.NexusContextMenuEvent
import pw.mihou.nexus.features.contexts.facade.NexusContextMenuHandler

object NexusContextMenuDispatcher {
    fun dispatch(event: UserContextMenuCommandEvent, contextMenu: NexusContextMenu) {
        Nexus.launcher.launch  {
            try {
                val contextMenuEvent = NexusContextMenuEvent<UserContextMenuCommandEvent, UserContextMenuInteraction>(contextMenu, event)
                @Suppress("UNCHECKED_CAST")
                (contextMenu.handler as? NexusContextMenuHandler<UserContextMenuCommandEvent, UserContextMenuInteraction>)?.onEvent(contextMenuEvent)
                    ?: throw IllegalStateException("Received user context menu event for a non-user-context menu handler.")
            } catch (throwable: Throwable) {
                Nexus.logger.error("An uncaught exception was received by Nexus User Context Menu Dispatcher for the " +
                        "command ${contextMenu.name} with the following stacktrace."
                )
                throwable.printStackTrace()
            }
        }
    }

    fun dispatch(event: MessageContextMenuCommandEvent, contextMenu: NexusContextMenu) {
        Nexus.launcher.launch  {
            try {
                val contextMenuEvent = NexusContextMenuEvent<MessageContextMenuCommandEvent, MessageContextMenuInteraction>(contextMenu, event)
                @Suppress("UNCHECKED_CAST")
                (contextMenu.handler as? NexusContextMenuHandler<MessageContextMenuCommandEvent, MessageContextMenuInteraction>)?.onEvent(contextMenuEvent)
                    ?: throw IllegalStateException("Received message context menu event for a non-message-context menu handler.")
            } catch (throwable: Throwable) {
                Nexus.logger.error("An uncaught exception was received by Nexus Message Context Menu Dispatcher for the " +
                        "command ${contextMenu.name} with the following stacktrace."
                )
                throwable.printStackTrace()
            }
        }
    }
}