package pw.mihou.nexus.express.core

import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.core.exceptions.NexusFailedActionException
import pw.mihou.nexus.core.threadpool.NexusThreadPool
import pw.mihou.nexus.express.NexusExpress
import pw.mihou.nexus.express.event.NexusExpressEvent
import pw.mihou.nexus.express.event.core.NexusExpressEventCore
import pw.mihou.nexus.express.event.status.NexusExpressEventStatus
import pw.mihou.nexus.express.request.NexusExpressRequest
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.concurrent.withLock

internal class NexusExpressCore: NexusExpress {

    private val globalQueue: BlockingQueue<NexusExpressEvent> = LinkedBlockingQueue()
    private val predicateQueue: BlockingQueue<Pair<Predicate<DiscordApi>, NexusExpressEvent>> = LinkedBlockingQueue()

    private val predicateQueueProcessingLock = ReentrantLock()
    private val globalQueueProcessingLock = ReentrantLock()

    private val localQueue: MutableMap<Int, BlockingQueue<NexusExpressEvent>> = ConcurrentHashMap()

    fun ready(shard: DiscordApi) {
        NexusThreadPool.executorService.submit {
            val local = localQueue(shard.currentShard)
            while (!local.isEmpty()) {
                try {
                    val event = local.poll()

                    if (event != null) {
                        NexusThreadPool.executorService.submit {
                            (event as NexusExpressEventCore).process(shard)
                        }
                    }
                } catch (exception: Exception) {
                    Nexus.logger.error("An uncaught exception was caught from Nexus Express Way.", exception)
                }
            }

            predicateQueueProcessingLock.withLock {
                while (!predicateQueue.isEmpty()) {
                    try {
                        val (predicate, _) = predicateQueue.peek()

                        if (!predicate.test(shard)) continue
                        val (_, event) = predicateQueue.poll()

                        NexusThreadPool.executorService.submit {
                            (event as NexusExpressEventCore).process(shard)
                        }
                    } catch (exception: Exception) {
                        Nexus.logger.error("An uncaught exception was caught from Nexus Express Way.", exception)
                    }
                }
            }
        }

        NexusThreadPool.executorService.submit {
            globalQueueProcessingLock.withLock {
                try {
                    val event = globalQueue.poll()

                    if (event != null) {
                        NexusThreadPool.executorService.submit {
                            (event as NexusExpressEventCore).process(shard)
                        }
                    }
                } catch (exception: Exception) {
                    Nexus.logger.error("An uncaught exception was caught from Nexus Express Way.", exception)
                }
            }
        }
    }

    private fun localQueue(shard: Int): BlockingQueue<NexusExpressEvent> {
        return localQueue.computeIfAbsent(shard) { LinkedBlockingQueue() }
    }

    override fun queue(shard: Int, event: NexusExpressRequest): NexusExpressEvent {
        val expressEvent = NexusExpressEventCore(event)

        if (Nexus.sharding[shard] == null){
            localQueue(shard).add(expressEvent)

            val maximumTimeout = Nexus.configuration.express.maximumTimeout
            if (!maximumTimeout.isZero && !maximumTimeout.isNegative) {
                NexusThreadPool.schedule({
                    expressEvent.`do` {
                        if (status() == NexusExpressEventStatus.WAITING) {
                            val removed = localQueue(shard).remove(this)
                            Nexus.logger.warn(
                                "An express request that was specified " +
                                    "for shard $shard has expired after ${maximumTimeout.toMillis()} milliseconds " +
                                    "without the shard connecting with Nexus. [acknowledged=$removed]"
                            )

                            expire()
                        }
                    }
                }, maximumTimeout.toMillis(), TimeUnit.MILLISECONDS)
            }
        } else {
            NexusThreadPool.executorService.submit { expressEvent.process(Nexus.sharding[shard]!!) }
        }

        return expressEvent
    }

    override fun queue(predicate: Predicate<DiscordApi>, event: NexusExpressRequest): NexusExpressEvent {
        val expressEvent = NexusExpressEventCore(event)
        val shard = Nexus.sharding.find { shard2 -> predicate.test(shard2) }

        if (shard == null){
            val pair = predicate to expressEvent
            predicateQueue.add(pair)

            val maximumTimeout = Nexus.configuration.express.maximumTimeout
            if (!maximumTimeout.isZero && !maximumTimeout.isNegative) {
                NexusThreadPool.schedule({
                    expressEvent.`do` {
                        if (status() == NexusExpressEventStatus.WAITING) {
                            val removed = predicateQueue.remove(pair)
                            Nexus.logger.warn(
                                "An express request that was specified " +
                                        "for a predicate has expired after ${maximumTimeout.toMillis()} milliseconds " +
                                        "without any matching shard connecting with Nexus. [acknowledged=$removed]"
                            )

                            expire()
                        }
                    }
                }, maximumTimeout.toMillis(), TimeUnit.MILLISECONDS)
            }
        } else {
            NexusThreadPool.executorService.submit { expressEvent.process(shard) }
        }

        return expressEvent
    }

    override fun queue(event: NexusExpressRequest): NexusExpressEvent {
        val expressEvent = NexusExpressEventCore(event)

        if (Nexus.sharding.size == 0){
            globalQueue.add(expressEvent)

            val maximumTimeout = Nexus.configuration.express.maximumTimeout
            if (!maximumTimeout.isZero && !maximumTimeout.isNegative) {
                NexusThreadPool.schedule({
                    expressEvent.`do` {
                        if (status() == NexusExpressEventStatus.WAITING) {
                            val removed = globalQueue.remove(this)
                            Nexus.logger.warn(
                                "An express request that was specified " +
                                        "for any available shards has expired after ${maximumTimeout.toMillis()} milliseconds " +
                                        "without any shard connecting with Nexus. [acknowledged=$removed]"
                            )

                            expire()
                        }
                    }
                }, maximumTimeout.toMillis(), TimeUnit.MILLISECONDS)
            }
        } else {
            NexusThreadPool.executorService.submit { expressEvent.process(Nexus.sharding.collection().first()) }
        }

        return expressEvent
    }

    override fun await(shard: Int): CompletableFuture<DiscordApi> {
        val shardA = Nexus.sharding[shard]

        if (shardA != null) {
            return CompletableFuture.completedFuture(shardA)
        }

        val future = CompletableFuture<DiscordApi>()
        failFutureOnExpire(queue(shard, future::complete), future)

        return future
    }

    override fun await(server: Long): CompletableFuture<Server> {
        val serverA = Nexus.sharding.server(server)

        if (serverA != null) {
            return CompletableFuture.completedFuture(serverA)
        }

        val future = CompletableFuture<Server>()
        failFutureOnExpire(
            queue(
                { shard -> shard.getServerById(server).isPresent },
                { shard -> future.complete(shard.getServerById(server).get()) }
            ),
            future
        )

        return future
    }

    override fun awaitAvailable(): CompletableFuture<DiscordApi> {
        val shardA = Nexus.sharding.collection().firstOrNull()

        if (shardA != null) {
            return CompletableFuture.completedFuture(shardA)
        }

        val future = CompletableFuture<DiscordApi>()
        failFutureOnExpire(queue(future::complete), future)

        return future
    }

    override fun <U> failFutureOnExpire(event: NexusExpressEvent, future: CompletableFuture<U>) {
        event.addStatusChangeListener { _, _, newStatus ->
            if (newStatus == NexusExpressEventStatus.EXPIRED || newStatus == NexusExpressEventStatus.STOPPED) {
                future.completeExceptionally(
                    NexusFailedActionException("Failed to connect with the shard that was being waited, it's possible " +
                            "that the maximum timeout has been reached or the event has been somehow cancelled.")
                )
            }
        }
    }

    override fun broadcast(event: Consumer<DiscordApi>) {
        Nexus.sharding.collection().forEach { shard ->
            NexusThreadPool.executorService.submit {
                try {
                    event.accept(shard)
                } catch (exception: Exception) {
                    Nexus.logger.error("An uncaught exception was caught from Nexus Express Broadcaster.", exception)
                }
            }
        }
    }
}