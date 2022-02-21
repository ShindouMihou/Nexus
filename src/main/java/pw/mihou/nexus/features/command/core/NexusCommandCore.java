package pw.mihou.nexus.features.command.core;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandOption;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.reflective.annotations.*;
import pw.mihou.nexus.features.command.annotation.NexusAttach;
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
@InvokeIfAnnotated(annotation = NexusAttach.class, invokingClass = NexusCore.class, methodName = "addCommand")
public class NexusCommandCore implements NexusCommand {

    @InjectUUID
    public String uuid;

    @Required
    public String name;

    @Required
    public String description;

    @WithDefault
    public List<SlashCommandOption> options = Collections.emptyList();

    @WithDefault
    public Duration cooldown = Duration.ofSeconds(5);

    @WithDefault
    public List<Long> requiredRoles = Collections.emptyList();

    @WithDefault
    public List<Long> requiredUsers = Collections.emptyList();

    @WithDefault
    public List<PermissionType> requiredPermissions = Collections.emptyList();

    @WithDefault
    public List<String> middlewares = Collections.emptyList();

    @WithDefault
    public List<String> afterwares = Collections.emptyList();

    @WithDefault
    public List<Long> serverIds = new ArrayList<>();

    @WithDefault
    public boolean defaultPermission = true;

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
    public List<Long> getRequiredRoles() {
        return requiredRoles;
    }

    @Override
    public List<Long> getRequiredUsers() {
        return requiredUsers;
    }

    @Override
    public List<PermissionType> getPermissions() {
        return requiredPermissions;
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
    public boolean isDefaultPermissionEnabled() {
        return defaultPermission;
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
                ", requiredRoles=" + requiredRoles +
                ", requiredUsers=" + requiredUsers +
                ", requiredPermissions=" + requiredPermissions +
                ", serverId=" + getServerIds().toString() +
                '}';
    }
}
