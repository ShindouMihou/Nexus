package pw.mihou.nexus.features.messages.facade;

import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.nexus.features.messages.core.NexusMessageCore;

import java.util.Optional;
import java.util.function.Function;

public interface NexusMessage {

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link EmbedBuilder} specified.
     *
     * @param builder The {@link EmbedBuilder} to send to the end-user.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage from(EmbedBuilder builder) {
        return new NexusMessageCore(builder);
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link EmbedBuilder} specified with ephemereal flag.
     *
     * @param builder The {@link EmbedBuilder} to send to the end-user.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage fromEphemeral(EmbedBuilder builder) {
        return new NexusMessageCore(builder).setBuilder(responseBuilder -> responseBuilder.setFlags(MessageFlag.EPHEMERAL));
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link EmbedBuilder} specified.
     *
     * @param builder The {@link EmbedBuilder} to send to the end-user.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage from(Function<EmbedBuilder, EmbedBuilder> builder) {
        return new NexusMessageCore(builder.apply(new EmbedBuilder()));
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link EmbedBuilder} specified with ephemereal flag.
     *
     * @param builder The {@link EmbedBuilder} to send to the end-user.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage fromEphemereal(Function<EmbedBuilder, EmbedBuilder> builder) {
        return fromEphemeral(builder.apply(new EmbedBuilder()));
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link String} specified.
     *
     * @param text The {@link String} to send to the end-user.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage from(String text) {
        return new NexusMessageCore(text);
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link String} specified with ephemereal flag.
     *
     * @param text The {@link String} to send to the end-user.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage fromEphemereal(String text) {
        return new NexusMessageCore(text).setBuilder(builder -> builder.setFlags(MessageFlag.EPHEMERAL));
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link EmbedBuilder} specified with a customized
     * {@link InteractionImmediateResponseBuilder} configuration.
     *
     * @param builder The {@link EmbedBuilder} to send to the end-user.
     * @param interactionImmediateResponseBuilderFunction The {@link InteractionImmediateResponseBuilder} configuration to
     *                                                    utilize when sending the message.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage fromWith(EmbedBuilder builder,
                                 Function<InteractionImmediateResponseBuilder, InteractionImmediateResponseBuilder>
                                         interactionImmediateResponseBuilderFunction) {
        return from(builder).setBuilder(interactionImmediateResponseBuilderFunction);
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link EmbedBuilder} specified with a customized
     * {@link InteractionImmediateResponseBuilder} configuration.
     *
     * @param builder The {@link EmbedBuilder} to send to the end-user.
     * @param interactionImmediateResponseBuilderFunction The {@link InteractionImmediateResponseBuilder} configuration to
     *                                                    utilize when sending the message.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage fromWith(Function<EmbedBuilder, EmbedBuilder> builder,
                                 Function<InteractionImmediateResponseBuilder, InteractionImmediateResponseBuilder>
                                         interactionImmediateResponseBuilderFunction) {
        return from(builder).setBuilder(interactionImmediateResponseBuilderFunction);
    }

    /**
     * Creates a new {@link NexusMessage} that utilizes the {@link String} specified with a customized
     * {@link InteractionImmediateResponseBuilder} configuration.
     *
     * @param text The {@link String} to send to the end-user.
     * @param interactionImmediateResponseBuilderFunction The {@link InteractionImmediateResponseBuilder} configuration to
     *                                                    utilize when sending the message.
     * @return The new {@link NexusMessage} instance.
     */
    static NexusMessage fromWith(String text,
                                 Function<InteractionImmediateResponseBuilder, InteractionImmediateResponseBuilder>
                                         interactionImmediateResponseBuilderFunction) {
        return from(text).setBuilder(interactionImmediateResponseBuilderFunction);
    }

    /**
     * Modifies the {@link InteractionImmediateResponseBuilder} to suit the needs of the developer. You can customize
     * this to your liking, although any text content or embed will be overriden at the end.
     *
     * @param builder The builder that is being utilized.
     * @return {@link NexusMessageCore} for chain-calling methods.
     */
    NexusMessage setBuilder(Function<InteractionImmediateResponseBuilder, InteractionImmediateResponseBuilder> builder);

    /**
     * Retrieves the {@link String} version of this {@link NexusMessage} instance.
     * This will return empty if it isn't configured to send a text message but an embed instead.
     *
     * @return The {@link String} value of this message.
     */
    Optional<String> asString();

    /**
     * Retrieves the {@link EmbedBuilder} version of this {@link NexusMessage} instance.
     * This will erturn empty if it isn't configured to send an embed but a text message instead.
     *
     * @return The {@link EmbedBuilder} value of this message.
     */
    Optional<EmbedBuilder> asEmbed();

}
