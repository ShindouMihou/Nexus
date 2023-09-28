package pw.mihou.nexus.express.request

import org.javacord.api.DiscordApi

fun interface NexusExpressRequest {

    fun onEvent(shard: DiscordApi)

}