package pw.mihou.nexus.core.builder;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.configuration.core.NexusConfiguration;
import pw.mihou.nexus.features.messages.facade.NexusMessageConfiguration;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class NexusBuilder {

    private NexusMessageConfiguration messageConfiguration;
    private DiscordApiBuilder builder;
    private Consumer<DiscordApi> onShardLogin;
    private NexusConfiguration nexusConfiguration = new NexusConfiguration(
            Duration.ofMinutes(10)
    );

    /**
     * Sets the {@link NexusMessageConfiguration} that {@link Nexus} uses whenever
     * it needs to handle a situation where the user needs to be notified, for instance, when reaching
     * a rate-limit.
     *
     * @param configuration The {@link NexusMessageConfiguration} to use. This is optional and can be null.
     * @return {@link NexusBuilder} for chain-calling methods.
     */
    public NexusBuilder setMessageConfiguration(NexusMessageConfiguration configuration) {
        this.messageConfiguration = configuration;
        return this;
    }

    /**
     * Sets the {@link DiscordApiBuilder} instance that {@link Nexus} will use for
     * creating a new {@link DiscordApi} to store in its {@link pw.mihou.nexus.core.managers.NexusShardManager}.
     *
     * @param builder The builder to use whenever Nexus boots.
     * @return {@link NexusBuilder} for chain-calling methods.
     */
    public NexusBuilder setDiscordApiBuilder(DiscordApiBuilder builder) {
        this.builder = builder;
        return this;
    }

    /**
     * Sets the {@link DiscordApiBuilder} instance that {@link Nexus} will use for
     * creating a new {@link DiscordApi} to store in its {@link pw.mihou.nexus.core.managers.NexusShardManager}.
     *
     * @param builder The builder to use whenever Nexus boots.
     * @return {@link NexusBuilder} for chain-calling methods.
     */
    public NexusBuilder setDiscordApiBuilder(Function<DiscordApiBuilder, DiscordApiBuilder> builder) {
        return this.setDiscordApiBuilder(builder.apply(new DiscordApiBuilder()));
    }

    /**
     * Sets the handler for whenever a new {@link DiscordApi} shard is booted
     * successfully.
     *
     * @param onShardLogin The handler for whenever a new {@link DiscordApi} shard is booted.
     * @return {@link NexusBuilder} for chain-calling methods.
     */
    public NexusBuilder setOnShardLogin(Consumer<DiscordApi> onShardLogin) {
        this.onShardLogin = onShardLogin;
        return this;
    }

    /**
     * Sets the {@link NexusConfiguration} for the {@link Nexus} instance to build.
     *
     * @param nexusConfiguration The {@link NexusConfiguration} to use.
     * @return {@link NexusBuilder} for chain-calling methods.
     */
    public NexusBuilder setNexusConfiguration(NexusConfiguration nexusConfiguration) {
        this.nexusConfiguration = nexusConfiguration;
        return this;
    }

    /**
     * This builds a new {@link Nexus} instance that uses the configuration
     * that was specified in this builder settings.
     *
     * @return The new {@link Nexus} instance.
     */
    public Nexus build() {
        return new NexusCore(messageConfiguration, builder, onShardLogin, nexusConfiguration);
    }
}
