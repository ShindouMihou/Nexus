package pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.core

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.features.command.core.NexusCommandCore
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.facade.NexusRatelimitData
import pw.mihou.nexus.features.command.interceptors.commons.modules.ratelimiter.facade.NexusRatelimiter
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrDefault

internal class NexusRatelimiterCore : NexusRatelimiter {

    private val ratelimits: MutableMap<Entity, NexusRatelimitData?> = ConcurrentHashMap()

    /**
     * Ratelimits a user on a specific server, or on their private channel for a
     * specific command.
     *
     * @param user      The user to rate-limit.
     * @param server    The server or private channel to rate-limit on.
     * @param command   The command to rate-limit on.
     * @return          The results from the rate-limit attempt.
     */
    private fun ratelimit(user: Long, server: Long, command: NexusCommandCore): AccessorRatelimitData {
        val key = Entity(command.uuid, user)

        if (!ratelimits.containsKey(key)) {
            ratelimits[key] = NexusRatelimitDataCore(user)
        }

        val entity = ratelimits[key] as NexusRatelimitDataCore?
        if (entity!!.isRatelimitedOn(server)) {
            if (getRemainingSecondsFrom(server, entity, command) > 0) {
                if (!entity.isNotifiedOn(server)) {
                    entity.notified(server)
                    return AccessorRatelimitData(
                        notified = false,
                        ratelimited = true,
                        remaining = getRemainingSecondsFrom(server, entity, command)
                    )
                }
                return AccessorRatelimitData(
                    notified = true,
                    ratelimited = true,
                    remaining = getRemainingSecondsFrom(server, entity, command)
                )
            }

            entity.release(server)
        }
        entity.ratelimit(server)
        return AccessorRatelimitData(
            notified = false,
            ratelimited = false,
            remaining = -1
        )
    }

    /**
     * Gets the remaining seconds from the data provided.
     *
     * @param server The ID of the server.
     * @param dataCore The rate-limit core data.
     * @param commandCore The Nexus Command Core data.
     * @return The remaining seconds before the rate-limit should be released.
     */
    private fun getRemainingSecondsFrom(
        server: Long,
        dataCore: NexusRatelimitDataCore?,
        commandCore: NexusCommandCore
    ): Long {
        return TimeUnit.MILLISECONDS.toSeconds(
            dataCore!!.getRatelimitedTimestampInMillisOn(server)
                    +
                    commandCore.get("cooldown", Duration::class.java).getOrDefault(Duration.ofSeconds(5)).toMillis()
        ) - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    override fun get(command: NexusCommand, user: Long): Optional<NexusRatelimitData> {
        return Optional.ofNullable(ratelimits.getOrDefault(Entity((command as NexusCommandCore).uuid, user), null))
    }

    override fun onBeforeCommand(event: NexusMiddlewareEvent) {
        val ratelimitData = ratelimit(
            event.userId, event.serverId.orElse(event.userId),
            event.command as NexusCommandCore
        )
        if (ratelimitData.ratelimited) {
            if (!ratelimitData.notified) {
                event.stop(
                    Nexus.configuration.commonsInterceptors.messages.ratelimited(event, ratelimitData.remaining)
                )
                return
            }
            event.stop()
        }
    }

    private inner class AccessorRatelimitData(val notified: Boolean, val ratelimited: Boolean, val remaining: Long)
    private inner class Entity(val command: String, val user: Long) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val entity = other as Entity
            return user == entity.user && command == entity.command
        }

        override fun hashCode(): Int {
            return Objects.hash(command, user)
        }
    }
}