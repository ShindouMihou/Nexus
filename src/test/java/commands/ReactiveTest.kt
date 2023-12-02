package commands

import org.javacord.api.entity.message.component.ComponentType
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.react.elements.Button
import pw.mihou.nexus.features.react.elements.Embed
import pw.mihou.nexus.features.react.elements.SelectMenu

class ReactiveTest: NexusHandler {
    val name = "react"
    val description = "A test regarding React"
    override fun onEvent(event: NexusCommandEvent) {
        event.R {
            var clicks by writable(0)
            render {
                Embed {
                    Title("R.Embeds")
                    SpacedBody(
                        p("Hello World", bold = true, underline = true),
                        p("This is a little experiment over how this would look DX-wise. Discord message components that will also support states."),
                        bold("Rendered with Nexus."),
                        link("Test @ Nexus", "https://github.com/ShindouMihou/Nexus")
                    )
                }
                Button(label = "Click to be DM'd") { event ->
                    event.interaction.user.sendMessage("Hi")
                    clicks += 1
                }
                SelectMenu(
                    componentType = ComponentType.SELECT_MENU_STRING,
                    onSelect = {
                        it.selectMenuInteraction.acknowledge()
                        it.selectMenuInteraction.selectedChannels.forEach { channel  ->
                            println("Channel selected: ${channel.name}")
                        }
                    }
                ) {
                    ChannelType(org.javacord.api.entity.channel.ChannelType.SERVER_TEXT_CHANNEL)
                }
            }
        }
    }
}