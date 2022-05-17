package pw.mihou.nexus;

import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Test {

    private static final Nexus nexus = Nexus.builder().build();

    public static void main(String[] args) {
        nexus.listenMany(new AGlobalCommand(), new ASpecificServerCommand());
        NexusCommand dynamic = nexus.createCommandFrom(new ADynamicCommand());

        new DiscordApiBuilder()
                .setToken(System.getenv("token"))
                .setAllIntents()
                .setTotalShards(4)
                .addListener(nexus)
                .loginAllShards()
                .forEach(future -> future.thenAccept(discordApi -> {
                    System.out.println("Shard " + discordApi.getCurrentShard() + " is now online.");

                    //--------------------
                    // IMPORTANT IMPORTANT IMPORTANT IMPORTANT
                    // Always remember to include this line when starting up your shard.
                    // This allows the Nexus Engine to start performing tasks dedicated to a specific shard.
                    // and also allows Nexus to function more completely.
                    // IMPORTANT IMPORTANT IMPORTANT IMPORTANT
                    // ------------------
                    nexus.getShardManager().put(discordApi);

                }).exceptionally(ExceptionLogger.get()));

        //---------------------
        // Global synchronization of all commands, recommended at startup.
        // This updates, creates or removes any commands that are missing, outdated or removed.
        //----------------------
        nexus.getSynchronizer()
                .synchronize(4)
                .thenAccept(unused -> System.out.println("Synchronization with Discord's and Nexus' command repository is now complete."))
                .exceptionally(ExceptionLogger.get());

        //------------------
        // Demonstration of dynamic server command updating.
        //-----------------

        NexusThreadPool.schedule(() -> {
            dynamic.addSupportFor(853911163355922434L, 858685857511112736L);
            System.out.println("Attempting to perform dynamic updates...");

            // We recommend using batch update if you performed more than 1 `addSupportFor` methods.
            // As batch update will update all of those command using only one request.
            // batchUpdate(853911163355922434L, 4);
            // batchUpdate(858685857511112736L, 4);

            // Single update, on the otherwise, allows multiple server ids but sends a single create or update
            // request for a command and doesn't scale well when done with many commands.
            singleUpdate(dynamic, 4, 853911163355922434L, 858685857511112736L);
        }, 1, TimeUnit.MINUTES);

        NexusThreadPool.schedule(() -> {
            dynamic.removeSupportFor(853911163355922434L);
            System.out.println("Attempting to perform dynamic updates...");

            // The same information as earlier, batch update will update the entire server slash command list
            // which means it will remove any slash commands that are no longer supporting that server
            // and will update or create any slash commands that still support that server.
            // batchUpdate(853911163355922434L, 4);

            // Single delete is fine when you are only deleting one command on a pile of servers.
            singleDelete(dynamic, 4, 853911163355922434L);
        }, 2, TimeUnit.MINUTES);
    }

    /**
     * Updates, removes or creates any commands that are outdated, removed or missing. This is recommended
     * especially when you recently added support to a lot of servers. Not recommended on startup since
     * {@link pw.mihou.nexus.features.command.synchronizer.NexusSynchronizer#synchronize(int)} is more recommended for
     * startup-related synchronization.
     *
     * @param serverId      The server id to synchronize commands to.
     * @param totalShards   The total shards of the server.
     */
    private static void batchUpdate(long serverId, int totalShards) {
        nexus.getSynchronizer()
                .batchUpdate(serverId, totalShards)
                .thenAccept(unused -> System.out.println("A batch update was complete. [server="+serverId+"]"))
                .exceptionally(ExceptionLogger.get());
    }

    /**
     * Updates a single command on one or many servers. This is practically the same as batch update but utilizes a more
     * update or create approach whilst {@link Test#batchUpdate(long, int)} overrides the entire server slash command list
     * with what Nexus knows.
     *
     * @param command       The command to update on the specified servers.
     * @param totalShards   The total amount of shards of the bot, this is used for sharding formula.
     * @param serverIds     The server ids to update the bot on.
     */
    private static void singleUpdate(NexusCommand command, int totalShards, long... serverIds) {
        nexus.getSynchronizer()
                .upsert(command, totalShards, serverIds)
                .thenAccept(unused -> System.out.println("A batch upsert was complete. [servers="+ Arrays.toString(serverIds) +"]"))
                .exceptionally(ExceptionLogger.get());
    }

    /**
     * Deletes a single command on one or many servers. This is practically the same as batch update but utilizes a more
     * delete approach whilst {@link Test#batchUpdate(long, int)} overrides the entire server slash command list
     * with what Nexus knows.
     *
     * @param command       The command to update on the specified servers.
     * @param totalShards   The total amount of shards of the bot, this is used for sharding formula.
     * @param serverIds     The server ids to update the bot on.
     */
    private static void singleDelete(NexusCommand command, int totalShards, long... serverIds) {
        nexus.getSynchronizer()
                .delete(command, totalShards, serverIds)
                .thenAccept(unused -> System.out.println("A batch delete was complete. [servers="+ Arrays.toString(serverIds) +"]"))
                .exceptionally(ExceptionLogger.get());
    }

}
