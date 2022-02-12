package pw.mihou.nexus.features.command.observer.facade;

import org.javacord.api.entity.server.Server;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.observer.core.NexusObserverCore;
import pw.mihou.nexus.features.command.observer.modes.ObserverMode;

import java.util.concurrent.CompletableFuture;

/**
 * {@link NexusObserver} is the Nexus variant of its predecessor's observer which was VelenObserver.
 * The code is more or less the same and offers the same performance, advancements and all those other good
 * stuff of the original VelenObserver.
 *
 * A {@link NexusObserver} is, in a sense, a slash command checker which observes which slash commands are
 * registered into Discord's database, the ones that aren't and the ones that needs to be deleted to match the
 * registry of Nexus. This also checks for differences in slash commands which allows the feature to be pretty
 * advanced.
 */
public interface NexusObserver {

    /**
     * This creates a new {@link NexusObserver} which observes for any changes
     * on the commands on start-up and updates them, register them or only log them
     * depending on the {@link ObserverMode} selected.
     *
     * @param nexus The {@link Nexus} instance that needs to be checked.
     * @param mode The {@link ObserverMode} to use for this instance.
     */
    static NexusObserver createForWith(Nexus nexus, ObserverMode mode) {
        return new NexusObserverCore(nexus, mode);
    }

    /**
     * Retrieves and performs either an update, removal or creation of the slash command on the
     * server if it exists otherwise ignores the command change application.
     *
     * @param command   The command to apply changes towards.
     * @return The {@link CompletableFuture} that indicates the progress of this task.
     */
    CompletableFuture<Void> applyChangesOnCommand(NexusCommand command);

    /**
     * Retrieves and performs an observation check for all slash commands of all the servers.
     * There are two different modes to the observation:
     * <br>
     * <br>
     * <b>Hard</b>: This is the riskier variant and will take longer to complete, it checks on all servers
     * for any slash command differences and updates them individually.
     * <br>
     * <b>Soft</b>: This is the faster but also less precise version, it checks only on the servers that are
     * specified by a command on the {@link pw.mihou.nexus.features.command.facade.NexusCommand}. This doesn't
     * support automatic deletion of commands.
     *
     * @param hardObservation Enable hard observation or not?
     * @return The {@link CompletableFuture} which indicates the progress of this task.
     */
    CompletableFuture<Void> observeOnAllShards(boolean hardObservation);

    /**
     * Retrieves and performs an observation check for all slash commands
     * in the specified server.
     *
     * @param server The server to observe and refer to.
     * @return The {@link CompletableFuture} which indicates the progress of this task.
     */
    CompletableFuture<Void> observeOnServer(Server server);

    /**
     * Retrieves and performs an observation check for all slash commands that are
     * registered globally. This is recommended to use in production, it isn't heavy and performs well.
     *
     * @return The {@link CompletableFuture} which indicates the progress of this task.
     */
    CompletableFuture<Void> observeOnGlobal();

}
