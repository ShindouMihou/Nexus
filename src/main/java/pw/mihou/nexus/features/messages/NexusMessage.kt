package pw.mihou.nexus.features.messages

import org.javacord.api.interaction.callback.InteractionMessageBuilderBase

class NexusMessage internal constructor(
    val ephemeral: Boolean = false,
    val builder: InteractionMessageBuilderBase<*>.() -> Unit = { }
) {
    companion object {
        fun with(ephemeral: Boolean, builder: InteractionMessageBuilderBase<*>.() -> Unit): NexusMessage {
            return NexusMessage(ephemeral, builder)
        }
    }
    fun <T: InteractionMessageBuilderBase<*>> into(instance: T): T {
        builder(instance)
        return instance
    }
}