package pw.mihou.nexus.features.contexts.facade

import org.javacord.api.event.interaction.ApplicationCommandEvent
import org.javacord.api.interaction.ApplicationCommandInteraction
import pw.mihou.nexus.features.contexts.NexusContextMenuEvent

interface NexusContextMenuHandler<Event: ApplicationCommandEvent, Interaction: ApplicationCommandInteraction> {
    fun onEvent(event: NexusContextMenuEvent<Event, Interaction>)
}