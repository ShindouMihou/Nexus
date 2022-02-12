package pw.mihou.nexus.core.configuration.core;

/**
 * {@link NexusConfiguration} is a record that contains all the configuration
 * settings of a {@link pw.mihou.nexus.Nexus} instance and is recommended to have.
 */
public record NexusConfiguration(
        boolean autoApplySupportedServerChangesForServers
) {}
