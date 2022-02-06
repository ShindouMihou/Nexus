package pw.mihou.nexus.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.managers.core.NexusCommandManagerCore;
import pw.mihou.nexus.core.managers.NexusShardManager;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.core.reflective.NexusReflectiveCore;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.core.NexusBaseCommandImplementation;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.messages.defaults.NexusDefaultMessageConfiguration;
import pw.mihou.nexus.features.messages.facade.NexusMessageConfiguration;
import pw.mihou.nexus.features.ratelimiter.core.NexusRatelimiterCore;
import pw.mihou.nexus.features.ratelimiter.facade.NexusRatelimiter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class NexusCore implements Nexus {

    private final NexusRatelimiterCore ratelimiter = new NexusRatelimiterCore();
    private final NexusCommandManagerCore commandManager = new NexusCommandManagerCore(this);
    private NexusShardManager shardManager;
    public static final Logger logger = LoggerFactory.getLogger("Nexus.Core");
    private final NexusMessageConfiguration messageConfiguration;
    private final List<String> globalMiddlewares = new ArrayList<>();
    private final List<String> globalAfterwares = new ArrayList<>();

    private final DiscordApiBuilder builder;
    private final Consumer<DiscordApi> onShardLogin;

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
            Consumer<DiscordApi> onShardLogin
    ) {
        this.builder = builder;
        this.onShardLogin = onShardLogin;
        this.shardManager = new NexusShardManager();

        if (messageConfiguration == null) {
            this.messageConfiguration = new NexusDefaultMessageConfiguration();
        } else {
            this.messageConfiguration = messageConfiguration;
        }
    }

    @Override
    public NexusCommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public NexusShardManager getShardManager() {
        return shardManager;
    }

    @Override
    public NexusRatelimiter getRatelimiter() {
        return ratelimiter;
    }

    @Override
    public NexusCommand createCommandFrom(Object model) {
        return NexusReflectiveCore.accept(model, NexusCommandCore.class, this);
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
                    shards.stream()
                            .sorted(Comparator.comparingInt(DiscordApi::getCurrentShard))
                            .toArray(DiscordApi[]::new)
            );

            // The shard startup should only happen once all the shards are connected.
            getShardManager().asStream().forEachOrdered(onShardLogin);
        }

        commandManager.index();
        return this;
    }


    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        commandManager
                .acceptEvent(event)
                .map(nexusCommand -> (NexusCommandCore) nexusCommand)
                .ifPresent(nexusCommand ->
                        NexusThreadPool.executorService.submit(() ->
                                new NexusBaseCommandImplementation(nexusCommand).dispatch(event)
                        )
                );
    }

    /**
     * An internal method which is used by reflection to add a command
     * into the registry of Nexus.
     *
     * @param command The command to add.
     */
    private static void addCommand(NexusCommandCore command) {
        command.core.getCommandManager().addCommand(command);
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
}
