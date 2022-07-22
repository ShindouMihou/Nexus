package pw.mihou.nexus.features.paginator.facade;

import org.javacord.api.entity.message.Message;

public interface NexusPaginatorDisabledEvent<I> {

    /**
     * This is executed when the paginator instance disabled with the message being successfully
     * acquired. There will be cases where this won't be executed like when the message is deleted
     * before this is triggered and similar cases.
     *
     * @param message The paginator message that was created.
     * @param instance The paginator instance that was used.
     */
    void onDisabled(Message message, NexusPaginatorInstance<I> instance);

}
