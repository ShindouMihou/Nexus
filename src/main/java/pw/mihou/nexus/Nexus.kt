package pw.mihou.nexus

import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.event.interaction.MessageContextMenuCommandEvent
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.event.interaction.UserContextMenuCommandEvent
import org.javacord.api.listener.interaction.ButtonClickListener
import org.javacord.api.listener.interaction.MessageContextMenuCommandListener
import org.javacord.api.listener.interaction.SlashCommandCreateListener
import org.javacord.api.listener.interaction.UserContextMenuCommandListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pw.mihou.nexus.configuration.NexusConfiguration
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter
import pw.mihou.nexus.core.logger.adapters.defaults.NexusConsoleLoggingAdapter
import pw.mihou.nexus.core.managers.core.NexusCommandManagerCore
import pw.mihou.nexus.core.managers.facade.NexusCommandManager
import pw.mihou.nexus.core.reflective.NexusReflection
import pw.mihou.nexus.core.threadpool.NexusThreadPool
import pw.mihou.nexus.express.NexusExpress
import pw.mihou.nexus.express.core.NexusExpressCore
import pw.mihou.nexus.features.command.core.NexusCommandCore
import pw.mihou.nexus.features.command.core.NexusCommandDispatcher
import pw.mihou.nexus.features.command.facade.NexusCommand
import pw.mihou.nexus.features.command.facade.NexusHandler
import pw.mihou.nexus.features.command.interceptors.NexusCommandInterceptors
import pw.mihou.nexus.features.command.synchronizer.NexusSynchronizer
import pw.mihou.nexus.features.contexts.NexusContextMenu
import pw.mihou.nexus.features.contexts.core.NexusContextMenuDispatcher
import pw.mihou.nexus.features.contexts.facade.NexusContextMenuHandler
import pw.mihou.nexus.features.paginator.feather.NexusFeatherPaging
import pw.mihou.nexus.features.paginator.feather.core.NexusFeatherViewEventCore
import pw.mihou.nexus.features.paginator.feather.core.NexusFeatherViewPagerCore
import pw.mihou.nexus.sharding.NexusShardingManager

object Nexus: SlashCommandCreateListener, ButtonClickListener, UserContextMenuCommandListener, MessageContextMenuCommandListener {

    /**
     * The [NexusConfiguration] that is being used by this one and only instance of [Nexus]. It contains all the
     * globally configurable parameters of Nexus and is recommended to be configured.
     */
    @JvmStatic
    val configuration = NexusConfiguration()

    /**
     * A short-hand intended to be used by internal methods to receive direct access to the logger without
     * navigating to the configuration. You shouldn't use this at all unless you want to send log messages that seem to
     * be from Nexus. (it can get messy, don't recommend).
     */
    @JvmStatic
    @set:JvmName("setLogger")
    var logger: NexusLoggingAdapter
        get() = configuration.global.logger
        set(value) { configuration.global.logger = value }

    /**
     * [NexusExpress] is a local shard router that any developer including internal methods uses as a simple, straightforward
     * router to route their different events, actions to specific or any available shards.
     *
     * You can learn more about this at [Understanding Nexus Express](https://github.com/ShindouMihou/Nexus/wiki/Nexus-Express-Way).
     */
    @JvmStatic
    val express: NexusExpress = NexusExpressCore()

    /**
     * [NexusShardingManager] is the simple sharding manager of Nexus that keeps a record of all the shards that the bot has active.
     * It is recommended and even required to set this up properly to have the framework running at its optimal and proper.
     *
     * You can learn more at [Understanding Nexus Sharding Manager](https://github.com/ShindouMihou/Nexus/wiki/Sharding-Manager).
     */
    @JvmStatic
    @get:JvmName("getShardingManager")
    val sharding = NexusShardingManager()

    /**
     * [NexusCommandManager] is the command manager of Nexus that holds knowledge of all the commands, identifiers and indexes of the framework.
     * You can use this to index commands, extract indexes and many more related to commands that may be handy.
     */
    @JvmStatic
    val commandManager: NexusCommandManager = NexusCommandManagerCore()

    /**
     * Global middlewares are middlewares that are prepended into the commands, these are first-order middlewares which means these are executed
     * first before any second-order middlewares (e.g. ones specified by the command).
     *
     * You can learn more about middlewares at [Understanding Command Interceptors](https://github.com/ShindouMihou/Nexus/wiki/Understanding-Command-Interceptors).
     */
    @JvmStatic
    val globalMiddlewares: Set<String> get() = configuration.global.middlewares

    /**
     * Global afterwares are afterwares that are prepended into the commands, these are first-order afterwares which means these are executed
     * first before any second-order afterwares (e.g. ones specified by the command).
     *
     * You can learn more about afterwares at [Understanding Command Interceptors](https://github.com/ShindouMihou/Nexus/wiki/Command-Interceptors).
     */
    @JvmStatic
    val globalAfterwares: Set<String> get() = configuration.global.afterwares

    /**
     * [NexusSynchronizer] is a tool that is used to synchronize commands between Discord and the bot.
     *
     * You can learn more about it at [Understanding Synchronization](https://github.com/ShindouMihou/Nexus/wiki/Command-Synchronization).
     */
    @JvmStatic
    val synchronizer = NexusSynchronizer()

    @get:JvmSynthetic
    internal val launch get() = configuration.launch

    @JvmStatic
    internal val launcher get() = configuration.launch.launcher

    /**
     * [NexusCommandInterceptors] is the interface between the interceptor registry (middlewares and afterwares) and
     * the [Nexus] interface, allowing simpler and more straightforward interceptor additions.
     */
    @JvmStatic
    val interceptors = NexusCommandInterceptors

    /**
     * Configures the [NexusConfiguration] in a more Kotlin way.
     * @param modifier the modifier to modify the state of the framework.
     */
    @JvmStatic
    @JvmSynthetic
    fun configure(modifier: NexusConfiguration.() -> Unit): Nexus {
        modifier(configuration)
        return this
    }

    /**
     * Adds one or more commands onto the command manager.
     * @param commands the commands to add to the command manager.
     */
    @JvmStatic
    fun <Any:NexusHandler> commands(vararg commands: Any): List<NexusCommand> {
        val list = mutableListOf<NexusCommand>()

        for (command in commands) {
            list.add(manifest(command).apply { commandManager.add(this) })
        }

        return list
    }

    /**
     * Adds one or more context menus onto the command manager.
     * @param contextMenus the context menus to add to the command manager.
     */
    @JvmStatic
    fun <Any:NexusContextMenuHandler<*, *>> contextMenus(vararg contextMenus: Any): List<NexusContextMenu> {
        val list = mutableListOf<NexusContextMenu>()

        for (contextMenu in contextMenus) {
            list.add(manifest(contextMenu).apply { commandManager.add(this) })
        }

        return list
    }

    /**
     * Adds one command onto the command manager.
     * @param command the command to add to the command manager.
     */
    @JvmStatic
    fun <Any:NexusHandler> command(command: Any): NexusCommand {
        return manifest(command).apply { commandManager.add(this) }
    }

    /**
     * Adds one context menu onto the command manager.
     * @param contextMenu the context menu to add to the command manager.
     */
    @JvmStatic
    fun <Any : NexusContextMenuHandler<*, *>> contextMenu(contextMenu: Any): NexusContextMenu {
        return manifest(contextMenu).apply { commandManager.add(this) }
    }

    /**
     * Manifests a model into a [NexusCommand] instance by mapping all the fields that are understood by the
     * engine into the instance. This doesn't auto-add the command into the manager.
     *
     * @param model the model to manifest into a [NexusCommand] instance.
     * @return the [NexusCommand] instance that was manifested.
     */
    @JvmStatic
    fun <Any : NexusHandler> manifest(model: Any): NexusCommand {
        return (NexusReflection.copy(model, NexusCommandCore::class.java) as NexusCommand)
    }

    /**
     * Manifests a model into a [NexusContextMenu] instance by mapping all the fields that are understood by the
     * engine into the instance. This doesn't auto-add the context menu into the manager.
     *
     * @param model the model to manifest into a [NexusContextMenu] instance.
     * @return the [NexusContextMenu] instance that was manifested.
     */
    @JvmStatic
    fun <Any : NexusContextMenuHandler<*, *>> manifest(model: Any): NexusContextMenu {
        return (NexusReflection.copy(model, NexusContextMenu::class.java) as NexusContextMenu)
    }

    /**
     * Adds one or more global middlewares.
     *
     * You can learn more about middlewares at [Understanding Command Interceptors](https://github.com/ShindouMihou/Nexus/wiki/Command-Interceptors).
     * @see [globalMiddlewares]
     */
    @JvmStatic
    fun addGlobalMiddlewares(vararg names: String): Nexus {
        configuration.global.middlewares.addAll(names)
        return this
    }

    /**
     * Adds one or more global afterwares.
     *
     * You can learn more about afterwares at [Understanding Command Interceptors](https://github.com/ShindouMihou/Nexus/wiki/Command-Interceptors).
     * @see [globalAfterwares]
     */
    @JvmStatic
    fun addGlobalAfterwares(vararg names: String): Nexus {
        configuration.global.afterwares.addAll(names)
        return this
    }

    /**
     * An internal method that is used to receive events from Javacord to dispatch to the right command. You should not
     * use this method at all unless you want to send your own [SlashCommandCreateEvent] that you somehow managed to
     * create.
     *
     * @param event The [SlashCommandCreateEvent] received from Javacord.
     */
    override fun onSlashCommandCreate(event: SlashCommandCreateEvent) {
        val command = (commandManager as NexusCommandManagerCore).acceptEvent(event) as NexusCommandCore? ?: return
        launcher.launch { NexusCommandDispatcher.dispatch(command, event) }
    }

    private val FEATHER_KEY_DELIMITER_REGEX = "\\[\\$;".toRegex()

    /**
     * An internal method that is used to receive events from Javacord to dispatch to the right [NexusFeatherPaging]. You
     * should not use this method at all unless you want to send your own [ButtonClickEvent] that you somehow managed
     * to create.
     *
     * @param event The [ButtonClickEvent] received from Javacord.
     */
    override fun onButtonClick(event: ButtonClickEvent) {
        if (!event.buttonInteraction.customId.contains("[$;")) return

        val keys = event.buttonInteraction.customId.split(FEATHER_KEY_DELIMITER_REGEX, limit = 3)
        if (keys.size < 3 || !NexusFeatherPaging.views.containsKey(keys[0])) return

        launcher.launch {
            try {
                NexusFeatherPaging.views[keys[0]]!!
                    .onEvent(NexusFeatherViewEventCore(event, NexusFeatherViewPagerCore(keys[1], keys[0]), keys[2]))
            } catch (exception: Throwable) {
                logger.error("An uncaught exception was received by Nexus Feather with the following stacktrace.", exception)
            }
        }
    }

    /**
     * An internal method that is used to receive events from Javacord to dispatch to the right context menu. You should not
     * use this method at all unless you want to send your own [UserContextMenuCommandEvent] that you somehow managed to
     * create.
     *
     * @param event The [UserContextMenuCommandEvent] received from Javacord.
     */
    override fun onUserContextMenuCommand(event: UserContextMenuCommandEvent) {
        val contextMenu = (commandManager as NexusCommandManagerCore).acceptEvent(event) ?: return
        NexusContextMenuDispatcher.dispatch(event, contextMenu)
    }

    /**
     * An internal method that is used to receive events from Javacord to dispatch to the right context menu. You should not
     * use this method at all unless you want to send your own [MessageContextMenuCommandEvent] that you somehow managed to
     * create.
     *
     * @param event The [MessageContextMenuCommandEvent] received from Javacord.
     */
    override fun onMessageContextMenuCommand(event: MessageContextMenuCommandEvent) {
        val contextMenu = (commandManager as NexusCommandManagerCore).acceptEvent(event) ?: return
        NexusContextMenuDispatcher.dispatch(event, contextMenu)
    }

}