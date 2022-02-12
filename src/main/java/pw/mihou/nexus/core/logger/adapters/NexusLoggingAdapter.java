package pw.mihou.nexus.core.logger.adapters;

public interface NexusLoggingAdapter {

    /**
     * Logs a new info message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    void info(String message, Object... values);

    /**
     * Logs a new error message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    void error(String message, Object... values);

    /**
     * Logs a new warn message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    void warn(String message, Object... values);

    /**
     * Logs a new debug message onto the logging framework.
     *
     * @param message   The raw message to log with placeholders equal to that of SLF4J.
     * @param values    The values to append into the placeholders.
     */
    void debug(String message, Object... values);

}
