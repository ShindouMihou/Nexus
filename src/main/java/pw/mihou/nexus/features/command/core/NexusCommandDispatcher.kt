package pw.mihou.nexus.features.command.core

import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.util.logging.ExceptionLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.Nexus.globalAfterwares
import pw.mihou.nexus.Nexus.globalMiddlewares
import pw.mihou.nexus.Nexus.logger
import pw.mihou.nexus.core.threadpool.NexusThreadPool
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore
import pw.mihou.nexus.features.command.validation.OptionValidation
import pw.mihou.nexus.features.command.validation.result.ValidationResult
import pw.mihou.nexus.features.messages.core.NexusMessageCore

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
        try {
            val nexusEvent: NexusCommandEvent = NexusCommandEventCore(event, instance)

            val middlewares: MutableList<String> = ArrayList()
            middlewares.addAll(globalMiddlewares)
            middlewares.addAll(instance.middlewares)

            val middlewareGate =
                NexusCommandInterceptorCore.execute(nexusEvent, NexusCommandInterceptorCore.middlewares(middlewares))

            if (middlewareGate != null) {
                val middlewareResponse = middlewareGate.response() as NexusMessageCore?
                middlewareResponse?.convertTo(nexusEvent.respondNow())?.respond()?.exceptionally(ExceptionLogger.get())
                return
            }

            if (event.slashCommandInteraction.channel.isEmpty) {
                logger.error(
                    "The channel of a slash command event is somehow not present; this is possibly a change in Discord's side " +
                            "and may need to be addressed, please send an issue @ https://github.com/ShindouMihou/Nexus"
                )
            }

            val validationResult = validate(validations = instance.validators, nexusEvent)

            if (validationResult != null) {
                if (validationResult.error == null) {
                    return
                }

                val responder = nexusEvent.respondNow()
                validationResult.error.convertTo(responder)

                responder.respond().exceptionally(ExceptionLogger.get())
                return
            }

            Nexus.launcher.launch {
                try {
                    instance.handler.onEvent(nexusEvent)
                } catch (throwable: Throwable) {
                    logger.error("An uncaught exception was received by Nexus Command Dispatcher for the " +
                            "command ${instance.name} with the following stacktrace."
                    )
                    throwable.printStackTrace()
                }
            }

            Nexus.launcher.launch {
                val afterwares: MutableList<String> = ArrayList()
                afterwares.addAll(globalAfterwares)
                afterwares.addAll(instance.afterwares)

                NexusCommandInterceptorCore.execute(nexusEvent, NexusCommandInterceptorCore.afterwares(afterwares))
            }
        } catch (exception: Exception) {
            logger.error("An uncaught exception occurred within Nexus' dispatcher for command ${instance.name}.")
            exception.printStackTrace()
        }
    }
}

private fun validate(validations: List<OptionValidation<*>>, event: NexusCommandEvent): ValidationResult? {
    var blocker: ValidationResult? = null

    for (validation in validations) {
        val result = validation.validate(event)

        if (!result.hasPassed) {
            blocker = result
            break
        }
    }

    return blocker
}