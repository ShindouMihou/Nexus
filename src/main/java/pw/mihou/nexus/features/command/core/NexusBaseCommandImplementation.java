package pw.mihou.nexus.features.command.core;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.interceptors.core.NexusCommandInterceptorCore;
import pw.mihou.nexus.features.command.interceptors.core.NexusMiddlewareGateCore;
import pw.mihou.nexus.features.messages.core.NexusMessageCore;
import pw.mihou.nexus.features.ratelimiter.core.NexusRatelimiterCore;
import pw.mihou.nexus.features.ratelimiter.facade.NexusRatelimitData;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NexusBaseCommandImplementation {

    private final NexusCommandCore instance;

    /**
     * Creates a new Base Command instance that is used to handle all the command
     * processing, gating, etc.
     *
     * @param instance The command instance.
     */
    public NexusBaseCommandImplementation(NexusCommandCore instance) {
        this.instance = instance;
    }

    /**
     * Checks whether the user can use this command or not via
     * the required permissions.
     *
     * @param server The server instance to use, nullable.
     * @param user The user instance to use.
     * @return Can the user use this command?
     */
    public boolean applyPermissionRestraint(Server server, User user) {
        return server == null || instance.getPermissions().isEmpty() || server.getPermissions(user)
                .getAllowedPermission().containsAll(instance.getPermissions());
    }

    /**
     * Checks whether the user can use this command or not via the
     * required roles.
     *
     * @param server The server instance to use, nullable.
     * @param user The user instance to use.
     * @return Can the user use this command?
     */
    public boolean applyRoleRestraints(Server server, User user) {
        return server == null || instance.getRequiredRoles().isEmpty() || instance.getRequiredRoles()
                .stream()
                .anyMatch(aLong -> server.getRoles(user)
                        .stream()
                        .map(Role::getId)
                        .anyMatch(r -> Objects.equals(aLong, r))
                );
    }

    /**
     * Applies general restraints for slash commands.
     *
     * @param event The event instance to use.
     * @return Can the user use this command?
     */
    public boolean applyRestraints(SlashCommandCreateEvent event) {
        if (instance.isServerOnly() && !event.getSlashCommandInteraction().getServer().isPresent())
            return false;

        if (instance.isPrivateChannelOnly() && event.getSlashCommandInteraction().getServer().isPresent())
            return false;

        // We need this condition in case Discord decides to go through with their
        // optional channel thing which sounds odd.
        if (!event.getSlashCommandInteraction().getChannel().isPresent())
            throw new IllegalStateException("The channel is somehow not present; this is possibly a change in Discord's side " +
                    "and may need to be addressed, please send an issue @ https://github.com/ShindouMihou/Nexus");

        if (!instance.getRequiredUsers().isEmpty() && !instance.getRequiredUsers()
                .contains(event.getInteraction().getUser().getId()))
            return false;

        return true;
    }

    /**
     * Applies the rate-limiter.
     *
     * @param user The user to rate-limit.
     * @param server The server where the command was executed.
     * @param onLimited If the user is rate-limited.
     * @param onSuccess If the command can be executed.
     */
    public void applyRatelimiter(long user, long server, Consumer<Long> onLimited, Consumer<NexusRatelimitData> onSuccess) {
        ((NexusRatelimiterCore) instance.core.getRatelimiter())
                .ratelimit(user, server, instance, onLimited, onSuccess);
    }

    public void dispatch(SlashCommandCreateEvent event) {
        NexusCommandEvent nexusEvent = new NexusCommandEventCore(event, instance);
        NexusMiddlewareGateCore middlewareGate = (NexusMiddlewareGateCore) NexusCommandInterceptorCore.interceptWithMany(instance.middlewares, nexusEvent);

        if (!middlewareGate.isAllowed()) {
            if (middlewareGate.getResponse() != null) {
                ((NexusMessageCore) middlewareGate.getResponse())
                        .convertTo(nexusEvent.respondNow())
                        .respond()
                        .exceptionally(ExceptionLogger.get());;
            }
            return;
        }

        if (!applyRestraints(event)) {
            return;
        }

        if (nexusEvent.getServer().isPresent()) {
            if (!applyPermissionRestraint(nexusEvent.getServer().get(), nexusEvent.getUser())) {
                ((NexusMessageCore) instance.core.getMessageConfiguration()
                        .onMissingPermission(nexusEvent, instance.getPermissions()))
                        .convertTo(nexusEvent.respondNow())
                        .respond()
                        .exceptionally(ExceptionLogger.get());
                return;
            }

            if (!applyRoleRestraints(nexusEvent.getServer().get(), nexusEvent.getUser())) {
                ((NexusMessageCore) instance.core.getMessageConfiguration()
                        .onRoleLockedCommand(nexusEvent, instance.getRequiredRoles()))
                        .convertTo(nexusEvent.respondNow())
                        .respond()
                        .exceptionally(ExceptionLogger.get());
                return;
            }
        }

        applyRatelimiter(nexusEvent.getUserId(), nexusEvent.getServerId().orElse(nexusEvent.getUserId()), remaining -> {
            if (remaining > 0) {
                ((NexusMessageCore) instance.core.getMessageConfiguration()
                        .onRatelimited(nexusEvent, remaining))
                        .convertTo(nexusEvent.respondNow())
                        .respond()
                        .exceptionally(ExceptionLogger.get());
            }
        }, nexusRatelimitData -> CompletableFuture.runAsync(() -> instance.handler.onEvent(nexusEvent), NexusThreadPool.executorService)
                .thenAcceptAsync(unused -> NexusCommandInterceptorCore.interceptWithMany(instance.afterwares, nexusEvent)));
    }

}
