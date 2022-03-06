package pw.mihou.nexus.features.command.facade;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandUpdater;
import pw.mihou.nexus.commons.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface NexusCommand {

    /**
     * Gets the name of the command.
     *
     * @return The name of the command.
     */
    String getName();

    /**
     * Gets the description of the command.
     *
     * @return The description of the command.
     */
    String getDescription();

    /**
     * Gets the options of the command.
     *
     * @return The options of the command.
     */
    List<SlashCommandOption> getOptions();

    /**
     * Gets the cooldown of the command.
     *
     * @return The cooldown of the command.
     */
    Duration getCooldown();

    /**
     * Gets the required roles of the user to be able to execute
     * this command.
     *
     * @return The list of required roles that a user must have to be able
     * to execute the command.
     */
    List<Long> getRequiredRoles();

    /**
     * Gets the list of users who are only able to execute this command.
     *
     * @return The list of users that can execute this command.
     */
    List<Long> getRequiredUsers();

    /**
     * Gets the required permissions needed to execute this command.
     *
     * @return The list of required permissions needed to execute this command.
     */
    List<PermissionType> getPermissions();

    /**
     * Gets all the servers that this command is for.
     *
     * @return A list of server ids that this command is for.
     */
    List<Long> getServerIds();

    /**
     * Adds the specified server to the list of servers to
     * register this command with. If {@link pw.mihou.nexus.Nexus} has the
     * <b>autoApplySupportedServersChangesOnCommands</b> option enabled then it
     * will automatically update this change.
     *
     * @param serverIds The server ids to add support for this command.
     * @return {@link NexusCommand} instance for chain-calling methods.
     */
    NexusCommand addSupportFor(Long... serverIds);

    /**
     * Removes the specified server to the list of servers to
     * register this command with. If {@link pw.mihou.nexus.Nexus} has the
     * <b>autoApplySupportedServersChangesOnCommands</b> option enabled then it
     * will automatically update this change.
     *
     * @param serverIds The server ids to remove support for this command.
     * @return {@link NexusCommand} instance for chain-calling methods.
     */
    NexusCommand removeSupportFor(Long... serverIds);

    /**
     * Gets a specific custom field that is annotated with {@link pw.mihou.nexus.core.reflective.annotations.Share} from
     * the command's object store, this returns empty if the value does not match the type requested.
     *
     * @param field The field name to grab, case-insensitive.
     * @param type  The type that this field should match.
     * @return      The value of the field, if present and matches the type.
     */
    <T> Optional<T> get(String field, Class<T> type);

    /**
     * Gets a specific custom field that is annotated with {@link pw.mihou.nexus.core.reflective.annotations.Share} from
     * the command's object store, this returns empty if the value is not present. Not to be confused with
     * {@link NexusCommand#get(String, Class)} which performs type-checking.
     *
     * @param field The field name to grab, case-insensitive.
     * @return      The value of the field, if present.
     */
    Optional<Object> get(String field);

    /**
     * Is the default permission configuration of Discord enabled?
     *
     * @return Whether or not the default permission configuration of Discord is
     * enabled.
     */
    boolean isDefaultPermissionEnabled();

    /**
     * Gets the server id of the command.
     *
     * @return The server ID of the command.
     */
    @Deprecated
    long getServerId();

    /**
     * Transforms this into a slash command builder that can be used to create
     * the slash command yourself, it returns this via a {@link Pair} containing the server id
     * and the builder.
     *
     * @return The server id of the server this is intended (nullable) and the slash command builder.
     */
    default SlashCommandBuilder asSlashCommand() {
        SlashCommandBuilder builder = SlashCommand.with(getName().toLowerCase(), getDescription())
                .setDefaultPermission(isDefaultPermissionEnabled());

        if (!getOptions().isEmpty()) {
            return builder.setOptions(getOptions());
        }

        return builder;
    }

    /**
     * Transforms this into a slash command updater that can be used to update
     * the slash command yourself, it returns this via a {@link Pair} containing the server id
     * and the updater.
     *
     * @param commandId The ID of the command to update.
     * @return The server id of the server this is intended (nullable) and the slash command updater.
     */
    default SlashCommandUpdater asSlashCommandUpdater(long commandId) {
        SlashCommandUpdater updater = new SlashCommandUpdater(commandId)
                .setName(getName())
                .setDescription(getDescription())
                .setDefaultPermission(isDefaultPermissionEnabled());

        if(!getOptions().isEmpty()) {
            updater.setSlashCommandOptions(getOptions());
        }

        return updater;
    }

}
