package pw.mihou.nexus.features.contexts

import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.event.interaction.MessageContextMenuCommandEvent
import org.javacord.api.event.interaction.UserContextMenuCommandEvent
import org.javacord.api.interaction.*
import pw.mihou.nexus.features.commons.NexusApplicationCommand
import pw.mihou.nexus.core.reflective.annotations.*
import pw.mihou.nexus.features.contexts.enums.ContextMenuKinds
import pw.mihou.nexus.features.contexts.facade.NexusContextMenuHandler

abstract class NexusUserContextMenu: NexusContextMenuHandler<UserContextMenuCommandEvent, UserContextMenuInteraction> {
    val kind = ContextMenuKinds.USER
}

abstract class NexusMessageContextMenu: NexusContextMenuHandler<MessageContextMenuCommandEvent, MessageContextMenuInteraction> {
    val kind = ContextMenuKinds.MESSAGE
}

@MustImplement(clazz = NexusContextMenuHandler::class)
class NexusContextMenu: NexusApplicationCommand {

    @InjectUUID
    override lateinit var uuid: String

    @Uuid
    @Required
    lateinit var name: String

    @Uuid
    @Required
    lateinit var kind: ContextMenuKinds

    @WithDefault
    var nameLocalizations = emptyMap<DiscordLocale, String>()

    @WithDefault
    var nsfw = false

    @WithDefault
    var requiredPermissions = emptyList<PermissionType>()

    @WithDefault
    var serverIds = emptyList<Long>()

    @WithDefault
    var enabledInDms = true

    @InjectReferenceClass
    lateinit var handler: NexusContextMenuHandler<*, *>

    val isServerOnly get() = serverIds.isNotEmpty()
    val builder: ApplicationCommandBuilder<*, *, *> get() {
        val builder = when(kind) {
            ContextMenuKinds.USER -> UserContextMenuBuilder()
            ContextMenuKinds.MESSAGE -> MessageContextMenuBuilder()
        }
            .setName(name)
            .setNsfw(nsfw)
            .setEnabledInDms(enabledInDms)

        if  (requiredPermissions.isNotEmpty()) {
            builder.setDefaultEnabledForPermissions(*requiredPermissions.toTypedArray())
        }

        nameLocalizations.forEach { builder.addNameLocalization(it.key, it.value) }
        return builder
    }
    fun createUpdater(id: Long): ApplicationCommandUpdater<*, *, *> {
        // TODO: Add support for NSFW once Javacord next version comes.
        val builder = when(kind) {
            ContextMenuKinds.USER -> UserContextMenuUpdater(id)
            ContextMenuKinds.MESSAGE -> MessageContextMenuUpdater(id)
        }
            .setName(name)
            .setEnabledInDms(enabledInDms)

        if  (requiredPermissions.isNotEmpty()) {
            builder.setDefaultEnabledForPermissions(*requiredPermissions.toTypedArray())
        }

        nameLocalizations.forEach { builder.addNameLocalization(it.key, it.value) }
        return builder
    }

    override fun toString(): String {
        return "NexusContextMenu{" +
                "kind='$kind'," +
                "name='$name'," +
                "nameLocalizations=$nameLocalizations," +
                "nsfw=$nsfw," +
                "requiredPermissions=$requiredPermissions," +
                "serverIds=$serverIds" +
                "enabledInDms=$enabledInDms" +
                "}"
    }

}