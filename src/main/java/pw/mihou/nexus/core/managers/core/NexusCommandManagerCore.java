package pw.mihou.nexus.core.managers.core;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.logger.adapters.NexusLoggingAdapter;
import pw.mihou.nexus.core.managers.facade.NexusCommandManager;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.*;
import java.util.stream.Collectors;

public class NexusCommandManagerCore implements NexusCommandManager {

    private final Map<String, NexusCommand> commands = new HashMap<>();

    private final Map<Long, String> indexes = new HashMap<>();

    private final NexusCore nexusCore;
    private static final NexusLoggingAdapter logger = NexusCore.logger;

    /**
     * Creates a new Nexus Command Manager that is utilized to manage commands,
     * index commands, etc.
     *
     * @param nexusCore The nexus core that is in charge of this command manager.
     */
    public NexusCommandManagerCore(NexusCore nexusCore) {
        this.nexusCore = nexusCore;
    }

    @Override
    public Collection<NexusCommand> getCommands() {
        return commands.values();
    }

    @Override
    public Nexus addCommand(NexusCommand command) {
        commands.put(((NexusCommandCore) command).uuid, command);

        return nexusCore;
    }

    @Override
    public Optional<NexusCommand> getCommandById(long id) {
        return Optional.ofNullable(commands.get(indexes.getOrDefault(id, null)));
    }

    @Override
    public Optional<NexusCommand> getCommandByUUID(String uuid) {
        return Optional.ofNullable(commands.get(uuid));
    }

    @Override
    public Optional<NexusCommand> getCommandByName(String name) {
        return getCommands().stream()
                .filter(nexusCommand -> nexusCommand.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public Optional<NexusCommand> getCommandByName(String name, long server) {
        return getCommands().stream()
                .filter(nexusCommand ->
                        nexusCommand.getName().equalsIgnoreCase(name) && nexusCommand.getServerIds().contains(server)
                )
                .findFirst();
    }

    /**
     * This performs indexing based on the data analyzed from the
     * {@link SlashCommandCreateEvent} and returns the results for post-processing
     * from the {@link NexusCore}. This is what we call dynamic indexing.
     *
     * @param event The event to handle.
     */
    public Optional<NexusCommand> acceptEvent(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        if (getCommandById(interaction.getCommandId()).isPresent()) {
            return getCommandById(interaction.getId());
        }

        if (interaction.getServer().isPresent()) {
            return getCommands().stream().filter(nexusCommand ->
                    nexusCommand.getName().equalsIgnoreCase(interaction.getCommandName()) &&
                            nexusCommand.getServerIds().contains(interaction.getServer().get().getId())
            ).findFirst().map(command -> {
                indexes.put(interaction.getCommandId(), ((NexusCommandCore) command).uuid);
                return command;
            });
        }

        return getCommandByName(interaction.getCommandName()).map(command -> {
            indexes.put(interaction.getCommandId(), ((NexusCommandCore) command).uuid);

            return command;
        });
    }

    @Override
    public void index() {
        logger.info("Nexus is now performing command indexing, this will delay your boot time by a few seconds but improve performance and precision in look-ups...");
        long start = System.currentTimeMillis();
        nexusCore.getShardManager()
                .asStream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Nexus was unable to perform command indexing because there are no shards registered in Nexus's Shard Manager."
                        )
                ).getGlobalSlashCommands().thenAcceptAsync(slashCommands -> {
                    Map<String, Long> newIndexes = slashCommands.stream()
                            .collect(Collectors.toMap(slashCommand -> slashCommand.getName().toLowerCase(), ApplicationCommand::getId));

                    // Ensure the indexes are clear otherwise we might end up with some wrongly placed commands.
                    indexes.clear();

                    if (commands.isEmpty()) {
                        return;
                    }

                    // Perform indexing which is basically mapping the ID of the slash command
                    // to the Nexus Command that will be called everytime the command executes.
                    getCommands().stream()
                            .filter(nexusCommand -> nexusCommand.getServerIds().isEmpty())
                            .forEach(nexusCommand -> indexes.put(
                                    newIndexes.get(nexusCommand.getName().toLowerCase()),
                                    ((NexusCommandCore) nexusCommand).uuid)
                            );

                    Map<Long, Map<String, Long>> serverIndexes = new HashMap<>();

                    for (NexusCommand command : getCommands().stream().filter(nexusCommand -> !nexusCommand.getServerIds().isEmpty()).toList()) {
                        command.getServerIds().forEach(id -> {
                            if (!serverIndexes.containsKey(id)) {
                               nexusCore.getShardManager().getShardOf(id)
                                       .flatMap(discordApi -> discordApi.getServerById(id))
                                       .ifPresent(server -> {
                                           serverIndexes.put(server.getId(), new HashMap<>());

                                           for (SlashCommand slashCommand : server.getSlashCommands().join()) {
                                               serverIndexes.get(server.getId()).put(slashCommand.getName().toLowerCase(), slashCommand.getId());
                                           }
                                       });
                            }

                            indexes.put(
                                    serverIndexes.get(id).get(command.getName().toLowerCase()),
                                    ((NexusCommandCore) command).uuid
                            );
                        });
                    }

                    serverIndexes.clear();
                    logger.info("All global and server slash commands are now indexed. It took {} milliseconds to complete indexing.", System.currentTimeMillis() - start);
                }).exceptionally(ExceptionLogger.get()).join();
    }

}
