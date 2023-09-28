package pw.mihou.nexus.features.command.router.example

import org.javacord.api.interaction.SlashCommandInteractionOption
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.router.types.Routeable

object PongSubcommand: Routeable {
    override fun accept(event: NexusCommandEvent, option: SlashCommandInteractionOption) {
        val message = event["message", String::class.java]
        event.respondNowWith("Ping! $message")
    }
}