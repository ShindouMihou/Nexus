package pw.mihou.nexus.core.managers.facade;

import org.javacord.api.interaction.SlashCommand;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface NexusCommandManager {

    /**
     * Gets all the commands that are stored inside the Nexus registry
     * of commands.
     *
     * @return All the commands stored in Nexus's command registry.
     */
    List<NexusCommand> getCommands();

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
     * @param UUID The UUID of the command to look for.
     * @return The first command that matches the UUID specified.
     */
    Optional<NexusCommand> getCommandByUUID(String UUID);

    /**
     * Gets the command that matches the {@link NexusCommand#getName()}. This will only fetch the first one that
     * matches and therefore can ignore several other commands that have the same name.
     *
     * @param name The name of the command to look for.
     * @return The first command that matches the name specified.
     */
    Optional<NexusCommand> getCommandByName(String name);

    /**
     * Gets the command that matches the {@link NexusCommand#getName()} and {@link NexusCommand#getServerId()}. This is a precise
     * method that fetches a server-only command, this will always return empty if there are no server slash commands.
     *
     * @param name The name of the command to look for.
     * @param server The ID of the command to fetch.
     * @return The first command that matches both the name and the server id.
     */
    Optional<NexusCommand> getCommandByName(String name, long server);

    /**
     * This indexes all the commands whether it'd be global or server commands to increase
     * performance and precision of slash commands.
     */
    void index();

}
