package pw.mihou.nexus.configuration.modules

import pw.mihou.nexus.core.threadpool.NexusThreadPool
import java.util.concurrent.TimeUnit

class NexusLaunchConfiguration internal constructor() {
    @JvmField var launcher: NexusLaunchWrapper = NexusLaunchWrapper { task ->
        NexusThreadPool.executorService.submit { task.run() }
    }
    @JvmField var scheduler: NexusScheduledLaunchWrapper = NexusScheduledLaunchWrapper { timeInMillis, task ->
        return@NexusScheduledLaunchWrapper object: Cancellable {
            val scheduledTask = NexusThreadPool.scheduledExecutorService.schedule(task::run, timeInMillis, TimeUnit.MILLISECONDS)
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                return scheduledTask.cancel(mayInterruptIfRunning)
            }
        }
    }
}

fun interface NexusLaunchWrapper {
    fun launch(task: NexusLaunchTask)
}

fun interface NexusScheduledLaunchWrapper {
    fun launch(timeInMillis: Long, task: NexusLaunchTask): Cancellable
}

interface Cancellable {
    fun cancel(mayInterruptIfRunning: Boolean): Boolean
}

fun interface NexusLaunchTask {
    fun run()
}