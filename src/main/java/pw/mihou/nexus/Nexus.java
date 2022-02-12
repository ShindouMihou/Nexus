package pw.mihou.nexus;

import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.builder.NexusBuilder;
import pw.mihou.nexus.core.configuration.core.NexusConfiguration;
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter;
import pw.mihou.nexus.core.managers.NexusShardManager;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.ratelimiter.facade.NexusRatelimiter;

public interface Nexus extends SlashCommandCreateListener {

    /**
     * This creates a new {@link NexusBuilder} which can be used to create
     * a new {@link Nexus} instance.
     *
     * @return A new {@link NexusBuilder} instance.
     */
    static NexusBuilder builder() {
        return new NexusBuilder();
    }

    /**
     * Sets the logging adapter that Nexus should use.
     *
     * @param adapter The logging adapter that Nexus should use.
     */
    static void setLogger(NexusLoggingAdapter adapter) {
        NexusCore.logger = adapter;
    }

    /**
     * Retrieves the command manager that is being utilized by
     * this {@link Nexus} instance.
     *
     * @return the {@link NexusCommandManager} instance that is being utilized
     * by this {@link Nexus} instance.
     */
    NexusCommandManager getCommandManager();

    /**
     * Retrieves the shard manager that is being utilized by
     * this {@link Nexus} instance.
     *
     * @return The {@link NexusShardManager} instance that is being utilized by
     * this {@link Nexus} instance.
     */
     NexusShardManager getShardManager();

    /**
     * Retrieves the rate-limiter that is being utilized by this
     * {@link Nexus} instance.
     *
     * @return The {@link NexusRatelimiter} that is being utilized by this
     * {@link Nexus} instance.
     */
    NexusRatelimiter getRatelimiter();

    /**
     * Retrieves the {@link NexusConfiguration} that is being utilized by
     * this {@link Nexus} instance.
     *
     * @return The {@link NexusConfiguration} that is being utilized by this
     * {@link Nexus} instance.
     */
    NexusConfiguration getConfiguration();

     /**
     * This creates a new command and attaches them if the annotation {@link pw.mihou.nexus.features.command.annotation.NexusAttach} is
     * present on the model.
     *
     * @param model The model that will be used as a reference.
     * @return The Nexus Command instance that is generated from the reference.
     */
    NexusCommand createCommandFrom(Object model);

    /**
     * Adds a set of middlewares into the global middleware list which are pre-appended into
     * the commands that are created after.
     *
     * @param middlewares The middlewares to add.
     * @return {@link  Nexus} for chain-calling methods.
     */
    Nexus addGlobalMiddlewares(String... middlewares);

    /**
     * Adds a set of afterwares into the global afterware list which are pre-appended into
     * the commands that are created after.
     *
     * @param afterwares The afterwares to add.
     * @return {@link  Nexus} for chain-calling methods.
     */
    Nexus addGlobalAfterwares(String... afterwares);

    /**
     * This starts the Nexus instance and allow it to perform its
     * necessary executions such as indexing.
     *
     * @return The {@link  Nexus} instance.
     */
    Nexus start();

}
