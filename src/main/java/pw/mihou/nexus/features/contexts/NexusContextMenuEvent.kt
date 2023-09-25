package pw.mihou.nexus.features.contexts

import org.javacord.api.event.interaction.ApplicationCommandEvent
import pw.mihou.nexus.features.commons.NexusInteractionEvent

class NexusContextMenuEvent<Event: ApplicationCommandEvent, Interaction: org.javacord.api.interaction.InteractionBase>(
    val contextMenu: NexusContextMenu,
    override val event: Event, override val interaction: Interaction
): NexusInteractionEvent<Event, Interaction>