package pw.mihou.nexus.features.command.validation

import pw.mihou.nexus.features.command.validation.errors.ValidationError
import pw.mihou.nexus.features.command.validation.result.ValidationResult
import pw.mihou.nexus.features.command.facade.NexusCommandEvent
import java.util.*

class OptionValidation<Option> internal constructor(val collector: (NexusCommandEvent) -> Optional<Option>) {

    /**
     * The validator to use to validate that the result is indeed what we want.
     */
    lateinit var validator: (Option) -> Boolean

    /**
     * The error to send to the client when it does not pass the validation, this is separate from the error that
     * will be sent from the requirements.
     */
    var error: (Option) -> ValidationError? = { null }

    /**
     * Additional requirements that is needed by the [OptionValidation] such as whether to
     * require the following optional to be non-null.
     */
    var requirements: Requires = createRequirements()

    companion object {
        /**
         * Creates the [Requires] that can be configured to include more different options in the validation.
         *
         * @param modifier the modifier to modify the [Requires] state.
         * @return the [Requires] instance.
         */
        @JvmStatic
        @JvmSynthetic
        fun createRequirements(modifier: (Requires.() -> Unit) = {}): Requires {
            val requires = Requires()
            modifier(requires)

            return requires
        }

        /**
         * Creates the [Requires] that can be configured to include more different options in the validation, intended for
         * Java usage where functions aren't best used.
         * @return the [Requires] instance.
         */
        @JvmStatic
        fun createRequirements(): Requires = Requires()

        /**
         * Creates an [OptionValidation] that can be used to validate an [NexusCommandEvent] to match a specific state.
         *
         * @param collector the collector to collect the item that is being validated.
         * @param validator the validator to use to validate that the option is valid.
         * @param error     the message to send when the validation causes an error.
         * @param requirements the additional requirements that is required such as non-nullable.
         * @return a [OptionValidation] instance.
         */
        @JvmOverloads
        @JvmStatic
        fun <Option> create(collector: (NexusCommandEvent) -> Optional<Option>, validator: (Option) -> Boolean,
                                     error: (Option) -> ValidationError? = { null }, requirements: Requires? = null): OptionValidation<Option> {
            val optionValidation = OptionValidation(collector)
            optionValidation.validator = validator
            optionValidation.error = error
            requirements?.apply { optionValidation.requirements = requirements }

            return optionValidation
        }
    }

    /**
     * An internal function of [OptionValidation] that is used to validate a [NexusCommandEvent] to match the
     * validation specifications.
     *
     * @param event the event to validate.
     * @return the validation result and the item retrieved.
     */
    internal fun validate(event: NexusCommandEvent): ValidationResult {
        val item = collector(event)
        if (requirements.nonNull != null && item.isEmpty) {
            return ValidationResult(hasPassed = false, error = requirements.nonNull!!.error)
        }

        if (requirements.nonNull != null && item.isEmpty) {
            return ValidationResult(hasPassed = false, error = requirements.nonNull!!.error)
        }

        val collectedItem = item.orElseThrow()
        if (!validator(collectedItem)) {
            return ValidationResult(hasPassed = false, error = error(collectedItem))
        }

        return ValidationResult(hasPassed = true, error = null)
    }
}

class Requires internal constructor() {

    /**
     * Whether to require the option to be null or not, if this is disabled, then the validation will not error
     * if the value is null and will simply ignore if the option isn't there.
     *
     * By default, this is enabled since most option validation will tend to require it to be non-null.
     */
    var nonNull: ErrorableRequireSetting? = createErrorableRequireSetting(error = null)

    /**
     * Creates an [ErrorableRequireSetting] which are settings that can be configured to have an error.
     * You do not need to use this method if you do not need the setting enabled.
     *
     * @param error The error to send to the end-user.
     * @return an [ErrorableRequireSetting] instance.
     */
    @SuppressWarnings("WeakerAccess")
    fun createErrorableRequireSetting(error: ValidationError?): ErrorableRequireSetting {
        return ErrorableRequireSetting(error)
    }

    inner class ErrorableRequireSetting internal constructor(val error: ValidationError?)
}