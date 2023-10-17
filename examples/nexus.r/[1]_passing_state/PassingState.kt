import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.react.elements.Button
import pw.mihou.nexus.features.react.elements.Embed
import java.time.Instant

/**
 * This example demonstrates how you pass [React.Writable] from the render function to another function.
 * Unlike JavaScript frameworks, this isn't easily done as Kotlin maintains an immutable-only function argument,
 * which means that we have to circumvent this by also maintaining the original [React.Writable] instance itself and
 * passing that instead.
 */
object PassingState: NexusHandler {
    val name = "react"
    val description = "Shows a demonstration of Nexus.R."
    override fun onEvent(event: NexusCommandEvent) {
        event.R {
            // As we need to maintain the original Writable instance, we need to separate it from the
            // delegated property, this property won't have the native setter and related.
            val clicksDelegate = writable(0)
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
                Button(label = "Click me!") {
                    it.buttonInteraction.acknowledge()
                    increment(clicksDelegate)
                }
                Button(label = "Click this!") {
                    it.buttonInteraction.acknowledge()
                    clicks += 1
                }
                Button(label = "Click also!") {
                    it.buttonInteraction.acknowledge()
                    incrementDelegated(clicksDelegate)
                }
            }
        }
    }

    /**
     * This demonstrates using the [React.Writable] methods to manipulate the value of the [React.Writable], thereby
     * causing a re-render. Generally, unless you perform a ton of manipulation, you can live with this.
     */
    private fun increment(clicks: React.Writable<Int>)  {
        clicks.getAndUpdate { it + 1 }
    }

    /**
     * This demonstrates how you can also still delegate inside the method as well to maintain
     * a similar feel to how a regular property would.
     */
    private fun incrementDelegated(clicksDelegate: React.Writable<Int>) {
        var clicks by clicksDelegate
        clicks += 1
    }
}