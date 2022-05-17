package pw.mihou.nexus.features.messages.core;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.nexus.features.command.core.NexusCommandDispatcher;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

import java.util.Optional;
import java.util.function.Function;

public class NexusMessageCore implements NexusMessage {

    private final String textVal;
    private final EmbedBuilder embedVal;
    private Function<InteractionImmediateResponseBuilder, InteractionImmediateResponseBuilder> builder =
            bob -> bob.setFlags(InteractionCallbackDataFlag.EPHEMERAL);
    private static final AllowedMentions ALLOWED_MENTIONS = new AllowedMentionsBuilder()
            .setMentionRoles(false)
            .setMentionUsers(false)
            .setMentionEveryoneAndHere(false)
            .build();

    private NexusMessageCore(String textVal, EmbedBuilder embedVal) {
        this.textVal = textVal;
        this.embedVal = embedVal;
    }

    /**
     * Creates a new {@link NexusMessageCore} instance that allows for a text
     * response instead of an embed response.
     *
     * @param textValue The text value to send to the user.
     */
    public NexusMessageCore(String textValue) {
        this(textValue, null);
    }

    /**
     * Creates a new {@link NexusMessageCore} instance that allows for an embed
     * response instead of a text response.
     *
     * @param embedValue The embed to send to the user.
     */
    public NexusMessageCore(EmbedBuilder embedValue) {
        this(null, embedValue);
    }

    /**
     * An internal method that is utilized by the likes of {@link NexusCommandDispatcher} to
     * transform the {@link NexusMessage} into an {@link InteractionImmediateResponseBuilder} that can then be used to send the response
     * to the end-user.
     *
     * @param instance The {@link InteractionImmediateResponseBuilder} provided by Javacord.
     * @return The {@link InteractionImmediateResponseBuilder} with all the proper configuration.
     */
    public InteractionImmediateResponseBuilder convertTo(InteractionImmediateResponseBuilder instance) {
        InteractionImmediateResponseBuilder build = builder.apply(instance);

        asString().ifPresent(build::setContent);
        asEmbed().ifPresent(build::addEmbed);

        build.setAllowedMentions(ALLOWED_MENTIONS);
        return build;
    }

    @Override
    public NexusMessage setBuilder(Function<InteractionImmediateResponseBuilder, InteractionImmediateResponseBuilder> builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public Optional<String> asString() {
        return Optional.ofNullable(textVal);
    }

    @Override
    public Optional<EmbedBuilder> asEmbed() {
        return Optional.ofNullable(embedVal);
    }
}
