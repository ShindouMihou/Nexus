package pw.mihou.nexus.core.logger.adapters.defaults

import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter
import pw.mihou.nexus.core.logger.adapters.defaults.configuration.enums.NexusConsoleLoggingLevel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

typealias NexusConsoleLoggingFormatter = (time: Instant, level: NexusConsoleLoggingLevel, message: String) -> String

object NexusFasterConsoleLoggingAdapter: NexusLoggingAdapter {

    @Volatile @JvmField var datetimeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
        .withZone(ZoneId.systemDefault())

    @Volatile @JvmField var formatter: NexusConsoleLoggingFormatter = {
        time: Instant,
        level: NexusConsoleLoggingLevel,
        message: String
        -> "${datetimeFormatter.format(time)} $level Nexus $message"
    }

    @Volatile @JvmField var allowed: MutableSet<NexusConsoleLoggingLevel> = mutableSetOf(
        NexusConsoleLoggingLevel.ERROR,
        NexusConsoleLoggingLevel.INFO,
        NexusConsoleLoggingLevel.WARN
    )

    /**
     * It is recommended to set this as false when using for Nexus only as the framework no longer
     * uses placeholders, but instead interpolate the data directly using Kotlin's string templates.
     *
     * (If for some reason that you are also using this for logger, you can set this as true, but this will
     * significantly slow down the performance as it will scan the message for any placeholders).
     */
    @Volatile @JvmField var usePlaceholders = false

    /**
     * Formats the Nexus log messages which are based out of
     * SLF4J-standard into log messages that are complete and readable.
     *
     * @param message   The message to format.
     * @param values    The values to append into the message.
     * @return          The formatted log message.
     */
    private fun exchange(message: String, vararg values: Any): String {
        @Suppress("NAME_SHADOWING") var message = message
        for (value in values) {
            message = message.replaceFirst("\\{}".toRegex(), value.toString())
        }
        return message
    }

    /**
     * Creates a message, performs legacy interpolation when [usePlaceholders] is enabled.
     * @param message the message to create
     * @param values the values to interpolate, if [usePlaceholders] is present.
     * @return the original message if [usePlaceholders] is disabled, otherwise the interpolated message.
     */
    private fun createMessage(message: String, values: List<Any>): String {
        if (usePlaceholders && message.contains("{}")) {
            return exchange(message, values)
        }
        return message
    }

    /**
     * Logs the message if the configuration supports for the logging level specified.
     *
     * @param message   The message to format and log into the console.
     * @param level     The logging level to log this message as.
     * @param values    The values to append into the message.
     */
    private fun log(message: String, level: NexusConsoleLoggingLevel, vararg values: Any) {
        if (allowed.contains(level)) {
            var printStream = System.out
            if (level == NexusConsoleLoggingLevel.ERROR) {
                printStream = System.err
            }
            @Suppress("NAME_SHADOWING") val message =  createMessage(message, values.toList())
            printStream.println(formatter(Instant.now(), level, message))
            if (!usePlaceholders || !message.contains("{}")) {
                Arrays.stream(values).forEach { `object`: Any? ->
                    if (`object` is Exception) {
                        `object`.printStackTrace()
                    } else if (`object` is Throwable) {
                        `object`.printStackTrace()
                    }
                }
            }
        }
    }

    override fun info(message: String, vararg values: Any) {
        log(message, NexusConsoleLoggingLevel.INFO, *values)
    }

    override fun error(message: String, vararg values: Any) {
        log(message, NexusConsoleLoggingLevel.ERROR, *values)
    }

    override fun warn(message: String, vararg values: Any) {
        log(message, NexusConsoleLoggingLevel.WARN, *values)
    }

    override fun debug(message: String, vararg values: Any) {
        log(message, NexusConsoleLoggingLevel.DEBUG, *values)
    }

}