import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.messages.R
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.react.ReactComponent
import pw.mihou.nexus.features.react.elements.Button
import pw.mihou.nexus.features.react.elements.Embed
import pw.mihou.nexus.features.react.hooks.useHideButtons
import pw.mihou.nexus.features.react.writable.plusAssign
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// In this example, we'll demonstrate how to create reusable hooks in Nexus.R. Hooks are simply
// just extension functions over [React] itself and isn't affected through re-renders as it lives
// under [React]. You can even use it to pass components, in this example, we'll demonstrate using it
// to pass components.

/**
 * In here, we create an extension function over [React] which returns a [Triple] that contains the `showButtons`, `clicks` and
 * the actual [ReactComponent] itself which will increment the clicks every time it is clicked.
 */
fun React.useClickButton(hideAfter: Duration = 10.minutes): Triple<React.Writable<Boolean>, React.Writable<Int>, ReactComponent> {
    val showButtons = writable(true)
    val clicks = writable(0)

    var removeButtons: Cancellable? = null

    onRender {
        removeButtons?.cancel(true)
        removeButtons = Nexus.launch.scheduler.launch(hideAfter.inWholeMilliseconds) {
            showButtons.set(false)
        }
    }

    return Triple(showButtons, clicks) {
        if (showButtons.get()) {
            Button(label = "Click me!") {
                it.buttonInteraction.acknowledge()
                clicks += 1
            }
        }
    }
}

object SlashCommand: NexusHandler {
    val name = "react"
    val description = "Shows a demonstration of Nexus.R."
    override fun onEvent(event: NexusCommandEvent) {
        event.R {
            val (showButtonsDelegate, clicksDelegate, ClickButton) = useClickButton(10.minutes)

            var showButtons by showButtonsDelegate
            var clicks by clicksDelegate

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
                ClickButton()
            }
        }
    }
}
