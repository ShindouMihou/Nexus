package pw.mihou.nexus.features.command.interceptors.commons.modules.auth;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

import java.util.List;

public class NexusAuthMiddleware {

    /**
     * Executed when the auth middleware is dedicated to the permissions
     * authentication.
     *
     * @param event The event to manage.
     */
    @SuppressWarnings("unchecked")
    public static void onPermissionsAuthenticationMiddleware(NexusMiddlewareEvent event) {
        if (event.getServer().isEmpty()) {
            event.stop();
            return;
        }

        Object field = event.getCommand().get("requiredPermissions").orElseThrow(() -> new IllegalStateException(
                event.getCommand().getName() + " has permission authentication middleware but doesn't have requiredPermissions shared field."
        ));

        List<PermissionType> permissionTypes = null;
        if (field instanceof List<?> && !((List<?>) field).isEmpty()) {
            if (((List<?>) field).get(0) instanceof PermissionType) {
                permissionTypes = (List<PermissionType>) field;
            }
        }

        Server server = event.getServer().get();
        if (permissionTypes == null) {
            throw new IllegalStateException(
                    event.getCommand().getName() + " has permission authentication middleware but doesn't have requiredPermissions " +
                            "that matches List<PermissionType> type."
            );
        }

        event.stopIf(
                !server.getPermissions(event.getUser()).getAllowedPermission().containsAll(permissionTypes),
                ((NexusCore) event.getNexus()).getMessageConfiguration().onMissingPermission(event, permissionTypes)
        );
    }

    /**
     * Executed when the auth middleware is dedicated to the roles
     * authentication.
     *
     * @param event The event to manage.
     */
    @SuppressWarnings("unchecked")
    public static void onRoleAuthenticationMiddleware(NexusMiddlewareEvent event) {
        if (event.getServer().isEmpty()) {
            event.stop();
            return;
        }

        Object field = event.getCommand().get("requiredRoles").orElseThrow(() -> new IllegalStateException(
                event.getCommand().getName() + " has role authentication middleware but doesn't have requiredRoles shared field."
        ));

        List<Long> roles = null;
        if (field instanceof List<?> && !((List<?>) field).isEmpty()) {
            if (((List<?>) field).get(0) instanceof Long) {
                roles = (List<Long>) field;
            }
        }

        Server server = event.getServer().get();
        if (roles == null) {
            throw new IllegalStateException(
                    event.getCommand().getName() + " has role authentication middleware but doesn't have requiredRoles " +
                            "that matches List<Long> type."
            );
        }

        event.stopIf(
                roles.stream().noneMatch(roleId ->
                        server.getRoles(event.getUser())
                        .stream()
                        .map(Role::getId)
                        .anyMatch(userRoleId -> userRoleId.equals(roleId))
                ),
                ((NexusCore) event.getNexus()).getMessageConfiguration().onRoleLockedCommand(event, roles)
        );
    }

    /**
     * Executed when the auth middleware is dedicated to the user
     * authentication.
     *
     * @param event The event to manage.
     */
    @SuppressWarnings("unchecked")
    public static void onUserAuthenticationMiddleware(NexusMiddlewareEvent event) {
        if (event.getServer().isEmpty()) {
            event.stop();
            return;
        }

        Object field = event.getCommand().get("requiredUsers").orElseThrow(() -> new IllegalStateException(
                event.getCommand().getName() + " has user authentication middleware but doesn't have requiredUsers shared field."
        ));

        List<Long> users = null;
        if (field instanceof List<?> && !((List<?>) field).isEmpty()) {
            if (((List<?>) field).get(0) instanceof Long) {
                users = (List<Long>) field;
            }
        }

        Server server = event.getServer().get();
        if (users == null) {
            throw new IllegalStateException(
                    event.getCommand().getName() + " has user authentication middleware but doesn't have requiredUsers " +
                            "that matches List<Long> type."
            );
        }

        event.stopIf( !users.contains(event.getUserId()));
    }

    /**
     * Executed when the auth middleware is dedicated to the bot owner
     * authentication.
     *
     * @param event The event to manage.
     */
    public static void onBotOwnerAuthenticationMiddleware(NexusMiddlewareEvent event) {
        event.stopIf(
                !event.getUser().isBotOwner(),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the bot owner to execute this command.")
        );
    }

    /**
     * Executed when the auth middleware is dedicated to the server owner
     * authentication.
     *
     * @param event The event to manage.
     */
    public static void onServerOwnerAuthenticationMiddleware(NexusMiddlewareEvent event) {
        event.stopIf(
                event.getServer().isPresent() && event.getServer().get().isOwner(event.getUser()),
                NexusMessage.fromEphemereal("**PERMISSION DENIED**\nYou need to be the server owner to execute this command.")
        );
    }

}
