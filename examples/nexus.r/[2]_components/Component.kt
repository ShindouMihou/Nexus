import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.messages.R
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.react.elements.Button
import pw.mihou.nexus.features.react.elements.Embed
import pw.mihou.nexus.features.react.writable.plusAssign
import java.time.Instant

// Creating components can be done by simply creating an extension function over [React.Component],
// note once more that since we don't have access to the `Nexus.R` scope, we need to pass states like
// a regular function. Read the `[1]_passing_state` example if you haven't.
fun React.Component.Example(clicks: React.Writable<Int>) {
    Embed {
        Title("Rendered with Nexus.R")
        SpacedBody(
            p("This message was rendered with Nexus.R."),
            p("The button has been clicked ") + bold("$clicks times.")
        )ge
        Color(java.awt.Color.YELLOW)
        Timestamp(Instant.now())
    }
    Button(label = "Click me!") {
        it.buttonInteraction.acknowledge()
        clicks += 1
    }
}

// Once you've created the extension function, just simply call it on the `render` method.
// It is noted that you can't just simply create new Writable inside `render` as it is recreated
// again upon re-rendering.
object ComponentMessageExample: MessageCreateListener {
    override fun onMessageCreate(event: MessageCreateEvent) {
        event.R {
            val clicksDelegate = writable(0)
            render {
                Example(clicksDelegate)
            }
        }
    }
}

// You can also do the same to slash commands, it doesn't matter as long as you have the parameters.
object ComponentSlashCommandExample: NexusHandler {
    val name = "test"
    val description = "An example of using components with Nexus.R"
    override fun onEvent(event: NexusCommandEvent) {
        event.R {
            val clicksDelegate = writable(0)
            render {
                Example(clicksDelegate)
            }
        }
    }
}