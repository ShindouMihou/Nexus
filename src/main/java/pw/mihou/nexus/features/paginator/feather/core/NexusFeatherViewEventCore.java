package pw.mihou.nexus.features.paginator.feather.core;

import org.javacord.api.event.interaction.ButtonClickEvent;
import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherViewEvent;
import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherViewPager;

public record NexusFeatherViewEventCore(ButtonClickEvent event, NexusFeatherViewPager pager, String action)
        implements NexusFeatherViewEvent { }
