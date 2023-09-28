package pw.mihou.nexus.features.command.validation.examples

import org.javacord.api.interaction.SlashCommandOption
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusHandler

object PingCommand: NexusHandler {
    val name = "ping"
    val description = "Does a ping-pong based on your answer."

    val options = NexusCommand.createOptions(
        SlashCommandOption.createStringOption("answer", "It must be either ping or pong!", true)
    )

    val validators = NexusCommand.createValidators(
        OptionValidators.PING_PONG_VALIDATOR.withCollector { event -> event.interaction.getArgumentStringValueByName("answer") }
    )

    override fun onEvent(event: NexusCommandEvent) {
        event.respondNowWith("Just kidding! Your answer didn't matter in the first place!")
    }
}