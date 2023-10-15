package commands

import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler

class ReactiveTest: NexusHandler {
    val name = "react"
    val description = "A test regarding React"
    override fun onEvent(event: NexusCommandEvent) {
        event.R {
            render {
                Embed {
                    Title("R.Embeds")
                    Body(
                        p("Hello World", bold = true, underline = true),
                        br(),
                        p("This is a little experiment over how this would look DX-wise. Discord message components that will also support states."),
                        link("Test @ Nexus", "https://github.com/ShindouMihou/Nexus")
                    )
                }
                Button(label = "Click to be DM'd") { event ->
                    event.interaction.user.sendMessage("Hi")
                }
            }
        }
    }
}