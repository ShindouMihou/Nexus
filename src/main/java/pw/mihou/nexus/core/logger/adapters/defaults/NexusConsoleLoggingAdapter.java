package pw.mihou.nexus.core.logger.adapters.defaults;

import org.jetbrains.annotations.Nullable;
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter;
import pw.mihou.nexus.core.logger.adapters.defaults.configuration.NexusConsoleLoggingConfiguration;
import pw.mihou.nexus.core.logger.adapters.defaults.configuration.enums.NexusConsoleLoggingLevel;

import java.io.PrintStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public class NexusConsoleLoggingAdapter implements NexusLoggingAdapter {

    private final NexusConsoleLoggingConfiguration configuration;

    /**
     * Creates a new {@link NexusConsoleLoggingAdapter} that has the specified
     * configuration.
     *
     * @param configuration The configuration to use when logging.
     */
    public NexusConsoleLoggingAdapter(NexusConsoleLoggingConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates a new {@link NexusConsoleLoggingAdapter} that has the default setting which shows the
     * date-time and a message:
     * <br>
     * <br>{@code $_DATE $_LEVEL Nexus $_MESSAGE}
     * <br>
     * <br> which can create logs that look similar to:
     * <br>
     * <br>{@code 2022-03-09 00:000:00.00+0000 INFO Nexus A command has been pushed to Discord without an issue.}
     * <br>
     * <br> and supports the following logging levels:
     * <br> {@link NexusConsoleLoggingLevel#INFO}, {@link NexusConsoleLoggingLevel#ERROR},
     * {@link NexusConsoleLoggingLevel#WARN}
     */
    public NexusConsoleLoggingAdapter() {
        this(new NexusConsoleLoggingConfiguration());
    }

    /**
     * Formats the Nexus log messages which are based out of
     * SLF4J-standard into log messages that are complete and readable.
     *
     * @param message   The message to format.
     * @param values    The values to append into the message.
     * @return          The formatted log message.
     */
    private String exchange(String message, Object... values) {
        for (Object value : values) {
            message = message.replaceFirst("\\{}", value.toString());
        }

        return message;
    }

    /**
     * Formats the message in accordance to the specifications of this
     * {@link NexusConsoleLoggingAdapter}.
     *
     * @param message   The message to format.
     * @param values    The values to append into the message.
     * @return          The complete, formatted logging message.
     */
    private String format(String message, NexusConsoleLoggingLevel level, Object... values) {
        String logMessage = configuration.format();
        if (configuration.dateFormat() != null && logMessage.contains("$_DATE")) {
            logMessage = logMessage.replaceAll("\\$_DATE", Objects.requireNonNull(configuration.dateFormat())
                    .format(Instant.now()));
        }

        if (logMessage.contains("$_LEVEL")) {
            logMessage = logMessage.replaceAll("\\$_LEVEL", level.name());
        }

        if (logMessage.contains("$_MESSAGE")) {
            logMessage = logMessage.replaceAll("\\$_MESSAGE", exchange(message, values));
        }

        return logMessage;
    }

    /**
     * Logs the message if the configuration supports for the logging level specified.
     *
     * @param message   The message to format and log into the console.
     * @param level     The logging level to log this message as.
     * @param values    The values to append into the message.
     */
    private void log(String message, NexusConsoleLoggingLevel level, Object... values) {
        if (configuration.allowed().contains(level)) {
            PrintStream printStream = System.out;
            if (level == NexusConsoleLoggingLevel.ERROR) {
                printStream = System.err;
            }
            printStream.println(format(message, level, values));

            if (!message.contains("{}")) {
                @Nullable Object object = Arrays.stream(values).findFirst().orElse(null);
                if (object instanceof Exception) {
                    ((Exception) object).printStackTrace();
                } else if (object instanceof Throwable) {
                    ((Throwable) object).printStackTrace();
                }
            }
        }
    }

    @Override
    public void info(String message, Object... values) {
        log(message, NexusConsoleLoggingLevel.INFO, values);
    }

    @Override
    public void error(String message, Object... values) {
        log(message, NexusConsoleLoggingLevel.ERROR, values);
    }

    @Override
    public void warn(String message, Object... values) {
        log(message, NexusConsoleLoggingLevel.WARN, values);
    }

    @Override
    public void debug(String message, Object... values) {
        log(message, NexusConsoleLoggingLevel.DEBUG, values);
    }
}
