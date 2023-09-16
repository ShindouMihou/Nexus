package pw.mihou.nexus.features.command.validation.middleware

import pw.mihou.nexus.features.command.core.NexusCommandCore
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor
import pw.mihou.nexus.features.command.interceptors.facades.NexusMiddleware
import pw.mihou.nexus.features.command.validation.OptionValidation
import pw.mihou.nexus.features.command.validation.result.ValidationResult
import pw.mihou.nexus.features.messages.NexusMessage

object OptionValidationMiddleware: NexusMiddleware {
    const val NAME = "nexus::native.validation"

    init {
        NexusCommandInterceptor.addMiddleware(NAME, this)
    }
    override fun onBeforeCommand(event: NexusMiddlewareEvent) {
        val command = event.command as NexusCommandCore
        if (command.validators.isEmpty()) {
            return
        }
        val result = validate(validations = command.validators, event)
        if (result != null) {
            if (result.error == null) {
                event.stop()
                return
            }

            event.stop(NexusMessage.with {
                result.error.convertTo(this)
            })
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
}