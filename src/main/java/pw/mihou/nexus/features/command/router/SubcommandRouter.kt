package pw.mihou.nexus.features.command.router

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.router.types.Routeable
import java.util.NoSuchElementException

class SubcommandRouter {

    private val routes: MutableMap<String, Routeable> = mutableMapOf()

    companion object {
        @JvmSynthetic
        fun create(modifier: RouteBuilder.() -> Unit): SubcommandRouter {
            val router = SubcommandRouter()
            modifier(RouteBuilder { name, route -> router.routes[name] = route })
            return router
        }

        fun create(vararg routes: Pair<String, Routeable>): SubcommandRouter {
            val router = SubcommandRouter()
            router.routes.putAll(routes)
            return router
        }
    }

    fun accept(event: NexusCommandEvent, modifier: NexusCommandEvent.() -> Unit = {}) {
        for ((key, value) in routes) {
            val option = event.interaction.getOptionByName(key).orElse(null) ?: continue
            modifier(event)
            value.accept(event, option)
            return
        }

        throw NoSuchElementException("No routes can be found for ${event.event.slashCommandInteraction.fullCommandName}.")
    }

    class RouteBuilder(private val modifier: (name: String, route: Routeable) -> Unit) {
        fun route(name: String, route: Routeable) = modifier(name, route)
    }
}