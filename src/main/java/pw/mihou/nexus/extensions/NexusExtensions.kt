package pw.mihou.nexus.extensions

import org.javacord.api.entity.message.embed.EmbedBuilder

/**
 * Builds an [EmbedBuilder] like a regular [EmbedBuilder] but with a fancier
 * developer experience.
 *
 * @param modifier the modifier to modify the [EmbedBuilder].
 * @return the [EmbedBuilder] that is being used.
 */
@JvmSynthetic
fun embed(modifier: EmbedBuilder.() -> Unit): EmbedBuilder {
    val embed = EmbedBuilder()
    modifier(embed)

    return embed
}