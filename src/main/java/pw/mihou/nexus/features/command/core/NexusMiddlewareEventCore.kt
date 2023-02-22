package pw.mihou.nexus.features.command.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore

class NexusMiddlewareEventCore(val event: NexusCommandEvent, private val gate: NexusMiddlewareGateCore): NexusMiddlewareEvent {
    override fun getBaseEvent(): SlashCommandCreateEvent {
        return event.baseEvent
    }

    override fun getCommand(): NexusCommand {
        return event.command
    }

    override fun store(): MutableMap<String, Any> {
        return event.store()
    }
}