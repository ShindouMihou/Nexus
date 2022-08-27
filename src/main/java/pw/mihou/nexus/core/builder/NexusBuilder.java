package pw.mihou.nexus.core.builder;

import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.configuration.core.NexusConfiguration;
import pw.mihou.nexus.features.messages.facade.NexusMessageConfiguration;

import java.time.Duration;

public class NexusBuilder {

    private NexusMessageConfiguration messageConfiguration;
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
        return new NexusCore(messageConfiguration, nexusConfiguration);
    }
}
