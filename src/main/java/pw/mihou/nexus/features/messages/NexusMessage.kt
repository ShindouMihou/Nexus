package pw.mihou.nexus.features.messages

import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase

typealias NexusMessageBuilder = InteractionMessageBuilderBase<*>.() -> Unit

class NexusMessage internal constructor(
    val ephemeral: Boolean = false,
    val builder: NexusMessageBuilder = { }
) {
    companion object {
        @JvmStatic
        fun with(ephemeral: Boolean, builder: NexusMessageBuilder): NexusMessage {
            return NexusMessage(ephemeral, builder)
        }

        @JvmStatic
        fun with(builder: NexusMessageBuilder): NexusMessage {
            return with(false, builder)
        }

        @JvmStatic
        @JvmOverloads
        fun from(text: String, ephemeral: Boolean = false, builder: NexusMessageBuilder = {}): NexusMessage =
            with(ephemeral) {
                setContent(text)
                builder(this)
            }

        @JvmStatic
        @JvmOverloads
        fun from(ephemeral: Boolean = false, builder: NexusMessageBuilder = {}, vararg embeds: EmbedBuilder): NexusMessage =
            with(ephemeral) {
                addEmbeds(*embeds)
                builder(this)
            }
    }
    fun <T: InteractionMessageBuilderBase<*>> into(instance: T): T {
        builder(instance)
        return instance
    }
}