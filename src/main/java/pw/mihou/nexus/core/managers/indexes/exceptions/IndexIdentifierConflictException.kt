package pw.mihou.nexus.core.managers.indexes.exceptions

import pw.mihou.nexus.features.command.facade.NexusCommand

class IndexIdentifierConflictException(command: NexusCommand):
    RuntimeException("An index-identifier conflict was identified between commands with the name ${command.name}. We do not " +
            "recommend having commands with the same name that have the same unique identifier, please change one of the commands' identifier " +
            "by using the @IdentifiableAs annotation. (https://github.com/ShindouMihou/Nexus/wiki/Index-Identifier-Conflict)")