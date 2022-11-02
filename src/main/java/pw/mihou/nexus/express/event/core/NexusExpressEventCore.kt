package pw.mihou.nexus.express.event.core

import org.javacord.api.DiscordApi
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.express.event.NexusExpressEvent
import pw.mihou.nexus.express.event.listeners.NexusExpressEventStatusChange
import pw.mihou.nexus.express.event.status.NexusExpressEventStatus
import pw.mihou.nexus.express.request.NexusExpressRequest

internal class NexusExpressEventCore(val request: NexusExpressRequest): NexusExpressEvent {

    @Volatile private var status = NexusExpressEventStatus.WAITING
    private val listeners = mutableListOf<NexusExpressEventStatusChange>()

    override fun cancel() {
        synchronized(this) {
            if (status == NexusExpressEventStatus.WAITING) {
                change(status = NexusExpressEventStatus.STOPPED)
            }
        }
    }

    fun expire() {
        synchronized (this) {
            if (status == NexusExpressEventStatus.WAITING) {
                change(status = NexusExpressEventStatus.WAITING)
            }
        }
    }

    fun `do`(task: NexusExpressEventCore.() -> Unit) {
        synchronized(this) {
            task()
        }
    }

    fun process(shard: DiscordApi) {
        synchronized(this) {
            try {
                if (status == NexusExpressEventStatus.STOPPED || status == NexusExpressEventStatus.EXPIRED) {
                    return@synchronized
                }

                change(status = NexusExpressEventStatus.PROCESSING)
                request.onEvent(shard)
            } catch (exception: Exception) {
                Nexus.configuration.global.logger.error("An uncaught exception was caught by Nexus Express Way.", exception)
            } finally {
                change(status = NexusExpressEventStatus.FINISHED)
            }
        }
    }

    private fun change(status: NexusExpressEventStatus) {
        synchronized(this) {
            val oldStatus = this.status
            this.status = status

            listeners.forEach { listener -> listener.onStatusChange(this, oldStatus, status) }
        }
    }

    override fun status(): NexusExpressEventStatus {
        synchronized(this) {
            return status
        }
    }

    override fun addStatusChangeListener(event: NexusExpressEventStatusChange) {
        synchronized(this) {
            listeners.add(event)
        }
    }
}