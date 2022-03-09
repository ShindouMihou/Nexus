package pw.mihou.nexus.core.logger.adapters.defaults.configuration;

import pw.mihou.nexus.core.logger.adapters.defaults.configuration.enums.NexusConsoleLoggingLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NexusConsoleLoggingConfiguration {

    private final DateTimeFormatter dateFormat;
    private final String format;

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ");

    private final Set<NexusConsoleLoggingLevel> allowed = new HashSet<>(Set.of(
            NexusConsoleLoggingLevel.ERROR,
            NexusConsoleLoggingLevel.INFO,
            NexusConsoleLoggingLevel.WARN
    ));

    /**
     * Creates a new {@link NexusConsoleLoggingConfiguration} that either logs the date or not depending
     * on if the DatetimeFormatter is null or not but contains the specified format which can be customized
     * accordingly with the following parameters:
     * <ul>
     *     <li><b>$_DATE</b>     :  Displays the date-time of when it was logged, unsupported in this constructor if dateFormat is null.</li>
     *     <li><b>$_LEVEL</b>    :  Displays the logging level of this log.</li>
     *     <li><b>$_MESSAGE</b>  :  Displays the message of the log on the location specified.</li>
     * </ul>
     *
     * @param dateFormat    The {@link DateTimeFormatter} to use for formatting the date-time, can be null.
     * @param format        The message format to use for this logging adapter.
     */
    public NexusConsoleLoggingConfiguration(@Nonnull String format, @Nullable DateTimeFormatter dateFormat) {
        if (dateFormat != null && dateFormat.getZone() == null) {
            dateFormat = dateFormat.withZone(ZoneId.systemDefault());
        }

        this.dateFormat = dateFormat;
        this.format = format;
    }

    /**
     * Creates a new {@link NexusConsoleLoggingConfiguration} that does not log any date-time but contains the
     * specified format which can be customized accordingly with the following parameters:
     * <ul>
     *     <li><b>$_DATE</b>     :  Displays the date-time of when it was logged, unsupported in this constructor.</li>
     *     <li><b>$_LEVEL</b>    :  Displays the logging level of this log.</li>
     *     <li><b>$_MESSAGE</b>  :  Displays the message of the log on the location specified.</li>
     * </ul>
     *
     * @param format    The message format to use for this logging adapter.
     */
    public NexusConsoleLoggingConfiguration(@Nonnull String format) {
        this(format, null);
    }

    /**
     * Creates a new {@link NexusConsoleLoggingConfiguration} that has the default setting which shows the
     * date-time and a message: <br><br>{@code $_DATE $_LEVEL Nexus $_MESSAGE}<br><br> which can create logs that looks similar
     * to: <br><br>{@code 2022-03-09 00:000:00.00+0000 INFO Nexus A command has been pushed to Discord without an issue.}
     */
    public NexusConsoleLoggingConfiguration() {
        this("$_DATE $_LEVEL Nexus $_MESSAGE", DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * Allows the specific logging levels.
     *
     * @param levels    The logging levels to enable.
     * @return          The {@link NexusConsoleLoggingConfiguration} for chain-calling methods.
     */
    public NexusConsoleLoggingConfiguration allow(NexusConsoleLoggingLevel... levels) {
        allowed.addAll(List.of(levels));
        return this;
    }

    /**
     * Disallows the specific logging levels.
     *
     * @param levels    The logging levels to disable.
     * @return          The {@link NexusConsoleLoggingConfiguration} for chain-calling methods.
     */
    public NexusConsoleLoggingConfiguration disallow(NexusConsoleLoggingLevel... levels) {
        List.of(levels).forEach(allowed::remove);
        return this;
    }

    /**
     * Sets the allowed logging levels, this will remove any previously set
     * logging levels including the ones that were added by default.
     *
     * @param levels    The logging levels to enable.
     * @return          The {@link NexusConsoleLoggingConfiguration} for chain-calling methods.
     */
    public NexusConsoleLoggingConfiguration set(NexusConsoleLoggingLevel... levels) {
        allowed.clear();
        allowed.addAll(List.of(levels));
        return this;
    }

    /**
     * Gets the set of allowed logging levels.
     *
     * @return  A set of {@link NexusConsoleLoggingLevel}.
     */
    public Set<NexusConsoleLoggingLevel> allowed() {
        return Collections.unmodifiableSet(allowed);
    }

    /**
     * Gets the logging message format for this logging configuration.
     *
     * @return  The logging message format for this configuration.
     */
    public String format() {
        return format;
    }

    /**
     * Gets the {@link DateTimeFormatter} for this logging configuration.
     *
     * @return  The {@link DateTimeFormatter} for this configuration, or null.
     */
    @Nullable
    public DateTimeFormatter dateFormat() {
        return dateFormat;
    }

}
