package pw.mihou.nexus.configuration.modules

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger
import pw.mihou.nexus.Nexus
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter
import pw.mihou.nexus.core.logger.adapters.defaults.NexusConsoleLoggingAdapter
import pw.mihou.nexus.core.logger.adapters.defaults.NexusDefaultLoggingAdapter

class NexusGlobalConfiguration internal constructor() {

    /**
     * A set that includes the names of the middlewares that will be included in the commands and processed
     * prior to execution, these middlewares will take a higher precedence than the middlewares defined in
     * the commands themselves.
     *
     * Note: This does not create any middlewares, rather it tells the dispatcher what middlewares to reference
     * before processing the local middlewares.
     */
    val middlewares: MutableSet<String> = mutableSetOf()

    /**
     * A set that includes the names of the afterwares that will be included in the commands and processed
     * at the end of execution, these afterwares will take a higher precedence than the afterwares defined in
     * the commands themselves.
     *
     * Note: This does not create any afterwares, rather it tells the dispatcher what afterwares to reference
     * before processing the local afterwares.
     */
    val afterwares: MutableSet<String> = mutableSetOf()

    /**
     * An adapter to help Nexus adopt the same way of logging that your application does.
     *
     * You can leave this as default if you are using an SLF4J-based logger such as Logback or
     * if you are using a Log4j logger but with a SLF4J bridge as the default logging adapter uses SLF4J.
     */
    @Volatile var logger: NexusLoggingAdapter =
        if (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) == null || LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) is NOPLogger) NexusConsoleLoggingAdapter()
        else NexusDefaultLoggingAdapter()

    /**
     * To enable global inheritance, wherein all the commands inherits properties from the class provided below, you can
     * specify which class you want to be inherited and Nexus will take care of inheriting them. Including a global inheritance
     * class would mean the properties of the global inheritance class will be inherited by children commands regardless of
     * whether they have a local inheritance class.
     */
    @JvmField @Volatile var inheritance: Any? = null

    /**
     * When in an automatic defer situation, the framework will automatically defer the response when the time has
     * surpassed the specified amount. You can specify this to any value less than 3,000 but the default value should
     * be more than enough even when considering network latencies.
     */
    @JvmField @Volatile var autoDeferAfterMilliseconds: Long = 2350
}