package pw.mihou.nexus.features.command.core;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandOption;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.reflective.annotations.*;
import pw.mihou.nexus.features.command.annotation.NexusAttach;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.command.observer.facade.NexusObserver;
import pw.mihou.nexus.features.command.observer.modes.ObserverMode;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    public boolean serverOnly = false;

    @WithDefault
    public boolean privateChannelOnly = false;

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
        this.serverIds.addAll(Arrays.asList(serverIds));

        if (core.getConfiguration().autoApplySupportedServerChangesForServers()) {
            applyChangesOnSupportedServers(NexusObserver.createForWith(core, ObserverMode.MASTER));
        }

        return this;
    }

    @Override
    public NexusCommand removeSupportFor(Long... serverIds) {
        this.serverIds.removeAll(Arrays.asList(serverIds));

        if (core.getConfiguration().autoApplySupportedServerChangesForServers()) {
            CompletableFuture.runAsync(() -> {
                for (Long serverId : serverIds) {
                    Optional<Server> optionalServer = core.getShardManager()
                            .getShardOf(serverId)
                            .flatMap(api -> api.getServerById(serverId));

                    if (optionalServer.isEmpty()) {
                        NexusCore.logger.warn(
                                "A command failed to apply changes for a server since no shard on this JVM is handling the server... " +
                                        "please ignore if this is normal." +
                                        "[server={}, command={}]",
                                serverId, getName()
                        );
                        return;
                    }

                    Server server = optionalServer.orElseThrow(AssertionError::new);
                    server.getSlashCommands().join().forEach(slashCommand -> {
                        if (slashCommand.getName().equalsIgnoreCase(name)) {
                            long start = System.currentTimeMillis();
                            slashCommand.deleteForServer(server).join();
                            NexusCore.logger.info(
                                    "Application command was deleted for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                                    server.getId(),
                                    getName(),
                                    getDescription(),
                                    slashCommand.getId(),
                                    System.currentTimeMillis() - start
                            );
                        }
                    });
                }
            });
        }

        return null;
    }

    @Override
    public boolean isServerOnly() {
        return serverOnly;
    }

    @Override
    public boolean isPrivateChannelOnly() {
        return privateChannelOnly;
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
    public CompletableFuture<Void> applyChangesOnSupportedServers(NexusObserver observer) {
        return observer.applyChangesOnCommand(this);
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
                ", serverOnly=" + serverOnly +
                ", serverId=" + getServerIds().toString() +
                '}';
    }
}
