package pw.mihou.nexus.features.command.facade;

import kotlin.Pair;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.*;
import pw.mihou.nexus.core.exceptions.NoSuchAfterwareException;
import pw.mihou.nexus.core.exceptions.NoSuchMiddlewareException;
import pw.mihou.nexus.features.commons.NexusApplicationCommand;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;
import pw.mihou.nexus.features.command.validation.OptionValidation;

import java.util.*;
import java.util.stream.Collectors;

public interface NexusCommand extends NexusApplicationCommand {

    long PLACEHOLDER_SERVER_ID = 0L;

    static Map<DiscordLocale, String> createLocalized(Pair<DiscordLocale, String>... entries) {
        return Arrays.stream(entries).collect(Collectors.toMap(Pair::component1, Pair::component2));
    }

    static List<SlashCommandOption> createOptions(SlashCommandOption... options) {
        return Arrays.stream(options).toList();
    }

    static List<OptionValidation<?>> createValidators(OptionValidation<?>... validators) {
        return Arrays.stream(validators).toList();
    }

    static List<String> createMiddlewares(String... middlewares) {
        List<String> list = new ArrayList<>();
        for (String middleware : middlewares) {
            if (!NexusCommandInterceptorCore.hasMiddleware(middleware)) throw new NoSuchMiddlewareException(middleware);
            list.add(middleware);
        }
        return list;
    }

    static List<String> createAfterwares(String... afterwares) {
        List<String> list = new ArrayList<>();
        for (String afterware : afterwares) {
            if (!NexusCommandInterceptorCore.hasAfterware(afterware)) throw new NoSuchAfterwareException(afterware);
            list.add(afterware);
        }
        return list;
    }

    static List<Long> with(long... servers) {
        return Arrays.stream(servers).boxed().toList();
    }

    static List<PermissionType> createDefaultEnabledForPermissions(PermissionType... permissions) {
        return Arrays.stream(permissions).toList();
    }

    /**
     * Gets the unique identifier of the command, this tends to be the command name unless the
     * command has an override using the {@link pw.mihou.nexus.features.command.annotation.IdentifiableAs} annotation.
     * <br>
     * There are many use-cases for this unique identifier such as retrieving the index (application command id) of the
     * command from the index store to be used for mentioning the command, and some other more.
     *
     * @return the unique identifier of the command.
     */
    String getUuid();

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
     * Gets all the servers that this command is for.
     *
     * @return A list of server ids that this command is for.
     */
    List<Long> getServerIds();

    /**
     * Checks whether this command is a server command.
     * <br><br>
     * A server command can be a server that does have an entry on its associated server ids.
     *
     * @return Is this a server command.
     */
    default boolean isServerCommand() {
        return !getServerIds().isEmpty();
    }

    /**
     * Associates this command from the given servers, this can be used to include this command into the command list
     * of the server after batching updating.
     * <br><br>
     * <b>This does not perform any changes onto Discord.</b>
     * <br><br>
     * If you want to update changes then please use
     * the {@link pw.mihou.nexus.features.command.synchronizer.NexusSynchronizer#batchUpdate(long)} method
     * <b>after using this method</b>.
     *
     * @param serverIds The snowflakes of the servers to disassociate this command from.
     * @return the current and updated {@link NexusCommand} instance for chain-calling methods.
     */
    NexusCommand associate(Long... serverIds);

    /**
     * Disassociates this command from the given servers, removing any form of association with
     * the given servers.
     * <br><br>
     * <b>This does not perform any changes onto Discord.</b> If you want to update changes then please use
     * the {@link pw.mihou.nexus.features.command.synchronizer.NexusSynchronizer#batchUpdate(long)} method
     * <b>after using this method</b>.
     *
     * @param serverIds The snowflakes of the servers to disassociate this command from.
     * @return the current and updated {@link NexusCommand} instance for chain-calling methods.
     */
    NexusCommand disassociate(Long... serverIds);

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
     * Checks whether the command is not-safe-for-work or not.
     * @return Whether the command is not-safe-for-work or not.
     */
    boolean isNsfw();

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
     * Transforms this into a slash command builder that can be used to create the slash command.
     * @return the {@link SlashCommandBuilder}.
     */
    default SlashCommandBuilder asSlashCommand() {
        SlashCommandBuilder builder = SlashCommand.with(getName().toLowerCase(), getDescription())
                .setEnabledInDms(isEnabledInDms());

        getNameLocalizations().forEach(builder::addNameLocalization);
        getDescriptionLocalizations().forEach(builder::addDescriptionLocalization);

        builder.setNsfw(isNsfw());
        if (isDefaultDisabled()) {
            builder.setDefaultDisabled();
        }

        if (isDefaultEnabledForEveryone() && !isDefaultDisabled()) {
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
     * Transforms this into a slash command updater that can be used to update the slash command yourself.
     *
     * @param commandId The ID of the command to update.
     * @return the {@link SlashCommandUpdater}.
     */
    default SlashCommandUpdater asSlashCommandUpdater(long commandId) {
        SlashCommandUpdater updater = new SlashCommandUpdater(commandId)
                .setName(getName())
                .setDescription(getDescription())
                .setEnabledInDms(isEnabledInDms());

        getNameLocalizations().forEach(updater::addNameLocalization);
        getDescriptionLocalizations().forEach(updater::addDescriptionLocalization);

        // TODO: Add support for `nsfw` update once new version out. Refers to (https://github.com/Javacord/Javacord/pull/1225).
        if (isDefaultDisabled()) {
            updater.setDefaultDisabled();
        }

        if (isDefaultEnabledForEveryone() && !isDefaultDisabled()) {
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
