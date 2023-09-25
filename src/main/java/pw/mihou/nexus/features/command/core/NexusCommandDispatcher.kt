package pw.mihou.nexus.features.command.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.Nexus.globalAfterwares
import pw.mihou.nexus.Nexus.globalMiddlewares
import pw.mihou.nexus.Nexus.logger
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore
import pw.mihou.nexus.features.command.validation.OptionValidation
import pw.mihou.nexus.features.command.validation.middleware.OptionValidationMiddleware
import pw.mihou.nexus.features.command.validation.result.ValidationResult
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

object NexusCommandDispatcher {
    /**
     * Dispatches one slash command create event of a command onto the given [NexusCommandCore].
     * This performs the necessary middleware handling, dispatching to the listener and afterware handling.
     * <br></br>
     * This is synchronous by nature except when the event is dispatched to its respective listener and also
     * when the afterwares are executed.
     *
     * @param instance  The [NexusCommandCore] instance to dispatch the event towards.
     * @param event     The [SlashCommandCreateEvent] event to dispatch.
     */
    fun dispatch(instance: NexusCommandCore, event: SlashCommandCreateEvent) {
        val nexusEvent = NexusCommandEventCore(event, instance)

        val middlewares: MutableList<String> = ArrayList()
        middlewares.add(OptionValidationMiddleware.NAME)
        middlewares.addAll(globalMiddlewares)
        middlewares.addAll(instance.middlewares)

        val afterwares: MutableList<String> = ArrayList()
        afterwares.addAll(globalAfterwares)
        afterwares.addAll(instance.afterwares)

        var dispatched = false

        try {
            val middlewareGate: NexusMiddlewareGateCore? = if (Nexus.configuration.interceptors.autoDeferMiddlewareResponses) {
                val future = CompletableFuture.supplyAsync {
                    NexusCommandInterceptorCore.execute(nexusEvent, NexusCommandInterceptorCore.middlewares(middlewares))
                }
                val timeUntil = Instant.now().toEpochMilli() -
                        event.interaction.creationTimestamp.minusMillis(Nexus.configuration.global.autoDeferAfterMilliseconds).toEpochMilli()
                val deferredTaskRan = AtomicBoolean(false)
                val task = Nexus.launch.scheduler.launch(timeUntil) {
                    deferredTaskRan.set(true)
                    if (future.isDone) {
                        return@launch
                    }
                    nexusEvent.respondLaterEphemerallyIf(Nexus.configuration.interceptors.autoDeferAsEphemeral)
                        .exceptionally(ExceptionLogger.get())
                }
                val gate = future.join()
                if (!deferredTaskRan.get()) {
                    task.cancel(false)
                }
                gate
            } else {
                NexusCommandInterceptorCore.execute(nexusEvent, NexusCommandInterceptorCore.middlewares(middlewares))
            }

            if (middlewareGate != null) {
                val middlewareResponse = middlewareGate.response()
                if (middlewareResponse != null) {
                    val updaterFuture = nexusEvent.updater.get()
                    if (updaterFuture != null) {
                        val updater = updaterFuture.join()
                        middlewareResponse.into(updater).update().exceptionally(ExceptionLogger.get())
                    } else {
                        var responder = nexusEvent.respondNow()
                        if (middlewareResponse.ephemeral) {
                            responder = nexusEvent.respondNowEphemerally()
                        }
                        middlewareResponse.into(responder).respond().exceptionally(ExceptionLogger.get())
                    }
                }
                return
            }

            if (event.slashCommandInteraction.channel.isEmpty) {
                logger.error(
                    "The channel of a slash command event is somehow not present; this is possibly a change in Discord's side " +
                            "and may need to be addressed, please send an issue @ https://github.com/ShindouMihou/Nexus"
                )
            }

            dispatched = true
            Nexus.launcher.launch {
                try {
                    instance.handler.onEvent(nexusEvent)
                } catch (throwable: Throwable) {
                    logger.error("An uncaught exception was received by Nexus Command Dispatcher for the " +
                            "command ${instance.name} with the following stacktrace.", throwable)
                }
            }

            Nexus.launcher.launch {
                NexusCommandInterceptorCore.execute(nexusEvent, NexusCommandInterceptorCore.afterwares(afterwares))
            }
        } catch (exception: Exception) {
            logger.error("An uncaught exception occurred within Nexus' dispatcher for command ${instance.name}.", exception)
        } finally {
            if (!dispatched) {
                NexusCommandInterceptorCore.execute(nexusEvent, NexusCommandInterceptorCore.afterwares(afterwares), dispatched = false)
            }
        }
    }
}