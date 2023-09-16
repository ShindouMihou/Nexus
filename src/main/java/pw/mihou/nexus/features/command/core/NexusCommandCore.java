package pw.mihou.nexus.features.command.core;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOption;
import pw.mihou.nexus.core.reflective.annotations.*;
import pw.mihou.nexus.features.command.validation.OptionValidation;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.facade.NexusHandler;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

/**
 * Nexus Command Core is the core implementation of a Nexus Command.
 * This is filled up with Reflection and isn't recommended being created
 * manually. This is done to provide the performance of a perfectly non-reflection
 * command framework.
 */
@MustImplement(clazz = NexusHandler.class)
public class NexusCommandCore implements NexusCommand {

    @InjectUUID
    public String uuid;

    @Stronghold
    private Map<String, Object> nexusCustomFields;

    @Required
    public String name;

    @WithDefault
    public Map<DiscordLocale, String> nameLocalizations = Collections.emptyMap();

    @Required
    public String description;

    @WithDefault
    public Map<DiscordLocale, String> descriptionLocalizations = Collections.emptyMap();

    @WithDefault
    public List<SlashCommandOption> options = Collections.emptyList();

    @WithDefault
    public List<OptionValidation<?>> validators = Collections.emptyList();

    @WithDefault
    public List<String> middlewares = Collections.emptyList();

    @WithDefault
    public List<String> afterwares = Collections.emptyList();
    @WithDefault
    public List<Long> serverIds = new ArrayList<>();
    @WithDefault
    public boolean defaultEnabledForEveryone = true;
    @WithDefault
    public boolean enabledInDms = true;
    @WithDefault
    public boolean defaultDisabled = false;
    @WithDefault
    public boolean nsfw = false;
    @WithDefault
    public List<PermissionType> defaultEnabledForPermissions = Collections.emptyList();
    @InjectReferenceClass
    public NexusHandler handler;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<SlashCommandOption> getOptions() {
        return options;
    }

    @Override
    public List<Long> getServerIds() {
        return serverIds;
    }

    @Override
    public NexusCommand associate(Long... serverIds) {
        this.serverIds = Stream.concat(this.serverIds.stream(), Stream.of(serverIds)).toList();
        return this;
    }

    @Override
    public NexusCommand disassociate(Long... serverIds) {
        List<Long> excludedSnowflakes = Arrays.asList(serverIds);
        this.serverIds = this.serverIds.stream().filter(snowflake -> !excludedSnowflakes.contains(snowflake)).toList();

        return this;
    }

    @Override
    public <T> Optional<T> get(String field, Class<T> type) {
        return get(field).map(object -> {
            if (type.isAssignableFrom(object.getClass())) {
                return type.cast(object);
            }

            return null;
        });
    }

    @Override
    public Optional<Object> get(String field) {
        return Optional.ofNullable(nexusCustomFields.get(field.toLowerCase()));
    }

    @Override
    public boolean isDefaultEnabledForEveryone() {
        return defaultEnabledForEveryone;
    }

    @Override
    public boolean isEnabledInDms() {
        return enabledInDms;
    }

    @Override
    public boolean isDefaultDisabled() {
        return defaultDisabled;
    }

    @Override
    public boolean isNsfw() {
        return nsfw;
    }

    @Override
    public List<PermissionType> getDefaultEnabledForPermissions() {
        return defaultEnabledForPermissions;
    }

    @Override
    public Map<DiscordLocale, String> getNameLocalizations() {
        return nameLocalizations;
    }

    @Override
    public Map<DiscordLocale, String> getDescriptionLocalizations() {
        return descriptionLocalizations;
    }

    @Override
    public String toString() {
        return "NexusCommandCore{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", options=" + options +
                ", serverId=" + getServerIds().toString() +
                '}';
    }
}
