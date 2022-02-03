package pw.mihou.nexus.features.messages.defaults;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.messages.facade.NexusMessage;
import pw.mihou.nexus.features.messages.facade.NexusMessageConfiguration;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class NexusDefaultMessageConfiguration extends NexusMessageConfiguration {

    @Override
    public NexusMessage onRoleLockedCommand(NexusCommandEvent event, List<Long> roles) {
        return NexusMessage.fromWith(builder -> builder
                .setTitle("ERROR")
                .setDescription("You need at least one of the following roles to execute this command.")
                        .addField("Roles", roles.stream().map(id -> "<@&"+id+">").collect(Collectors.joining("\n")))
                        .setColor(Color.RED),
                builder -> builder.setFlags(InteractionCallbackDataFlag.EPHEMERAL)
        );
    }

    @Override
    public NexusMessage onMissingPermission(NexusCommandEvent event, List<PermissionType> permissions) {
        return NexusMessage.fromWith(builder -> builder
                .setTitle("ERROR")
                .setDescription("You are missing the following permissions to execute this command.")
                        .addField("Roles", permissions.stream().map(Enum::name).collect(Collectors.joining("\n")))
                        .setColor(Color.RED),
                builder -> builder.setFlags(InteractionCallbackDataFlag.EPHEMERAL)
        );
    }

    @Override
    public NexusMessage onRatelimited(NexusCommandEvent event, long remainingSeconds) {
        return NexusMessage.fromWith(
                "**SLOW DOWN**!\nYou are executing commands too fast, please try again in " + remainingSeconds + " seconds.",
                builder -> builder.setFlags(InteractionCallbackDataFlag.EPHEMERAL)
        );
    }

}
