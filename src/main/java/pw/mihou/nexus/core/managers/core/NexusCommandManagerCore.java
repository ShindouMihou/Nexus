package pw.mihou.nexus.core.managers.core;

import org.javacord.api.entity.server.Server;
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

    private final List<NexusCommand> commands = new ArrayList<>();

    private final Map<Long, NexusCommand> indexes = new HashMap<>();
    private Map<String, Long> indexMap;

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
    public List<NexusCommand> getCommands() {
        return commands;
    }

    @Override
    public Nexus addCommand(NexusCommand command) {
        commands.add(command);

        if (indexMap == null || command.isServerOnly() || !indexMap.containsKey(command.getName().toLowerCase()))
            return nexusCore;

        indexes.put(indexMap.get(command.getName().toLowerCase()), command);
        return nexusCore;
    }

    @Override
    public Optional<NexusCommand> getCommandById(long id) {
        return Optional.ofNullable(indexes.getOrDefault(id, null));
    }

    @Override
    public Optional<NexusCommand> getCommandByUUID(String UUID) {
        return commands.stream()
                .map(command -> (NexusCommandCore) command)
                .filter(commandCore -> commandCore.uuid.equals(UUID))
                .map(command -> (NexusCommand) command)
                .findFirst();
    }

    @Override
    public Optional<NexusCommand> getCommandByName(String name) {
        return commands.stream()
                .filter(nexusCommand -> nexusCommand.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public Optional<NexusCommand> getCommandByName(String name, long server) {
        return commands.stream()
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

        if (getCommandById(interaction.getCommandId()).isPresent())
            return Optional.of(indexes.get(interaction.getCommandId()));

        // If index map contains the interaction command name and the indexes already has a key with the ID stored.
        // then that means this is a server slash command.
        if (
                indexMap != null &&
                indexMap.containsKey(interaction.getCommandName()) &&
                indexes.containsKey(indexMap.get(interaction.getCommandName())) &&
                interaction.getServer().isPresent()
        ) {
            indexes.put(interaction.getCommandId(), getCommandByName(interaction.getCommandName(), interaction.getServer().get().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Nexus couldn't find any command with the data. {name:"+ interaction.getCommandName()
                                    +", id: " + interaction.getCommandId()+"}"
                    )));
            return Optional.ofNullable(indexes.getOrDefault(interaction.getCommandId(), null));
        }

        // If index map contains the slash command name but the indexes is not recorded then we'll use that.
        if (
                indexMap != null &&
                indexMap.containsKey(interaction.getCommandName())
                && !indexes.containsKey(indexMap.get(interaction.getCommandName()))
        ) {
            indexes.put(indexMap.get(interaction.getCommandName()), getCommandByName(interaction.getCommandName())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Nexus couldn't find any command with the data. {name:"+ interaction.getCommandName()
                            +", id: " + interaction.getCommandId()+"}"
                    )));
            return Optional.ofNullable(indexes.getOrDefault(interaction.getCommandId(), null));
        }

        return getCommandByName(interaction.getCommandName());
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

                    // Store for any future slash commands that will try to use.
                    indexMap = newIndexes;

                    if (commands.isEmpty()) {
                        return;
                    }

                    // Perform indexing which is basically mapping the ID of the slash command
                    // to the Nexus Command that will be called everytime the command executes.
                    commands.stream()
                            .filter(nexusCommand -> !nexusCommand.isServerOnly())
                            .forEach(nexusCommand -> indexes.put(
                                    newIndexes.get(
                                            nexusCommand.getName().toLowerCase()
                                    ), nexusCommand)
                            );

                    Map<Long, Map<String, Long>> serverIndexes = new HashMap<>();

                    for (NexusCommand command : commands.stream().filter(NexusCommand::isServerOnly).toList()) {
                        command.getServerIds().forEach(id -> {
                            if (!serverIndexes.containsKey(id)) {
                                Server server = nexusCore.getShardManager()
                                        .getShardOf(id)
                                        .flatMap(discordApi -> discordApi.getServerById(id))
                                        .orElseThrow(AssertionError::new);
                                serverIndexes.put(server.getId(), new HashMap<>());

                                for (SlashCommand slashCommand : server.getSlashCommands().join()) {
                                    serverIndexes.get(server.getId()).put(slashCommand.getName().toLowerCase(), slashCommand.getId());
                                }
                            }

                            indexes.put(serverIndexes.get(id).get(command.getName().toLowerCase()), command);
                        });
                    }

                    logger.info("All global and server slash commands are now indexed. It took {} milliseconds to complete indexing.", System.currentTimeMillis() - start);
                }).exceptionally(ExceptionLogger.get()).join();
    }

}
