package pw.mihou.nexus.core.managers.facade;

import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommand;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.managers.records.NexusCommandIndex;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.*;
import java.util.function.Function;

public interface NexusCommandManager {

    /**
     * Gets all the commands that are stored inside the Nexus registry
     * of commands.
     *
     * @return All the commands stored in Nexus's command registry.
     */
    Collection<NexusCommand> getCommands();

    /**
     * Gets all the global commands that are stored inside the Nexus registry of commands.
     * <br><br>
     * In this scenario, the definition of a global command is a command that does not have an association
     * with a server.
     *
     * @return All the global commands that were created inside the registry.
     */
    default Set<NexusCommand> getGlobalCommands() {
        Set<NexusCommand> commands = new HashSet<>();
        for (NexusCommand command : getCommands()) {
            if (!command.isServerCommand()) continue;
            commands.add(command);
        }

        return commands;
    }

    /**
     * Gets all the server commands that are stored inside the Nexus registry of commands.
     * <br><br>
     * In this scenario, the definition of a server command is a command that does have an association
     * with a server.
     *
     * @return All the server commands that were created inside the registry.
     */
    default Set<NexusCommand> getServerCommands() {
        Set<NexusCommand> commands = new HashSet<>();
        for (NexusCommand command : getCommands()) {
            if (command.isServerCommand()) continue;
            commands.add(command);
        }

        return commands;
    }

    /**
     * Gets all the commands that have an association with the given server.
     * <br><br>
     * This method does a complete O(n) loop over the commands to identify any commands that matches the
     * {@link List#contains(Object)} predicate over its server ids.
     *
     * @param server The server to find all associated commands of.
     * @return All associated commands of the given server.
     */
    default Set<NexusCommand> getCommandsAssociatedWith(long server) {
        Set<NexusCommand> commands = new HashSet<>();
        for (NexusCommand command : getCommands()) {
            if (!command.getServerIds().contains(server)) continue;
            commands.add(command);
        }

        return commands;
    }

    /**
     * Adds a command to the registry.
     *
     * @param command The command to add.
     * @return The {@link Nexus} for chain-calling methods.
     */
    Nexus addCommand(NexusCommand command);

    /**
     * Gets the command that matches the {@link SlashCommand#getId()}. This can return empty
     * if the indexing is still in progress and no commands have a slash command index.
     *
     * @param id The ID of the slash command to look for.
     * @return The first command that matches the ID specified.
     */
    Optional<NexusCommand> getCommandById(long id);

    /**
     * Gets the command that matches the special UUID assigned to all {@link NexusCommand}. This is useful
     * for when you want to retrieve a command by only have a UUID which is what most Nexus methods will return.
     *
     * @param uuid The UUID of the command to look for.
     * @return The first command that matches the UUID specified.
     */
    Optional<NexusCommand> getCommandByUUID(String uuid);

    /**
     * Gets the command that matches the {@link NexusCommand#getName()}. This will only fetch the first one that
     * matches and therefore can ignore several other commands that have the same name.
     *
     * @param name The name of the command to look for.
     * @return The first command that matches the name specified.
     */
    default Optional<NexusCommand> getCommandByName(String name) {
        return getCommands().stream().filter(nexusCommand -> nexusCommand.getName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Gets the command that matches the {@link NexusCommand#getName()} and {@link NexusCommand#getServerIds()}. This is a precise
     * method that fetches a server-only command, this will always return empty if there are no server slash commands.
     *
     * @param name The name of the command to look for.
     * @param server The ID of the command to fetch.
     * @return The first command that matches both the name and the server id.
     */
    Optional<NexusCommand> getCommandByName(String name, long server);

    /**
     * Exports the indexes that was created which can then be used to create a database copy of the given indexes.
     * <br><br>
     * It is not recommended to use this for any other purposes other than creating a database copy because this creates
     * more garbage for the garbage collector.
     *
     * @return A snapshot of the in-memory indexes that the command manager has.
     */
    List<NexusCommandIndex> export();


    /**
     * Creates a new fresh in-memory index map by using the {@link NexusCommandManager#index()} method before creating
     * an export of the given indexes with the {@link NexusCommandManager#export()} command.
     *
     * @return A snapshot of the in-memory indexes that the command manager has.
     */
    default List<NexusCommandIndex> indexThenExport() {
        index();
        return export();
    }

    /**
     * This indexes all the commands whether it'd be global or server commands to increase
     * performance and precision of slash commands.
     */
    void index();

    /**
     * Creates an in-memory index mapping of the given command and the given slash command snowflake.
     * <br><br>
     * You can use this method to index commands from your database.
     *
     * @param command   The command that will be associated with the given snowflake.
     * @param snowflake The snowflake that will be associated with the given command.
     */
    void index(NexusCommand command, long snowflake);

    /**
     * Massively creates an in-memory index mapping for all the given command using the indexer reducer
     * provided below.
     * <br><br>
     * This is a completely synchronous operation and could cause major blocking on the application if you use
     * it daringly.
     *
     * @param indexer The indexer reducer to collectively find the index of the commands.
     */
    default void index(Function<NexusCommand, Long> indexer) {
        for (NexusCommand command : getCommands()) {
            index(command, indexer.apply(command));
        }
    }

    /**
     * Creates an in-memory index of all the slash commands provided. This will map all the commands based on properties
     * that matches e.g. the name (since a command can only have one global and one server command that have the same name)
     * and the server property if available.
     *
     * @param applicationCommandList the command list to use for indexing.
     */
    default void index(Set<ApplicationCommand> applicationCommandList) {
        for (ApplicationCommand applicationCommand : applicationCommandList) {
            index(applicationCommand);
        }
    }

    /**
     * Creates an in-memory index of the given slash command provided. This will map all the command based on the property
     * that matches e.g. the name (since a command can only have one global and one server command that have the same name)
     * and the server property  if available.
     *
     * @param command The command to index.
     */
    void index(ApplicationCommand command);

}
