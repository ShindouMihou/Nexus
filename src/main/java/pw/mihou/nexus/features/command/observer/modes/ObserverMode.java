package pw.mihou.nexus.features.command.observer.modes;

import pw.mihou.nexus.features.command.observer.core.NexusObserverCore;

@Deprecated(forRemoval = true)
public enum ObserverMode {

    /**
     * This mode tells {@link NexusObserverCore} to only send a log
     * for every commands that needs to be updated or created, it will not do anything else.
     */
    WATCHDOG(false, false, false),

    /**
     * This mode tells {@link NexusObserverCore} to only update
     * the commands that needs to be updated to the Discord API, it will not create any new
     * commands.
     */
    UPDATE(true, false, false),

    /**
     * This mode tells {@link NexusObserverCore} to only register
     * the commands that needs to be registered to the Discord API, it will not update existing
     * commands.
     */
    CREATE(false, true, false),

    /**
     * This mode tells {@link  NexusObserverCore} to only delete
     * the commands that needs to be deleted from the Discord API.
     */
    DELETE(false, false, true),

    /**
     * This mode tells {@link NexusObserverCore} that it should
     * update or create any commands that needs to be created, updated or deleted.
     */
    MASTER(true, true, true);

    private final boolean update;
    private final boolean create;
    private final boolean delete;

    /**
     * Creates a new Observer Mode that either allows creation or
     * updating of commands.
     *
     * @param update To allow updating of commands?
     * @param create To allow creating of commands?
     */
    ObserverMode(boolean update, boolean create, boolean delete) {
        this.update = update;
        this.create = create;
        this.delete = delete;
    }

    /**
     * Does this observer mode allow updating of commands?
     *
     * @return {@link Boolean}
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * Does this observer mode allow creating of commands?
     *
     * @return {@link Boolean}
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * Does this observer mode allow deleting of commands?
     *
     * @return {@link Boolean}
     */
    public boolean isDelete() {
        return delete;
    }
    
}
