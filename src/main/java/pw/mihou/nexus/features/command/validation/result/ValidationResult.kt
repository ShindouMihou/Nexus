package pw.mihou.nexus.features.command.validation.result

import pw.mihou.nexus.features.command.validation.errors.ValidationError

class ValidationResult internal constructor(val hasPassed: Boolean, val error: ValidationError?)