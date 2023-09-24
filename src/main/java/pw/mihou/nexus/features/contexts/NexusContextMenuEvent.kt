package pw.mihou.nexus.features.contexts

import org.javacord.api.event.interaction.ApplicationCommandEvent
import org.javacord.api.interaction.ApplicationCommandInteraction
import pw.mihou.nexus.features.commons.NexusInteractionEvent

class NexusContextMenuEvent<Event: ApplicationCommandEvent, Interaction: ApplicationCommandInteraction>(
    val contextMenu: NexusContextMenu,
    override val event: Event
): NexusInteractionEvent<Event, Interaction>