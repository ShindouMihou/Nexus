package pw.mihou.nexus.features.command.validation.examples

import pw.mihou.nexus.features.command.validation.OptionValidation
import pw.mihou.nexus.features.command.validation.errors.ValidationError

object OptionValidators {
    val PING_PONG_VALIDATOR = OptionValidation.create<String>(
        validator = { option -> option.equals("ping", ignoreCase = true) || option.equals("pong", ignoreCase = true) },
        error = { ValidationError.create("❌ You must either use `ping` or `pong` as an answer!") },
        // If you want to support optional values then you must add the line below.
        // requirements = OptionValidation.createRequirements { nonNull = null }
    )
}