package pw.mihou.nexus.configuration.modules

import pw.mihou.nexus.core.threadpool.NexusThreadPool

class NexusLaunchConfiguration internal constructor() {
    @JvmField var launcher: NexusLaunchWrapper = NexusLaunchWrapper { task ->
        NexusThreadPool.executorService.submit { task.run() }
    }
}

fun interface NexusLaunchWrapper {
    fun launch(task: NexusLaunchTask)
}

fun interface NexusLaunchTask {
    fun run()
}