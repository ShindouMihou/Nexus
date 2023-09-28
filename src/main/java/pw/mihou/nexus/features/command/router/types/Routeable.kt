package pw.mihou.nexus.features.command.router.types

import org.javacord.api.interaction.SlashCommandInteractionOption
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import java.util.function.Consumer

interface Routeable {
    fun accept(event: NexusCommandEvent, option: SlashCommandInteractionOption)
    fun subcommand(name: String, from: SlashCommandInteractionOption, then: Consumer<SlashCommandInteractionOption>) {
        val subcommand = from.getOptionByName(name).orElse(null) ?: return
        then.accept(subcommand)
    }
}