package pw.mihou.nexus.features.command.validation.errors

import org.javacord.api.entity.message.MessageFlag
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.message.mention.AllowedMentions
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase

interface ValidationError {
    fun convertTo(instance: InteractionMessageBuilderBase<*>)

    companion object {
        /**
         * A text template is used to define the standard template that will be used to send to the client.
         * You can use this to prepend or append different details such as the X emoji or related without repeating
         * the emoji.
         */
        @JvmStatic
        var textTemplate: (message: String) -> String = { message -> message }

        @JvmStatic
        fun create(embed: EmbedBuilder, modifier: (ValidationEmbedError.() -> Unit) = {}): ValidationEmbedError {
            val validationError = ValidationEmbedError(embed)
            modifier(validationError)

            return validationError
        }

        @JvmStatic
        fun create(contents: String, modifier: (ValidationStringError.() -> Unit) = {}): ValidationStringError {
            val validationError = ValidationStringError(textTemplate(contents))
            modifier(validationError)

            return validationError
        }
    }
}

class ValidationEmbedError(@SuppressWarnings("WeakerAccess") val embed: EmbedBuilder): ValidationError {
    var ephemeral = true
    var allowedMentions: AllowedMentions? = null

    override fun convertTo(instance: InteractionMessageBuilderBase<*>) {
        instance.addEmbed(embed)
        if (ephemeral) instance.setFlags(MessageFlag.EPHEMERAL)
        if (allowedMentions != null) instance.setAllowedMentions(allowedMentions)
    }
}

class ValidationStringError(@SuppressWarnings("WeakerAccess") val contents: String): ValidationError {
    var ephemeral = true
    var allowedMentions: AllowedMentions? = null

    override fun convertTo(instance: InteractionMessageBuilderBase<*>) {
        instance.setContent(contents)
        if (ephemeral) instance.setFlags(MessageFlag.EPHEMERAL)
        if (allowedMentions != null) instance.setAllowedMentions(allowedMentions)
    }
}