package pw.mihou.nexus.features.messages.facade;

import org.javacord.api.entity.permission.PermissionType;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;

import java.util.List;

public abstract class NexusMessageConfiguration {

    /**
     * This is executed whenever the user is missing a specific role to execute
     * a certain command.
     *
     * @param event The Nexus Event instance received.
     * @param roles The list of roles allowed.
     * @return The {@link NexusMessage} response that will be sent to the end-user.
     */
    public abstract NexusMessage onRoleLockedCommand(NexusCommandEvent event, List<Long> roles);

    /**
     * This is executed whenever the user is missing the specific permissions to execute
     * a certain command.
     *
     * @param event The Nexus Event instance received.
     * @param permissions The list of roles allowed.
     * @return The {@link NexusMessage} response that will be sent to the end-user.
     */
    public abstract NexusMessage onMissingPermission(NexusCommandEvent event, List<PermissionType> permissions);

    /**
     * This is executed whenever the user is rate-limited, or under cooldown
     * on a certain command.
     *
     * @param event The Nexus Event instance received.
     * @param remainingSeconds The remaining time in seconds.
     * @return The {@link NexusMessage} response that will be sent to the end-user.
     */
    public abstract NexusMessage onRatelimited(NexusCommandEvent event, long remainingSeconds);

}
