package pw.mihou.nexus.features.paginator.feather.facades;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface NexusFeatherViewEvent {

    ButtonClickEvent event();

    /**
     * Gets the {@link NexusFeatherViewPager} of this event which contains critical fields that enables
     * pagination. It contains fields such as the current key, type of event and other related information.
     *
     * @return The {@link NexusFeatherViewPager} of this event.
     */
    NexusFeatherViewPager pager();

    /**
     * Gets the action of this view event. An action is a defined event that is specified during the creation
     * of {@link NexusFeatherView} that specifies what action is being done at this moment. For example, when
     * a user presses the next button, the action will be "next".
     *
     * @return The action of this view event.
     */
    String action();

    /**
     * Gets the interaction received from Javacord.
     *
     * @return The interaction that was received from Javacord.
     */
    default ButtonInteraction getInteraction() {
        return event().getButtonInteraction();
    }

    /**
     * Gets the user received from Javacord.
     *
     * @return The user that was received from Javacord.
     */
    default User getUser() {
        return getInteraction().getUser();
    }

    /**
     * Gets the channel that isn't an optional. This exists primarily because there is no currently possible
     * way for the text channel to be optional in Discord Slash Commands.
     *
     * @return The text channel that is being used.
     */
    default TextChannel getChannel() {
        return getInteraction().getChannel().orElseThrow(() -> new NoSuchElementException(
                "It seems like the text channel is no longer always available. Please create an issue on https://github.com/ShindouMihou/Nexus"
        ));
    }

    /**
     * Gets the server instance.
     *
     * @return The server instance.
     */
    default Optional<Server> getServer() {
        return getInteraction().getServer();
    }

    /**
     * Gets the ID of the server.
     *
     * @return The ID of the server.
     */
    default Optional<Long> getServerId() {
        return getInteraction().getServer().map(Server::getId);
    }

    /**
     * Gets the ID of the text channel where the command was executed.
     *
     * @return The ID of the text channel where the command was executed.
     */
    default Long getChannelId() {
        return getChannel().getId();
    }

    /**
     * Gets the ID of the user that executed the command.
     *
     * @return The ID of the user who executed this command.
     */
    default Long getUserId() {
        return getUser().getId();
    }

    /**
     * Gets the text channel as a server text channel.
     *
     * @return The text channel as a server text channel.
     */
    default Optional<ServerTextChannel> getServerTextChannel() {
        return getChannel().asServerTextChannel();
    }

    /**
     * Gets the Discord API shard that was in charge of this event.
     *
     * @return The Discord API shard that was in charge of this event.
     */
    default DiscordApi getApi() {
        return event().getApi();
    }

    /**
     * Gets the message of this event.
     *
     * @return The message of this event.
     */
    default Message getMessage() {
        return event().getButtonInteraction().getMessage();
    }

}
