package pw.mihou.nexus.features.command.facade;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.*;
import pw.mihou.nexus.commons.Pair;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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
     * Checks whether the command is default enabled for everyone or not.
     *
     * @return Whether this command is default enabled for everyone or not.
     */
    boolean isDefaultEnabledForEveryone();

    /**
     * Checks whether the command is enabled in DMs or not.
     *
     * @return Whether the command is enabled in DMs or not.
     */
    boolean isEnabledInDms();

    /**
     * Checks whether the command is default disabled or not.
     *
     * @return Whether the command is default disabled or not.
     */
    boolean isDefaultDisabled();

    /**
     * Gets the permission types required to have this command enabled for that user.
     *
     * @return The permission types required for this command to be enabled for
     * that specific user.
     */
    List<PermissionType> getDefaultEnabledForPermissions();

    /**
     * Gets all the names in different localizations for this slash command.
     *
     * @return All the name localizations of this command.
     */
    Map<DiscordLocale, String> getNameLocalizations();

    /**
     * Gets all the description in different localizations for this slash command.
     *
     * @return All the description localizations of this command.
     */
    Map<DiscordLocale, String> getDescriptionLocalizations();

    /**
     * Gets the description localized value for the given localization for this slash command.
     *
     * @param locale The locale to get from.
     * @return The localized description for this slash command, if present.
     */
    default Optional<String> getDescriptionLocalization(DiscordLocale locale) {
        return Optional.ofNullable(getDescriptionLocalizations().get(locale));
    }

    /**
     * Gets the name localized value for the given localization for this slash command.
     *
     * @param locale The locale to get from.
     * @return The localized name for this slash command, if present.
     */
    default Optional<String> getNameLocalization(DiscordLocale locale) {
        return Optional.ofNullable(getDescriptionLocalizations().get(locale));
    }

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
                .setEnabledInDms(isEnabledInDms());

        getNameLocalizations().forEach(builder::addNameLocalization);
        getDescriptionLocalizations().forEach(builder::addDescriptionLocalization);

        if (isDefaultDisabled()) {
            builder.setDefaultDisabled();
        }

        if (isDefaultEnabledForEveryone()) {
            builder.setDefaultEnabledForEveryone();
        }

        if (!getDefaultEnabledForPermissions().isEmpty()) {
            builder.setDefaultEnabledForPermissions(getDefaultEnabledForPermissions().toArray(PermissionType[]::new));
        }

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
                .setEnabledInDms(isEnabledInDms());

        getNameLocalizations().forEach(updater::addNameLocalization);
        getDescriptionLocalizations().forEach(updater::addDescriptionLocalization);

        if (isDefaultDisabled()) {
            updater.setDefaultDisabled();
        }

        if (isDefaultEnabledForEveryone()) {
            updater.setDefaultEnabledForEveryone();
        }

        if (!getDefaultEnabledForPermissions().isEmpty()) {
            updater.setDefaultEnabledForPermissions(getDefaultEnabledForPermissions().toArray(PermissionType[]::new));
        }


        if(!getOptions().isEmpty()) {
            updater.setSlashCommandOptions(getOptions());
        }

        return updater;
    }

}
