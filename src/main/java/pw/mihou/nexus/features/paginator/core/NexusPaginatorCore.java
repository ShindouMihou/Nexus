package pw.mihou.nexus.features.paginator.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.nexus.core.assignment.NexusUuidAssigner;
import pw.mihou.nexus.features.paginator.enums.NexusPaginatorButtonAssignment;
import pw.mihou.nexus.features.paginator.facade.NexusPaginator;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorEvents;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorInstance;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NexusPaginatorCore<I> extends NexusPaginatorEventCore implements NexusPaginator<I>  {

    /**
     * As specified, {@link  NexusPaginator} is reusable which means for each
     * {@link  NexusPaginator} send that is executed, there will be another instance of that is in charge of everything.
     */
    private static final Map<String, Map<String, NexusPaginatorInstance>> instances = new ConcurrentHashMap<>();
    /**
     * This is to prevent having multiple listeners of the same parent paginator which would end up with multiple execution of
     * the same paginate event. This is done per-shard basis which means for each prepare statement, a listener of the paginator
     * is attached and will be kept there unless called with a destroy statement.
     */
    private static final Map<Integer, Map<String, ListenerManager<ButtonClickListener>>> listeners = new ConcurrentHashMap<>();

    /**
     * Unlike the instances variable, the instance keys is a local list of all the UUIDs of the instances
     * that are alive right now. This allows us to do O(1) performance on paginate events.
     */
    private final List<String> instanceKeys = new ArrayList<>();

    private final String uuid = NexusUuidAssigner.request();
    private final List<I> items;
    private final NexusPaginatorEvents<I> events;
    private final Map<NexusPaginatorButtonAssignment, ButtonBuilder> buttons = new ConcurrentHashMap<>();

    /**
     * Creates a new {@link  NexusPaginator} instance with the specifications.
     *
     * @param items     The items to paginate.
     * @param events    The event handlers for each paginate event.
     * @param buttons   The button templates to use.
     */
    public NexusPaginatorCore(@Nonnull List<I> items,
                              @Nonnull NexusPaginatorEvents<I> events,
                              @Nonnull Map<NexusPaginatorButtonAssignment, Button> buttons) {
        this.items = items;
        this.events = events;

        instances.put(uuid, new HashMap<>());
        buttons.forEach((assignment, button) -> this.buttons.put(assignment, replicateButtonWith(button, assignment.style)));

        if (!this.buttons.containsKey(NexusPaginatorButtonAssignment.NEXT)) {
            this.buttons.put(
                    NexusPaginatorButtonAssignment.NEXT,
                    new ButtonBuilder()
                            .setStyle( NexusPaginatorButtonAssignment.NEXT.style)
                            .setLabel("")
                            .setEmoji("➡")
            );
        }

        if (!this.buttons.containsKey(NexusPaginatorButtonAssignment.PREVIOUS)) {
            this.buttons.put(
                    NexusPaginatorButtonAssignment.PREVIOUS,

                    new ButtonBuilder()
                            .setStyle( NexusPaginatorButtonAssignment.PREVIOUS.style)
                            .setLabel("")
                            .setEmoji("⬅")
            );
        }
    }

    /**
     * Replicates the button specified with a few exceptions and that includes the
     * styling, type of button and the custom identifier of the button which should be
     * assigned by this instance only.
     *
     * @param button        The button to reference from.
     * @param defaultStyle  The default style of the button.
     * @return The replicated button with the following exceptions changed.
     */
    private ButtonBuilder replicateButtonWith(Button button,  ButtonStyle defaultStyle) {
        ButtonBuilder builder = new ButtonBuilder();
        button.getEmoji().ifPresent(builder::setEmoji);
        button.getLabel().ifPresent(builder::setLabel);

        // This is done to ensure that the buttons we add
        // must not be a URL button.
        if (button.getUrl().isEmpty()) {
            builder.setStyle(button.getStyle());
        } else {
            builder.setStyle(defaultStyle);
        }

        return builder;
    }

    /**
     * Creates a new button from the specified assignment template and the instance.
     *
     * @param instance      The instance to refer from.
     * @param assignment    The paginator button assignment.
     * @return A {@link  Button} instance with a custom identifier.
     */
    private Button createFrom(NexusPaginatorInstance<I> instance, NexusPaginatorButtonAssignment assignment) {
        return buttons.get(assignment).setCustomId(instance.getUUID() + "." + assignment.name()).build();
    }

    /**
     * Adds the instance into the repository.
     *
     * @param instance The instance to add to the repository.
     */
    private void addInstance(NexusPaginatorInstanceCore<I> instance) {
        instances.get(uuid).put(instance.getUUID(), instance);
        instanceKeys.add(instance.getUUID());
    }

    @Override
    public List<NexusPaginatorInstance> getInstances() {
        return new ArrayList<>(instances.get(uuid).values());
    }

    @Override
    public List<I> getItems() {
        return items;
    }

    @Override
    public void destroy(Message message) {
        Optional<ActionRow> actionRowOptional = message.getComponents()
                .stream()
                .map(HighLevelComponent::asActionRow)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(row -> {
                    if (row.getComponents().isEmpty()) {
                        return false;
                    }

                    Optional<Button> buttonOptional = row.getComponents().get(0).asButton();
                    if (buttonOptional.isEmpty()) {
                        return false;
                    }

                    Button button = buttonOptional.get();
                    if (button.getCustomId().isEmpty()) {
                        return false;
                    }

                    String customId = button.getCustomId().get();
                    if (!customId.contains(".")) {
                        return false;
                    }

                    String uuid = customId.split("\\.")[0];
                    return instanceKeys.contains(uuid);
                }).findFirst();

        Optional<String> uuidOptional = actionRowOptional.map(actionRow ->
                actionRow.getComponents()
                        .get(0)
                        .asButton()
                        .orElseThrow(AssertionError::new)
                        .getCustomId()
                        .orElseThrow(AssertionError::new)
                        .split("\\.")[0]
        );

        uuidOptional.ifPresent(instanceUuid -> {
            MessageUpdater updater = new MessageUpdater(message).removeAllComponents();
            ActionRow actionRow = actionRowOptional.orElseThrow(AssertionError::new);

            destroy(instanceUuid);
            updater.addComponents(
                    message.getComponents()
                            .stream()
                            .filter(component -> !(component.asActionRow().isPresent() && component.asActionRow().get().equals(actionRow)))
                            .toArray(HighLevelComponent[]::new)
            );

            updater.applyChanges();
        });
    }

    @Override
    public void destroy() {
        instanceKeys.clear();

        instances.get(uuid).clear();

        listeners.entrySet()
                .stream()
                .filter(entry -> entry.getValue().containsKey(uuid))
                .forEach(entry -> {
                    entry.getValue().get(uuid).remove();
                    entry.getValue().remove(uuid);
                });
    }

    @Override
    public void destroy(NexusPaginatorInstance<I> instance) {
        destroy(instance.getUUID());
    }

    @Override
    public void destroy(String instanceUuid) {
        instanceKeys.remove(instanceUuid);
        instances.get(uuid).remove(instanceUuid);
        NexusUuidAssigner.deny(instanceUuid);
    }

    /**
     * Performs a handshake that adds the {@link NexusPaginator} into the listeners
     * repository as well as add the {@link NexusPaginator} into {@link  DiscordApi}'s listeners.
     * <br>
     * You don't have to worry about duplicates of this as it is all done once.
     *
     * @param api The {@link  DiscordApi} shard.
     */
    private void handshake(DiscordApi api) {
        int shard = api.getCurrentShard();
        if (!listeners.containsKey(shard)) {
            listeners.put(shard, new ConcurrentHashMap<>());
        }

        if (listeners.get(shard).containsKey(uuid)) {
            return;
        }

        ListenerManager<ButtonClickListener> listener = api.addButtonClickListener(this);
        listeners.get(shard).put(uuid, listener);
    }

    /**
     * Creates the pagination action row with the specifications defined
     * on the {@link NexusPaginator}.
     *
     * @param instance The {@link NexusPaginatorInstance} to refer form.
     * @return The {@link ActionRow} to add.
     */
    private ActionRow createActionRow(NexusPaginatorInstance<I> instance) {
        ActionRowBuilder actionRowBuilder = new ActionRowBuilder();
        actionRowBuilder.addComponents(createFrom(instance, NexusPaginatorButtonAssignment.PREVIOUS));

        if (buttons.containsKey(NexusPaginatorButtonAssignment.SELECT)) {
            actionRowBuilder.addComponents(createFrom(instance, NexusPaginatorButtonAssignment.SELECT));
        }

        if (buttons.containsKey(NexusPaginatorButtonAssignment.CANCEL)) {
            actionRowBuilder.addComponents(createFrom(instance, NexusPaginatorButtonAssignment.CANCEL));
        }

        actionRowBuilder.addComponents(createFrom(instance, NexusPaginatorButtonAssignment.NEXT));
        return actionRowBuilder.build();
    }

    @Override
    public CompletableFuture<NexusPaginatorInstance<I>> send(Interaction interaction, InteractionOriginalResponseUpdater responseUpdater) {
        NexusPaginatorInstanceCore<I> instance = new NexusPaginatorInstanceCore<>(this);

        addInstance(instance);
        handshake(interaction.getApi());

        return events.onInit(responseUpdater, new NexusPaginatorCursorCore<>(instance.getCursor().get(), instance))
                .addComponents(createActionRow(instance))
                .update()
                .thenApply(instance::setMessage)
                .exceptionally(ExceptionLogger.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onButtonClick(ButtonClickEvent event) {
        ButtonInteraction interaction = event.getButtonInteraction();

        if (!interaction.getCustomId().contains(".")) {
            return;
        }

        String interactionUUID = interaction.getCustomId().split("\\.")[0];
        if (!instanceKeys.contains(interactionUUID)) {
            return;
        }

        NexusPaginatorInstanceCore<I> instance = (NexusPaginatorInstanceCore<I>) instances.get(uuid).get(interactionUUID);
        NexusPaginatorButtonAssignment type = NexusPaginatorButtonAssignment.valueOf(interaction.getCustomId().replaceFirst(interactionUUID + ".", ""));

        interaction.acknowledge();
        switch (type) {
            case NEXT -> {
                if (instance.getCursor().get() < items.size() - 1) {
                    events.onPageChange(new NexusPaginatorCursorCore<>(instance.getCursor().incrementAndGet(), instance), event);
                }

            }
            case PREVIOUS -> {
                if (instance.getCursor().get() > 0) {
                    events.onPageChange(new NexusPaginatorCursorCore<>(instance.getCursor().decrementAndGet(), instance), event);
                }

            }
            case SELECT -> events.onSelect(new NexusPaginatorCursorCore<>(instance.getCursor().get(), instance), event);
            case CANCEL -> events.onCancel(new NexusPaginatorCursorCore<>(instance.getCursor().get(), instance), event);
        }

    }
}
