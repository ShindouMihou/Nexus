import org.javacord.api.event.interaction.MessageContextMenuCommandEvent
import org.javacord.api.interaction.MessageContextMenuInteraction
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.contexts.NexusContextMenuEvent
import pw.mihou.nexus.features.contexts.NexusMessageContextMenu
import pw.mihou.nexus.features.contexts.NexusUserContextMenu
import pw.mihou.nexus.features.messages.R
import pw.mihou.nexus.features.react.elements.Button
import pw.mihou.nexus.features.react.elements.Embed
import java.time.Instant

/**
 * Nexus.R supports basically everything, you can copy-paste the code from one event to another and it'll work
 * the same regardless without much if any modification. In this example, we demonstrate how we can use Nexus.R
 * to build amazingly simple yet reactive component.
 *
 * Our example will render a simple message with a button that increments the click count and re-render the
 * message whenever a click is registered.
 */
object ContextMenu: NexusMessageContextMenu() {
    val name = "react"
    override fun onEvent(event: NexusContextMenuEvent<MessageContextMenuCommandEvent, MessageContextMenuInteraction>) {
        event.R {
            var clicks by writable(0)
            render {
                Embed {
                    Title("Rendered with Nexus.R")
                    SpacedBody(
                        p("This message was rendered with Nexus.R."),
                        p("The button has been clicked ") + bold("$clicks times.")
                    )
                    Color(java.awt.Color.YELLOW)
                    Timestamp(Instant.now())
                }
                Button(label = "Click me!") {
                    it.buttonInteraction.acknowledge()
                    clicks += 1
                }
            }
        }
    }
}