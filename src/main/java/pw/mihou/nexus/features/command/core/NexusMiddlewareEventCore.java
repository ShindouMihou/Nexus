package pw.mihou.nexus.features.command.core;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusMiddlewareEvent;

import java.util.Map;

public record NexusMiddlewareEventCore(NexusCommandEvent event) implements NexusMiddlewareEvent {

    @Override
    public SlashCommandCreateEvent getBaseEvent() {
        return event.getBaseEvent();
    }

    @Override
    public NexusCommand getCommand() {
        return event.getCommand();
    }

    @Override
    public Map<String, Object> store() {
        return event.store();
    }

}
