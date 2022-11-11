package pw.mihou.nexus.configuration.modules

import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.messages.facade.NexusMessage

class NexusCommonsInterceptorsMessageConfiguration internal constructor() {

    @set:JvmName("setRatelimitedMessage")
    @get:JvmName("getRatelimitedMessage")
    @Volatile
    var ratelimited: (event: NexusCommandEvent, remainingSeconds: Long) -> NexusMessage = { _, remainingSeconds ->
        NexusMessage.fromEphemereal(
            "**SLOW DOWN***!"
                    + "\n"
                    + "You are executing commands too fast, please try again in $remainingSeconds seconds."
        )
    }

}