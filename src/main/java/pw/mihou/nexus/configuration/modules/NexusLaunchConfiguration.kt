package pw.mihou.nexus.configuration.modules

import java.util.concurrent.CompletableFuture

class NexusLaunchConfiguration internal constructor() {
    @JvmField var launcher: NexusLaunchWrapper = NexusLaunchWrapper { task -> CompletableFuture.runAsync { task.run() } }
}

fun interface NexusLaunchWrapper {
    fun launch(task: NexusLaunchTask)
}

fun interface NexusLaunchTask {
    fun run()
}