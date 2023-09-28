package pw.mihou.nexus.features.paginator.feather.facades;

import org.javacord.api.entity.message.component.ButtonBuilder;

public interface NexusFeatherViewPager {

    /**
     * Gets the key of this view event. A key is a unique identifier that is specified during
     * the creation of {@link NexusFeatherView} that can be used as either the page or the last
     * unique key of the last item in the pager.
     *
     * You are able to use the key to query for the next item.
     *
     * @return The key of this view event.
     */
    String key();

    /**
     * Gets the type of this view event. A type, otherwise known as event, is the event specified
     * during the creation of {@link NexusFeatherView} that can be used to identify what event we
     * are handling. For example, we want to handle pagination for all the quests of the user which
     * makes us use the "quest" type.
     *
     * @return The type of this view event.
     */
    String type();

    /**
     * Makes one component that is identifiable by {@link pw.mihou.nexus.features.paginator.feather.NexusFeatherPaging}
     * and can be routed to the same event as this one.
     *
     * @param newKey The new key of this component, can be found as the next {@link NexusFeatherViewPager#key()}.
     * @param action The action of this component, can be found as the next {@link NexusFeatherViewEvent#action()}
     * @return A {@link ButtonBuilder} with the custom identifier specified in a custom manner.
     */
    default ButtonBuilder makeWith(String newKey, String action) {
        return new ButtonBuilder().setCustomId(type() + "[$;" + newKey + "[$;" + action);
    }

    /**
     * Makes one component that is identifiable by {@link pw.mihou.nexus.features.paginator.feather.NexusFeatherPaging}
     * and can be routed to the same event as this one.
     *
     * Not to be confused with {@link NexusFeatherViewPager#makeWith(String, String)} which uses a different key.
     * It is recommended to use this method only for initial creation and
     * {@link NexusFeatherViewPager#makeWith(String, String)} for next keys, etc.
     *
     * @param action The action of this component, can be found as the next {@link NexusFeatherViewEvent#action()}
     * @return A {@link ButtonBuilder} with the custom identifier specified in a custom manner.
     */
    default ButtonBuilder makeWithCurrentKey(String action) {
        return new ButtonBuilder().setCustomId(type() + "[$;" + key() + "[$;" + action);
    }

}
