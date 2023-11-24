package pw.mihou.nexus.features.react.elements

import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.entity.message.component.ComponentType
import org.javacord.api.entity.message.component.SelectMenuBuilder
import org.javacord.api.entity.message.component.SelectMenuOption
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder
import org.javacord.api.event.interaction.SelectMenuChooseEvent
import org.javacord.api.listener.interaction.SelectMenuChooseListener
import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.features.react.React

fun React.Component.SelectMenu(
    componentType: ComponentType,
    customId: String = NexusUuidAssigner.request(),
    minimumValues: Int = 1,
    maximumValues: Int = 1,
    disabled: Boolean = false,
    onSelect: ((event: SelectMenuChooseEvent) -> Unit)? = null,
    selectMenu: SelectMenu.() -> Unit
) {
    val element = SelectMenu(
        SelectMenuBuilder(componentType, customId)
            .setDisabled(disabled)
            .setMaximumValues(maximumValues)
            .setMinimumValues(minimumValues))
    selectMenu(element)

    if (onSelect != null) {
        listeners += SelectMenuChooseListener {
            if (it.selectMenuInteraction.customId != customId) {
                return@SelectMenuChooseListener
            }

            onSelect(it)
        }
    }

    components += element.selectMenu.build()
}

class SelectMenu(internal val selectMenu: SelectMenuBuilder) {
    fun ChannelType(type: ChannelType) {
        selectMenu.addChannelType(type)
    }

    fun Option(option: SelectMenuOption) {
        selectMenu.addOption(option)
    }

    fun Option(builder: SelectMenuOptionBuilder.() -> Unit) {
        val original = SelectMenuOptionBuilder()
        builder(original)
        selectMenu.addOption(original.build())
    }

    fun Placeholder(text: String) {
        selectMenu.setPlaceholder(text)
    }
}