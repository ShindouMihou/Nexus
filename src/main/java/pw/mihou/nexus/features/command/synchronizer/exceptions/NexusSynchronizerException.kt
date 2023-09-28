package pw.mihou.nexus.features.command.synchronizer.exceptions

import pw.mihou.nexus.features.command.facade.NexusCommand
import java.lang.Exception

class NexusSynchronizerException(val server: Long?, val command: NexusCommand?, val exception: Exception):
    RuntimeException("An exception occurred while trying to perform command synchronization. "
            + "{server=${server ?: "N/A"}, command=${command?.name ?: "N/A"}}: "
            + exception)