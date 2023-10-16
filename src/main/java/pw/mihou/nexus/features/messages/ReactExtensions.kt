package pw.mihou.nexus.features.messages

import org.javacord.api.entity.message.Message
import org.javacord.api.event.message.MessageCreateEvent
import pw.mihou.nexus.features.command.react.React
import java.util.concurrent.CompletableFuture

/**
 * An experimental feature to use the new Nexus.R rendering mechanism to render Discord messages
 * with a syntax similar to a template engine that sports states (writable) that can easily update message
 * upon state changes.
 * @param react the entire procedure over how rendering the response works.
 */
@JvmSynthetic
fun MessageCreateEvent.R(react: React.() -> Unit): CompletableFuture<Message> {
    val r = React(this.api, React.RenderMode.Message)
    react(r)

    return r.messageBuilder!!.replyTo(message).send(channel)
}