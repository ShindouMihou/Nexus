package pw.mihou.nexus.configuration.modules

import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.messages.NexusMessage

class NexusCommonsInterceptorsMessageConfiguration internal constructor() {

    @set:JvmName("setRatelimitedMessage")
    @get:JvmName("getRatelimitedMessage")
    @Volatile
    var ratelimited: (event: NexusCommandEvent, remainingSeconds: Long) -> NexusMessage = { _, remainingSeconds ->
        NexusMessage.with(true) {
            this.setContent("***SLOW DOWN***!\nYou are executing commands too fast, please try again in $remainingSeconds seconds.")
        }
    }

}