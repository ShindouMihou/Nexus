package pw.mihou.nexus.features.command.facade;

public interface NexusHandler {

    /**
     * This is called once a user executes the specific command
     * that this handler is assigned to.
     *
     * @param event The main event that contains all the necessary information
     *              that was provided by Javacord and Nexus.
     */
    void onEvent(NexusCommandEvent event);

}
