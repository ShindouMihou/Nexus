package pw.mihou.nexus.features.react.elements

import org.javacord.api.entity.message.component.ButtonBuilder
import org.javacord.api.entity.message.component.ButtonStyle
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.listener.interaction.ButtonClickListener
import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.features.react.React

fun React.Component.Button(
    style: ButtonStyle = ButtonStyle.PRIMARY,
    label: String,
    customId: String? = null,
    emoji: String? = null,
    disabled: Boolean = false,
    onClick: ((event: ButtonClickEvent) -> Unit)? = null
) {
    val button = ButtonBuilder()
    button.setStyle(style)
    button.setLabel(label)

    if (emoji != null) {
        button.setEmoji(emoji)
    }

    button.setDisabled(disabled)

    val uuid = customId ?: run {
        val id = NexusUuidAssigner.request()
        uuids.add(id)
        return@run id
    }
    button.setCustomId(uuid)

    if (onClick != null) {
        listeners += ButtonClickListener {
            if (it.buttonInteraction.customId != uuid) {
                return@ButtonClickListener
            }

            onClick(it)
        }
    }

    components += button.build()
}