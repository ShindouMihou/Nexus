package pw.mihou.nexus.features.command.core;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;

import java.util.HashMap;
import java.util.Map;

public class NexusCommandEventCore implements NexusCommandEvent {

    private final SlashCommandCreateEvent event;
    private final NexusCommand command;
    private final Map<String, Object> store = new HashMap<>();

    /**
     * Creates a new Nexus Event Core that is sent along with the Command Interceptors
     * and other sorts of handlers.
     *
     * @param event The base event received from Javacord.
     * @param command The command instance that is used.
     */
    public NexusCommandEventCore(SlashCommandCreateEvent event, NexusCommand command) {
        this.event = event;
        this.command = command;
    }

    @Override
    public SlashCommandCreateEvent getBaseEvent() {
        return event;
    }

    @Override
    public NexusCommand getCommand() {
        return command;
    }

    @Override
    public Map<String, Object> store() {
        return store;
    }
}
