package pw.mihou.nexus.features.paginator.feather.core;

import org.javacord.api.event.interaction.ButtonClickEvent;
import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherViewEvent;
import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherViewPager;

public class NexusFeatherViewEventCore implements NexusFeatherViewEvent {

    private final ButtonClickEvent event;
    private final NexusFeatherViewPager pager;
    private final String action;

    public NexusFeatherViewEventCore(ButtonClickEvent event, NexusFeatherViewPager pager, String action) {
        this.event = event;
        this.pager = pager;
        this.action = action;
    }

    @Override
    public ButtonClickEvent getEvent() {
        return event;
    }

    @Override
    public NexusFeatherViewPager getPager() {
        return pager;
    }

    @Override
    public String getAction() {
        return action;
    }
}
