package pw.mihou.nexus.features.messages.core;

import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import pw.mihou.nexus.features.command.core.NexusCommandDispatcher;
import pw.mihou.nexus.features.messages.facade.NexusMessage;

import java.util.Optional;
import java.util.function.Function;

public class NexusMessageCore implements NexusMessage {

    private final String textVal;
    private final EmbedBuilder embedVal;
    private Function<InteractionMessageBuilderBase<?>, InteractionMessageBuilderBase<?>> builder = bob -> bob;
    boolean ephemeral = false;
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
        InteractionImmediateResponseBuilder build = (InteractionImmediateResponseBuilder) builder.apply(instance);

        if (ephemeral) {
            build.setFlags(MessageFlag.EPHEMERAL);
        }

        asString().ifPresent(build::setContent);
        asEmbed().ifPresent(build::addEmbed);

        build.setAllowedMentions(ALLOWED_MENTIONS);
        return build;
    }

    /**
     * An internal method that is utilized by the likes of {@link NexusCommandDispatcher} to
     * transform the {@link NexusMessage} into an {@link InteractionMessageBuilderBase} that can then be used to send the response
     * to the end-user.
     *
     * @param instance The {@link InteractionImmediateResponseBuilder} provided by Javacord.
     * @return The {@link InteractionMessageBuilderBase} with all the proper configuration.
     */
    public InteractionMessageBuilderBase<?> convertTo(InteractionMessageBuilderBase<?> instance) {
        asString().ifPresent(instance::setContent);
        asEmbed().ifPresent(instance::addEmbed);

        instance.setAllowedMentions(ALLOWED_MENTIONS);
        return instance;
    }

    @Override
    public NexusMessage setBuilder(Function<InteractionMessageBuilderBase<?>, InteractionMessageBuilderBase<?>> builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public NexusMessage setEphemeral(boolean ephemeral) {
        this.ephemeral = true;
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
