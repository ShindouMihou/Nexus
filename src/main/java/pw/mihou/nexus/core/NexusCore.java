package pw.mihou.nexus.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.configuration.core.NexusConfiguration;
import pw.mihou.nexus.core.enginex.core.NexusEngineXCore;
import pw.mihou.nexus.core.enginex.facade.NexusEngineX;
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter;
import pw.mihou.nexus.core.logger.adapters.defaults.NexusDefaultLoggingAdapter;
import pw.mihou.nexus.core.managers.core.NexusCommandManagerCore;
import pw.mihou.nexus.core.managers.NexusShardManager;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.core.reflective.NexusReflectiveCore;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.core.NexusCommandDispatcher;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.interceptors.commons.core.NexusCommonInterceptorsCore;
import pw.mihou.nexus.features.command.interceptors.facades.NexusCommandInterceptor;
import pw.mihou.nexus.features.command.responders.NexusResponderRepository;
import pw.mihou.nexus.features.command.synchronizer.NexusSynchronizer;
import pw.mihou.nexus.features.messages.defaults.NexusDefaultMessageConfiguration;
import pw.mihou.nexus.features.messages.facade.NexusMessageConfiguration;
import pw.mihou.nexus.features.paginator.feather.NexusFeatherPaging;
import pw.mihou.nexus.features.paginator.feather.core.NexusFeatherViewEventCore;
import pw.mihou.nexus.features.paginator.feather.core.NexusFeatherViewPagerCore;


import java.util.*;
import java.util.function.Consumer;

public class NexusCore implements Nexus {

    private final NexusCommandManagerCore commandManager = new NexusCommandManagerCore(this);
    private NexusShardManager shardManager;
    public static NexusLoggingAdapter logger = new NexusDefaultLoggingAdapter();
    private final NexusMessageConfiguration messageConfiguration;
    private final List<String> globalMiddlewares = new ArrayList<>();
    private final List<String> globalAfterwares = new ArrayList<>();
    private final DiscordApiBuilder builder;
    private final Consumer<DiscordApi> onShardLogin;
    private final NexusConfiguration nexusConfiguration;
    private final NexusEngineX engineX = new NexusEngineXCore(this);
    private final NexusSynchronizer synchronizer = new NexusSynchronizer(this);
    private final NexusResponderRepository responderRepository = new NexusResponderRepository();

    /**
     * Creates a new Nexus Core with a customized {@link NexusMessageConfiguration} and
     * default specifications.
     *
     * @param messageConfiguration The message configuration to use.
     * @param builder The builder when creating a new {@link DiscordApi} instance.
     * @param onShardLogin This is executed everytime a shard logins.
     */
    public NexusCore(
            NexusMessageConfiguration messageConfiguration,
            DiscordApiBuilder builder,
            Consumer<DiscordApi> onShardLogin,
            NexusConfiguration nexusConfiguration
    ) {
        this.builder = builder;
        this.onShardLogin = onShardLogin;
        this.shardManager = new NexusShardManager(this);
        this.nexusConfiguration = nexusConfiguration;
        this.messageConfiguration = Objects.requireNonNullElseGet(messageConfiguration, NexusDefaultMessageConfiguration::new);
        NexusCommandInterceptor.addRepository(new NexusCommonInterceptorsCore());
    }

    @Override
    public NexusCommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public NexusSynchronizer getSynchronizer() {
        return synchronizer;
    }

    @Override
    public NexusResponderRepository getResponderRepository() {
        return responderRepository;
    }

    @Override
    public NexusShardManager getShardManager() {
        return shardManager;
    }

    @Override
    public NexusConfiguration getConfiguration() {
        return nexusConfiguration;
    }

    /**
     * Gets the queueing engine for this {@link Nexus} instance.
     *
     * @return  The queueing engine of this instance.
     */
    public NexusEngineX getEngineX() {
        return engineX;
    }

    @Override
    @Deprecated(forRemoval = true)
    public NexusCommand createCommandFrom(Object model) {
        return listenOne(model);
    }

    @Override
    public NexusCommand defineOne(Object command) {
        return NexusReflectiveCore.command(command, this);
    }

    @Override
    public NexusCommand listenOne(Object command) {
        NexusCommand definition = defineOne(command);
        getCommandManager().addCommand(definition);

        return definition;
    }

    @Override
    public List<NexusCommand> defineMany(Object... commands) {
        return Arrays.stream(commands)
                .map(reference -> ((NexusCommand) NexusReflectiveCore.command(reference, this)))
                .toList();
    }

    @Override
    public List<NexusCommand> listenMany(Object... commands) {
        List<NexusCommand> definitions = defineMany(commands);
        definitions.forEach(definition -> getCommandManager().addCommand(definition));

        return definitions;
    }

    /**
     * Gets the list of global middlewares that are pre-appended into
     * commands that are registered.
     *
     * @return The list of global middlewares.
     */
    public List<String> getGlobalMiddlewares() {
        return globalMiddlewares;
    }

    /**
     * Gets the list of global afterwares that are pre-appended into
     * commands that are registered.
     *
     * @return The list of global afterwares.
     */
    public List<String> getGlobalAfterwares() {
        return globalAfterwares;
    }

    @Override
    public Nexus addGlobalMiddlewares(String... middlewares) {
        globalMiddlewares.addAll(Arrays.asList(middlewares));
        return this;
    }

    @Override
    public Nexus addGlobalAfterwares(String... afterwares) {
        globalAfterwares.addAll(Arrays.asList(afterwares));
        return this;
    }

    @Override
    public Nexus start() {
        if (builder != null && onShardLogin != null) {
            List<DiscordApi> shards = new ArrayList<>();
            builder.addListener(this)
                    .loginAllShards()
                    .forEach(future -> future.thenAccept(shards::add).join());

            this.shardManager = new NexusShardManager(
                    this,
                    shards.stream()
                            .sorted(Comparator.comparingInt(DiscordApi::getCurrentShard))
                            .toArray(DiscordApi[]::new)
            );

            // The shard startup should only happen once all the shards are connected.
            getShardManager().asStream().forEachOrdered(onShardLogin);
        }

        return this;
    }


    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        commandManager
                .acceptEvent(event)
                .map(nexusCommand -> (NexusCommandCore) nexusCommand)
                .ifPresent(nexusCommand ->
                        NexusThreadPool.executorService.submit(() ->
                                NexusCommandDispatcher.dispatch(nexusCommand, event)
                        )
                );
    }

    /**
     * An internal method which is used by the command handler to retrieve the message
     * configuration that is being utilized by this instance.
     *
     * @return The {@link NexusMessageConfiguration} that is being utilized by Nexus.
     */
    public NexusMessageConfiguration getMessageConfiguration() {
        return messageConfiguration;
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        if (!event.getButtonInteraction().getCustomId().contains("[$;")) return;

        String[] keys = event.getButtonInteraction().getCustomId().split("\\[\\$;", 3);
        if (keys.length < 3 || !NexusFeatherPaging.views.containsKey(keys[0])) return;

        NexusThreadPool.executorService.submit(() -> {
            try {
                NexusFeatherPaging.views.get(keys[0])
                        .onEvent(new NexusFeatherViewEventCore(event, new NexusFeatherViewPagerCore(keys[1], keys[0]), keys[2]));
            } catch (Throwable exception) {
                logger.error("An uncaught exception was received by Nexus Feather with the following stacktrace.");
                exception.printStackTrace();
            }
        });
    }
}
