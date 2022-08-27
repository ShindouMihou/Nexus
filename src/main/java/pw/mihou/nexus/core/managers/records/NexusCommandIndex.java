package pw.mihou.nexus.core.managers.records;

import pw.mihou.nexus.features.command.facade.NexusCommand;

public record NexusCommandIndex(NexusCommand command, long applicationCommandId) {}
