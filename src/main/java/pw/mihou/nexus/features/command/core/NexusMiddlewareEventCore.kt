package pw.mihou.nexus.features.command.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.command.responses.NexusAutoResponse
import pw.mihou.nexus.features.messages.NexusMessage
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class NexusMiddlewareEventCore(private val _event: NexusCommandEvent, private val gate: NexusMiddlewareGateCore): NexusMiddlewareEvent {
    override val event: SlashCommandCreateEvent get() = _event.event
    override val command: NexusCommand get() = _event.command
    override fun R(ephemeral: Boolean, lifetime: Duration = 1.hours, react: React.() -> Unit): CompletableFuture<NexusAutoResponse> {
        return _event.R(ephemeral, lifetime, react)
    }

    override fun store(): MutableMap<String, Any> = _event.store()
    override fun autoDefer(ephemeral: Boolean, response: Function<Void?, NexusMessage>): CompletableFuture<NexusAutoResponse> {
        return _event.autoDefer(ephemeral, response)
    }

    override fun next() {
        gate.next()
    }

    override fun stop(response: NexusMessage?) {
        gate.stop(response)
    }
}