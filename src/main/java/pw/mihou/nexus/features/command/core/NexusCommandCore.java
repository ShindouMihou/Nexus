package pw.mihou.nexus.features.command.core;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOption;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.reflective.annotations.*;
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
    public Duration cooldown = Duration.ofSeconds(5);

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
    public List<PermissionType> defaultEnabledForPermissions = Collections.emptyList();

    @InjectNexusCore
    public NexusCore core;

    @InjectReferenceClass
    public NexusHandler handler;

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
    public Duration getCooldown() {
        return cooldown;
    }

    @Override
    public List<Long> getServerIds() {
        return serverIds;
    }

    @Override
    public NexusCommand addSupportFor(Long... serverIds) {
        this.serverIds = Stream.concat(this.serverIds.stream(), Stream.of(serverIds)).toList();
        return this;
    }

    @Override
    public NexusCommand removeSupportFor(Long... serverIds) {
        List<Long> mutableList = new ArrayList<>(this.serverIds);
        mutableList.removeAll(Arrays.stream(serverIds).toList());

        this.serverIds = mutableList.stream().toList();
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
    public long getServerId() {
        return serverIds.get(0);
    }

    @Override
    public String toString() {
        return "NexusCommandCore{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", options=" + options +
                ", cooldown=" + cooldown +
                ", serverId=" + getServerIds().toString() +
                '}';
    }
}
