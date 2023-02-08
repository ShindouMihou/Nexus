package pw.mihou.nexus.features.command.router.example

import org.javacord.api.interaction.SlashCommandOption
import org.javacord.api.interaction.SlashCommandOptionType
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.command.router.SubcommandRouter

object PingCommand: NexusHandler {
    private val name = "ping"
    private val description = "Pings and pongs!"

    private val options = listOf(
        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "pong", "Performs a pong!"),
        SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "ping", "Performs a ping!")
    )

    private val router = SubcommandRouter.create {
        route("ping", PingSubcommand)
        route("pong", PongSubcommand)
    }

    override fun onEvent(event: NexusCommandEvent) = router.accept(event) {
        // Optionally you can add additional shared data such as this:
        event.store("message", "Ping pong! You can add stuff like this here!")
    }
}