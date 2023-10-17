import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.react.elements.Embed
import java.time.Instant

// In this example, we'll demonstrate the different ways we can do data fetching via `onRender` and `onInitialRender`.
// Nexus.R uses `autoDefer` underneath, which means that you don't have to worry about calling `respondLater` or any
// of those mess, just take your time in grabbing that fresh data!
object DataFetching: NexusHandler {
    val name = "react"
    val description = "Shows a demonstration of Nexus.R."
    override fun onEvent(event: NexusCommandEvent) {
        event.R {
            lateinit var qotd: String

            // event.R itself is actually a function, so you can initialize stuff here.
            // but for demonstration purposes, we'll use `onInitialRender`, but just so you know,
            // this is a 100% valid thing to do.
            //
            // qotd = "Lorem Ipsum Somethin' Magical!"

            onInitialRender {
                // This is called way before `onRender` and `render` and can be where you can initialize
                // data for the first time on render, although this is just the equivalent of doing it on `event.R` scope
                // itself, but for people who prefer something like this, you can do this.

                // Imagine this is a data fetching thing.
                qotd = "Lorem Ipsum Somethin' Magical!"
            }
            onRender {
                // Anything that you want to do before the actual `render` is called.
                // This is called again for re-rendering.
            }
            render {
                Embed {
                    Title("Rendered with Nexus.R")
                    SpacedBody(
                        p("QOTD: $qotd"),
                    )
                    Color(java.awt.Color.YELLOW)
                    Timestamp(Instant.now())
                }
            }
        }
    }
}