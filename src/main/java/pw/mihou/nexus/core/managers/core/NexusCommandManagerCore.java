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
import pw.mihou.nexus.core.managers.records.NexusCommandIndex;
import pw.mihou.nexus.features.command.core.NexusCommandCore;
import pw.mihou.nexus.features.command.facade.NexusCommand;

import java.util.*;

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
    public Optional<NexusCommand> getCommandByName(String name, long server) {
        return getCommands().stream()
                .filter(nexusCommand ->
                        nexusCommand.getName().equalsIgnoreCase(name) && nexusCommand.getServerIds().contains(server)
                )
                .findFirst();
    }

    @Override
    public List<NexusCommandIndex> export() {
        return indexes.entrySet()
                .stream()
                .map(entry -> new NexusCommandIndex(commands.get(entry.getValue()), entry.getKey()))
                .toList();
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
            return getCommandById(interaction.getCommandId());
        }

        if (interaction.getServer().isPresent()) {
            return getCommands().stream()
                    .filter(nexusCommand -> nexusCommand.getName().equalsIgnoreCase(interaction.getCommandName())
                            && nexusCommand.getServerIds().contains(interaction.getServer().get().getId()))
                    .findFirst()
                    .or(() -> getCommandByName(interaction.getCommandName()))
                    .map(command -> {
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
        nexusCore.getEngineX().awaitAvailable().thenAcceptAsync(shard -> {
           Set<SlashCommand> slashCommands = shard.getGlobalSlashCommands().join();

           // Clearing the entire in-memory index mapping to make sure that we don't have any outdated indexes.
           indexes.clear();

           for (SlashCommand slashCommand : slashCommands) {
               index(slashCommand);
           }

           Set<Long> servers = new HashSet<>();
           for (NexusCommand serverCommand : getServerCommands()) {
               servers.addAll(serverCommand.getServerIds());
           }

           for (long server : servers) {
               if (server == 0L) continue;

               Set<SlashCommand> $slashCommands = nexusCore.getEngineX().await(server)
                       .thenComposeAsync(Server::getSlashCommands).join();

               for (SlashCommand slashCommand : $slashCommands) {
                   index(slashCommand);
               }
           }

            logger.info("All global and server slash commands are now indexed. It took {} milliseconds to complete indexing.", System.currentTimeMillis() - start);
        }).exceptionally(ExceptionLogger.get()).join();
    }

    @Override
    public void index(NexusCommand command, long snowflake) {
        indexes.put(snowflake, ((NexusCommandCore) command).uuid);
    }

    @Override
    public void index(ApplicationCommand applicationCommand) {
        long serverId = applicationCommand.getServerId().orElse(-1L);

        if (serverId == -1L) {
            for (NexusCommand command : commands.values()) {
                if (!command.getServerIds().isEmpty()) continue;
                if (!command.getName().equalsIgnoreCase(applicationCommand.getName())) continue;

                index(command, applicationCommand.getApplicationId());
                break;
            }
            return;
        }

        for (NexusCommand command : commands.values()) {
            if (command.getServerIds().contains(serverId)) continue;
            if (!command.getName().equalsIgnoreCase(applicationCommand.getName())) continue;

            index(command, applicationCommand.getApplicationId());
            break;
        }
    }

}
