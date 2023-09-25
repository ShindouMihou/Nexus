package pw.mihou.nexus.core.logger.adapters.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter;

public class NexusDefaultLoggingAdapter implements NexusLoggingAdapter {

    private final Logger logger = LoggerFactory.getLogger(Nexus.class);

    @Override
    public void info(String message, Object... values) {
        logger.info(message, values);
    }

    @Override
    public void error(String message, Object... values) {
        logger.error(message, values);
    }

    @Override
    public void warn(String message, Object... values) {
        logger.warn(message, values);
    }

    @Override
    public void debug(String message, Object... values) {
        logger.debug(message, values);
    }
}
