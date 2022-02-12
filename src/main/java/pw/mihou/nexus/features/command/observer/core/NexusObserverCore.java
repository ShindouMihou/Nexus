package pw.mihou.nexus.features.command.observer.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.Nexus;
import pw.mihou.nexus.core.NexusCore;
import pw.mihou.nexus.core.threadpool.NexusThreadPool;
import pw.mihou.nexus.features.command.facade.NexusCommand;
import pw.mihou.nexus.features.command.observer.facade.NexusObserver;
import pw.mihou.nexus.features.command.observer.modes.ObserverMode;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * {@link NexusObserverCore} is the Nexus variant of its predecessor's observer which was VelenObserver.
 * The code is more or less the same and offers the same performance, advancements and all those other good
 * stuff of the original VelenObserver.
 *
 * A {@link NexusObserverCore} is, in a sense, a slash command checker which observes which slash commands are
 * registered into Discord's database, the ones that aren't and the ones that needs to be deleted to match the
 * registry of Nexus. This also checks for differences in slash commands which allows the feature to be pretty
 * advanced.
 */
public class NexusObserverCore implements NexusObserver {

    private final NexusCore nexusCore;
    private final ObserverMode mode;

    /**
     * This creates a new {@link NexusObserverCore} which observes for any changes
     * on the commands on start-up and updates them, register them or only log them
     * depending on the {@link ObserverMode} selected.
     *
     * @param nexus The {@link Nexus} instance that needs to be checked.
     * @param mode The {@link ObserverMode} to use for this instance.
     */
    public NexusObserverCore(Nexus nexus, ObserverMode mode) {
        this.nexusCore = (NexusCore) nexus;
        this.mode = mode;
    }

    @Override
    public CompletableFuture<Void> observeOnAllShards(boolean hardObservation) {
        return hardObservation ? hardObserveForAllShards() : softObserveForAllShards();
    }

    @Override
    public CompletableFuture<Void> applyChangesOnCommand(NexusCommand command) {
        return CompletableFuture.runAsync(() -> command.getServerIds().forEach(serverId -> {
            Optional<Server> optionalServer = nexusCore.getShardManager()
                    .getShardOf(serverId)
                    .flatMap(discordApi -> discordApi.getServerById(serverId));

            if (optionalServer.isEmpty()) {
                NexusCore.logger.warn(
                        "A command failed to apply changes for a server since no shard on this JVM is handling the server... " +
                                "please ignore if this is normal." +
                                "[server={}, command={}]",
                        serverId, command.getName()
                );
                return;
            }

            Server server = optionalServer.orElseThrow(AssertionError::new);
            List<SlashCommand> commands = server.getSlashCommands().join()
                    .stream()
                    .filter(slashCommand -> slashCommand.getName().equalsIgnoreCase(command.getName()))
                    .toList();

            if (commands.isEmpty()) {
                long start = System.currentTimeMillis();
                command.asSlashCommand().createForServer(server).thenAccept(slashCommand -> NexusCore.logger.info(
                        "Application command was created for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                        server.getId(),
                        slashCommand.getName(),
                        slashCommand.getDescription(),
                        slashCommand.getId(),
                        System.currentTimeMillis() - start
                )).join();
            } else {
                boolean update = false;
                SlashCommand slashCommand = commands.get(0);

                if (!slashCommand.getDescription().equalsIgnoreCase(command.getDescription())) {
                    update = true;
                }

                if (slashCommand.getDefaultPermission() != command.isDefaultPermissionEnabled()) {
                    update = true;
                }

                if (!getOptionChoiceDifferences(command, slashCommand, new HashMap<>(), slashCommand.getOptions(), command.getOptions()).isEmpty()) {
                    update = true;
                }

                if (update) {
                    long start = System.currentTimeMillis();
                    command.asSlashCommand().createForServer(server).thenAccept(slashCommand1 -> NexusCore.logger.info(
                            "Application command was updated for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                            server.getId(),
                            slashCommand1.getName(),
                            slashCommand1.getDescription(),
                            slashCommand1.getId(),
                            System.currentTimeMillis() - start
                    )).join();
                }
            }
        }));
    }

    @Override
    public CompletableFuture<Void> observeOnServer(Server server) {
        List<NexusCommand> commands = nexusCore.getCommandManager().getCommands()
                .stream()
                .filter(NexusCommand::isServerOnly)
                .filter(nexusCommand ->  nexusCommand.getServerIds().contains(server.getId()))
                .collect(Collectors.toList());

        return server.getSlashCommands()
                .thenAcceptAsync(
                        slashCommands -> commands.forEach(
                                nexusCommand -> doFinalizationFor(server, slashCommands, commands)
                        )
                );
    }

    @Override
    public CompletableFuture<Void> observeOnGlobal() {

        DiscordApi shard = nexusCore.getShardManager()
                .asStream()
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Nexus couldn't find any shards registered inside its Shard Manager. Are you sure your Nexus configuration is proper?"
                        )
                );
        return shard.getGlobalSlashCommands().thenAcceptAsync(slashCommands -> {

                    if (mode.isCreate()) {
                        for (NexusCommand nexusCommand : getNotRegistered(slashCommands)) {
                            long start = System.currentTimeMillis();
                            nexusCommand.asSlashCommand().createGlobal(shard).thenAccept(
                                    slashCommand -> NexusCore.logger.info(
                                            "Application command was created. [name={}, description={}, id={}]. It took {} milliseconds.",
                                            slashCommand.getName(),
                                            slashCommand.getDescription(),
                                            slashCommand.getId(),
                                            System.currentTimeMillis() - start
                                    )
                            ).exceptionally(ExceptionLogger.get());
                        }
                    }

                    if (mode.isDelete()) {
                        for (SlashCommand command : getNoMatches(slashCommands)) {
                            long start = System.currentTimeMillis();
                            command.deleteGlobal().thenAccept(unused -> NexusCore.logger.info(
                                    "Application command was deleted. [name={}, description={}, id={}]. It took {} milliseconds.",
                                    command.getName(),
                                    command.getDescription(),
                                    command.getId(),
                                    System.currentTimeMillis() - start)).exceptionally(ExceptionLogger.get());
                        }
                    }

                    if (mode.isUpdate()) {
                        getChanges(slashCommands).forEach((aLong, nexusCommand) -> {
                            long start = System.currentTimeMillis();
                            nexusCommand.asSlashCommandUpdater(aLong).updateGlobal(shard).thenAccept(
                                    slashCommand -> NexusCore.logger.info(
                                            "Application command was updated. [name={}, description={}, id={}]. It took {} milliseconds.",
                                            slashCommand.getName(),
                                            slashCommand.getDescription(),
                                            slashCommand.getId(),
                                            System.currentTimeMillis() - start
                                    )
                            ).exceptionally(ExceptionLogger.get());

                        });
                    }

                    if (!mode.isUpdate() && !mode.isCreate() && !mode.isDelete()) {
                        for (NexusCommand nexusCommand : getNotRegistered(slashCommands)) {
                            NexusCore.logger.warn(
                                    "Application command is not registered on Discord's database. [command={}]",
                                    nexusCommand.toString()
                            );
                        }

                        getChanges(slashCommands).forEach((aLong, nexusCommand) -> NexusCore.logger.warn(
                                "Application command requires updating. [id={}, name={}]",
                                aLong,
                                nexusCommand.getName()
                        ));

                        for (SlashCommand command : getNoMatches(slashCommands)) {
                            NexusCore.logger.warn(
                                    "Application command is not registered on Nexus's repository. [name={}, id={}]",
                                    command.getName(), command.getId()
                            );
                        }
                    }

                }).exceptionally(ExceptionLogger.get());
    }

    /**
     * Retrieves and performs an observation check for all
     * slash commands of all servers in the shards specified. <b>DO THIS AS YOUR OWN RISK.</b>.
     *
     * Not to be confused as {@link NexusObserverCore#softObserveForAllShards()} which only checks
     * for all the commands registered in the registry, disabling the automatic removal functionality.
     *.
     * @return A future that indicates progress or completion.
     */
    private CompletableFuture<Void> hardObserveForAllShards() {
        return CompletableFuture.allOf(
                nexusCore.getShardManager()
                        .asStream()
                        .map(DiscordApi::getServers)
                        .map(servers ->
                                CompletableFuture.allOf(
                                        servers.stream().map(this::observeOnServer)
                                                .toArray(CompletableFuture[]::new)
                                )
                        ).toArray(CompletableFuture[]::new)
        );
    }


    /**
     * Retrieves and performs an observation check for all
     * slash commands of all the servers <bold>THAT HAS A COMMAND ATTACHED TO IT</bold> on the Velen registry.
     *
     * Not to be confused with {@link NexusObserverCore#hardObserveForAllShards()} which checks all
     * servers' slash commands which allows that method to be both risky and also support all functionalities such as
     * deleting slash commands that are no longer registered in Nexus.
     *
     * @return A future that indicates progress or completion.
     */
    private CompletableFuture<Void> softObserveForAllShards() {
        List<NexusCommand> commands = nexusCore.getCommandManager().getCommands()
                .stream()
                .filter(nexusCommand -> !nexusCommand.getServerIds().isEmpty() && nexusCommand.isServerOnly())
                .collect(Collectors.toList());

        Map<Long, List<SlashCommand>> serverSlashCommands = new HashMap<>();

        return CompletableFuture.runAsync(() -> commands.forEach(nexusCommand -> nexusCommand.getServerIds().forEach(serverId -> {
            Server server = nexusCore.getShardManager().getShardOf(serverId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nexus was unable to find the shard in charge of the server " +
                                    serverId + " which is needed for " + nexusCommand.getName()
                    ))
                    .getServerById(serverId)
                    .orElseThrow(AssertionError::new); // Assertion Error because the Shard Manager already checked.

            if (!serverSlashCommands.containsKey(serverId)) {
                serverSlashCommands.put(serverId, server.getApi().getServerSlashCommands(server).join());
            }

            List<SlashCommand> slashCommands = serverSlashCommands.get(serverId);

            doFinalizationFor(server, slashCommands, commands);
        })), NexusThreadPool.executorService);
    }

    /**
     * This performs a filter over which commands are not registered into
     * Discord's database so far.
     *
     * @param slashCommands The slash commands to validate.
     * @return A list of commands that haven't been registered to the Discord API.
     */
    private List<NexusCommand> getNotRegistered(List<SlashCommand> slashCommands) {
        return nexusCore.getCommandManager().getCommands()
                .stream()
                .filter(nexusCommand -> slashCommands.stream()
                        .map(SlashCommand::getName)
                        .noneMatch(s1 -> s1.equalsIgnoreCase(nexusCommand.getName())))
                .collect(Collectors.toList());
    }

    /**
     * This performs a filter over which commands are registered into
     * Discord's database so far.
     *
     * @param slashCommands The slash commands to validate.
     * @return A list of commands that have been registered to the Discord API.
     */
    private List<NexusCommand> getRegistered(List<SlashCommand> slashCommands) {
        return nexusCore.getCommandManager().getCommands()
                .stream()
                .filter(nexusCommand -> slashCommands.stream()
                        .map(SlashCommand::getName)
                        .anyMatch(s1 -> s1.equalsIgnoreCase(nexusCommand.getName())))
                .collect(Collectors.toList());
    }

    /**
     * Filters everything that does not match the repository of Nexus. This is
     * used to handle commands that were detached.
     *
     * @param slashCommands The slash commands to validate.
     * @return A list of commands that have been registered to the Discord API.
     */
    private List<SlashCommand> getNoMatches(List<SlashCommand> slashCommands) {
        return slashCommands.stream()
                .filter(slashCommand -> nexusCore.getCommandManager().getCommands()
                        .stream()
                        .map(NexusCommand::getName)
                        .noneMatch(s -> s.equalsIgnoreCase(slashCommand.getName())))
                .collect(Collectors.toList());
    }

    /**
     * This performs a little differential check between the names, description and
     * the settings of a slash command and a {@link NexusCommand} to identify which one
     * needs changing. After checking, it then performs a little option differential check as well
     * as the choices.
     *
     * @param slashCommands The slash command list to check.
     * @return The commands that needs updating.
     */
    private Map<Long, NexusCommand> getChanges(List<SlashCommand> slashCommands) {
        List<NexusCommand> commandList = getRegistered(slashCommands);

        if (commandList.isEmpty())
            return Collections.emptyMap();

        // The purpose of this being an AtomicReference is because we will have to change this
        // many times and lambdas love to hate on these stuff and also atomic operations.
        AtomicReference<Map<Long, NexusCommand>> referenceMap = new AtomicReference<>(new HashMap<>());
        commandList.forEach(command -> {

            // In case you are wondering why the Assertion Error, we have already validated with the getRegistered
            // that the command is supposed to exist but for some reason, it doesn't exist. I haven't experienced this yet
            // so this is an impossible situation.
            SlashCommand slashCommand = slashCommands.stream()
                    .filter(slashCmd -> slashCmd.getName().equalsIgnoreCase(command.getName()))
                    .findFirst()
                    .orElseThrow(() ->
                            new AssertionError(
                                    "Nexus was unable to find a command that was supposed to match, did a bit-flip happen?"
                            )
                    );

            if (!command.getDescription().equalsIgnoreCase(slashCommand.getDescription())) {
                referenceMap.get().put(slashCommand.getId(), command);
                return;
            }

            if (slashCommand.getDefaultPermission() != command.isDefaultPermissionEnabled()) {
                referenceMap.get().put(slashCommand.getId(), command);
                return;
            }

            // This is the reason why we are utilizing AtomicReference for the HashMap. We need to swap out the Map
            // for every depth filter that is being utilized.
            referenceMap.set(
                    getOptionChoiceDifferences(
                            command,
                            slashCommand,
                            referenceMap.get(),
                            slashCommand.getOptions(),
                            command.getOptions()
                    )
            );
        });

        return referenceMap.get();
    }

    /**
     * This performs a filter that checks the options, choices and other
     * deeper levels of two commands and compares them.
     *
     * @param command The command to check.
     * @param slashCommand The slash command equivalent to check.
     * @param differences The differences in the two.
     * @param slashCommandOptions The options to check.
     * @param nexusCommandOptions The Nexus options to check.
     * @return An updated difference map that contains either newer or equal map as the original
     * difference map.
     */
    private Map<Long, NexusCommand> getOptionChoiceDifferences(NexusCommand command, SlashCommand slashCommand, Map<Long, NexusCommand> differences,
                                                List<SlashCommandOption> slashCommandOptions, List<SlashCommandOption> nexusCommandOptions) {

        // This checks if the slash command option is present on Discord API
        // but removed on Velen.
        slashCommandOptions.forEach(slashCommandOption -> {
            Optional<SlashCommandOption> nexusOptional = nexusCommandOptions.stream()
                    .filter(o -> o.getName().equalsIgnoreCase(slashCommandOption.getName()))
                    .filter(o -> o.getDescription().equals(slashCommandOption.getDescription()))
                    .filter(o -> o.isRequired() == slashCommandOption.isRequired())
                    .filter(o -> o.getType().getValue() == slashCommandOption.getType().getValue())
                    .findFirst();

            if (nexusOptional.isEmpty()) {
                differences.put(slashCommand.getId(), command);
            }
        });

        nexusCommandOptions.forEach(slashCommandOption -> {
            Optional<SlashCommandOption> nexusOptional = slashCommandOptions.stream()
                    .filter(o -> o.getName().equalsIgnoreCase(slashCommandOption.getName()))
                    .filter(o -> o.getDescription().equals(slashCommandOption.getDescription()))
                    .filter(o -> o.isRequired() == slashCommandOption.isRequired())
                    .filter(o -> o.getType().getValue() == slashCommandOption.getType().getValue())
                    .findFirst();

            if (nexusOptional.isEmpty()) {
                differences.put(slashCommand.getId(), command);
                return;
            }

            SlashCommandOption option = nexusOptional.get();

            // Time to start checking the choices for any differences.
            option.getChoices().forEach(choice -> {
                Optional<SlashCommandOptionChoice> oChoice = slashCommandOption
                        .getChoices().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(choice.getName()))
                        .filter(c -> c.getLongValue().isPresent() == choice.getLongValue().isPresent())
                        .filter(c -> c.getStringValue().isPresent() == choice.getStringValue().isPresent())
                        .filter(c -> c.getValueAsString().equalsIgnoreCase(choice.getValueAsString()))
                        .findFirst();

                if (oChoice.isEmpty()) {
                    differences.put(slashCommand.getId(), command);
                    return;
                }

                SlashCommandOptionChoice kO = oChoice.get();

                if ((kO.getLongValue().isPresent() && choice.getLongValue().isPresent())
                        && (kO.getLongValue().get().equals(choice.getLongValue().get()))) {
                    differences.put(slashCommand.getId(), command);
                    return;
                }

                if (kO.getStringValue().isPresent() && choice.getStringValue().isPresent()
                        && !kO.getStringValue().get().equalsIgnoreCase(choice.getStringValue().get())) {
                    differences.put(slashCommand.getId(), command);
                }
            });

            // Break off from the repeated loop after this.
            if (option.getOptions().isEmpty() && slashCommandOption.getOptions().isEmpty()) {
                return;
            }

            // We'll have to generate a new HashMap to prevent duplication.
            differences.putAll(getOptionChoiceDifferences(command, slashCommand, new HashMap<>(),
                    option.getOptions(), slashCommandOption.getOptions()));
        });

        return differences;
    }

    /**
     * This performs finalization on server-command checks for Nexus.
     *
     * @param server The server to check.
     * @param slashCommands The slash commands to check.
     * @param commands The commands to check.
     */
    private void doFinalizationFor(Server server, List<SlashCommand> slashCommands, List<NexusCommand> commands) {
        if (mode.isCreate()) {
            for (NexusCommand nexusCommand : getNotRegistered(slashCommands)) {
                long start = System.currentTimeMillis();
                nexusCommand.asSlashCommand().createForServer(server).thenAccept(
                        slashCommand -> NexusCore.logger.info(
                                "Application command was created for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                                server.getId(),
                                slashCommand.getName(),
                                slashCommand.getDescription(),
                                slashCommand.getId(),
                                System.currentTimeMillis() - start
                        )
                ).exceptionally(ExceptionLogger.get());
            }
        }

        if (mode.isDelete()) {
            for (SlashCommand command : getNoMatches(slashCommands)) {
                long start = System.currentTimeMillis();
                command.deleteForServer(server).thenAccept(unused -> NexusCore.logger.info(
                        "Application command was deleted for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                        server.getId(),
                        command.getName(),
                        command.getDescription(),
                        command.getId(),
                        System.currentTimeMillis() - start)).exceptionally(ExceptionLogger.get());
            }
        }

        if (mode.isUpdate()) {
            getChanges(slashCommands).forEach((aLong, nexusCommand) -> {
                long start = System.currentTimeMillis();
                nexusCommand.asSlashCommandUpdater(aLong).updateForServer(server).thenAccept(
                        slashCommand -> NexusCore.logger.info(
                                "Application command was updated for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                                server.getId(),
                                slashCommand.getName(),
                                slashCommand.getDescription(),
                                slashCommand.getId(),
                                System.currentTimeMillis() - start
                        )
                ).exceptionally(ExceptionLogger.get());

            });
        }

        if (!mode.isUpdate() && !mode.isCreate()) {
            for (NexusCommand nexusCommand : getNotRegistered(slashCommands)) {
                NexusCore.logger.warn(
                        "Application command is not registered on Discord's database for server {}. [name={}]",
                        server.getId(),
                        nexusCommand.toString()
                );
            }

            getChanges(slashCommands).forEach((aLong, nexusCommand) -> NexusCore.logger.warn(
                    "Application command requires updating for server {}. [id={}, name={}]",
                    server.getId(),
                    aLong,
                    nexusCommand.getName()
            ));

            for (SlashCommand command : getNoMatches(slashCommands)) {
                NexusCore.logger.warn(
                        "Application command is not registered on Nexus's repository for server {}. [name={}, id={}]",
                        server.getId(),
                        command.getName(),
                        command.getId()
                );
            }
        }
    }


}
