package pw.mihou.nexus;

import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.builder.NexusBuilder;
import pw.mihou.nexus.core.configuration.core.NexusConfiguration;
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter;
import pw.mihou.nexus.core.managers.NexusShardManager;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.responders.NexusResponderRepository;
import pw.mihou.nexus.features.command.synchronizer.NexusSynchronizer;

import java.util.Collection;
import java.util.List;

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
     * Retrieves the command synchronizer that is available for
     * this {@link Nexus} instance.
     *
     * @return  The command synchronizer that is usable by this
     * {@link Nexus} instance.
     */
    NexusSynchronizer getSynchronizer();

    /**
     * Retrieves the command responder repository that is responsible for
     * handling cross-middleware and command responders.
     *
     * @return  The command responder repository reasonable for this shard's
     * cross-middleware and command responders.
     */
    NexusResponderRepository getResponderRepository();

    /**
     * Retrieves the shard manager that is being utilized by
     * this {@link Nexus} instance.
     *
     * @return The {@link NexusShardManager} instance that is being utilized by
     * this {@link Nexus} instance.
     */
     NexusShardManager getShardManager();

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
     * @see Nexus#listenMany(Object...)
     * @see Nexus#defineMany(Object...)
     * @see Nexus#listenOne(Object)
     * @see Nexus#defineOne(Object)
     * @param model The model that will be used as a reference.
     * @return The Nexus Command instance that is generated from the reference.
     */
     @Deprecated
    NexusCommand createCommandFrom(Object model);

    /**
     * Molds one command from the reference provided, not to be confused with {@link Nexus#listenOne(Object)}, this does not
     * enable the event dispatcher for this command and won't be listed in {@link NexusCommandManager}. This is intended for when you want
     * to simply test the result of the command generation engine of Nexus.
     *
     * @param command  The models used as a reference for the command definition.
     * @return All the commands that were generated from the references provided.
     */
    NexusCommand defineOne(Object command);

    /**
     * Molds one command from the reference provided and allows events to be dispatched onto the command when an event
     * is intended to be dispatched to the given command.
     *
     * @param command  The model used as a reference for the command definition.
     * @return All the commands that were generated from the references provided.
     */
    NexusCommand listenOne(Object command);

    /**
     * Molds many commands from the references provided, not to be confused with {@link Nexus#listenMany(Object...)}, this does not
     * enable the event dispatcher for this command and won't be listed in {@link NexusCommandManager}. This is intended for when you want
     * to simply test the result of the command generation engine of Nexus.
     *
     * @param commands  The models used as a reference for the command definition.
     * @return All the commands that were generated from the references provided.
     */
     List<NexusCommand> defineMany(Object... commands);

    /**
     * Molds many commands from the references provided and allows events to be dispatched onto the commands when an event
     * is intended to be dispatched to the given command.
     *
     * @param commands  The models used as a reference for the command definition.
     * @return All the commands that were generated from the references provided.
     */
    List<NexusCommand> listenMany(Object... commands);

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
