package pw.mihou.nexus.features.paginator.feather;

import pw.mihou.nexus.features.paginator.feather.core.NexusFeatherViewPagerCore;
import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherView;
import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherViewPager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NexusFeatherPaging {

    public static final Map<String, NexusFeatherView> views = new ConcurrentHashMap<>();

    /**
     * Creates a new {@link NexusFeatherViewPager} that can be used to initialize the buttons and other
     * related data to use for feather paging.
     *
     * @param initialKey The initial key that will be included in the buttons.
     * @param type       The type of this feather paging.
     * @param view       The general view or handler for this paging.
     * @return A {@link NexusFeatherViewPager} that can be used to initialize the buttons, etc.
     */
    public static NexusFeatherViewPager create(String initialKey, String type, NexusFeatherView view) {
        views.put(type, view);

        return new NexusFeatherViewPagerCore(initialKey, type);
    }

    /**
     * Discards the {@link NexusFeatherView} that is being used to handle events with that key. This prevents
     * any handling on the {@link org.javacord.api.event.interaction.ButtonClickEvent} that matches the type.
     *
     * @param type The type to discard.
     */
    public static void discard(String type) {
        views.remove(type);
    }


}
